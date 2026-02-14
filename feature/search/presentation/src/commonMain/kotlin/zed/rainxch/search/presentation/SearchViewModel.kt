package zed.rainxch.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import zed.rainxch.githubstore.core.presentation.res.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import zed.rainxch.core.domain.logging.GitHubStoreLogger
import zed.rainxch.core.domain.repository.FavouritesRepository
import zed.rainxch.core.domain.repository.InstalledAppsRepository
import zed.rainxch.core.presentation.model.DiscoveryRepository
import zed.rainxch.domain.repository.SearchRepository

class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val installedAppsRepository: InstalledAppsRepository,
    private val favouritesRepository: FavouritesRepository,
    private val logger: GitHubStoreLogger,
) : ViewModel() {

    private var hasLoadedInitialData = false
    private var currentSearchJob: Job? = null
    private var searchDebounceJob: Job? = null

    private val _state = MutableStateFlow(SearchState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeInstalledApps()
                observeFavouriteApps()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = SearchState()
        )

    private fun observeInstalledApps() {
        viewModelScope.launch {
            installedAppsRepository.getAllInstalledApps().collect { installedApps ->
                val installedMap = installedApps.associateBy { it.componentId }
                _state.update { current ->
                    current.copy(
                        results = current.results.map { item ->
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

    private fun observeFavouriteApps() {
        viewModelScope.launch {
            favouritesRepository.getAllFavorites().collect { favoriteRepos ->
                val favouriteMap = favoriteRepos.associateBy { it.componentId }
                _state.update { current ->
                    current.copy(
                        results = current.results.map { item ->
                            val favourite = favouriteMap[item.component.id]
                            item.copy(isFavourite = favourite != null)
                        }
                    )
                }
            }
        }
    }

    private fun performSearch() {
        val query = _state.value.query
        if (query.isBlank()) {
            _state.update {
                it.copy(
                    isLoading = false,
                    results = emptyList(),
                    errorMessage = null
                )
            }
            return
        }

        currentSearchJob?.cancel()
        currentSearchJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val installedMap = installedAppsRepository
                    .getAllInstalledApps()
                    .first()
                    .associateBy { it.componentId }
                val favouriteMap = favouritesRepository
                    .getAllFavorites()
                    .first()
                    .associateBy { it.componentId }

                searchRepository.searchComponents(query).collect { components ->
                    val results = components.map { component ->
                        val app = installedMap[component.id]
                        val favourite = favouriteMap[component.id]

                        DiscoveryRepository(
                            isInstalled = app != null,
                            isUpdateAvailable = app?.isUpdateAvailable ?: false,
                            isFavourite = favourite != null,
                            component = component
                        )
                    }

                    _state.update {
                        it.copy(
                            results = results,
                            isLoading = false,
                            errorMessage = if (results.isEmpty()) {
                                getString(Res.string.no_repositories_found)
                            } else null
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.error("Search failed: ${e.message}")
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: getString(Res.string.search_failed)
                    )
                }
            }
        }
    }

    fun onAction(action: SearchAction) {
        when (action) {
            is SearchAction.OnSearchChange -> {
                _state.update { it.copy(query = action.query) }

                searchDebounceJob?.cancel()

                if (action.query.isBlank()) {
                    _state.update {
                        it.copy(
                            results = emptyList(),
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                } else {
                    searchDebounceJob = viewModelScope.launch {
                        try {
                            delay(300)
                            performSearch()
                        } catch (_: CancellationException) {
                            logger.debug("Debounce cancelled (expected)")
                        }
                    }
                }
            }

            SearchAction.OnSearchImeClick -> {
                searchDebounceJob?.cancel()
                performSearch()
            }

            SearchAction.Retry -> {
                searchDebounceJob?.cancel()
                performSearch()
            }

            is SearchAction.OnComponentClick -> {
                /* Handled in composable */
            }

            SearchAction.OnNavigateBackClick -> {
                /* Handled in composable */
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentSearchJob?.cancel()
        searchDebounceJob?.cancel()
    }
}
