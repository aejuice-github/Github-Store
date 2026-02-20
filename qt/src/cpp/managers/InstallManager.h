#ifndef INSTALLMANAGER_H
#define INSTALLMANAGER_H

#include <QObject>
#include <QVariantMap>

#include "../data/Component.h"

class DownloadManager;
class JsonStorage;
class ElevatedCopyHelper;

class InstallManager : public QObject
{
    Q_OBJECT
    Q_PROPERTY(bool isBusy READ isBusy NOTIFY isBusyChanged)

public:
    explicit InstallManager(QObject *parent = nullptr);

    void setDownloadManager(DownloadManager *download);
    void setJsonStorage(JsonStorage *storage);

    bool isBusy() const { return m_isBusy; }

    Q_INVOKABLE void install(const QString &componentId, const QVariantMap &componentData, const QString &version = QString());
    QStringList verifyInstalled(const QList<Component> &components, JsonStorage *storage);

signals:
    void isBusyChanged();
    void installStarted(const QString &componentId);
    void installProgress(const QString &componentId, double progress, const QString &status);
    void installCompleted(const QString &componentId);
    void installFailed(const QString &componentId, const QString &error);

private:
    void finishInstall(const QString &componentId, const QString &filePath,
                       const QString &version, const QString &installPath,
                       const QString &fileName, const QStringList &waitForFinish,
                       const QStringList &silentArgs);
    void runInstaller(const QString &componentId, const QString &filePath,
                      const QString &version, const QStringList &silentArgs,
                      const QStringList &waitForFinish);
    void waitForProcesses(const QString &componentId, const QString &version,
                          const QStringList &processNames);
    QStringList resolveInstallPaths(const QString &installPath) const;
    QStringList findRunningProcesses(const QStringList &processNames) const;
    bool fileExistsAtDestination(const QString &fileName, const QStringList &targetDirs) const;

    DownloadManager *m_download = nullptr;
    JsonStorage *m_storage = nullptr;
    ElevatedCopyHelper *m_elevatedHelper = nullptr;
    bool m_isBusy = false;
};

#endif // INSTALLMANAGER_H
