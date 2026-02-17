#ifndef APPCONTROLLER_H
#define APPCONTROLLER_H

#include <QObject>
#include <QVariantMap>
#include "models/ComponentModel.h"
#include "models/CategoryModel.h"
#include "managers/SettingsManager.h"
#include "managers/ManifestManager.h"
#include "managers/InstallManager.h"
#include "managers/DownloadManager.h"
#include "managers/DragDropManager.h"
#include "managers/JsonStorage.h"

class AppController : public QObject {
    Q_OBJECT
    Q_PROPERTY(ComponentModel* componentModel READ componentModel CONSTANT)
    Q_PROPERTY(CategoryModel* categoryModel READ categoryModel CONSTANT)
    Q_PROPERTY(InstallManager* installer READ installer CONSTANT)
    Q_PROPERTY(SettingsManager* settings READ settings CONSTANT)
    Q_PROPERTY(DragDropManager* dragDrop READ dragDrop CONSTANT)
    Q_PROPERTY(bool loading READ loading NOTIFY loadingChanged)
    Q_PROPERTY(QString selectedApp READ selectedApp NOTIFY selectedAppChanged)
    Q_PROPERTY(QStringList authors READ authors NOTIFY authorsChanged)

public:
    explicit AppController(QObject *parent = nullptr);

    ComponentModel* componentModel() const;
    CategoryModel* categoryModel() const;
    InstallManager* installer() const;
    SettingsManager* settings() const;
    DragDropManager* dragDrop() const;
    bool loading() const;
    QString selectedApp() const;
    QStringList authors() const;

    Q_INVOKABLE void initialize();

    // Navigation
    Q_INVOKABLE void navigateTo(const QString &screen, const QVariantMap &params = QVariantMap());
    Q_INVOKABLE void navigateBack();

    // Search & Filter
    Q_INVOKABLE void search(const QString &query);
    Q_INVOKABLE void filterByCategory(const QString &category);
    Q_INVOKABLE void filterByApp(const QString &app);
    Q_INVOKABLE void filterByPrice(const QString &filter);
    Q_INVOKABLE void filterByAuthor(const QString &author);
    Q_INVOKABLE QStringList getAuthors() const;

    // Component details
    Q_INVOKABLE QVariantMap getComponentDetails(const QString &componentId);

    // Install
    Q_INVOKABLE void installComponent(const QString &componentId);

    // Favorites
    Q_INVOKABLE bool isFavorite(const QString &componentId) const;
    Q_INVOKABLE void toggleFavorite(const QString &componentId);
    Q_INVOKABLE QVariantList getFavoriteComponents() const;
    Q_INVOKABLE QVariantList getInstalledComponents() const;

    // Available apps for filter dropdown
    Q_INVOKABLE QStringList getAvailableApps() const;

signals:
    void loadingChanged();
    void selectedAppChanged();
    void authorsChanged();
    void navigationRequested(const QString &screen, const QVariantMap &params);
    void navigationBackRequested();
    void toastRequested(const QString &message, const QString &type);

private:
    void onManifestLoaded(const QList<Component> &components, const QStringList &categories);

    ComponentModel *m_componentModel;
    CategoryModel *m_categoryModel;
    SettingsManager *m_settings;
    ManifestManager *m_manifest;
    InstallManager *m_installer;
    DownloadManager *m_downloads;
    DragDropManager *m_dragDrop;
    JsonStorage *m_storage;

    QList<Component> m_allComponents;
    bool m_loading = false;
    QString m_selectedApp = "All";
};

#endif // APPCONTROLLER_H
