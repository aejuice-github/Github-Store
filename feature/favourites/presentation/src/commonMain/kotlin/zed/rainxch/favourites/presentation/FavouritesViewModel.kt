package zed.rainxch.favourites.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import zed.rainxch.core.domain.repository.FavouritesRepository
import zed.rainxch.favourites.presentation.mappers.toFavouriteRepositoryUi

class FavouritesViewModel(
    private val favouritesRepository: FavouritesRepository
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(FavouritesState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadFavourites()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = FavouritesState()
        )

    private fun loadFavourites() {
        viewModelScope.launch {
            favouritesRepository
                .getAllFavorites()
                .map { list -> list.map { it.toFavouriteRepositoryUi() } }
                .flowOn(Dispatchers.Default)
                .collect { favourites ->
                    _state.update { it.copy(
                        favourites = favourites
                    ) }
                }
        }
    }

    fun onAction(action: FavouritesAction) {
        when (action) {
            FavouritesAction.OnNavigateBack -> {
                // Handled in composable
            }

            is FavouritesAction.OnComponentClick -> {
                // Handled in composable
            }

            is FavouritesAction.OnToggleFavourite -> {
                viewModelScope.launch {
                    val componentId = action.componentId
                    val isFavourite = favouritesRepository.isFavoriteSync(componentId)
                    if (isFavourite) {
                        favouritesRepository.toggleFavorite(
                            zed.rainxch.core.domain.model.FavoriteRepo(
                                componentId = componentId,
                                name = "",
                                author = ""
                            )
                        )
                    }
                }
            }
        }
    }
}
