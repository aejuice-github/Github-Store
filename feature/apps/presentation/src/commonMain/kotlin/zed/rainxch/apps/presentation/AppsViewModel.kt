package zed.rainxch.apps.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import zed.rainxch.githubstore.core.presentation.res.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import zed.rainxch.apps.domain.repository.AppsRepository
import zed.rainxch.apps.presentation.model.AppItem
import zed.rainxch.apps.presentation.model.UpdateAllProgress
import zed.rainxch.apps.presentation.model.UpdateState
import zed.rainxch.core.data.install.InstallEngine
import zed.rainxch.core.data.install.InstallProgress
import zed.rainxch.core.domain.logging.GitHubStoreLogger
import zed.rainxch.core.domain.model.InstalledApp
import zed.rainxch.core.domain.repository.ComponentRepository
import zed.rainxch.core.domain.use_cases.SyncInstalledAppsUseCase

class AppsViewModel(
    private val appsRepository: AppsRepository,
    private val installEngine: InstallEngine,
    private val componentRepository: ComponentRepository,
    private val syncInstalledAppsUseCase: SyncInstalledAppsUseCase,
    private val logger: GitHubStoreLogger
) : ViewModel() {

    private var hasLoadedInitialData = false
    private val activeUpdates = mutableMapOf<String, Job>()
    private var updateAllJob: Job? = null

    private val _state = MutableStateFlow(AppsState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadApps()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AppsState()
        )

    private val _events = Channel<AppsEvent>()
    val events = _events.receiveAsFlow()

    private fun loadApps() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val syncResult = syncInstalledAppsUseCase()
                if (syncResult.isFailure) {
                    logger.error("Sync had issues but continuing: ${syncResult.exceptionOrNull()?.message}")
                }

                appsRepository.getApps().collect { apps ->
                    val appItems = apps.map { app ->
                        val existing = _state.value.apps.find {
                            it.installedApp.componentId == app.componentId
                        }
                        AppItem(
                            installedApp = app,
                            updateState = existing?.updateState ?: UpdateState.Idle,
                            downloadProgress = existing?.downloadProgress,
                            error = existing?.error
                        )
                    }.sortedBy { it.installedApp.isUpdateAvailable }

                    _state.update {
                        it.copy(
                            apps = appItems,
                            isLoading = false,
                            updateAllButtonEnabled = appItems.any { item ->
                                item.installedApp.isUpdateAvailable
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                logger.error("Failed to load apps: ${e.message}")
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    fun onAction(action: AppsAction) {
        when (action) {
            AppsAction.OnNavigateBackClick -> {
            }

            is AppsAction.OnSearchChange -> {
                _state.update { it.copy(searchQuery = action.query) }
            }

            is AppsAction.OnOpenApp -> {
                openApp(action.app)
            }

            is AppsAction.OnUpdateApp -> {
                updateSingleApp(action.app)
            }

            is AppsAction.OnUninstallApp -> {
                uninstallApp(action.app)
            }

            AppsAction.OnUpdateAll -> {
                updateAllApps()
            }

            AppsAction.OnCancelUpdateAll -> {
                cancelAllUpdates()
            }

            is AppsAction.OnNavigateToComponent -> {
                viewModelScope.launch {
                    _events.send(AppsEvent.NavigateToComponent(action.componentId))
                }
            }
        }
    }

    private fun openApp(app: InstalledApp) {
        viewModelScope.launch {
            try {
                appsRepository.openApp(
                    installedApp = app,
                    onCantLaunchApp = {
                        viewModelScope.launch {
                            _events.send(
                                AppsEvent.ShowError(
                                    getString(
                                        Res.string.cannot_launch,
                                        arrayOf(app.name)
                                    )
                                )
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                logger.error("Failed to open app: ${e.message}")
                _events.send(
                    AppsEvent.ShowError(
                        getString(
                            Res.string.failed_to_open,
                            arrayOf(app.name)
                        )
                    )
                )
            }
        }
    }

    private fun updateSingleApp(app: InstalledApp) {
        if (activeUpdates.containsKey(app.componentId)) {
            logger.debug("Update already in progress for ${app.componentId}")
            return
        }

        val job = viewModelScope.launch {
            try {
                updateAppState(app.componentId, UpdateState.CheckingUpdate)

                val component = componentRepository.getComponentById(app.componentId)
                if (component == null) {
                    throw IllegalStateException("Component not found: ${app.componentId}")
                }

                updateAppState(app.componentId, UpdateState.Downloading)

                installEngine.update(component).collect { progress ->
                    when (progress) {
                        is InstallProgress.Downloading -> {
                            updateAppState(app.componentId, UpdateState.Downloading)
                            updateAppProgress(app.componentId, progress.percent)
                        }

                        is InstallProgress.Verifying -> {
                            updateAppState(app.componentId, UpdateState.Installing)
                        }

                        is InstallProgress.Installing -> {
                            updateAppState(app.componentId, UpdateState.Installing)
                        }

                        is InstallProgress.RunningHook -> {
                            updateAppState(app.componentId, UpdateState.Installing)
                        }

                        is InstallProgress.Completed -> {
                            updateAppState(app.componentId, UpdateState.Success)
                            logger.debug("Successfully updated ${app.name} to latest version")
                        }

                        is InstallProgress.Failed -> {
                            throw IllegalStateException(progress.message)
                        }
                    }
                }

                delay(2000)
                updateAppState(app.componentId, UpdateState.Idle)

            } catch (e: CancellationException) {
                logger.debug("Update cancelled for ${app.componentId}")
                updateAppState(app.componentId, UpdateState.Idle)
                throw e
            } catch (e: Exception) {
                logger.error("Update failed for ${app.componentId}: ${e.message}")
                updateAppState(
                    app.componentId,
                    UpdateState.Error(e.message ?: "Update failed")
                )
                _events.send(
                    AppsEvent.ShowError(
                        getString(
                            Res.string.failed_to_update,
                            arrayOf(app.name, e.message ?: "")
                        )
                    )
                )
            } finally {
                activeUpdates.remove(app.componentId)
            }
        }

        activeUpdates[app.componentId] = job
    }

    private fun uninstallApp(app: InstalledApp) {
        viewModelScope.launch {
            try {
                updateAppState(app.componentId, UpdateState.Installing)

                val result = installEngine.uninstall(app.componentId)
                result.onSuccess {
                    _events.send(
                        AppsEvent.ShowSuccess(
                            getString(
                                Res.string.uninstalled_successfully,
                                arrayOf(app.name)
                            )
                        )
                    )
                }.onFailure { error ->
                    updateAppState(
                        app.componentId,
                        UpdateState.Error(error.message ?: "Uninstall failed")
                    )
                    _events.send(
                        AppsEvent.ShowError(
                            getString(
                                Res.string.failed_to_uninstall,
                                arrayOf(app.name, error.message ?: "")
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                logger.error("Uninstall failed for ${app.componentId}: ${e.message}")
                updateAppState(
                    app.componentId,
                    UpdateState.Error(e.message ?: "Uninstall failed")
                )
                _events.send(
                    AppsEvent.ShowError(
                        getString(
                            Res.string.failed_to_uninstall,
                            arrayOf(app.name, e.message ?: "")
                        )
                    )
                )
            }
        }
    }

    private fun updateAllApps() {
        if (_state.value.isUpdatingAll) {
            logger.error("Update all already in progress")
            return
        }

        updateAllJob = viewModelScope.launch {
            try {
                _state.update { it.copy(isUpdatingAll = true) }

                val appsToUpdate = _state.value.apps.filter {
                    it.installedApp.isUpdateAvailable &&
                            it.updateState !is UpdateState.Success
                }

                if (appsToUpdate.isEmpty()) {
                    _events.send(AppsEvent.ShowError(getString(Res.string.no_updates_available)))
                    return@launch
                }

                logger.debug("Starting update all for ${appsToUpdate.size} apps")

                appsToUpdate.forEachIndexed { index, appItem ->
                    if (!isActive) {
                        logger.debug("Update all cancelled")
                        return@launch
                    }

                    _state.update {
                        it.copy(
                            updateAllProgress = UpdateAllProgress(
                                current = index + 1,
                                total = appsToUpdate.size,
                                currentAppName = appItem.installedApp.name
                            )
                        )
                    }

                    logger.debug("Updating ${index + 1}/${appsToUpdate.size}: ${appItem.installedApp.name}")

                    updateSingleApp(appItem.installedApp)
                    activeUpdates[appItem.installedApp.componentId]?.join()

                    delay(1000)
                }

                logger.debug("Update all completed successfully")
                _events.send(AppsEvent.ShowSuccess(getString(Res.string.all_apps_updated_successfully)))

            } catch (e: CancellationException) {
                logger.debug("Update all cancelled")
            } catch (e: Exception) {
                logger.error("Update all failed: ${e.message}")
                _events.send(
                    AppsEvent.ShowError(
                        getString(
                            Res.string.update_all_failed,
                            arrayOf(e.message)
                        )
                    )
                )
            } finally {
                _state.update {
                    it.copy(
                        isUpdatingAll = false,
                        updateAllProgress = null
                    )
                }
                updateAllJob = null
            }
        }
    }

    private fun cancelAllUpdates() {
        updateAllJob?.cancel()
        updateAllJob = null

        activeUpdates.values.forEach { it.cancel() }
        activeUpdates.clear()

        _state.value.apps.forEach { appItem ->
            if (appItem.updateState != UpdateState.Idle &&
                appItem.updateState != UpdateState.Success
            ) {
                updateAppState(appItem.installedApp.componentId, UpdateState.Idle)
            }
        }

        _state.update {
            it.copy(
                isUpdatingAll = false,
                updateAllProgress = null
            )
        }
    }

    private fun updateAppState(componentId: String, state: UpdateState) {
        _state.update { currentState ->
            currentState.copy(
                apps = currentState.apps.map { appItem ->
                    if (appItem.installedApp.componentId == componentId) {
                        appItem.copy(
                            updateState = state,
                            downloadProgress = if (state is UpdateState.Downloading)
                                appItem.downloadProgress else null,
                            error = if (state is UpdateState.Error) state.message else null
                        )
                    } else {
                        appItem
                    }
                }
            )
        }
    }

    private fun updateAppProgress(componentId: String, progress: Int?) {
        _state.update { currentState ->
            currentState.copy(
                apps = currentState.apps.map { appItem ->
                    if (appItem.installedApp.componentId == componentId) {
                        appItem.copy(downloadProgress = progress)
                    } else {
                        appItem
                    }
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        updateAllJob?.cancel()
        activeUpdates.values.forEach { it.cancel() }
    }
}
