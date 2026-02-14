package zed.rainxch.githubstore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    private val syncUseCase: SyncInstalledAppsUseCase,
    private val dragDropHandler: DragDropHandler? = null
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
        when (action) {
            MainAction.OnDragEnter -> {
                _state.update { it.copy(isDraggingOver = true) }
            }

            MainAction.OnDragExit -> {
                _state.update { it.copy(isDraggingOver = false) }
            }

            is MainAction.OnFilesDropped -> {
                handleFileDrop(action.filePaths)
            }

            MainAction.DismissDragDropMessage -> {
                _state.update { it.copy(dragDropMessage = null, showBrowseMore = false) }
            }

            is MainAction.SwitchMode -> {
                _state.update { it.copy(appMode = action.mode) }
            }
        }
    }

    private fun handleFileDrop(filePaths: List<String>) {
        val handler = dragDropHandler ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val results = handler.installFiles(filePaths)
            val successCount = results.count { it.success }
            val failCount = results.size - successCount

            val message = buildString {
                if (successCount > 0) {
                    append("Installed $successCount file${if (successCount > 1) "s" else ""}")
                }
                if (failCount > 0) {
                    if (successCount > 0) append(", ")
                    append("$failCount failed")
                }
            }

            _state.update {
                it.copy(
                    dragDropMessage = message,
                    showBrowseMore = successCount > 0
                )
            }

            delay(10_000)
            _state.update { it.copy(dragDropMessage = null, showBrowseMore = false) }
        }
    }
}

interface DragDropHandler {
    suspend fun installFiles(filePaths: List<String>): List<DragDropResult>
}

data class DragDropResult(
    val fileName: String,
    val type: String,
    val success: Boolean,
    val error: String? = null
)
