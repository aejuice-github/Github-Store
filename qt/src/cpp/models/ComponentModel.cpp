#include "ComponentModel.h"

ComponentModel::ComponentModel(QObject *parent)
    : QAbstractListModel(parent)
{
}

int ComponentModel::rowCount(const QModelIndex &parent) const
{
    Q_UNUSED(parent)
    return m_filteredComponents.count();
}

QVariant ComponentModel::data(const QModelIndex &index, int role) const
{
    if (!index.isValid() || index.row() >= m_filteredComponents.count())
        return {};

    const Component &component = m_filteredComponents[index.row()];

    switch (role) {
    case IdRole: return component.id;
    case NameRole: return component.name;
    case TypeRole: return component.type;
    case DescriptionRole: return component.description;
    case VersionRole: return component.version;
    case AuthorRole: return component.author;
    case CategoryRole: return component.category;
    case IconRole: return component.icon;
    case PriceRole: return component.price;
    case IsInstalledRole: return m_installedIds.contains(component.id);
    case CompatibleAppsRole: return component.compatibleApps;
    case TagsRole: return component.tags;
    case RunnableRole: return component.runnable;
    }

    return {};
}

QHash<int, QByteArray> ComponentModel::roleNames() const
{
    return {
        {IdRole, "componentId"},
        {NameRole, "name"},
        {TypeRole, "type"},
        {DescriptionRole, "description"},
        {VersionRole, "version"},
        {AuthorRole, "author"},
        {CategoryRole, "category"},
        {IconRole, "icon"},
        {PriceRole, "price"},
        {IsInstalledRole, "isInstalled"},
        {CompatibleAppsRole, "compatibleApps"},
        {TagsRole, "tags"},
        {RunnableRole, "runnable"}
    };
}

void ComponentModel::setComponents(const QList<Component> &components)
{
    m_allComponents = components;
    applyFilters();
}

void ComponentModel::search(const QString &query)
{
    m_searchQuery = query;
    applyFilters();
}

void ComponentModel::setSelectedCategory(const QString &category)
{
    m_categoryFilter = category;
    applyFilters();
}

void ComponentModel::setSelectedApp(const QString &app)
{
    m_appFilter = app;
    applyFilters();
}

void ComponentModel::setSelectedAuthor(const QString &author)
{
    m_authorFilter = author;
    applyFilters();
}

void ComponentModel::setPriceFilter(const QString &filter)
{
    m_priceFilter = filter;
    applyFilters();
}

QStringList ComponentModel::getAuthors() const
{
    QSet<QString> authors;
    for (const auto &component : m_allComponents)
        authors.insert(component.author);

    QStringList list = authors.values();
    list.sort(Qt::CaseInsensitive);
    list.prepend("All");
    return list;
}

void ComponentModel::setInstalledIds(const QSet<QString> &ids)
{
    m_installedIds = ids;
    emit dataChanged(index(0), index(rowCount() - 1), {IsInstalledRole});
}

QVariantMap ComponentModel::getComponentById(const QString &id) const
{
    for (const auto &component : m_allComponents) {
        if (component.id == id) {
            QVariantMap map = component.toVariantMap();
            map["isInstalled"] = m_installedIds.contains(id);

#ifdef Q_OS_WIN
            QString platformKey = "windows";
#else
            QString platformKey = "macos";
#endif
            if (component.platforms.contains(platformKey))
                map["platform"] = component.platforms[platformKey].toVariantMap();

            return map;
        }
    }
    return {};
}

void ComponentModel::applyFilters()
{
    beginResetModel();
    m_filteredComponents.clear();

    for (const auto &component : m_allComponents) {
        // Category filter
        if (!m_categoryFilter.isEmpty() && m_categoryFilter != "All") {
            if (component.category != m_categoryFilter)
                continue;
        }

        // App filter
        if (!m_appFilter.isEmpty() && m_appFilter != "All") {
            if (!component.compatibleApps.contains(m_appFilter))
                continue;
        }

        // Author filter
        if (!m_authorFilter.isEmpty() && m_authorFilter != "All") {
            if (component.author != m_authorFilter)
                continue;
        }

        // Price filter
        if (!m_priceFilter.isEmpty() && m_priceFilter != "All") {
            if (m_priceFilter == "Free" && component.price > 0)
                continue;
            if (m_priceFilter == "Paid" && component.price == 0)
                continue;
        }

        // Search query
        if (!m_searchQuery.isEmpty()) {
            bool matches = component.name.contains(m_searchQuery, Qt::CaseInsensitive)
                        || component.description.contains(m_searchQuery, Qt::CaseInsensitive)
                        || component.author.contains(m_searchQuery, Qt::CaseInsensitive)
                        || component.category.contains(m_searchQuery, Qt::CaseInsensitive);

            if (!matches) {
                for (const auto &tag : component.tags) {
                    if (tag.contains(m_searchQuery, Qt::CaseInsensitive)) {
                        matches = true;
                        break;
                    }
                }
            }

            if (!matches)
                continue;
        }

        m_filteredComponents.append(component);
    }

    endResetModel();
    emit countChanged();
}
