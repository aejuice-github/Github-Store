#include "FileLocations.h"
#include <QStandardPaths>
#include <QDir>

FileLocations::FileLocations(QObject *parent)
    : QObject(parent) {
}

QString FileLocations::mediaCorePath() const {
#ifdef Q_OS_WIN
    return "C:/Program Files/Adobe/Common/Plug-ins/7.0/MediaCore/";
#elif defined(Q_OS_MAC)
    return "/Library/Application Support/Adobe/Common/Plug-ins/7.0/MediaCore/";
#else
    return QString();
#endif
}

QStringList FileLocations::scriptUIPanelsPaths() const {
    QStringList paths;

#ifdef Q_OS_WIN
    QDir programFiles("C:/Program Files/Adobe");
    if (programFiles.exists()) {
        for (const auto &entry : programFiles.entryList({"Adobe After Effects *"}, QDir::Dirs)) {
            QString scriptUIPath = programFiles.absoluteFilePath(entry)
                + "/Support Files/Scripts/ScriptUI Panels/";
            if (QDir(scriptUIPath).exists())
                paths.append(scriptUIPath);
        }
    }
#elif defined(Q_OS_MAC)
    QDir appDir("/Applications");
    if (appDir.exists()) {
        for (const auto &entry : appDir.entryList({"Adobe After Effects *"}, QDir::Dirs)) {
            QString scriptUIPath = appDir.absoluteFilePath(entry)
                + "/Scripts/ScriptUI Panels/";
            if (QDir(scriptUIPath).exists())
                paths.append(scriptUIPath);
        }
    }
#endif

    return paths;
}

QString FileLocations::tempDownloadPath() const {
    QString path = QStandardPaths::writableLocation(QStandardPaths::TempLocation)
        + "/ComponentManager/downloads";
    QDir().mkpath(path);
    return path;
}

QString FileLocations::appDataPath() const {
    QString path = QStandardPaths::writableLocation(QStandardPaths::AppDataLocation);
    QDir().mkpath(path);
    return path;
}

QString FileLocations::platformKey() {
#ifdef Q_OS_WIN
    return "windows";
#elif defined(Q_OS_MAC)
    return "macos";
#else
    return "linux";
#endif
}
