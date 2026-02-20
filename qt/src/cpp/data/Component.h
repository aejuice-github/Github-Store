#ifndef COMPONENT_H
#define COMPONENT_H

#include <QString>
#include <QStringList>
#include <QVariantMap>
#include <QJsonObject>
#include <QJsonArray>

struct PlatformAsset {
    QString url;
    QString sha256;
    qint64 size = 0;
    QString installPath;
    bool requiresAdmin = false;
    QString fileName;
    QStringList waitForFinish;  // Processes that must be closed before overwriting
    QStringList silentArgs;     // Arguments for silent/hidden install (exe/msi)

    static PlatformAsset fromJson(const QJsonObject &json) {
        PlatformAsset asset;
        asset.url = json["url"].toString();
        asset.sha256 = json["sha256"].toString();
        asset.size = json["size"].toVariant().toLongLong();
        asset.installPath = json["installPath"].toString();
        asset.requiresAdmin = json["requiresAdmin"].toBool();
        asset.fileName = json["fileName"].toString();
        for (const auto &proc : json["wait_for_finish"].toArray())
            asset.waitForFinish.append(proc.toString());
        for (const auto &arg : json["silentArgs"].toArray())
            asset.silentArgs.append(arg.toString());
        return asset;
    }

    QVariantMap toVariantMap() const {
        QVariantList procList;
        for (const auto &proc : waitForFinish)
            procList.append(proc);
        QVariantList argsList;
        for (const auto &arg : silentArgs)
            argsList.append(arg);
        return {
            {"url", url},
            {"sha256", sha256},
            {"size", size},
            {"installPath", installPath},
            {"requiresAdmin", requiresAdmin},
            {"fileName", fileName},
            {"waitForFinish", procList},
            {"silentArgs", argsList}
        };
    }
};

struct ComponentDependency {
    QString id;
    QString version;

    static ComponentDependency fromJson(const QJsonObject &json) {
        ComponentDependency dep;
        dep.id = json["id"].toString();
        dep.version = json["version"].toString();
        return dep;
    }
};

struct ComponentHooks {
    QString postInstall;
    QString preRemove;

    static ComponentHooks fromJson(const QJsonObject &json) {
        ComponentHooks hooks;
        hooks.postInstall = json["postInstall"].toString();
        hooks.preRemove = json["preRemove"].toString();
        return hooks;
    }
};

struct Component {
    QString id;
    QString name;
    QString type; // plugin, script, extension, software
    QString description;
    QString tooltip;
    QString version;
    QString author;
    QString category;
    QStringList tags;
    QString icon;
    QStringList screenshots;
    QMap<QString, PlatformAsset> platforms;
    QList<ComponentDependency> dependencies;
    ComponentHooks hooks;
    bool hasHooks = false;
    bool runnable = false;
    QString runCommand;
    QString changelog;
    QStringList compatibleApps;
    int price = 0;
    int rolloutPercentage = 100;
    bool required = false;
    QString pageUrl;

    static Component fromJson(const QJsonObject &json) {
        Component component;
        component.id = json["id"].toString();
        component.name = json["name"].toString();
        component.type = json["type"].toString();
        component.description = json["description"].toString();
        component.tooltip = json["tooltip"].toString();
        component.version = json["version"].toString();
        component.author = json["author"].toString();
        component.category = json["category"].toString();
        component.icon = json["icon"].toString();
        component.runnable = json["runnable"].toBool();
        component.runCommand = json["runCommand"].toString();
        component.changelog = json["changelog"].toString();
        component.price = json["price"].toInt();
        component.rolloutPercentage = json.contains("rollout_percentage")
            ? json["rollout_percentage"].toInt() : 100;
        component.required = json["required"].toBool();
        component.pageUrl = json["page_url"].toString();

        for (const auto &tag : json["tags"].toArray())
            component.tags.append(tag.toString());

        for (const auto &screenshot : json["screenshots"].toArray())
            component.screenshots.append(screenshot.toString());

        for (const auto &app : json["compatibleApps"].toArray())
            component.compatibleApps.append(app.toString());

        QJsonObject platformsObj = json["platforms"].toObject();
        for (auto it = platformsObj.begin(); it != platformsObj.end(); ++it)
            component.platforms[it.key()] = PlatformAsset::fromJson(it.value().toObject());

        for (const auto &dep : json["dependencies"].toArray())
            component.dependencies.append(ComponentDependency::fromJson(dep.toObject()));

        if (json.contains("hooks") && !json["hooks"].isNull()) {
            component.hooks = ComponentHooks::fromJson(json["hooks"].toObject());
            component.hasHooks = true;
        }

        return component;
    }

    QVariantMap toVariantMap() const {
        QVariantList tagsList, screenshotsList, appsList;
        for (const auto &tag : tags) tagsList.append(tag);
        for (const auto &s : screenshots) screenshotsList.append(s);
        for (const auto &app : compatibleApps) appsList.append(app);

        return {
            {"id", id},
            {"name", name},
            {"type", type},
            {"description", description},
            {"tooltip", tooltip},
            {"version", version},
            {"author", author},
            {"category", category},
            {"tags", tagsList},
            {"icon", icon},
            {"screenshots", screenshotsList},
            {"runnable", runnable},
            {"runCommand", runCommand},
            {"changelog", changelog},
            {"compatibleApps", appsList},
            {"price", price},
            {"pageUrl", pageUrl}
        };
    }
};

#endif // COMPONENT_H
