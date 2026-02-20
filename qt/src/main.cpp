#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QQmlContext>
#include <QQuickWindow>
#include <QQuickStyle>
#include <QFile>
#include <QTextStream>
#include <QStandardPaths>
#include <QTimer>
#include "cpp/AppController.h"
#include "cpp/NativeDropHandler.h"
#include "cpp/services/ElevatedCopyHelper.h"
#include "cpp/managers/ManifestManager.h"
#include "cpp/managers/DownloadManager.h"
#include "cpp/managers/InstallManager.h"
#include "cpp/managers/JsonStorage.h"
#include "cpp/data/Component.h"

static QFile *logFile = nullptr;

void messageHandler(QtMsgType type, const QMessageLogContext &context, const QString &msg) {
    if (!logFile) return;
    QTextStream stream(logFile);
    stream << msg << "\n";
    stream.flush();
}

// Headless install mode — no GUI, installs requested components and exits
// installRequired: if true, auto-detect required components from manifest
// componentIds: explicit list of IDs (used when installRequired is false)
static int runHeadlessInstall(int argc, char *argv[],
                              const QStringList &componentIds, bool installRequired)
{
    QCoreApplication app(argc, argv);
    app.setApplicationName("AEJuice App Store");
    app.setOrganizationName("AEJuice");
    app.setApplicationVersion("1.0.0");

    QFile file(QStandardPaths::writableLocation(QStandardPaths::TempLocation) + "/cm_install.log");
    file.open(QIODevice::WriteOnly | QIODevice::Truncate);
    logFile = &file;
    qInstallMessageHandler(messageHandler);

    qDebug() << "=== HEADLESS INSTALL ==="
             << (installRequired ? "required" : componentIds.join(","));

    JsonStorage storage;
    storage.load();
    DownloadManager downloads;
    InstallManager installer;
    ManifestManager manifest;
    installer.setDownloadManager(&downloads);
    installer.setJsonStorage(&storage);
    manifest.setJsonStorage(&storage);

    auto queue = QSharedPointer<QList<Component>>::create();
    auto totalCount = QSharedPointer<int>::create(0);
    auto failCount = QSharedPointer<int>::create(0);

    // Install next component in queue
    std::function<void()> installNext;
    installNext = [&installer, queue, totalCount, failCount]() {
        if (queue->isEmpty()) {
            qDebug() << "=== DONE ===" << *totalCount - *failCount
                     << "installed," << *failCount << "failed";
            QCoreApplication::exit(*failCount > 0 ? 1 : 0);
            return;
        }

        Component comp = queue->takeFirst();
#ifdef Q_OS_WIN
        QString platform = "windows";
#else
        QString platform = "macos";
#endif
        if (!comp.platforms.contains(platform)) {
            qDebug() << "No platform asset for" << comp.id << ", skipping";
            (*failCount)++;
            QTimer::singleShot(0, [queue, totalCount, failCount]() {
                if (queue->isEmpty())
                    QCoreApplication::exit(*failCount > 0 ? 1 : 0);
            });
            return;
        }

        PlatformAsset asset = comp.platforms[platform];
        QVariantMap data = asset.toVariantMap();
        qDebug() << "Installing" << comp.id << comp.version;
        installer.install(comp.id, data, comp.version);
    };

    QObject::connect(&installer, &InstallManager::installCompleted,
        [&installNext](const QString &id) {
            qDebug() << "Installed:" << id;
            installNext();
        });

    QObject::connect(&installer, &InstallManager::installFailed,
        [&installNext, failCount](const QString &id, const QString &error) {
            qDebug() << "Failed:" << id << "-" << error;
            (*failCount)++;
            installNext();
        });

    QObject::connect(&manifest, &ManifestManager::manifestLoaded,
        [&installNext, queue, totalCount, &storage, installRequired, componentIds]
        (const QList<Component> &components, const QStringList &) {
            for (const Component &comp : components) {
                if (installRequired) {
                    // Install required components that aren't already installed
                    if (comp.required && !storage.isInstalled(comp.id))
                        queue->append(comp);
                } else {
                    if (componentIds.contains(comp.id))
                        queue->append(comp);
                }
            }

            *totalCount = queue->size();

            if (queue->isEmpty()) {
                qDebug() << (installRequired
                    ? "Nothing to install, all required components are up to date"
                    : "No matching components found");
                QCoreApplication::exit(0);
                return;
            }

            qDebug() << "Installing" << *totalCount << "components";
            installNext();
        });

    QObject::connect(&manifest, &ManifestManager::errorOccurred,
        [](const QString &error) {
            qDebug() << "Manifest error:" << error;
            QCoreApplication::exit(1);
        });

    manifest.loadManifest();
    return app.exec();
}

int main(int argc, char *argv[]) {
    // Elevated pipe helper mode — runs as admin, no GUI, handles file copies
    for (int i = 1; i < argc; i++) {
        if (QString(argv[i]) == "--pipe-helper" && i + 1 < argc) {
            QCoreApplication app(argc, argv);
            return ElevatedCopyHelper::runHelper(QString(argv[i + 1]));
        }
    }

    // Headless install modes — no GUI, installs and exits
    for (int i = 1; i < argc; i++) {
        if (QString(argv[i]) == "--install-required")
            return runHeadlessInstall(argc, argv, {}, true);
        if (QString(argv[i]) == "--install" && i + 1 < argc) {
            QStringList ids = QString(argv[i + 1]).split(",", Qt::SkipEmptyParts);
            return runHeadlessInstall(argc, argv, ids, false);
        }
    }

    QQuickStyle::setStyle("Basic");

    QGuiApplication app(argc, argv);
    app.setApplicationName("AEJuice App Store");
    app.setOrganizationName("AEJuice");
    app.setApplicationVersion("1.0.0");

    QFile file(QStandardPaths::writableLocation(QStandardPaths::TempLocation) + "/cm_qml_debug.log");
    file.open(QIODevice::WriteOnly | QIODevice::Truncate);
    logFile = &file;
    qInstallMessageHandler(messageHandler);

    qDebug() << "=== APP STARTING ===";

    AppController controller;

    QQmlApplicationEngine engine;
    engine.rootContext()->setContextProperty("appController", &controller);
    engine.addImportPath("qrc:/qml");
    NativeDropHandler dropHandler(controller.dragDrop());

    engine.load(QUrl("qrc:/qml/main.qml"));

    qDebug() << "Root objects count:" << engine.rootObjects().size();

    if (engine.rootObjects().isEmpty()) {
        qDebug() << "LOAD ERROR: Failed to load main.qml";
        return -1;
    }

    // Find the window and register for native drops
    QObject *rootObj = engine.rootObjects().first();
    qDebug() << "Root class:" << rootObj->metaObject()->className();

    auto windows = app.allWindows();
    qDebug() << "Window count:" << windows.size();
    for (QWindow *w : windows) {
        qDebug() << "Registering window:" << w->title();
        dropHandler.registerWindow(w);
    }

    return app.exec();
}
