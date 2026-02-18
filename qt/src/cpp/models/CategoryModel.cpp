#include "CategoryModel.h"

CategoryModel::CategoryModel(QObject *parent)
    : QAbstractListModel(parent)
{
}

int CategoryModel::rowCount(const QModelIndex &parent) const
{
    Q_UNUSED(parent)
    return m_categories.count();
}

QVariant CategoryModel::data(const QModelIndex &index, int role) const
{
    if (!index.isValid() || index.row() >= m_categories.count())
        return {};

    switch (role) {
    case NameRole: return m_categories[index.row()];
    case IsSelectedRole: return m_categories[index.row()] == m_selectedCategory;
    }

    return {};
}

QHash<int, QByteArray> CategoryModel::roleNames() const
{
    return {
        {NameRole, "name"},
        {IsSelectedRole, "isSelected"}
    };
}

void CategoryModel::setCategories(const QStringList &categories)
{
    beginResetModel();
    m_categories.clear();
    m_categories.append("All");
    m_categories.append(categories);
    endResetModel();
}

void CategoryModel::setSelectedCategory(const QString &category)
{
    if (m_selectedCategory == category)
        return;
    m_selectedCategory = category;
    emit selectedCategoryChanged();
    emit dataChanged(index(0), index(rowCount() - 1), {IsSelectedRole});
}
