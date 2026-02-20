#include "InstallManager.h"
#include "DownloadManager.h"
#include "JsonStorage.h"
#include "services/ElevatedCopyHelper.h"
#include "services/FileLocations.h"
#include <QProcess>
#include <QStandardPaths>
#include <QDir>
#include <QFile>
#include <QFileInfo>
#include <QDebug>
#include <QTimer>

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

    QStringList waitForFinish;
    for (const auto &proc : componentData.value("waitForFinish").toList())
        waitForFinish.append(proc.toString());

    QStringList silentArgs;
    for (const auto &arg : componentData.value("silentArgs").toList())
        silentArgs.append(arg.toString());

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
            this, [this, componentId, version, installPath, fileName, waitForFinish, silentArgs](const QString &id, const QString &filePath) {
        if (id != componentId)
            return;
        finishInstall(componentId, filePath, version, installPath, fileName, waitForFinish, silentArgs);
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
                                    const QString &fileName, const QStringList &waitForFinish,
                                    const QStringList &silentArgs)
{
    QFileInfo info(filePath);
    QString ext = info.suffix().toLower();

    // Executables and installers — run with silent args and wait for completion
    if (ext == "exe" || ext == "msi") {
        emit installProgress(componentId, -1, "Installing...");
        runInstaller(componentId, filePath, version, silentArgs, waitForFinish);
        return;
    }

    emit installProgress(componentId, 1.0, "Installing...");

    // Plugins and scripts — copy to install path(s)
    if (!installPath.isEmpty()) {
        QStringList targetDirs = resolveInstallPaths(installPath);
        if (targetDirs.isEmpty()) {
            m_isBusy = false;
            emit isBusyChanged();
            emit installFailed(componentId, "No install locations found");
            return;
        }

        // Check if file already exists and blocking processes are running
        if (!waitForFinish.isEmpty() && fileExistsAtDestination(fileName, targetDirs)) {
            QStringList running = findRunningProcesses(waitForFinish);
            if (!running.isEmpty()) {
                m_isBusy = false;
                emit isBusyChanged();
                emit installFailed(componentId,
                    "Close " + running.join(", ") + " and try again");
                return;
            }
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

void InstallManager::runInstaller(const QString &componentId, const QString &filePath,
                                   const QString &version, const QStringList &silentArgs,
                                   const QStringList &waitForFinish)
{
    QFileInfo info(filePath);
    QString ext = info.suffix().toLower();

    QString program;
    QStringList arguments;

    if (ext == "msi") {
        program = "msiexec";
        arguments << "/i" << QDir::toNativeSeparators(filePath);
        arguments.append(silentArgs);
    } else {
        program = filePath;
        arguments = silentArgs;
    }

    qDebug() << "Running installer:" << program << arguments;

    auto *process = new QProcess(this);
    connect(process, QOverload<int, QProcess::ExitStatus>::of(&QProcess::finished),
            this, [this, componentId, version, process, waitForFinish](int exitCode, QProcess::ExitStatus status) {
        process->deleteLater();

        if (status != QProcess::NormalExit || exitCode != 0) {
            qDebug() << "Installer failed, exit code:" << exitCode;
            m_isBusy = false;
            emit isBusyChanged();
            emit installFailed(componentId, "Installer exited with code " + QString::number(exitCode));
            return;
        }

        qDebug() << "Installer process exited successfully";

        if (!waitForFinish.isEmpty()) {
            waitForProcesses(componentId, version, waitForFinish);
            return;
        }

        if (m_storage)
            m_storage->addInstalled(componentId, version);

        m_isBusy = false;
        emit isBusyChanged();
        emit installCompleted(componentId);
    });

    connect(process, &QProcess::errorOccurred,
            this, [this, componentId, process](QProcess::ProcessError error) {
        Q_UNUSED(error)
        qDebug() << "Installer error:" << process->errorString();
        process->deleteLater();
        m_isBusy = false;
        emit isBusyChanged();
        emit installFailed(componentId, "Failed to launch installer: " + process->errorString());
    });

    process->start(program, arguments);
}

void InstallManager::waitForProcesses(const QString &componentId, const QString &version,
                                       const QStringList &processNames)
{
    QStringList running = findRunningProcesses(processNames);
    if (running.isEmpty()) {
        qDebug() << "All child processes finished";
        if (m_storage)
            m_storage->addInstalled(componentId, version);

        m_isBusy = false;
        emit isBusyChanged();
        emit installCompleted(componentId);
        return;
    }

    qDebug() << "Waiting for processes:" << running;
    emit installProgress(componentId, -1, "Waiting for " + running.join(", ") + "...");

    QTimer::singleShot(2000, this, [this, componentId, version, processNames]() {
        waitForProcesses(componentId, version, processNames);
    });
}

QStringList InstallManager::resolveInstallPaths(const QString &installPath) const
{
    // Replace %PROGRAMFILES% and %COMMONFILES% placeholders from manifest
    QString resolved = installPath;
    resolved.replace("%PROGRAMFILES%", FileLocations::programFilesPath());
    resolved.replace("%COMMONFILES%", FileLocations::commonFilesPath());

    // Absolute paths used as-is
    if (QDir::isAbsolutePath(resolved))
        return QStringList() << resolved;

    // Find all After Effects versions on disk
    QStringList results;
    QString searchDir = FileLocations::programFilesPath() + "/Adobe";
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

QStringList InstallManager::findRunningProcesses(const QStringList &processNames) const
{
    QStringList running;
    for (const QString &name : processNames) {
        QProcess process;
#ifdef Q_OS_WIN
        process.start("tasklist", {"/FI", "IMAGENAME eq " + name, "/NH"});
#else
        process.start("pgrep", {"-x", name});
#endif
        process.waitForFinished(3000);
        QString output = process.readAllStandardOutput();
        if (output.contains(name, Qt::CaseInsensitive))
            running.append(name);
    }
    return running;
}

bool InstallManager::fileExistsAtDestination(const QString &fileName, const QStringList &targetDirs) const
{
    for (const QString &dir : targetDirs) {
        if (QFile::exists(dir + "/" + fileName))
            return true;
    }
    return false;
}
