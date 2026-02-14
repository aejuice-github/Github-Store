package zed.rainxch.favourites.presentation.model

data class FavouriteRepository(
    val componentId: String,
    val name: String,
    val description: String?,
    val author: String,
    val category: String,
    val icon: String,
    val addedAt: Long
)
