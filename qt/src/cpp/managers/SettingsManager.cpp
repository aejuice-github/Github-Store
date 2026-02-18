#include "SettingsManager.h"

SettingsManager::SettingsManager(QObject *parent)
    : QObject(parent)
    , m_settings("AEJuice", "ComponentManager")
{
}

QString SettingsManager::themeColor() const
{
    return m_settings.value("theme/color", "ocean").toString();
}

void SettingsManager::setThemeColor(const QString &color)
{
    if (themeColor() == color)
        return;
    m_settings.setValue("theme/color", color);
    emit themeColorChanged();
}

bool SettingsManager::autoUpdate() const
{
    return m_settings.value("updates/autoUpdate", false).toBool();
}

void SettingsManager::setAutoUpdate(bool enabled)
{
    if (autoUpdate() == enabled)
        return;
    m_settings.setValue("updates/autoUpdate", enabled);
    emit autoUpdateChanged();
}

bool SettingsManager::keepUpToDate() const
{
    return m_settings.value("updates/keepUpToDate", true).toBool();
}

void SettingsManager::setKeepUpToDate(bool enabled)
{
    if (keepUpToDate() == enabled)
        return;
    m_settings.setValue("updates/keepUpToDate", enabled);
    emit keepUpToDateChanged();
}

QString SettingsManager::getValue(const QString &key, const QString &defaultValue) const
{
    return m_settings.value(key, defaultValue).toString();
}

void SettingsManager::setValue(const QString &key, const QString &value)
{
    m_settings.setValue(key, value);
}
