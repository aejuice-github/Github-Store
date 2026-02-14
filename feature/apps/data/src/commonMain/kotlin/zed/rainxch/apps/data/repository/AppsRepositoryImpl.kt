package zed.rainxch.apps.data.repository

import kotlinx.coroutines.flow.Flow
import zed.rainxch.apps.domain.repository.AppsRepository
import zed.rainxch.core.domain.logging.GitHubStoreLogger
import zed.rainxch.core.domain.model.InstalledApp
import zed.rainxch.core.domain.repository.InstalledAppsRepository
import zed.rainxch.core.domain.utils.AppLauncher

class AppsRepositoryImpl(
    private val appLauncher: AppLauncher,
    private val appsRepository: InstalledAppsRepository,
    private val logger: GitHubStoreLogger,
) : AppsRepository {
    override suspend fun getApps(): Flow<List<InstalledApp>> {
        return appsRepository.getAllInstalledApps()
    }

    override suspend fun openApp(
        installedApp: InstalledApp,
        onCantLaunchApp: () -> Unit
    ) {
        val canLaunch = appLauncher.canLaunchApp(installedApp)

        if (canLaunch) {
            appLauncher.launchApp(installedApp)
                .onFailure { error ->
                    logger.error("Failed to launch app: ${error.message}")
                    onCantLaunchApp()
                }
        } else {
            onCantLaunchApp()
        }
    }
}
