#ifndef JSONSTORAGE_H
#define JSONSTORAGE_H

#include <QObject>
#include <QSet>
#include <QMap>

class JsonStorage : public QObject
{
    Q_OBJECT

public:
    explicit JsonStorage(QObject *parent = nullptr);

    void load();
    void save();

    bool isInstalled(const QString &componentId) const;
    void addInstalled(const QString &componentId, const QString &version);
    void removeInstalled(const QString &componentId);
    QSet<QString> installedIds() const;
    QMap<QString, QString> installedVersions() const { return m_installedVersions; }
    QString installedVersion(const QString &componentId) const;

    bool isFavorite(const QString &componentId) const;
    void toggleFavorite(const QString &componentId);
    QSet<QString> favoriteIds() const { return m_favoriteIds; }

    // Rollout: returns true if user is in the rollout for this component
    bool isInRollout(const QString &componentId, int rolloutPercentage);

private:
    QString storagePath() const;
    QString favoritesPath() const;
    QString rolloutPath() const;
    void loadRollout();
    void saveRollout();

    QMap<QString, QString> m_installedVersions;
    QSet<QString> m_favoriteIds;

    // Maps componentId -> {roll: 1-100, percentage: last seen rollout_percentage}
    struct RolloutEntry { int roll; int percentage; };
    QMap<QString, RolloutEntry> m_rolloutEntries;
    bool m_rolloutLoaded = false;
};

#endif // JSONSTORAGE_H
