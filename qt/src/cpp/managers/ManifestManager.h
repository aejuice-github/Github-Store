#ifndef MANIFESTMANAGER_H
#define MANIFESTMANAGER_H

#include <QObject>
#include <QNetworkAccessManager>
#include <QList>
#include <QStringList>
#include "data/Component.h"

class JsonStorage;

class ManifestManager : public QObject
{
    Q_OBJECT

public:
    explicit ManifestManager(QObject *parent = nullptr);

    void setJsonStorage(JsonStorage *storage);
    void loadManifest();

    static int compareVersions(const QString &a, const QString &b);

signals:
    void manifestLoaded(const QList<Component> &components, const QStringList &categories);
    void appUpdateAvailable(const QString &version, const QString &url);
    void errorOccurred(const QString &error);

private slots:
    void onNetworkReply(QNetworkReply *reply);

private:
    void parseManifest(const QByteArray &data);
    void loadBundledManifest();

    QNetworkAccessManager m_networkManager;
    JsonStorage *m_storage = nullptr;
    QString m_manifestUrl = "https://install.aejuice.com/Products.json";
};

#endif // MANIFESTMANAGER_H
