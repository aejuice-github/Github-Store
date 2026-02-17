#include "JsonStorage.h"
#include <QStandardPaths>
#include <QDir>
#include <QFile>
#include <QJsonDocument>
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
        m_installedIds.clear();
        for (const auto &value : doc.array())
            m_installedIds.insert(value.toString());
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

    QJsonArray array;
    for (const auto &id : m_installedIds)
        array.append(id);

    QFile file(storagePath());
    if (!file.open(QIODevice::WriteOnly)) {
        qWarning() << "Failed to save installed.json:" << file.errorString();
        return;
    }

    file.write(QJsonDocument(array).toJson());
}

bool JsonStorage::isInstalled(const QString &componentId) const
{
    return m_installedIds.contains(componentId);
}

void JsonStorage::addInstalled(const QString &componentId)
{
    m_installedIds.insert(componentId);
    save();
}

void JsonStorage::removeInstalled(const QString &componentId)
{
    m_installedIds.remove(componentId);
    save();
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
