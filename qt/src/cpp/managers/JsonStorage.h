#ifndef JSONSTORAGE_H
#define JSONSTORAGE_H

#include <QObject>
#include <QSet>

class JsonStorage : public QObject
{
    Q_OBJECT

public:
    explicit JsonStorage(QObject *parent = nullptr);

    void load();
    void save();

    bool isInstalled(const QString &componentId) const;
    void addInstalled(const QString &componentId);
    void removeInstalled(const QString &componentId);
    QSet<QString> installedIds() const { return m_installedIds; }

    bool isFavorite(const QString &componentId) const;
    void toggleFavorite(const QString &componentId);
    QSet<QString> favoriteIds() const { return m_favoriteIds; }

private:
    QString storagePath() const;
    QString favoritesPath() const;

    QSet<QString> m_installedIds;
    QSet<QString> m_favoriteIds;
};

#endif // JSONSTORAGE_H
