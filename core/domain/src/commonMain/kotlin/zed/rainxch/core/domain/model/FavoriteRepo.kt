package zed.rainxch.core.domain.model

data class FavoriteRepo(
    val componentId: String,
    val name: String,
    val author: String,
    val icon: String = "",
    val description: String? = null,
    val category: String = "",
    val type: ComponentType = ComponentType.PLUGIN,
    val isInstalled: Boolean = false,
    val latestVersion: String? = null,
    val addedAt: Long = 0,
    val lastSyncedAt: Long = 0
)
