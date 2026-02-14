package zed.rainxch.settings.presentation

import zed.rainxch.core.domain.model.AppTheme
import zed.rainxch.core.domain.model.FontTheme

data class SettingsState(
    val selectedColorTheme: AppTheme = AppTheme.OCEAN,
    val selectedFontTheme: FontTheme = FontTheme.CUSTOM,
    val isAmoledThemeEnabled: Boolean = false,
    val isDarkTheme: Boolean? = null,
    val version: String = "",
)
