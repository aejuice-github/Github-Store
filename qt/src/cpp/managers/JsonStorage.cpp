#include "JsonStorage.h"
#include <QStandardPaths>
#include <QDir>
#include <QFile>
#include <QJsonDocument>
#include <QJsonObject>
#include <QJsonArray>
#include <QDebug>

JsonStorage::JsonStorage(QObject *parent)
    : QObject(parent)
{
}

void JsonStorage::load()
{
    QFile file(storagePath());
    if (file.open(QIODevice::ReadOnly)) {
        QJsonDocument doc = QJsonDocument::fromJson(file.readAll());
        m_installedVersions.clear();

        // Support both old format (array of strings) and new format (object with versions)
        if (doc.isArray()) {
            for (const auto &value : doc.array())
                m_installedVersions.insert(value.toString(), "");
        } else if (doc.isObject()) {
            QJsonObject obj = doc.object();
            for (auto it = obj.begin(); it != obj.end(); ++it)
                m_installedVersions.insert(it.key(), it.value().toString());
        }
    }

    QFile favFile(favoritesPath());
    if (favFile.open(QIODevice::ReadOnly)) {
        QJsonDocument doc = QJsonDocument::fromJson(favFile.readAll());
        m_favoriteIds.clear();
        for (const auto &value : doc.array())
            m_favoriteIds.insert(value.toString());
    }
}

void JsonStorage::save()
{
    QDir().mkpath(QFileInfo(storagePath()).absolutePath());

    QJsonObject obj;
    for (auto it = m_installedVersions.begin(); it != m_installedVersions.end(); ++it)
        obj.insert(it.key(), it.value());

    QFile file(storagePath());
    if (!file.open(QIODevice::WriteOnly)) {
        qWarning() << "Failed to save installed.json:" << file.errorString();
        return;
    }

    file.write(QJsonDocument(obj).toJson());
}

bool JsonStorage::isInstalled(const QString &componentId) const
{
    return m_installedVersions.contains(componentId);
}

void JsonStorage::addInstalled(const QString &componentId, const QString &version)
{
    m_installedVersions.insert(componentId, version);
    save();
}

void JsonStorage::removeInstalled(const QString &componentId)
{
    m_installedVersions.remove(componentId);
    save();
}

QSet<QString> JsonStorage::installedIds() const
{
    QSet<QString> ids;
    for (auto it = m_installedVersions.begin(); it != m_installedVersions.end(); ++it)
        ids.insert(it.key());
    return ids;
}

QString JsonStorage::installedVersion(const QString &componentId) const
{
    return m_installedVersions.value(componentId);
}

bool JsonStorage::isFavorite(const QString &componentId) const
{
    return m_favoriteIds.contains(componentId);
}

void JsonStorage::toggleFavorite(const QString &componentId)
{
    if (m_favoriteIds.contains(componentId))
        m_favoriteIds.remove(componentId);
    else
        m_favoriteIds.insert(componentId);

    QDir().mkpath(QFileInfo(favoritesPath()).absolutePath());
    QJsonArray array;
    for (const auto &id : m_favoriteIds)
        array.append(id);

    QFile file(favoritesPath());
    if (file.open(QIODevice::WriteOnly))
        file.write(QJsonDocument(array).toJson());
}

QString JsonStorage::storagePath() const
{
    return QStandardPaths::writableLocation(QStandardPaths::AppDataLocation)
           + "/installed.json";
}

QString JsonStorage::favoritesPath() const
{
    return QStandardPaths::writableLocation(QStandardPaths::AppDataLocation)
           + "/favorites.json";
}
