package zed.rainxch.core.data.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import zed.rainxch.core.domain.logging.GitHubStoreLogger
import zed.rainxch.core.domain.model.InstalledApp
import zed.rainxch.core.domain.model.Platform
import zed.rainxch.core.domain.utils.AppLauncher
import java.io.File

class DesktopAppLauncher(
    private val logger: GitHubStoreLogger,
    private val platform: Platform
) : AppLauncher {

    override suspend fun launchApp(installedApp: InstalledApp): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (!installedApp.runnable) {
                    throw Exception("Component ${installedApp.name} is not runnable")
                }

                val runCommand = installedApp.runCommand
                if (!runCommand.isNullOrBlank()) {
                    ProcessBuilder("sh", "-c", runCommand).start()
                    logger.debug("Launched via runCommand: $runCommand")
                    return@runCatching
                }

                val installDir = File(installedApp.installPath)
                if (!installDir.exists()) {
                    throw Exception("Install path not found: ${installedApp.installPath}")
                }

                when (platform) {
                    Platform.WINDOWS -> launchWindows(installedApp, installDir)
                    Platform.MACOS -> launchMacOS(installedApp, installDir)
                    Platform.LINUX -> launchLinux(installedApp, installDir)
                }
            }.onFailure { error ->
                logger.error("Failed to launch ${installedApp.name}: ${error.message}")
            }
        }

    override suspend fun canLaunchApp(installedApp: InstalledApp): Boolean =
        withContext(Dispatchers.IO) {
            if (!installedApp.runnable) return@withContext false
            if (!installedApp.runCommand.isNullOrBlank()) return@withContext true
            File(installedApp.installPath).exists()
        }

    private fun launchWindows(app: InstalledApp, installDir: File) {
        val exeFile = installDir.walkTopDown()
            .maxDepth(3)
            .filter { it.extension.equals("exe", ignoreCase = true) }
            .filter { !it.name.contains("uninstall", ignoreCase = true) }
            .firstOrNull()

        if (exeFile != null) {
            ProcessBuilder("cmd", "/c", "start", "", exeFile.absolutePath).start()
            logger.debug("Launched: ${exeFile.absolutePath}")
        } else {
            throw Exception("No executable found in ${installDir.absolutePath}")
        }
    }

    private fun launchMacOS(app: InstalledApp, installDir: File) {
        val appBundle = installDir.listFiles()?.find { it.name.endsWith(".app") }
        if (appBundle != null) {
            ProcessBuilder("open", "-a", appBundle.absolutePath).start()
            logger.debug("Launched: ${appBundle.absolutePath}")
        } else {
            ProcessBuilder("open", installDir.absolutePath).start()
        }
    }

    private fun launchLinux(app: InstalledApp, installDir: File) {
        val executable = installDir.listFiles()?.find { it.canExecute() && !it.isDirectory }
        if (executable != null) {
            ProcessBuilder(executable.absolutePath).start()
            logger.debug("Launched: ${executable.absolutePath}")
        } else {
            throw Exception("No executable found in ${installDir.absolutePath}")
        }
    }
}
