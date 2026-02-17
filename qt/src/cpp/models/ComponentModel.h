#ifndef COMPONENTMODEL_H
#define COMPONENTMODEL_H

#include <QAbstractListModel>
#include <QList>
#include <QMap>
#include "data/Component.h"

class ComponentModel : public QAbstractListModel
{
    Q_OBJECT
    Q_PROPERTY(int count READ count NOTIFY countChanged)

public:
    enum Roles {
        IdRole = Qt::UserRole + 1,
        NameRole,
        TypeRole,
        DescriptionRole,
        VersionRole,
        AuthorRole,
        CategoryRole,
        IconRole,
        PriceRole,
        IsInstalledRole,
        IsUpdateAvailableRole,
        CompatibleAppsRole,
        TagsRole,
        RunnableRole
    };

    explicit ComponentModel(QObject *parent = nullptr);

    int rowCount(const QModelIndex &parent = QModelIndex()) const override;
    QVariant data(const QModelIndex &index, int role) const override;
    QHash<int, QByteArray> roleNames() const override;

    int count() const { return m_filteredComponents.count(); }

    void setComponents(const QList<Component> &components);
    void search(const QString &query);
    void setSelectedCategory(const QString &category);
    void setSelectedApp(const QString &app);
    void setSelectedAuthor(const QString &author);
    void setPriceFilter(const QString &filter);
    void setInstalledIds(const QSet<QString> &ids);
    void setInstalledVersions(const QMap<QString, QString> &versions);
    QStringList getAuthors() const;

    QVariantMap getComponentById(const QString &id) const;

signals:
    void countChanged();

private:
    void applyFilters();
    bool isUpdateAvailable(const Component &component) const;

    QList<Component> m_allComponents;
    QList<Component> m_filteredComponents;
    QSet<QString> m_installedIds;
    QMap<QString, QString> m_installedVersions;
    QString m_searchQuery;
    QString m_categoryFilter;
    QString m_appFilter;
    QString m_authorFilter;
    QString m_priceFilter;
};

#endif // COMPONENTMODEL_H
