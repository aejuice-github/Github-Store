#include "DownloadManager.h"
#include <QNetworkReply>
#include <QFile>
#include <QDir>
#include <QStandardPaths>

DownloadManager::DownloadManager(QObject *parent)
    : QObject(parent)
{
}

void DownloadManager::downloadFile(const QString &componentId, const QUrl &url, const QString &destination)
{
    QNetworkRequest request(url);
    request.setAttribute(QNetworkRequest::RedirectPolicyAttribute, QNetworkRequest::NoLessSafeRedirectPolicy);

    QNetworkReply *reply = m_networkManager.get(request);
    m_activeDownloads[componentId] = reply;

    connect(reply, &QNetworkReply::downloadProgress,
            this, [this, componentId](qint64 bytesReceived, qint64 bytesTotal) {
        emit downloadProgress(componentId, bytesReceived, bytesTotal);
    });

    connect(reply, &QNetworkReply::finished,
            this, [this, componentId, destination, reply]() {
        reply->deleteLater();
        m_activeDownloads.remove(componentId);

        if (reply->error() != QNetworkReply::NoError) {
            emit downloadError(componentId, reply->errorString());
            return;
        }

        QDir().mkpath(QFileInfo(destination).absolutePath());
        QFile file(destination);
        if (!file.open(QIODevice::WriteOnly)) {
            emit downloadError(componentId, "Failed to write file: " + destination);
            return;
        }

        file.write(reply->readAll());
        file.close();
        emit downloadCompleted(componentId, destination);
    });
}

void DownloadManager::cancelDownload(const QString &componentId)
{
    if (m_activeDownloads.contains(componentId)) {
        m_activeDownloads[componentId]->abort();
        m_activeDownloads.remove(componentId);
    }
}
