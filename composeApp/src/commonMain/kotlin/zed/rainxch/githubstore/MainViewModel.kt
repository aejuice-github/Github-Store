package zed.rainxch.githubstore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import zed.rainxch.core.domain.repository.InstalledAppsRepository
import zed.rainxch.core.domain.repository.ThemesRepository
import zed.rainxch.core.domain.use_cases.SyncInstalledAppsUseCase

class MainViewModel(
    private val themesRepository: ThemesRepository,
    private val installedAppsRepository: InstalledAppsRepository,
    private val syncUseCase: SyncInstalledAppsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            themesRepository
                .getThemeColor()
                .collect { theme ->
                    _state.update { it.copy(currentColorTheme = theme) }
                }
        }

        viewModelScope.launch {
            themesRepository
                .getAmoledTheme()
                .collect { isAmoled ->
                    _state.update { it.copy(isAmoledTheme = isAmoled) }
                }
        }

        viewModelScope.launch {
            themesRepository
                .getIsDarkTheme()
                .collect { isDarkTheme ->
                    _state.update { it.copy(isDarkTheme = isDarkTheme) }
                }
        }

        viewModelScope.launch {
            themesRepository
                .getFontTheme()
                .collect { fontTheme ->
                    _state.update { it.copy(currentFontTheme = fontTheme) }
                }
        }

        viewModelScope.launch(Dispatchers.IO) {
            syncUseCase()
        }
    }

    fun onAction(action: MainAction) {
        // Handle actions as needed
    }
}
