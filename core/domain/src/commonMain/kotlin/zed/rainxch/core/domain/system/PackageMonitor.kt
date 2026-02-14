package zed.rainxch.core.domain.system

interface PackageMonitor {
    suspend fun isComponentInstalled(componentId: String): Boolean
    suspend fun getAllInstalledComponentIds(): Set<String>
    suspend fun verifyInstallation(componentId: String, installPath: String, files: List<String>): Boolean
}
