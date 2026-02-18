#ifndef CATEGORYMODEL_H
#define CATEGORYMODEL_H

#include <QAbstractListModel>
#include <QStringList>

class CategoryModel : public QAbstractListModel
{
    Q_OBJECT
    Q_PROPERTY(QString selectedCategory READ selectedCategory NOTIFY selectedCategoryChanged)

public:
    enum Roles {
        NameRole = Qt::UserRole + 1,
        IsSelectedRole
    };

    explicit CategoryModel(QObject *parent = nullptr);

    int rowCount(const QModelIndex &parent = QModelIndex()) const override;
    QVariant data(const QModelIndex &index, int role) const override;
    QHash<int, QByteArray> roleNames() const override;

    QString selectedCategory() const { return m_selectedCategory; }

    void setCategories(const QStringList &categories);
    void setSelectedCategory(const QString &category);

signals:
    void selectedCategoryChanged();

private:
    QStringList m_categories;
    QString m_selectedCategory = "All";
};

#endif // CATEGORYMODEL_H
