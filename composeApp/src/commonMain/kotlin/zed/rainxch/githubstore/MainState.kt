package zed.rainxch.githubstore

import zed.rainxch.core.domain.model.AppTheme
import zed.rainxch.core.domain.model.FontTheme

data class MainState(
    val currentColorTheme: AppTheme = AppTheme.OCEAN,
    val isAmoledTheme: Boolean = false,
    val isDarkTheme: Boolean? = null,
    val currentFontTheme: FontTheme = FontTheme.CUSTOM,
)
