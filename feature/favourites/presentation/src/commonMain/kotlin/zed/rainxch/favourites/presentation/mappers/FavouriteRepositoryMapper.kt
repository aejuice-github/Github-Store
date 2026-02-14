package zed.rainxch.favourites.presentation.mappers

import zed.rainxch.core.domain.model.FavoriteRepo
import zed.rainxch.favourites.presentation.model.FavouriteRepository

fun FavoriteRepo.toFavouriteRepositoryUi(): FavouriteRepository {
    return FavouriteRepository(
        componentId = componentId,
        name = name,
        description = description,
        author = author,
        category = category,
        icon = icon,
        addedAt = addedAt
    )
}
