package zed.rainxch.core.data.services

import zed.rainxch.core.domain.system.PackageMonitor
import java.io.File

class DesktopPackageMonitor : PackageMonitor {
    override suspend fun isComponentInstalled(componentId: String): Boolean {
        return false
    }

    override suspend fun getAllInstalledComponentIds(): Set<String> {
        return emptySet()
    }

    override suspend fun verifyInstallation(
        componentId: String,
        installPath: String,
        files: List<String>
    ): Boolean {
        val dir = File(installPath)
        if (!dir.exists()) return false
        return files.all { File(dir, it).exists() }
    }
}
