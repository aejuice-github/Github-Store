#ifndef SETTINGSMANAGER_H
#define SETTINGSMANAGER_H

#include <QObject>
#include <QSettings>

class SettingsManager : public QObject
{
    Q_OBJECT
    Q_PROPERTY(QString themeColor READ themeColor WRITE setThemeColor NOTIFY themeColorChanged)
    Q_PROPERTY(bool autoUpdate READ autoUpdate WRITE setAutoUpdate NOTIFY autoUpdateChanged)
    Q_PROPERTY(bool keepUpToDate READ keepUpToDate WRITE setKeepUpToDate NOTIFY keepUpToDateChanged)

public:
    explicit SettingsManager(QObject *parent = nullptr);

    QString themeColor() const;
    void setThemeColor(const QString &color);

    bool autoUpdate() const;
    void setAutoUpdate(bool enabled);

    bool keepUpToDate() const;
    void setKeepUpToDate(bool enabled);

    Q_INVOKABLE QString getValue(const QString &key, const QString &defaultValue = "") const;
    Q_INVOKABLE void setValue(const QString &key, const QString &value);

signals:
    void themeColorChanged();
    void autoUpdateChanged();
    void keepUpToDateChanged();

private:
    QSettings m_settings;
};

#endif // SETTINGSMANAGER_H
