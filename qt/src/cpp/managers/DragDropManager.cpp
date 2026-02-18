#include "DragDropManager.h"
#include "services/ElevatedCopyHelper.h"
#include <QFile>
#include <QFileInfo>
#include <QDir>
#include <QDebug>
#include <QStandardPaths>
#include <QProcess>

static const QStringList SUPPORTED_EXTENSIONS = {"jsx", "jsxbin", "aex", "ofx", "zxp"};

DragDropManager::DragDropManager(QObject *parent)
    : QObject(parent)
    , m_elevatedHelper(new ElevatedCopyHelper(this))
{
}

void DragDropManager::setDragActive(bool active)
{
    if (m_isDragActive == active)
        return;
    m_isDragActive = active;
    emit isDragActiveChanged();
}

void DragDropManager::handleDroppedFiles(const QStringList &files)
{
    m_droppedFiles = files;
    emit droppedFilesChanged();
    emit filesDropped(files);
    installFiles(files);
}

void DragDropManager::clearDroppedFiles()
{
    m_droppedFiles.clear();
    emit droppedFilesChanged();
}

void DragDropManager::installFiles(const QStringList &files)
{
    QSet<QString> installedTypesSet;
    int fileCount = 0;
    QStringList allSources;
    QStringList allDestinations;

    for (const QString &filePath : files) {
        QFileInfo fileInfo(filePath);
        if (!fileInfo.exists()) {
            qDebug() << "DragDrop: file not found:" << filePath;
            continue;
        }

        QString extension = fileInfo.suffix().toLower();
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            qDebug() << "DragDrop: unsupported extension:" << extension;
            continue;
        }

        QStringList installPaths = findInstallPaths(extension);
        if (installPaths.isEmpty()) {
            qDebug() << "DragDrop: no install paths found for" << extension;
            continue;
        }

        // Check if file exists at any destination and blocking processes are running
        QStringList blockingProcesses = processesForExtension(extension);
        if (!blockingProcesses.isEmpty()) {
            for (const QString &installDir : installPaths) {
                QString destination = installDir + fileInfo.fileName();
                if (QFile::exists(destination)) {
                    QStringList running = findRunningProcesses(blockingProcesses);
                    if (!running.isEmpty()) {
                        emit installResult(
                            "Close " + running.join(", ") + " and try again",
                            "error", {});
                        return;
                    }
                    break;  // Only need to check once per file
                }
            }
        }

        installedTypesSet.insert(extension);

        for (const QString &installDir : installPaths) {
            allSources.append(filePath);
            allDestinations.append(installDir + fileInfo.fileName());
        }

        fileCount++;
    }

    QStringList installedTypes(installedTypesSet.begin(), installedTypesSet.end());

    if (allDestinations.isEmpty()) {
        emit installResult("No supported files to install", "error", {});
        return;
    }

    // Use elevated copy helper (same single-UAC approach as component installs)
    bool success = true;
    for (int i = 0; i < allSources.size(); i++) {
        if (!m_elevatedHelper->copyFiles(allSources[i], {allDestinations[i]}))
            success = false;
    }

    if (success) {
        QString message = QString("Installed %1 file%2")
            .arg(fileCount)
            .arg(fileCount > 1 ? "s" : "");
        qDebug() << "DragDrop:" << message;
        emit installResult(message, "success", installedTypes);
    } else {
        qDebug() << "DragDrop: install failed";
        emit installResult("Installation failed", "error", {});
    }
}

QStringList DragDropManager::findInstallPaths(const QString &extension)
{
    if (extension == "jsx" || extension == "jsxbin") {
        QStringList paths;
#ifdef Q_OS_WIN
        QDir programFiles("C:/Program Files/Adobe");
        if (programFiles.exists()) {
            for (const auto &entry : programFiles.entryList({"Adobe After Effects *"}, QDir::Dirs)) {
                QString scriptUIPath = programFiles.absoluteFilePath(entry)
                    + "/Support Files/Scripts/ScriptUI Panels/";
                paths.append(scriptUIPath);
            }
        }
#elif defined(Q_OS_MAC)
        QDir appDir("/Applications");
        if (appDir.exists()) {
            for (const auto &entry : appDir.entryList({"Adobe After Effects *"}, QDir::Dirs)) {
                QString scriptUIPath = appDir.absoluteFilePath(entry)
                    + "/Scripts/ScriptUI Panels/";
                paths.append(scriptUIPath);
            }
        }
#endif
        return paths;
    }

    if (extension == "aex") {
        return {"C:/Program Files/Adobe/Common/Plug-ins/7.0/MediaCore/"};
    }

    if (extension == "ofx") {
        return {"C:/Program Files/Common Files/OFX/Plugins/"};
    }

    if (extension == "zxp") {
        return {"C:/Program Files/Common Files/Adobe/CEP/extensions/"};
    }

    return {};
}

QStringList DragDropManager::processesForExtension(const QString &extension)
{
    if (extension == "aex")
        return {"AfterFX.exe", "Adobe Premiere Pro.exe", "Adobe Media Encoder.exe"};
    if (extension == "ofx")
        return {"resolve.exe"};
    return {};
}

QStringList DragDropManager::findRunningProcesses(const QStringList &processNames)
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
