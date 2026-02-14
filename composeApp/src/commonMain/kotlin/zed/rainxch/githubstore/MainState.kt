package zed.rainxch.githubstore

import zed.rainxch.core.domain.model.AppTheme
import zed.rainxch.core.domain.model.FontTheme

enum class AppMode {
    STORE,
    INSTALL
}

data class MainState(
    val currentColorTheme: AppTheme = AppTheme.OCEAN,
    val isAmoledTheme: Boolean = false,
    val isDarkTheme: Boolean? = null,
    val currentFontTheme: FontTheme = FontTheme.CUSTOM,
    val isDraggingOver: Boolean = false,
    val dragDropMessage: String? = null,
    val showBrowseMore: Boolean = false,
    val appMode: AppMode = AppMode.STORE,
)
