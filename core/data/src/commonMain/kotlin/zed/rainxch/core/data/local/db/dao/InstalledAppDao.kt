package zed.rainxch.core.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import zed.rainxch.core.data.local.db.entities.InstalledAppEntity

@Dao
interface InstalledAppDao {
    @Query("SELECT * FROM installed_apps ORDER BY installedAt DESC")
    fun getAllInstalledApps(): Flow<List<InstalledAppEntity>>

    @Query("SELECT * FROM installed_apps WHERE isUpdateAvailable = 1 ORDER BY lastCheckedAt DESC")
    fun getAppsWithUpdates(): Flow<List<InstalledAppEntity>>

    @Query("SELECT * FROM installed_apps WHERE componentId = :componentId")
    suspend fun getByComponentId(componentId: String): InstalledAppEntity?

    @Query("SELECT COUNT(*) FROM installed_apps WHERE isUpdateAvailable = 1")
    fun getUpdateCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: InstalledAppEntity)

    @Update
    suspend fun update(app: InstalledAppEntity)

    @Query("DELETE FROM installed_apps WHERE componentId = :componentId")
    suspend fun deleteByComponentId(componentId: String)

    @Query("UPDATE installed_apps SET isPendingInstall = :isPending WHERE componentId = :componentId")
    suspend fun updatePendingStatus(componentId: String, isPending: Boolean)

    @Query("""
        UPDATE installed_apps
        SET isUpdateAvailable = :available,
            latestVersion = :version,
            releaseNotes = :releaseNotes,
            lastCheckedAt = :timestamp
        WHERE componentId = :componentId
    """)
    suspend fun updateVersionInfo(
        componentId: String,
        available: Boolean,
        version: String?,
        releaseNotes: String?,
        timestamp: Long
    )
}
