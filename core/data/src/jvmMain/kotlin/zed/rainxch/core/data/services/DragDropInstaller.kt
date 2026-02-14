package zed.rainxch.core.data.services

import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import zed.rainxch.core.domain.system.Installer
import java.io.File

class DragDropInstaller(
    private val installer: Installer
) {
    data class InstallResult(
        val fileName: String,
        val type: String,
        val success: Boolean,
        val error: String? = null
    )

    suspend fun installFiles(filePaths: List<String>): List<InstallResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<InstallResult>()

        for (path in filePaths) {
            val file = File(path)
            if (!file.exists()) {
                results.add(InstallResult(file.name, "unknown", false, "File not found"))
                continue
            }

            val extension = file.extension.lowercase()
            val type = resolveType(extension)
            if (type == null) {
                results.add(InstallResult(file.name, extension, false, "Unsupported file type"))
                continue
            }

            try {
                val installPaths = resolveInstallPaths(type)
                if (installPaths.isEmpty()) {
                    results.add(InstallResult(file.name, type, false, "No install paths found"))
                    continue
                }

                installer.installToMultiplePaths(path, installPaths, true)
                results.add(InstallResult(file.name, type, true))
                Logger.i { "Drag-drop installed ${file.name} to ${installPaths.size} paths" }
            } catch (e: Exception) {
                Logger.e { "Drag-drop install failed for ${file.name}: ${e.message}" }
                results.add(InstallResult(file.name, type, false, e.message))
            }
        }

        results
    }

    private fun resolveType(extension: String): String? {
        return when (extension) {
            "jsx", "jsxbin" -> "script"
            "aex" -> "plugin"
            "ofx" -> "ofx-plugin"
            "zxp" -> "extension"
            else -> null
        }
    }

    private fun resolveInstallPaths(type: String): List<String> {
        return when (type) {
            "script" -> installer.findScriptUIPanelsPaths()
            "plugin" -> listOf("C:/Program Files/Adobe/Common/Plug-ins/7.0/MediaCore/")
            "ofx-plugin" -> listOf("C:/Program Files/Common Files/OFX/Plugins/")
            "extension" -> listOf("C:/Program Files/Common Files/Adobe/CEP/extensions/")
            else -> emptyList()
        }
    }

    companion object {
        val SUPPORTED_EXTENSIONS = setOf("jsx", "jsxbin", "aex", "ofx", "zxp")

        fun isSupportedFile(path: String): Boolean {
            val extension = File(path).extension.lowercase()
            return extension in SUPPORTED_EXTENSIONS
        }
    }
}
