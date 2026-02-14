package zed.rainxch.core.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "installed_apps")
data class InstalledAppEntity(
    @PrimaryKey val componentId: String,
    val name: String,
    val type: String = "plugin",
    val description: String? = null,
    val author: String = "",
    val category: String = "",
    val icon: String = "",
    val installedVersion: String,
    val latestVersion: String? = null,
    val isUpdateAvailable: Boolean = false,
    val installPath: String,
    val files: String = "",
    val sha256: String = "",
    val installedAt: Long = 0,
    val lastCheckedAt: Long = 0,
    val lastUpdatedAt: Long = 0,
    val runnable: Boolean = false,
    val runCommand: String? = null,
    val releaseNotes: String? = null,
    val isPendingInstall: Boolean = false
)
