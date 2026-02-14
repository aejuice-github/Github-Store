package zed.rainxch.core.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_repos")
data class FavoriteRepoEntity(
    @PrimaryKey val componentId: String,
    val name: String,
    val author: String,
    val icon: String = "",
    val description: String? = null,
    val category: String = "",
    val type: String = "plugin",
    val isInstalled: Boolean = false,
    val latestVersion: String? = null,
    val addedAt: Long = 0,
    val lastSyncedAt: Long = 0
)
