package zed.rainxch.core.domain.model

data class InstalledApp(
    val componentId: String,
    val name: String,
    val type: ComponentType = ComponentType.PLUGIN,
    val description: String? = null,
    val author: String = "",
    val category: String = "",
    val icon: String = "",
    val installedVersion: String,
    val latestVersion: String? = null,
    val isUpdateAvailable: Boolean = false,
    val installPath: String,
    val files: List<String> = emptyList(),
    val sha256: String = "",
    val installedAt: Long = 0,
    val lastCheckedAt: Long = 0,
    val lastUpdatedAt: Long = 0,
    val runnable: Boolean = false,
    val runCommand: String? = null,
    val releaseNotes: String? = null,
    val isPendingInstall: Boolean = false
)
