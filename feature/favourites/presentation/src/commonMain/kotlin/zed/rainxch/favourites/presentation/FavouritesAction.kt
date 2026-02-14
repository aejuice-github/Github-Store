package zed.rainxch.favourites.presentation

sealed interface FavouritesAction {
    data object OnNavigateBack : FavouritesAction
    data class OnToggleFavourite(val componentId: String) : FavouritesAction
    data class OnComponentClick(val componentId: String) : FavouritesAction
}
