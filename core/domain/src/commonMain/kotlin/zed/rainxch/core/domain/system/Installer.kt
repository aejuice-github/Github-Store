package zed.rainxch.core.domain.system

import zed.rainxch.core.domain.model.Component
import zed.rainxch.core.domain.model.SystemArchitecture

interface Installer {
    suspend fun install(archivePath: String, installPath: String, requiresAdmin: Boolean)
    suspend fun uninstall(installPath: String, files: List<String>, requiresAdmin: Boolean)
    suspend fun runHook(hookPath: String, requiresAdmin: Boolean)
    fun detectSystemArchitecture(): SystemArchitecture
}
