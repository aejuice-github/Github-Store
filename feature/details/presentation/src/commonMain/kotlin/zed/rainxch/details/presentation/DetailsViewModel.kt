package zed.rainxch.details.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import zed.rainxch.core.data.install.DependencyResolver
import zed.rainxch.core.data.install.DependencyResult
import zed.rainxch.core.data.install.InstallEngine
import zed.rainxch.core.data.install.InstallProgress
import zed.rainxch.core.domain.logging.GitHubStoreLogger
import zed.rainxch.core.domain.model.FavoriteRepo
import zed.rainxch.core.domain.repository.FavouritesRepository
import zed.rainxch.core.domain.repository.InstalledAppsRepository
import zed.rainxch.core.domain.utils.AppLauncher
import zed.rainxch.details.domain.repository.DetailsRepository

class DetailsViewModel(
    private val componentId: String,
    private val detailsRepository: DetailsRepository,
    private val favouritesRepository: FavouritesRepository,
    private val installedAppsRepository: InstalledAppsRepository,
    private val installEngine: InstallEngine,
    private val dependencyResolver: DependencyResolver,
    private val appLauncher: AppLauncher,
    private val logger: GitHubStoreLogger
) : ViewModel() {

    private var hasLoadedInitialData = false
    private var installJob: Job? = null

    private val _state = MutableStateFlow(DetailsState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadComponent()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DetailsState()
        )

    private val _events = Channel<DetailsEvent>()
    val events = _events.receiveAsFlow()

    private fun loadComponent() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, errorMessage = null)

                val component = detailsRepository.getComponentById(componentId)
                if (component == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Component not found"
                    )
                    return@launch
                }

                val isFavourite = try {
                    favouritesRepository.isFavoriteSync(componentId)
                } catch (t: Throwable) {
                    logger.error("Failed to check favourite status: ${t.message}")
                    false
                }

                val installedApp = try {
                    installedAppsRepository.getByComponentId(componentId)
                } catch (t: Throwable) {
                    logger.error("Failed to check installed status: ${t.message}")
                    null
                }

                val isUpdateAvailable = if (installedApp != null) {
                    installedApp.isUpdateAvailable ||
                        (installedApp.latestVersion != null && installedApp.latestVersion != installedApp.installedVersion)
                } else {
                    false
                }

                _state.value = _state.value.copy(
                    isLoading = false,
                    component = component,
                    isFavourite = isFavourite,
                    isInstalled = installedApp != null,
                    installedVersion = installedApp?.installedVersion,
                    isUpdateAvailable = isUpdateAvailable
                )

            } catch (t: Throwable) {
                logger.error("Failed to load component details: ${t.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = t.message ?: "Failed to load details"
                )
            }
        }
    }

    fun onAction(action: DetailsAction) {
        when (action) {
            DetailsAction.Retry -> {
                hasLoadedInitialData = false
                loadComponent()
            }

            DetailsAction.OnNavigateBack -> {
                viewModelScope.launch {
                    _events.send(DetailsEvent.NavigateBack)
                }
            }

            DetailsAction.OnInstall -> handleInstall()
            DetailsAction.OnUninstall -> handleUninstall()
            DetailsAction.OnUpdate -> handleUpdate()
            DetailsAction.OnToggleFavourite -> handleToggleFavourite()
            DetailsAction.OnRun -> handleRun()
        }
    }

    private fun handleInstall() {
        val component = _state.value.component ?: return

        installJob?.cancel()
        installJob = viewModelScope.launch {
            try {
                val depResult = dependencyResolver.resolve(component)
                when (depResult) {
                    is DependencyResult.Missing -> {
                        val names = depResult.dependencies.joinToString(", ") { it.id }
                        _events.send(DetailsEvent.ShowError("Missing dependencies: $names"))
                        return@launch
                    }
                    is DependencyResult.CircularDependency -> {
                        _events.send(DetailsEvent.ShowError("Circular dependency detected"))
                        return@launch
                    }
                    is DependencyResult.Satisfied -> { }
                }

                _state.value = _state.value.copy(isInstalling = true, installProgress = 0f)

                installEngine.install(component).collect { progress ->
                    when (progress) {
                        is InstallProgress.Downloading -> {
                            _state.value = _state.value.copy(
                                installProgress = progress.percent / 100f
                            )
                        }
                        is InstallProgress.Verifying -> {
                            _state.value = _state.value.copy(installProgress = null)
                        }
                        is InstallProgress.Installing -> {
                            _state.value = _state.value.copy(installProgress = null)
                        }
                        is InstallProgress.RunningHook -> { }
                        is InstallProgress.Completed -> {
                            _state.value = _state.value.copy(
                                isInstalling = false,
                                isInstalled = true,
                                installedVersion = component.version,
                                isUpdateAvailable = false,
                                installProgress = null
                            )
                            _events.send(DetailsEvent.ShowSuccess("${component.name} installed successfully"))
                        }
                        is InstallProgress.Failed -> {
                            _state.value = _state.value.copy(
                                isInstalling = false,
                                installProgress = null
                            )
                            _events.send(DetailsEvent.ShowError("Install failed: ${progress.message}"))
                        }
                    }
                }
            } catch (t: Throwable) {
                logger.error("Install failed: ${t.message}")
                _state.value = _state.value.copy(
                    isInstalling = false,
                    installProgress = null
                )
                _events.send(DetailsEvent.ShowError(t.message ?: "Install failed"))
            }
        }
    }

    private fun handleUninstall() {
        val component = _state.value.component ?: return

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isUninstalling = true)

                val result = installEngine.uninstall(component.id)

                result.fold(
                    onSuccess = {
                        _state.value = _state.value.copy(
                            isUninstalling = false,
                            isInstalled = false,
                            installedVersion = null,
                            isUpdateAvailable = false
                        )
                        _events.send(DetailsEvent.ShowSuccess("${component.name} uninstalled"))
                    },
                    onFailure = { error ->
                        _state.value = _state.value.copy(isUninstalling = false)
                        _events.send(DetailsEvent.ShowError("Uninstall failed: ${error.message}"))
                    }
                )
            } catch (t: Throwable) {
                logger.error("Uninstall failed: ${t.message}")
                _state.value = _state.value.copy(isUninstalling = false)
                _events.send(DetailsEvent.ShowError(t.message ?: "Uninstall failed"))
            }
        }
    }

    private fun handleUpdate() {
        val component = _state.value.component ?: return

        installJob?.cancel()
        installJob = viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isInstalling = true, installProgress = 0f)

                installEngine.update(component).collect { progress ->
                    when (progress) {
                        is InstallProgress.Downloading -> {
                            _state.value = _state.value.copy(
                                installProgress = progress.percent / 100f
                            )
                        }
                        is InstallProgress.Verifying -> {
                            _state.value = _state.value.copy(installProgress = null)
                        }
                        is InstallProgress.Installing -> {
                            _state.value = _state.value.copy(installProgress = null)
                        }
                        is InstallProgress.RunningHook -> { }
                        is InstallProgress.Completed -> {
                            _state.value = _state.value.copy(
                                isInstalling = false,
                                isInstalled = true,
                                installedVersion = component.version,
                                isUpdateAvailable = false,
                                installProgress = null
                            )
                            _events.send(DetailsEvent.ShowSuccess("${component.name} updated to ${component.version}"))
                        }
                        is InstallProgress.Failed -> {
                            _state.value = _state.value.copy(
                                isInstalling = false,
                                installProgress = null
                            )
                            _events.send(DetailsEvent.ShowError("Update failed: ${progress.message}"))
                        }
                    }
                }
            } catch (t: Throwable) {
                logger.error("Update failed: ${t.message}")
                _state.value = _state.value.copy(
                    isInstalling = false,
                    installProgress = null
                )
                _events.send(DetailsEvent.ShowError(t.message ?: "Update failed"))
            }
        }
    }

    private fun handleToggleFavourite() {
        val component = _state.value.component ?: return

        viewModelScope.launch {
            try {
                val favorite = FavoriteRepo(
                    componentId = component.id,
                    name = component.name,
                    author = component.author,
                    icon = component.icon,
                    description = component.description,
                    category = component.category,
                    type = component.type,
                    isInstalled = _state.value.isInstalled,
                    latestVersion = component.version,
                    addedAt = System.currentTimeMillis(),
                    lastSyncedAt = System.currentTimeMillis()
                )

                favouritesRepository.toggleFavorite(favorite)
                val newState = favouritesRepository.isFavoriteSync(component.id)
                _state.value = _state.value.copy(isFavourite = newState)

                val message = if (newState) "Added to favourites" else "Removed from favourites"
                _events.send(DetailsEvent.ShowSuccess(message))

            } catch (t: Throwable) {
                logger.error("Failed to toggle favourite: ${t.message}")
                _events.send(DetailsEvent.ShowError("Failed to update favourite"))
            }
        }
    }

    private fun handleRun() {
        viewModelScope.launch {
            try {
                val installed = installedAppsRepository.getByComponentId(componentId) ?: return@launch
                val result = appLauncher.launchApp(installed)
                result.fold(
                    onSuccess = { },
                    onFailure = { error ->
                        _events.send(DetailsEvent.ShowError("Failed to run: ${error.message}"))
                    }
                )
            } catch (t: Throwable) {
                logger.error("Run failed: ${t.message}")
                _events.send(DetailsEvent.ShowError(t.message ?: "Failed to run"))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        installJob?.cancel()
    }
}
