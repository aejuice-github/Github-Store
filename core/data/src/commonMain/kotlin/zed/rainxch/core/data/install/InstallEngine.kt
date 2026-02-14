package zed.rainxch.core.data.install

import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import zed.rainxch.core.data.local.db.dao.InstalledAppDao
import zed.rainxch.core.data.local.db.dao.UpdateHistoryDao
import zed.rainxch.core.data.local.db.entities.UpdateHistoryEntity
import zed.rainxch.core.domain.model.Component
import zed.rainxch.core.domain.model.ComponentType
import zed.rainxch.core.domain.model.InstalledApp
import zed.rainxch.core.domain.model.Platform
import zed.rainxch.core.domain.network.Downloader
import zed.rainxch.core.domain.repository.ComponentRepository
import zed.rainxch.core.domain.repository.InstalledAppsRepository
import zed.rainxch.core.domain.system.Installer
import java.io.File
import java.security.MessageDigest

class InstallEngine(
    private val platform: Platform,
    private val installer: Installer,
    private val downloader: Downloader,
    private val componentRepository: ComponentRepository,
    private val installedAppsRepository: InstalledAppsRepository,
    private val installedAppsDao: InstalledAppDao,
    private val historyDao: UpdateHistoryDao
) {
    suspend fun install(component: Component): Flow<InstallProgress> = flow {
        val asset = component.platforms[platform.manifestKey]
        if (asset == null) {
            emit(InstallProgress.Failed("No ${platform.manifestKey} build available"))
            return@flow
        }

        try {
            val installFileName = asset.fileName.ifBlank {
                asset.url.substringAfterLast('/').let { java.net.URLDecoder.decode(it, "UTF-8") }
            }
            val installPaths = resolveInstallPaths(component, asset)
            val isAeRunning = installer.isProcessRunning("AfterFX.exe")

            if (component.type == ComponentType.PLUGIN && isAeRunning) {
                val existingFile = installPaths.firstOrNull { path ->
                    File(File(path), installFileName).exists()
                }
                if (existingFile != null) {
                    emit(InstallProgress.Failed("Close After Effects before updating plugins"))
                    return@flow
                }
            }

            val alreadyInstalled = installPaths.all { path ->
                val target = File(File(path), installFileName)
                target.exists() && target.length() > 0
            }
            if (alreadyInstalled && installPaths.isNotEmpty()) {
                saveInstalledRecord(component, asset, installPaths.first(), installFileName)
                val message = buildPostInstallMessage(component, isAeRunning)
                emit(InstallProgress.CompletedWithMessage("Already installed. $message"))
                return@flow
            }

            emit(InstallProgress.Downloading(0))

            val extension = asset.url.substringAfterLast('.').substringBefore('?')
            val downloadFileName = "${component.id}-${component.version}.$extension"
            downloader.download(asset.url, downloadFileName).collect { progress ->
                emit(InstallProgress.Downloading(progress.percent ?: 0))
            }

            val filePath = downloader.getDownloadedFilePath(downloadFileName)
            if (filePath == null) {
                emit(InstallProgress.Failed("Download failed"))
                return@flow
            }

            emit(InstallProgress.Verifying)
            if (asset.sha256.isNotBlank()) {
                val actualHash = computeSha256(filePath)
                if (!actualHash.equals(asset.sha256, ignoreCase = true)) {
                    emit(InstallProgress.Failed("Hash verification failed"))
                    File(filePath).delete()
                    return@flow
                }
            }

            val downloadedFile = File(filePath)
            val renamedFile = File(downloadedFile.parent, installFileName)
            if (downloadedFile.name != installFileName) {
                downloadedFile.renameTo(renamedFile)
            }
            val sourceFile = if (renamedFile.exists()) renamedFile else downloadedFile

            emit(InstallProgress.Installing)
            installer.installToMultiplePaths(sourceFile.absolutePath, installPaths, asset.requiresAdmin)

            val postInstallHook = component.hooks?.postInstall
            if (postInstallHook != null) {
                emit(InstallProgress.RunningHook("postInstall"))
                installer.runHook(postInstallHook, asset.requiresAdmin)
            }

            saveInstalledRecord(component, asset, installPaths.first(), installFileName)

            val message = buildPostInstallMessage(component, isAeRunning)
            emit(InstallProgress.CompletedWithMessage(message))

        } catch (e: Exception) {
            Logger.e { "Install failed for ${component.id}: ${e.message}" }
            emit(InstallProgress.Failed(e.message ?: "Unknown error"))
        }
    }

    private suspend fun saveInstalledRecord(
        component: Component,
        asset: zed.rainxch.core.domain.model.PlatformAsset,
        installPath: String,
        fileName: String
    ) {
        val installedApp = InstalledApp(
            componentId = component.id,
            name = component.name,
            type = component.type,
            description = component.description,
            author = component.author,
            category = component.category,
            icon = component.icon,
            installedVersion = component.version,
            installPath = installPath,
            files = listOf(fileName),
            sha256 = asset.sha256,
            installedAt = System.currentTimeMillis(),
            lastCheckedAt = System.currentTimeMillis(),
            lastUpdatedAt = System.currentTimeMillis(),
            runnable = component.runnable,
            runCommand = component.runCommand
        )
        installedAppsRepository.save(installedApp)
    }

    private fun resolveInstallPaths(
        component: Component,
        asset: zed.rainxch.core.domain.model.PlatformAsset
    ): List<String> {
        if (asset.installPath == "scriptui") {
            val paths = installer.findScriptUIPanelsPaths()
            if (paths.isNotEmpty()) return paths
        }

        val path = asset.installPath.ifBlank {
            getDefaultInstallPath(component, platform)
        }
        return listOf(path)
    }

    private fun buildPostInstallMessage(component: Component, isAeRunning: Boolean): String {
        val menuPath = when (component.type) {
            ComponentType.PLUGIN -> "Effect > AEJuice > ${component.name}"
            ComponentType.SCRIPT -> "Window > AEJuice ${component.name}"
            ComponentType.EXTENSION -> "Window > Extensions > AEJuice ${component.name}"
            ComponentType.SOFTWARE -> component.name
        }

        val restartNote = if (isAeRunning && component.type != ComponentType.SOFTWARE) {
            "\nRestart After Effects to see changes."
        } else ""

        return "${component.name} installed.\nFind it: $menuPath$restartNote"
    }

    suspend fun uninstall(componentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val installed = installedAppsRepository.getByComponentId(componentId)
                ?: return@withContext Result.failure(Exception("Component not installed"))

            val component = componentRepository.getComponentById(componentId)
            val preRemoveHook = component?.hooks?.preRemove
            if (preRemoveHook != null) {
                val compAsset = component.platforms[platform.manifestKey]
                installer.runHook(preRemoveHook, compAsset?.requiresAdmin ?: false)
            }

            val asset = component?.platforms?.get(platform.manifestKey)
            val requiresAdmin = asset?.requiresAdmin ?: false

            val uninstallPaths = if (component != null && asset != null) {
                resolveInstallPaths(component, asset)
            } else {
                listOf(installed.installPath)
            }

            installer.uninstallFromMultiplePaths(uninstallPaths, installed.files, requiresAdmin)

            installedAppsRepository.delete(componentId)
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e { "Uninstall failed for $componentId: ${e.message}" }
            Result.failure(e)
        }
    }

    suspend fun update(component: Component): Flow<InstallProgress> = flow {
        val existing = installedAppsRepository.getByComponentId(component.id)
        if (existing != null) {
            val fromVersion = existing.installedVersion

            install(component).collect { progress ->
                emit(progress)
                if (progress is InstallProgress.Completed) {
                    historyDao.insert(
                        UpdateHistoryEntity(
                            componentId = component.id,
                            name = component.name,
                            fromVersion = fromVersion,
                            toVersion = component.version,
                            updatedAt = System.currentTimeMillis(),
                            success = true
                        )
                    )
                }
            }
        } else {
            install(component).collect { emit(it) }
        }
    }

    suspend fun checkForUpdates() {
        val manifest = componentRepository.refreshManifest()
        val allInstalled = installedAppsDao.getAllInstalledApps()

        allInstalled.collect { apps ->
            apps.forEach { app ->
                val latest = manifest.components.find { it.id == app.componentId }
                if (latest != null) {
                    val isNewer = compareVersions(latest.version, app.installedVersion) > 0
                    installedAppsDao.updateVersionInfo(
                        componentId = app.componentId,
                        available = isNewer,
                        version = latest.version,
                        releaseNotes = latest.changelog,
                        timestamp = System.currentTimeMillis()
                    )
                }
            }
            return@collect
        }
    }

    private fun compareVersions(a: String, b: String): Int {
        val partsA = a.removePrefix("v").split(".")
        val partsB = b.removePrefix("v").split(".")
        val maxLength = maxOf(partsA.size, partsB.size)
        for (i in 0 until maxLength) {
            val numA = partsA.getOrNull(i)?.toIntOrNull() ?: 0
            val numB = partsB.getOrNull(i)?.toIntOrNull() ?: 0
            if (numA != numB) return numA.compareTo(numB)
        }
        return 0
    }

    private fun computeSha256(filePath: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        File(filePath).inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun getDefaultInstallPath(component: Component, platform: Platform): String {
        return when (platform) {
            Platform.WINDOWS -> "C:/Program Files/Common Files/AEJuice/${component.id}"
            Platform.MACOS -> "/Library/Application Support/AEJuice/${component.id}"
            Platform.LINUX -> "/opt/aejuice/${component.id}"
        }
    }

    private fun listInstalledFiles(installPath: String): List<String> {
        val dir = File(installPath)
        if (!dir.exists()) return emptyList()
        return dir.listFiles()?.map { it.name } ?: emptyList()
    }
}

sealed class InstallProgress {
    data class Downloading(val percent: Int) : InstallProgress()
    data object Verifying : InstallProgress()
    data object Installing : InstallProgress()
    data class RunningHook(val hookName: String) : InstallProgress()
    data object Completed : InstallProgress()
    data class CompletedWithMessage(val message: String) : InstallProgress()
    data class Failed(val message: String) : InstallProgress()
}
