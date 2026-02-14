package zed.rainxch.core.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import zed.rainxch.core.data.local.db.entities.FavoriteRepoEntity

@Dao
interface FavoriteRepoDao {
    @Query("SELECT * FROM favorite_repos ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteRepoEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_repos WHERE componentId = :componentId)")
    fun isFavorite(componentId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_repos WHERE componentId = :componentId)")
    suspend fun isFavoriteSync(componentId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteRepoEntity)

    @Query("DELETE FROM favorite_repos WHERE componentId = :componentId")
    suspend fun deleteByComponentId(componentId: String)

    @Query("UPDATE favorite_repos SET isInstalled = :installed WHERE componentId = :componentId")
    suspend fun updateInstallStatus(componentId: String, installed: Boolean)
}
