#include "InstallManager.h"
#include "DownloadManager.h"
#include "JsonStorage.h"
#include "services/ElevatedCopyHelper.h"
#include <QProcess>
#include <QStandardPaths>
#include <QDir>
#include <QFile>
#include <QFileInfo>
#include <QDebug>

InstallManager::InstallManager(QObject *parent)
    : QObject(parent)
    , m_elevatedHelper(new ElevatedCopyHelper(this))
{
}

void InstallManager::setDownloadManager(DownloadManager *download)
{
    m_download = download;
}

void InstallManager::setJsonStorage(JsonStorage *storage)
{
    m_storage = storage;
}

void InstallManager::install(const QString &componentId, const QVariantMap &componentData, const QString &version)
{
    if (m_isBusy)
        return;

    QString url = componentData.value("url").toString();
    QString fileName = componentData.value("fileName").toString();
    if (url.isEmpty() || fileName.isEmpty()) {
        emit installFailed(componentId, "Missing download URL or file name");
        return;
    }

    QString installPath = componentData.value("installPath").toString();

    m_isBusy = true;
    emit isBusyChanged();
    emit installStarted(componentId);

    QString destination = QStandardPaths::writableLocation(QStandardPaths::TempLocation)
                          + "/ComponentManager/" + fileName;

    connect(m_download, &DownloadManager::downloadProgress,
            this, [this, componentId](const QString &id, qint64 received, qint64 total) {
        if (id != componentId)
            return;
        double progress = total > 0 ? static_cast<double>(received) / total : 0;
        emit installProgress(componentId, progress, "Downloading...");
    });

    connect(m_download, &DownloadManager::downloadCompleted,
            this, [this, componentId, version, installPath, fileName](const QString &id, const QString &filePath) {
        if (id != componentId)
            return;
        finishInstall(componentId, filePath, version, installPath, fileName);
    });

    connect(m_download, &DownloadManager::downloadError,
            this, [this, componentId](const QString &id, const QString &error) {
        if (id != componentId)
            return;
        m_isBusy = false;
        emit isBusyChanged();
        emit installFailed(componentId, error);
    });

    m_download->downloadFile(componentId, QUrl(url), destination);
}

void InstallManager::finishInstall(const QString &componentId, const QString &filePath,
                                    const QString &version, const QString &installPath,
                                    const QString &fileName)
{
    emit installProgress(componentId, 1.0, "Installing...");

    QFileInfo info(filePath);
    QString ext = info.suffix().toLower();

    // Executables and installers — launch them
    if (ext == "exe" || ext == "msi") {
        bool started = QProcess::startDetached(filePath, QStringList());
        if (!started) {
            m_isBusy = false;
            emit isBusyChanged();
            emit installFailed(componentId, "Failed to launch installer");
            return;
        }
    }
    // Plugins and scripts — copy to install path(s)
    else if (!installPath.isEmpty()) {
        QStringList targetDirs = resolveInstallPaths(installPath);
        if (targetDirs.isEmpty()) {
            m_isBusy = false;
            emit isBusyChanged();
            emit installFailed(componentId, "No install locations found");
            return;
        }

        QStringList destinations;
        for (const QString &targetDir : targetDirs)
            destinations << targetDir + "/" + fileName;

        bool copied = m_elevatedHelper->copyFiles(filePath, destinations);
        if (!copied) {
            m_isBusy = false;
            emit isBusyChanged();
            emit installFailed(componentId, "Failed to copy to install locations");
            return;
        }
    }
    else {
        m_isBusy = false;
        emit isBusyChanged();
        emit installFailed(componentId, "Unknown file type: " + ext);
        return;
    }

    if (m_storage)
        m_storage->addInstalled(componentId, version);

    m_isBusy = false;
    emit isBusyChanged();
    emit installCompleted(componentId);
}

QStringList InstallManager::resolveInstallPaths(const QString &installPath) const
{
    // Absolute paths used as-is (e.g. "C:/Program Files/Adobe/Common/...")
    if (QDir::isAbsolutePath(installPath))
        return QStringList() << installPath;

    // Find all After Effects versions on disk
    QStringList results;
    QString searchDir = "C:/Program Files/Adobe";
    QDir dir(searchDir);
    QStringList entries = dir.entryList(QStringList() << "Adobe After Effects*", QDir::Dirs);

    for (const QString &entry : entries) {
        QString aeBase = searchDir + "/" + entry;

        if (installPath == "scriptui")
            results << aeBase + "/Support Files/Scripts/ScriptUI Panels";
        else if (installPath == "scripts")
            results << aeBase + "/Support Files/Scripts";
        else
            results << aeBase + "/" + installPath;
    }

    if (results.isEmpty())
        qDebug() << "No After Effects installations found";

    return results;
}

