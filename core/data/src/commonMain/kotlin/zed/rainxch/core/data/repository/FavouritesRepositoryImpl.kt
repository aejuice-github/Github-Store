package zed.rainxch.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import zed.rainxch.core.data.local.db.dao.FavoriteRepoDao
import zed.rainxch.core.data.mappers.toDomain
import zed.rainxch.core.data.mappers.toEntity
import zed.rainxch.core.domain.model.FavoriteRepo
import zed.rainxch.core.domain.repository.FavouritesRepository

class FavouritesRepositoryImpl(
    private val favoriteRepoDao: FavoriteRepoDao
) : FavouritesRepository {

    override fun getAllFavorites(): Flow<List<FavoriteRepo>> {
        return favoriteRepoDao
            .getAllFavorites()
            .map { it.map { entity -> entity.toDomain() } }
    }

    override fun isFavorite(componentId: String): Flow<Boolean> {
        return favoriteRepoDao.isFavorite(componentId)
    }

    override suspend fun isFavoriteSync(componentId: String): Boolean {
        return favoriteRepoDao.isFavoriteSync(componentId)
    }

    override suspend fun toggleFavorite(favorite: FavoriteRepo) {
        if (favoriteRepoDao.isFavoriteSync(favorite.componentId)) {
            favoriteRepoDao.deleteByComponentId(favorite.componentId)
        } else {
            favoriteRepoDao.insert(favorite.toEntity())
        }
    }

    override suspend fun updateInstallStatus(componentId: String, installed: Boolean) {
        favoriteRepoDao.updateInstallStatus(componentId, installed)
    }
}
