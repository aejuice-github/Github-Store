#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QQmlContext>
#include <QQuickWindow>
#include <QQuickStyle>
#include <QFile>
#include <QTextStream>
#include <QStandardPaths>
#include "cpp/AppController.h"
#include "cpp/NativeDropHandler.h"
#include "cpp/WindowsDnDFix.h"

static QFile *logFile = nullptr;

void messageHandler(QtMsgType type, const QMessageLogContext &context, const QString &msg) {
    if (!logFile) return;
    QTextStream stream(logFile);
    stream << msg << "\n";
    stream.flush();
}

int main(int argc, char *argv[]) {
    // On Windows, UIPI blocks drag-and-drop to elevated apps.
    // Relaunch as normal user if running as admin.
    if (WindowsDnDFix::relaunchIfElevated(argv[0]))
        return 0;

    QQuickStyle::setStyle("Basic");

    QGuiApplication app(argc, argv);
    app.setApplicationName("Component Manager");
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
