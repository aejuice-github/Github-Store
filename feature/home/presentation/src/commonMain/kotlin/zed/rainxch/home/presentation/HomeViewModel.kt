package zed.rainxch.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import zed.rainxch.core.domain.logging.GitHubStoreLogger
import zed.rainxch.core.domain.repository.FavouritesRepository
import zed.rainxch.core.domain.repository.InstalledAppsRepository
import zed.rainxch.core.domain.use_cases.SyncInstalledAppsUseCase
import zed.rainxch.core.presentation.model.DiscoveryRepository
import zed.rainxch.githubstore.core.presentation.res.*
import zed.rainxch.home.domain.model.HomeCategory
import zed.rainxch.home.domain.repository.HomeRepository

class HomeViewModel(
    private val homeRepository: HomeRepository,
    private val installedAppsRepository: InstalledAppsRepository,
    private val syncInstalledAppsUseCase: SyncInstalledAppsUseCase,
    private val favouritesRepository: FavouritesRepository,
    private val logger: GitHubStoreLogger
) : ViewModel() {

    private var hasLoadedInitialData = false
    private var currentJob: Job? = null
    private var switchCategoryJob: Job? = null

    private val _state = MutableStateFlow(HomeState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                syncSystemState()
                loadCategories()
                loadComponents(isInitial = true)
                observeInstalledApps()
                observeFavourites()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HomeState()
        )

    private val _events = Channel<HomeEvent>()
    val events = _events.receiveAsFlow()

    private fun syncSystemState() {
        viewModelScope.launch {
            try {
                val result = syncInstalledAppsUseCase()
                if (result.isFailure) {
                    logger.warn("Initial sync had issues: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                logger.error("Initial sync failed: ${e.message}")
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val categoryNames = homeRepository.getCategories()
                val categories = listOf(HomeCategory.ALL) + categoryNames.map { HomeCategory(name = it) }
                _state.update { it.copy(categories = categories) }
            } catch (e: Exception) {
                logger.error("Failed to load categories: ${e.message}")
            }
        }
    }

    private fun observeInstalledApps() {
        viewModelScope.launch {
            installedAppsRepository.getAllInstalledApps().collect { installedApps ->
                val installedMap = installedApps.associateBy { it.componentId }
                _state.update { current ->
                    current.copy(
                        components = current.components.map { item ->
                            val app = installedMap[item.component.id]
                            item.copy(
                                isInstalled = app != null,
                                isUpdateAvailable = app?.isUpdateAvailable ?: false
                            )
                        }
                    )
                }
            }
        }
    }

    private fun observeFavourites() {
        viewModelScope.launch {
            favouritesRepository.getAllFavorites().collect { favourites ->
                val favouritesMap = favourites.associateBy { it.componentId }
                _state.update { current ->
                    current.copy(
                        components = current.components.map { item ->
                            item.copy(
                                isFavourite = favouritesMap.containsKey(item.component.id)
                            )
                        }
                    )
                }
            }
        }
    }

    private fun loadComponents(isInitial: Boolean = false, category: HomeCategory? = null): Job? {
        currentJob?.cancel()

        if (_state.value.isLoading) {
            logger.debug("Already loading, skipping...")
            return null
        }

        val targetCategory = category ?: _state.value.currentCategory

        logger.debug("Loading components: category=${targetCategory.name}, isInitial=$isInitial")

        return viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = isInitial,
                    errorMessage = null,
                    currentCategory = targetCategory,
                    components = if (isInitial) emptyList() else it.components
                )
            }

            try {
                val componentsFlow = if (targetCategory == HomeCategory.ALL) {
                    homeRepository.getAllComponents()
                } else {
                    homeRepository.getComponentsByCategory(targetCategory.name)
                }

                componentsFlow.collect { components ->
                    logger.debug("Received ${components.size} components for ${targetCategory.name}")

                    val installedAppsMap = installedAppsRepository
                        .getAllInstalledApps()
                        .first()
                        .associateBy { it.componentId }

                    val favouritesMap = favouritesRepository
                        .getAllFavorites()
                        .first()
                        .associateBy { it.componentId }

                    val discoveryItems = components.map { component ->
                        val app = installedAppsMap[component.id]
                        val favourite = favouritesMap[component.id]

                        DiscoveryRepository(
                            isInstalled = app != null,
                            isFavourite = favourite != null,
                            isUpdateAvailable = app?.isUpdateAvailable ?: false,
                            component = component
                        )
                    }

                    _state.update { currentState ->
                        currentState.copy(
                            components = discoveryItems,
                            errorMessage = if (discoveryItems.isEmpty()) {
                                getString(Res.string.no_repositories_found)
                            } else null
                        )
                    }
                }

                logger.debug("Flow completed")
                _state.update {
                    it.copy(isLoading = false)
                }

            } catch (t: Throwable) {
                if (t is CancellationException) {
                    logger.debug("Load cancelled (expected)")
                    throw t
                }

                logger.error("Load failed: ${t.message}")
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = t.message
                            ?: getString(Res.string.home_failed_to_load_repositories)
                    )
                }
            }
        }.also {
            currentJob = it
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.Refresh -> {
                viewModelScope.launch {
                    syncInstalledAppsUseCase()
                    loadCategories()
                    loadComponents(isInitial = true)
                }
            }

            HomeAction.Retry -> {
                loadComponents(isInitial = true)
            }

            is HomeAction.SwitchCategory -> {
                if (_state.value.currentCategory != action.category) {
                    switchCategoryJob?.cancel()
                    switchCategoryJob = viewModelScope.launch {
                        loadComponents(isInitial = true, category = action.category)?.join()
                            ?: return@launch
                        _events.send(HomeEvent.OnScrollToListTop)
                    }
                }
            }

            is HomeAction.OnComponentClick -> {
                /* Handled in composable */
            }

            HomeAction.OnSearchClick -> {
                /* Handled in composable */
            }

            HomeAction.OnSettingsClick -> {
                /* Handled in composable */
            }

            HomeAction.OnAppsClick -> {
                /* Handled in composable */
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }
}
