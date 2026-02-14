package zed.rainxch.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import zed.rainxch.core.domain.repository.ThemesRepository
import zed.rainxch.core.domain.utils.BrowserHelper
import zed.rainxch.settings.domain.repository.SettingsRepository

class SettingsViewModel(
    private val browserHelper: BrowserHelper,
    private val themesRepository: ThemesRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(SettingsState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadCurrentTheme()
                loadVersionName()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = SettingsState()
        )

    private val _events = Channel<SettingsEvent>()
    val events = _events.receiveAsFlow()

    private fun loadVersionName() {
        viewModelScope.launch {
            _state.update { it.copy(
                version = settingsRepository.getVersionName()
            ) }
        }
    }

    private fun loadCurrentTheme() {
        viewModelScope.launch {
            themesRepository.getThemeColor().collect { theme ->
                _state.update {
                    it.copy(selectedColorTheme = theme)
                }
            }
        }

        viewModelScope.launch {
            themesRepository.getAmoledTheme().collect { isAmoled ->
                _state.update {
                    it.copy(isAmoledThemeEnabled = isAmoled)
                }
            }
        }

        viewModelScope.launch {
            themesRepository.getIsDarkTheme().collect { isDarkTheme ->
                _state.update {
                    it.copy(isDarkTheme = isDarkTheme)
                }
            }
        }

        viewModelScope.launch {
            themesRepository.getFontTheme().collect { fontTheme ->
                _state.update {
                    it.copy(selectedFontTheme = fontTheme)
                }
            }
        }
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.OnHelpClick -> {
                browserHelper.openUrl(
                    url = "https://github.com/rainxchzed/Github-Store/issues"
                )
            }

            is SettingsAction.OnThemeColorSelected -> {
                viewModelScope.launch {
                    themesRepository.setThemeColor(action.theme)
                }
            }

            is SettingsAction.OnAmoledThemeSelected -> {
                viewModelScope.launch {
                    themesRepository.setAmoledTheme(action.isAmoled)
                }
            }

            SettingsAction.OnNavigateBack -> {
                /* Handled in composable */
            }

            is SettingsAction.OnFontThemeSelected -> {
                viewModelScope.launch {
                    themesRepository.setFontTheme(action.fontTheme)
                }
            }

            is SettingsAction.OnDarkThemeSelected -> {
                viewModelScope.launch {
                    themesRepository.setDarkTheme(action.isDark)
                }
            }
        }
    }
}
