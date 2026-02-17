#include <QGuiApplication>
#include <QQuickView>
#include <QQmlContext>
#include <QQmlEngine>
#include <QQuickStyle>
#include <QFile>
#include <QTextStream>
#include <QStandardPaths>
#include "cpp/AppController.h"

static QFile *logFile = nullptr;

void messageHandler(QtMsgType type, const QMessageLogContext &context, const QString &msg) {
    if (!logFile) return;
    QTextStream stream(logFile);
    stream << msg << "\n";
    stream.flush();
}

int main(int argc, char *argv[]) {
    QQuickStyle::setStyle("Basic");

    QGuiApplication app(argc, argv);
    app.setApplicationName("Component Manager");
    app.setOrganizationName("AEJuice");
    app.setApplicationVersion("1.0.0");

    QFile file(QStandardPaths::writableLocation(QStandardPaths::TempLocation) + "/cm_qml_debug.log");
    file.open(QIODevice::WriteOnly | QIODevice::Truncate);
    logFile = &file;
    qInstallMessageHandler(messageHandler);

    AppController controller;

    QQuickView view;
    view.rootContext()->setContextProperty("appController", &controller);
    view.engine()->addImportPath("qrc:/qml");
    view.setResizeMode(QQuickView::SizeRootObjectToView);
    view.setSource(QUrl("qrc:/qml/main.qml"));

    if (view.status() == QQuickView::Error) {
        for (const auto &error : view.errors()) {
            QTextStream stream(logFile);
            stream << "LOAD ERROR: " << error.toString() << "\n";
            stream.flush();
        }
    }

    view.setTitle("Component Manager");
    view.resize(1100, 700);
    view.setMinimumSize(QSize(800, 500));
    view.show();

    return app.exec();
}
