#include "AppController.h"
#include <QDebug>

AppController::AppController(QObject *parent)
    : QObject(parent)
    , m_componentModel(new ComponentModel(this))
    , m_categoryModel(new CategoryModel(this))
    , m_settings(new SettingsManager(this))
    , m_manifest(new ManifestManager(this))
    , m_installer(new InstallManager(this))
    , m_downloads(new DownloadManager(this))
    , m_dragDrop(new DragDropManager(this))
    , m_storage(new JsonStorage(this)) {
}

ComponentModel* AppController::componentModel() const { return m_componentModel; }
CategoryModel* AppController::categoryModel() const { return m_categoryModel; }
InstallManager* AppController::installer() const { return m_installer; }
SettingsManager* AppController::settings() const { return m_settings; }
DragDropManager* AppController::dragDrop() const { return m_dragDrop; }

bool AppController::loading() const {
    return m_loading;
}

void AppController::initialize() {
    m_storage->load();
    m_installer->setDownloadManager(m_downloads);
    m_installer->setJsonStorage(m_storage);

    connect(m_manifest, &ManifestManager::manifestLoaded,
            this, &AppController::onManifestLoaded);

    connect(m_installer, &InstallManager::installCompleted,
            this, [this](const QString &componentId) {
                Q_UNUSED(componentId)
                m_componentModel->setInstalledIds(m_storage->installedIds());
                emit toastRequested("Installation complete", "success");
            });

    connect(m_installer, &InstallManager::installFailed,
            this, [this](const QString &componentId, const QString &error) {
                Q_UNUSED(componentId)
                emit toastRequested("Installation failed: " + error, "error");
            });

    m_loading = true;
    emit loadingChanged();
    m_manifest->loadManifest();
}

void AppController::onManifestLoaded(const QList<Component> &components,
                                      const QStringList &categories) {
    m_allComponents = components;
    m_componentModel->setComponents(components);
    m_componentModel->setInstalledIds(m_storage->installedIds());
    m_categoryModel->setCategories(categories);
    m_loading = false;
    emit loadingChanged();
    emit authorsChanged();
}

// Navigation

void AppController::navigateTo(const QString &screen, const QVariantMap &params) {
    emit navigationRequested(screen, params);
}

void AppController::navigateBack() {
    emit navigationBackRequested();
}

// Search & Filter

void AppController::search(const QString &query) {
    m_componentModel->search(query);
}

void AppController::filterByCategory(const QString &category) {
    m_componentModel->setSelectedCategory(category);
    m_categoryModel->setSelectedCategory(category);
}

QString AppController::selectedApp() const {
    return m_selectedApp;
}

void AppController::filterByApp(const QString &app) {
    m_selectedApp = app;
    m_componentModel->setSelectedApp(app);
    emit selectedAppChanged();
}

void AppController::filterByPrice(const QString &filter) {
    m_componentModel->setPriceFilter(filter);
}

void AppController::filterByAuthor(const QString &author) {
    m_componentModel->setSelectedAuthor(author);
}

QStringList AppController::authors() const {
    return m_componentModel->getAuthors();
}

QStringList AppController::getAuthors() const {
    return m_componentModel->getAuthors();
}

// Component details

QVariantMap AppController::getComponentDetails(const QString &componentId) {
    for (const auto &component : m_allComponents) {
        if (component.id == componentId) {
            QVariantMap details = component.toVariantMap();
            details["isInstalled"] = m_storage->isInstalled(componentId);
            return details;
        }
    }
    return QVariantMap();
}

// Install

void AppController::installComponent(const QString &componentId) {
    QVariantMap details = getComponentDetails(componentId);
    if (details.isEmpty())
        return;

    QVariantMap platform = details.value("platform").toMap();
    if (platform.isEmpty()) {
        // Try to get platform data directly
        for (const auto &component : m_allComponents) {
            if (component.id == componentId) {
#ifdef Q_OS_WIN
                QString key = "windows";
#else
                QString key = "macos";
#endif
                if (component.platforms.contains(key))
                    platform = component.platforms[key].toVariantMap();
                break;
            }
        }
    }

    if (platform.isEmpty()) {
        emit toastRequested("No download available for this platform", "error");
        return;
    }

    m_installer->install(componentId, platform);
}

// Favorites

bool AppController::isFavorite(const QString &componentId) const {
    return m_storage->isFavorite(componentId);
}

void AppController::toggleFavorite(const QString &componentId) {
    m_storage->toggleFavorite(componentId);
}

QVariantList AppController::getFavoriteComponents() const {
    QVariantList list;
    QSet<QString> favIds = m_storage->favoriteIds();
    for (const auto &component : m_allComponents) {
        if (favIds.contains(component.id)) {
            QVariantMap map = component.toVariantMap();
            map["isInstalled"] = m_storage->isInstalled(component.id);
            list.append(map);
        }
    }
    return list;
}

QVariantList AppController::getInstalledComponents() const {
    QVariantList list;
    for (const auto &component : m_allComponents) {
        if (m_storage->isInstalled(component.id)) {
            QVariantMap map = component.toVariantMap();
            map["isInstalled"] = true;
            list.append(map);
        }
    }
    return list;
}

// Available apps

QStringList AppController::getAvailableApps() const {
    return {"All", "After Effects", "Premiere Pro", "DaVinci Resolve", "Vegas", "Final Cut Pro"};
}
