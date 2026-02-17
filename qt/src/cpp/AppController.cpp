#include "AppController.h"
#include <QCoreApplication>
#include <QStandardPaths>
#include <QProcess>
#include <QFile>
#include <QDir>
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

bool AppController::appUpdateAvailable() const {
    return m_appUpdateAvailable;
}

QString AppController::appUpdateVersion() const {
    return m_appUpdateVersion;
}

int AppController::updatesAvailableCount() const {
    int count = 0;
    for (const auto &component : m_allComponents) {
        if (!m_storage->isInstalled(component.id))
            continue;
        QString installedVersion = m_storage->installedVersion(component.id);
        if (!installedVersion.isEmpty()
            && ManifestManager::compareVersions(installedVersion, component.version) < 0)
            count++;
    }
    return count;
}

void AppController::initialize() {
    m_storage->load();
    m_installer->setDownloadManager(m_downloads);
    m_installer->setJsonStorage(m_storage);

    connect(m_manifest, &ManifestManager::manifestLoaded,
            this, &AppController::onManifestLoaded);

    connect(m_manifest, &ManifestManager::appUpdateAvailable,
            this, &AppController::onAppUpdateAvailable);

    connect(m_installer, &InstallManager::installCompleted,
            this, [this](const QString &componentId) {
                Q_UNUSED(componentId)
                m_componentModel->setInstalledVersions(m_storage->installedVersions());
                emit toastRequested("Installation complete", "success");
            });

    connect(m_installer, &InstallManager::installFailed,
            this, [this](const QString &componentId, const QString &error) {
                Q_UNUSED(componentId)
                emit toastRequested("Installation failed: " + error, "error");
            });

    connect(m_dragDrop, &DragDropManager::installResult,
            this, [this](const QString &message, const QString &type, const QStringList &installedTypes) {
                if (type == "success") {
                    QVariantMap params;
                    params["message"] = message;
                    params["installedTypes"] = installedTypes;
                    emit navigationRequested("installSuccess", params);
                } else {
                    emit toastRequested(message, type);
                }
            });

    m_loading = true;
    emit loadingChanged();
    m_manifest->loadManifest();
}

void AppController::onManifestLoaded(const QList<Component> &components,
                                      const QStringList &categories) {
    m_allComponents = components;
    m_componentModel->setComponents(components);
    m_componentModel->setInstalledVersions(m_storage->installedVersions());
    m_categoryModel->setCategories(categories);
    m_loading = false;
    emit loadingChanged();
    emit authorsChanged();
    emit updatesAvailableCountChanged();
}

void AppController::onAppUpdateAvailable(const QString &version, const QString &url) {
    m_appUpdateVersion = version;
    m_appUpdateUrl = url;
    m_appUpdateAvailable = true;
    emit appUpdateAvailableChanged();
}

void AppController::updateApp() {
    if (m_appUpdateUrl.isEmpty()) {
        emit toastRequested("No update URL available", "error");
        return;
    }

    emit toastRequested("Downloading update...", "info");

    QString tempPath = QStandardPaths::writableLocation(QStandardPaths::TempLocation)
                       + "/ComponentManager_update.exe";

    connect(m_downloads, &DownloadManager::downloadCompleted,
            this, [this, tempPath](const QString &id, const QString &filePath) {
        if (id != "_app_update")
            return;

        Q_UNUSED(filePath)
        QString currentExe = QCoreApplication::applicationFilePath();
        QString batchPath = QStandardPaths::writableLocation(QStandardPaths::TempLocation)
                            + "/cm_update.bat";

        QFile batch(batchPath);
        if (!batch.open(QIODevice::WriteOnly | QIODevice::Text)) {
            emit toastRequested("Failed to create update script", "error");
            return;
        }

        // Batch script: wait for app to close, copy new exe, launch, clean up
        QString script = QString(
            "@echo off\r\n"
            "timeout /t 2 /nobreak >nul\r\n"
            "copy /y \"%1\" \"%2\" >nul\r\n"
            "start \"\" \"%2\"\r\n"
            "del \"%1\" >nul\r\n"
            "del \"%3\" >nul\r\n"
        ).arg(tempPath, currentExe, batchPath);

        batch.write(script.toLocal8Bit());
        batch.close();

        QProcess::startDetached("cmd.exe", {"/c", batchPath});
        QCoreApplication::quit();
    });

    connect(m_downloads, &DownloadManager::downloadError,
            this, [this](const QString &id, const QString &error) {
        if (id != "_app_update")
            return;
        emit toastRequested("Update failed: " + error, "error");
    });

    m_downloads->downloadFile("_app_update", QUrl(m_appUpdateUrl), tempPath);
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

    QString version = details.value("version").toString();
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
                version = component.version;
                break;
            }
        }
    }

    if (platform.isEmpty()) {
        emit toastRequested("No download available for this platform", "error");
        return;
    }

    m_installer->install(componentId, platform, version);
}

// Update all

void AppController::updateAllComponents() {
    for (const auto &component : m_allComponents) {
        if (!m_storage->isInstalled(component.id))
            continue;
        QString installedVersion = m_storage->installedVersion(component.id);
        if (installedVersion.isEmpty())
            continue;
        if (ManifestManager::compareVersions(installedVersion, component.version) < 0)
            installComponent(component.id);
    }
}

// Uninstall

void AppController::uninstallComponent(const QString &componentId) {
    m_storage->removeInstalled(componentId);
    m_componentModel->setInstalledVersions(m_storage->installedVersions());
    emit updatesAvailableCountChanged();
    emit toastRequested("Component uninstalled", "success");
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
            QString installedVersion = m_storage->installedVersion(component.id);
            bool hasUpdate = !installedVersion.isEmpty()
                && ManifestManager::compareVersions(installedVersion, component.version) < 0;
            map["isUpdateAvailable"] = hasUpdate;
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
            QString installedVersion = m_storage->installedVersion(component.id);
            bool hasUpdate = !installedVersion.isEmpty()
                && ManifestManager::compareVersions(installedVersion, component.version) < 0;
            map["isUpdateAvailable"] = hasUpdate;
            list.append(map);
        }
    }
    return list;
}

// Available apps

QStringList AppController::getAvailableApps() const {
    return {"All", "After Effects", "Premiere Pro", "DaVinci Resolve", "Vegas", "Final Cut Pro"};
}
