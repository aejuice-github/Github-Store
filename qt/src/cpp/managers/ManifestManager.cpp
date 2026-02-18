#include "ManifestManager.h"
#include "JsonStorage.h"
#include <QNetworkReply>
#include <QJsonDocument>
#include <QJsonObject>
#include <QJsonArray>
#include <QFile>
#include <QCoreApplication>
#include <QDebug>

ManifestManager::ManifestManager(QObject *parent)
    : QObject(parent)
{
    m_networkManager.setRedirectPolicy(QNetworkRequest::NoLessSafeRedirectPolicy);
    connect(&m_networkManager, &QNetworkAccessManager::finished,
            this, &ManifestManager::onNetworkReply);
}

void ManifestManager::setJsonStorage(JsonStorage *storage)
{
    m_storage = storage;
}

void ManifestManager::loadManifest()
{
    QUrl url(m_manifestUrl);
    QNetworkRequest request(url);
    request.setAttribute(QNetworkRequest::RedirectPolicyAttribute,
                         static_cast<int>(QNetworkRequest::NoLessSafeRedirectPolicy));
    m_networkManager.get(request);
}

void ManifestManager::onNetworkReply(QNetworkReply *reply)
{
    reply->deleteLater();

    if (reply->error() != QNetworkReply::NoError) {
        qWarning() << "Network error, falling back to bundled manifest:" << reply->errorString();
        qWarning() << "URL:" << reply->url().toString();
        qWarning() << "HTTP status:" << reply->attribute(QNetworkRequest::HttpStatusCodeAttribute).toInt();
        loadBundledManifest();
        return;
    }

    QByteArray data = reply->readAll();
    qDebug() << "Manifest loaded from network, size:" << data.size();

    QJsonDocument doc = QJsonDocument::fromJson(data);
    if (doc.isNull() || doc.object()["components"].toArray().isEmpty()) {
        qWarning() << "Network manifest has no components, falling back to bundled";
        loadBundledManifest();
        return;
    }

    parseManifest(data);
}

void ManifestManager::parseManifest(const QByteArray &data)
{
    QJsonDocument doc = QJsonDocument::fromJson(data);
    if (doc.isNull()) {
        emit errorOccurred("Invalid manifest JSON");
        return;
    }

    QJsonObject root = doc.object();
    QStringList categories;
    QList<Component> components;

    for (const auto &category : root["categories"].toArray())
        categories.append(category.toString());

    for (const auto &componentJson : root["components"].toArray()) {
        Component component = Component::fromJson(componentJson.toObject());
        if (m_storage && !m_storage->isInRollout(component.id, component.rolloutPercentage))
            continue;
        components.append(component);
    }

    // Check for app update
    QString manifestAppVersion = root["appVersion"].toString();
    QString appUpdateUrl = root["appUpdateUrl"].toString();
    if (!manifestAppVersion.isEmpty()) {
        QString currentVersion = QCoreApplication::applicationVersion();
        if (compareVersions(currentVersion, manifestAppVersion) < 0)
            emit appUpdateAvailable(manifestAppVersion, appUpdateUrl);
    }

    emit manifestLoaded(components, categories);
}

void ManifestManager::loadBundledManifest()
{
    QFile file(":/resources/bundled-manifest.json");
    if (!file.open(QIODevice::ReadOnly)) {
        emit errorOccurred("Failed to load bundled manifest");
        return;
    }

    parseManifest(file.readAll());
}

int ManifestManager::compareVersions(const QString &a, const QString &b)
{
    auto partsA = a.mid(a.startsWith("v") ? 1 : 0).split(".");
    auto partsB = b.mid(b.startsWith("v") ? 1 : 0).split(".");
    int maxLength = qMax(partsA.size(), partsB.size());
    for (int i = 0; i < maxLength; i++) {
        int numberA = i < partsA.size() ? partsA[i].toInt() : 0;
        int numberB = i < partsB.size() ? partsB[i].toInt() : 0;
        if (numberA != numberB)
            return numberA < numberB ? -1 : 1;
    }
    return 0;
}
