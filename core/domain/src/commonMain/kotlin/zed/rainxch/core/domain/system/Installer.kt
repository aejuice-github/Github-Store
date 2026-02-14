package zed.rainxch.core.domain.system

import zed.rainxch.core.domain.model.SystemArchitecture

interface Installer {
    suspend fun install(archivePath: String, installPath: String, requiresAdmin: Boolean)
    suspend fun installToMultiplePaths(archivePath: String, installPaths: List<String>, requiresAdmin: Boolean)
    suspend fun uninstall(installPath: String, files: List<String>, requiresAdmin: Boolean)
    suspend fun uninstallFromMultiplePaths(installPaths: List<String>, files: List<String>, requiresAdmin: Boolean)
    suspend fun runHook(hookPath: String, requiresAdmin: Boolean)
    fun detectSystemArchitecture(): SystemArchitecture
    fun isProcessRunning(processName: String): Boolean
    fun findScriptUIPanelsPaths(): List<String>
    fun fileExistsWithSize(path: String, expectedSize: Long): Boolean
}
