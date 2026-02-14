package zed.rainxch.settings.presentation

import zed.rainxch.core.domain.model.AppTheme
import zed.rainxch.core.domain.model.FontTheme

sealed interface SettingsAction {
    data object OnNavigateBack : SettingsAction
    data class OnThemeColorSelected(val theme: AppTheme) : SettingsAction
    data class OnAmoledThemeSelected(val isAmoled: Boolean) : SettingsAction
    data class OnDarkThemeSelected(val isDark: Boolean?) : SettingsAction
    data class OnFontThemeSelected(val fontTheme: FontTheme) : SettingsAction
    data object OnHelpClick : SettingsAction
}
