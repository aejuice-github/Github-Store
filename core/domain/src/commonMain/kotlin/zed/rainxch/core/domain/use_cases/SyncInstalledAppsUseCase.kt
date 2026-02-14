package zed.rainxch.core.domain.use_cases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import zed.rainxch.core.domain.logging.GitHubStoreLogger
import zed.rainxch.core.domain.repository.InstalledAppsRepository
import zed.rainxch.core.domain.system.PackageMonitor

/**
 * Synchronize installed apps state with filesystem
 * Remove entries from DB for components whose files no longer exist
 */
class SyncInstalledAppsUseCase(
    private val packageMonitor: PackageMonitor,
    private val installedAppsRepository: InstalledAppsRepository,
    private val logger: GitHubStoreLogger
) {
    suspend operator fun invoke(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val installedIds = packageMonitor.getAllInstalledComponentIds()
            val appsInDb = installedAppsRepository.getAllInstalledApps().first()

            appsInDb.forEach { app ->
                if (!installedIds.contains(app.componentId)) {
                    try {
                        val filesExist = packageMonitor.verifyInstallation(
                            app.componentId,
                            app.installPath,
                            app.files
                        )
                        if (!filesExist) {
                            installedAppsRepository.delete(app.componentId)
                            logger.info("Removed missing component: ${app.componentId}")
                        }
                    } catch (e: Exception) {
                        logger.error("Failed to verify ${app.componentId}: ${e.message}")
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Sync failed: ${e.message}")
            Result.failure(e)
        }
    }
}
