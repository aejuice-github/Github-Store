#ifndef DOWNLOADMANAGER_H
#define DOWNLOADMANAGER_H

#include <QObject>
#include <QNetworkAccessManager>
#include <QMap>
#include "data/DownloadProgress.h"

class DownloadManager : public QObject
{
    Q_OBJECT

public:
    explicit DownloadManager(QObject *parent = nullptr);

    void downloadFile(const QString &componentId, const QUrl &url, const QString &destination);
    void cancelDownload(const QString &componentId);

signals:
    void downloadProgress(const QString &componentId, qint64 bytesReceived, qint64 bytesTotal);
    void downloadCompleted(const QString &componentId, const QString &filePath);
    void downloadError(const QString &componentId, const QString &error);

private:
    QNetworkAccessManager m_networkManager;
    QMap<QString, QNetworkReply*> m_activeDownloads;
};

#endif // DOWNLOADMANAGER_H
