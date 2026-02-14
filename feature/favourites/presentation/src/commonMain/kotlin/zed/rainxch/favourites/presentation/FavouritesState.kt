package zed.rainxch.favourites.presentation

import zed.rainxch.favourites.presentation.model.FavouriteRepository

data class FavouritesState(
    val favourites: List<FavouriteRepository> = emptyList(),
    val isLoading: Boolean = false,
)
