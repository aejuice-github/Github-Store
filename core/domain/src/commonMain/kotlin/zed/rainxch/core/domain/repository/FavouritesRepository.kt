package zed.rainxch.core.domain.repository

import kotlinx.coroutines.flow.Flow
import zed.rainxch.core.domain.model.FavoriteRepo

interface FavouritesRepository {
    fun getAllFavorites(): Flow<List<FavoriteRepo>>
    fun isFavorite(componentId: String): Flow<Boolean>
    suspend fun isFavoriteSync(componentId: String): Boolean
    suspend fun toggleFavorite(favorite: FavoriteRepo)
    suspend fun updateInstallStatus(componentId: String, installed: Boolean)
}
