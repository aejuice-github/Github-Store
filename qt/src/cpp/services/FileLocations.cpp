#include "FileLocations.h"
#include <QStandardPaths>
#include <QDir>

#ifdef Q_OS_WIN
#include <windows.h>
#include <shlobj.h>
#endif

FileLocations::FileLocations(QObject *parent)
    : QObject(parent) {
}

QString FileLocations::mediaCorePath() const {
#ifdef Q_OS_WIN
    return programFilesPath() + "/Adobe/Common/Plug-ins/7.0/MediaCore/";
#elif defined(Q_OS_MAC)
    return "/Library/Application Support/Adobe/Common/Plug-ins/7.0/MediaCore/";
#else
    return QString();
#endif
}

QStringList FileLocations::scriptUIPanelsPaths() const {
    QStringList paths;

#ifdef Q_OS_WIN
    QDir adobeDir(programFilesPath() + "/Adobe");
    if (adobeDir.exists()) {
        for (const auto &entry : adobeDir.entryList({"Adobe After Effects *"}, QDir::Dirs)) {
            QString scriptUIPath = adobeDir.absoluteFilePath(entry)
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
    createDirectory(path);
    return path;
}

QString FileLocations::appDataPath() const {
    QString path = QStandardPaths::writableLocation(QStandardPaths::AppDataLocation);
    createDirectory(path);
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

QString FileLocations::programFilesPath() {
#ifdef Q_OS_WIN
    wchar_t *path = nullptr;
    if (SUCCEEDED(SHGetKnownFolderPath(FOLDERID_ProgramFiles, 0, nullptr, &path))) {
        QString result = QString::fromWCharArray(path);
        CoTaskMemFree(path);
        return QDir::fromNativeSeparators(result);
    }
    return "C:/Program Files";
#else
    return QString();
#endif
}

QString FileLocations::commonFilesPath() {
#ifdef Q_OS_WIN
    wchar_t *path = nullptr;
    if (SUCCEEDED(SHGetKnownFolderPath(FOLDERID_ProgramFilesCommon, 0, nullptr, &path))) {
        QString result = QString::fromWCharArray(path);
        CoTaskMemFree(path);
        return QDir::fromNativeSeparators(result);
    }
    return "C:/Program Files/Common Files";
#else
    return QString();
#endif
}

bool FileLocations::createDirectory(const QString &path) {
    if (QDir(path).exists())
        return true;
    if (!QDir().mkpath(path)) {
        qWarning() << "Failed to create directory:" << path;
        return false;
    }
    return true;
}
