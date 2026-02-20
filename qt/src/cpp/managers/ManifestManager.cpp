#include "ManifestManager.h"
#include "JsonStorage.h"
#include <QNetworkReply>
#include <QJsonDocument>
#include <QJsonObject>
#include <QJsonArray>
#include <QFile>
#include <QUrl>
#include <QSet>
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
        qWarning() << "Network error:" << reply->errorString();
        emit errorOccurred("Internet connection error");
        return;
    }

    QByteArray data = reply->readAll();
    qDebug() << "Manifest loaded from network, size:" << data.size();

    QJsonDocument doc = QJsonDocument::fromJson(data);
    if (doc.isNull() || !doc.isArray() || doc.array().isEmpty()) {
        qWarning() << "Network manifest invalid";
        emit errorOccurred("Failed to load component list");
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

    QStringList categories;
    QList<Component> components;
    QSet<QString> categorySet;

    for (const auto &componentJson : doc.array()) {
        QJsonObject obj = componentJson.toObject();
        Component component = Component::fromJson(obj);

        applyDefaults(component, obj);

#ifdef Q_OS_WIN
        QString platform = "windows";
#else
        QString platform = "macos";
#endif
        if (!component.platforms.isEmpty() && !component.platforms.contains(platform))
            continue;
        if (m_storage && !m_storage->isInRollout(component.id, component.rolloutPercentage))
            continue;
        if (!component.category.isEmpty() && !categorySet.contains(component.category)) {
            categorySet.insert(component.category);
            categories.append(component.category);
        }
        components.append(component);
    }

    categories.sort();
    categories.prepend("All");
    emit manifestLoaded(components, categories);
}

void ManifestManager::applyDefaults(Component &component, const QJsonObject &json)
{
    if (component.id.isEmpty() && !component.name.isEmpty())
        component.id = component.name.toLower().replace(" ", "-").replace("&", "and");

    if (component.version.isEmpty())
        component.version = "1.0";

    // Skip if platforms already have windows/macos keys (fully specified)
    if (component.platforms.contains("windows") || component.platforms.contains("macos"))
        return;

    QJsonObject platforms = json["platforms"].toObject();
    QString baseUrl = "https://install.aejuice.com/";
    QString urlName = QUrl::toPercentEncoding(component.name, "", " ");
    urlName.replace("%20", "%20");  // keep spaces encoded

    if (component.type == "plugin") {
        bool hasAdobe = platforms.contains("adobe");
        bool hasOpenFx = platforms.contains("openfx");
        if (!hasAdobe && !hasOpenFx)
            return;

        PlatformAsset windows;
        windows.url = baseUrl + "Plugins/Win/Adobe/" + urlName + ".aex";
        windows.installPath = "%PROGRAMFILES%/Adobe/Common/Plug-ins/7.0/MediaCore/";
        windows.requiresAdmin = true;
        windows.fileName = "AEJuice " + component.name + ".aex";
        component.platforms["windows"] = windows;

        PlatformAsset macos;
        macos.url = baseUrl + "Plugins/Mac/Adobe/" + urlName + ".plugin";
        macos.installPath = "/Library/Application Support/Adobe/Common/Plug-ins/7.0/MediaCore/";
        macos.requiresAdmin = true;
        macos.fileName = "AEJuice " + component.name + ".plugin";
        component.platforms["macos"] = macos;

        if (!hasOpenFx)
            component.compatibleApps = {"After Effects", "Premiere Pro"};
        else
            component.compatibleApps = {"After Effects", "Premiere Pro", "DaVinci Resolve", "Vegas"};
    }
    else if (component.type == "script") {
        PlatformAsset windows;
        QString file = json["file"].toString();
        if (file.isEmpty())
            file = urlName + ".jsx";
        windows.url = baseUrl + "Scripts/" + file;
        windows.installPath = "scriptui";
        windows.fileName = file;
        component.platforms["windows"] = windows;

        PlatformAsset macos;
        macos.url = windows.url;
        macos.installPath = "scriptui";
        macos.fileName = file;
        component.platforms["macos"] = macos;

        component.compatibleApps = {"After Effects"};
    }
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
