#include "DragDropManager.h"
#include <QFile>
#include <QFileInfo>
#include <QDir>
#include <QDebug>
#include <QStandardPaths>
#include <QProcess>

#ifdef Q_OS_WIN
#include <windows.h>
#include <shellapi.h>
#endif

static const QStringList SUPPORTED_EXTENSIONS = {"jsx", "jsxbin", "aex", "ofx", "zxp"};

DragDropManager::DragDropManager(QObject *parent)
    : QObject(parent)
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
    // Build list of copy operations: source -> destinations
    QStringList copyCommands;
    QSet<QString> installedTypesSet;
    int fileCount = 0;

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

        installedTypesSet.insert(extension);

        for (const QString &installDir : installPaths) {
            QString destination = installDir + fileInfo.fileName();
            QString src = QDir::toNativeSeparators(filePath);
            QString dst = QDir::toNativeSeparators(destination);
            QString dir = QDir::toNativeSeparators(installDir);

            copyCommands.append(QString("if not exist \"%1\" mkdir \"%1\"").arg(dir));
            copyCommands.append(QString("copy /Y \"%1\" \"%2\"").arg(src, dst));
        }

        fileCount++;
    }

    QStringList installedTypes(installedTypesSet.begin(), installedTypesSet.end());

    if (copyCommands.isEmpty()) {
        emit installResult("No supported files to install", "error", {});
        return;
    }

#ifdef Q_OS_WIN
    // Write a batch file with all copy commands
    QString tempDir = QStandardPaths::writableLocation(QStandardPaths::TempLocation);
    QString batPath = tempDir + "/cm_install.bat";

    QFile batFile(batPath);
    if (batFile.open(QIODevice::WriteOnly | QIODevice::Text)) {
        QTextStream stream(&batFile);
        stream << "@echo off\r\n";
        for (const QString &cmd : copyCommands) {
            stream << cmd << "\r\n";
        }
        batFile.close();
    }

    // Run the batch file elevated via ShellExecuteW runas
    QString nativeBatPath = QDir::toNativeSeparators(batPath);
    HINSTANCE result = ShellExecuteW(
        nullptr,
        L"runas",
        L"cmd.exe",
        reinterpret_cast<const wchar_t *>(QString("/c \"%1\"").arg(nativeBatPath).utf16()),
        nullptr,
        SW_HIDE
    );

    intptr_t resultCode = reinterpret_cast<intptr_t>(result);
    if (resultCode > 32) {
        QString message = QString("Installed %1 file%2")
            .arg(fileCount)
            .arg(fileCount > 1 ? "s" : "");
        qDebug() << "DragDrop:" << message;
        emit installResult(message, "success", installedTypes);
    } else {
        qDebug() << "DragDrop: elevated install failed, code:" << resultCode;
        emit installResult("Installation cancelled or failed", "error", {});
    }
#else
    // On macOS/Linux try direct copy first
    int successCount = 0;
    for (const QString &filePath : files) {
        QFileInfo fileInfo(filePath);
        QString extension = fileInfo.suffix().toLower();
        if (!SUPPORTED_EXTENSIONS.contains(extension)) continue;

        QStringList installPaths = findInstallPaths(extension);
        for (const QString &installDir : installPaths) {
            QDir().mkpath(installDir);
            QString destination = installDir + fileInfo.fileName();
            if (QFile::exists(destination))
                QFile::remove(destination);
            if (QFile::copy(filePath, destination))
                successCount++;
        }
    }

    if (successCount > 0) {
        emit installResult(QString("Installed %1 file%2").arg(successCount).arg(successCount > 1 ? "s" : ""), "success", installedTypes);
    } else {
        emit installResult("Installation failed", "error", {});
    }
#endif
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
