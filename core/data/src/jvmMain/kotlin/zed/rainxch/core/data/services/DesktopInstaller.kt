package zed.rainxch.core.data.services

import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import zed.rainxch.core.domain.model.Platform
import zed.rainxch.core.domain.model.SystemArchitecture
import zed.rainxch.core.domain.system.Installer
import java.io.File
import java.io.IOException
import java.util.zip.ZipFile

class DesktopInstaller(
    private val platform: Platform
) : Installer {

    private val systemArchitecture: SystemArchitecture by lazy {
        determineSystemArchitecture()
    }

    override suspend fun install(
        archivePath: String,
        installPath: String,
        requiresAdmin: Boolean
    ) = withContext(Dispatchers.IO) {
        val archive = File(archivePath)
        if (!archive.exists()) {
            throw IllegalStateException("Archive not found: $archivePath")
        }

        val targetDir = File(installPath)
        val isZip = archive.extension.equals("zip", ignoreCase = true)

        if (requiresAdmin) {
            if (isZip) {
                installWithElevation(archive, targetDir)
            } else {
                copyFileWithElevation(archive, targetDir)
            }
        } else {
            if (isZip) {
                extractZip(archive, targetDir)
            } else {
                copyFile(archive, targetDir)
            }
        }

        Logger.i { "Installed ${archive.name} to $installPath" }
    }

    override suspend fun installToMultiplePaths(
        archivePath: String,
        installPaths: List<String>,
        requiresAdmin: Boolean
    ) = withContext(Dispatchers.IO) {
        if (installPaths.isEmpty()) return@withContext
        if (installPaths.size == 1) {
            install(archivePath, installPaths.first(), requiresAdmin)
            return@withContext
        }

        val source = File(archivePath)
        if (!source.exists()) {
            throw IllegalStateException("File not found: $archivePath")
        }

        val isZip = source.extension.equals("zip", ignoreCase = true)

        if (!requiresAdmin) {
            for (path in installPaths) {
                val targetDir = File(path)
                if (isZip) extractZip(source, targetDir) else copyFile(source, targetDir)
            }
        } else {
            if (isZip) {
                batchInstallZipWithElevation(source, installPaths)
            } else {
                batchCopyWithElevation(source, installPaths)
            }
        }

        Logger.i { "Installed ${source.name} to ${installPaths.size} paths" }
    }

    override suspend fun uninstall(
        installPath: String,
        files: List<String>,
        requiresAdmin: Boolean
    ) = withContext(Dispatchers.IO) {
        val targetDir = File(installPath)

        if (requiresAdmin) {
            uninstallWithElevation(targetDir, files)
        } else {
            for (fileName in files) {
                val file = File(targetDir, fileName)
                if (file.exists()) {
                    file.delete()
                    Logger.d { "Deleted: ${file.absolutePath}" }
                }
            }
            if (targetDir.exists() && targetDir.listFiles()?.isEmpty() == true) {
                targetDir.delete()
            }
        }

        Logger.i { "Uninstalled from $installPath" }
    }

    override suspend fun uninstallFromMultiplePaths(
        installPaths: List<String>,
        files: List<String>,
        requiresAdmin: Boolean
    ) = withContext(Dispatchers.IO) {
        if (installPaths.isEmpty()) return@withContext
        if (installPaths.size == 1) {
            uninstall(installPaths.first(), files, requiresAdmin)
            return@withContext
        }

        if (!requiresAdmin) {
            for (path in installPaths) {
                val targetDir = File(path)
                for (fileName in files) {
                    val file = File(targetDir, fileName)
                    if (file.exists()) {
                        file.delete()
                        Logger.d { "Deleted: ${file.absolutePath}" }
                    }
                }
            }
        } else {
            when (platform) {
                Platform.WINDOWS -> {
                    val script = File(System.getProperty("java.io.tmpdir"), "aejuice-uninstall-${System.currentTimeMillis()}.bat")
                    try {
                        script.writeText(buildString {
                            appendLine("@echo off")
                            for (path in installPaths) {
                                for (fileName in files) {
                                    appendLine("del /f \"${File(File(path), fileName).absolutePath}\"")
                                }
                            }
                        })
                        runElevatedBat(script)
                    } finally {
                        script.delete()
                    }
                }
                Platform.MACOS -> {
                    val allFiles = installPaths.flatMap { path ->
                        files.map { "'${File(File(path), it).absolutePath}'" }
                    }.joinToString(" ")
                    val process = ProcessBuilder(
                        "osascript", "-e",
                        "do shell script \"rm -f $allFiles\" with administrator privileges"
                    ).start()
                    process.waitFor()
                }
                Platform.LINUX -> {
                    val allFiles = installPaths.flatMap { path ->
                        files.map { "'${File(File(path), it).absolutePath}'" }
                    }.joinToString(" ")
                    val process = ProcessBuilder(
                        "pkexec", "sh", "-c", "rm -f $allFiles"
                    ).start()
                    process.waitFor()
                }
            }
        }

        Logger.i { "Uninstalled from ${installPaths.size} paths" }
    }

    override suspend fun runHook(hookPath: String, requiresAdmin: Boolean) = withContext(Dispatchers.IO) {
        val hookScript = when (platform) {
            Platform.WINDOWS -> "$hookPath.bat"
            else -> "$hookPath.sh"
        }

        val file = File(hookScript)
        if (!file.exists()) {
            Logger.w { "Hook script not found: $hookScript" }
            return@withContext
        }

        try {
            val command = when (platform) {
                Platform.WINDOWS -> {
                    if (requiresAdmin) {
                        listOf("powershell", "-Command", "Start-Process", "cmd", "/c", hookScript, "-Verb", "RunAs")
                    } else {
                        listOf("cmd", "/c", hookScript)
                    }
                }
                Platform.MACOS -> {
                    if (requiresAdmin) {
                        listOf("osascript", "-e", "do shell script \"sh '${file.absolutePath}'\" with administrator privileges")
                    } else {
                        listOf("sh", file.absolutePath)
                    }
                }
                Platform.LINUX -> {
                    if (requiresAdmin) {
                        listOf("pkexec", "sh", file.absolutePath)
                    } else {
                        listOf("sh", file.absolutePath)
                    }
                }
            }

            val process = ProcessBuilder(command).start()
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                Logger.w { "Hook exited with code $exitCode: $hookScript" }
            }
        } catch (e: Exception) {
            Logger.e { "Failed to run hook $hookScript: ${e.message}" }
        }
    }

    override fun detectSystemArchitecture(): SystemArchitecture = systemArchitecture

    private fun copyFile(source: File, targetDir: File) {
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        val destFile = File(targetDir, source.name)
        source.copyTo(destFile, overwrite = true)
        Logger.d { "Copied ${source.name} to ${destFile.absolutePath}" }
    }

    private fun copyFileWithElevation(source: File, targetDir: File) {
        batchCopyWithElevation(source, listOf(targetDir.absolutePath))
    }

    private fun batchCopyWithElevation(source: File, targetPaths: List<String>) {
        when (platform) {
            Platform.WINDOWS -> {
                val script = File(System.getProperty("java.io.tmpdir"), "aejuice-copy-${System.currentTimeMillis()}.bat")
                try {
                    script.writeText(buildString {
                        appendLine("@echo off")
                        for (path in targetPaths) {
                            appendLine("mkdir \"$path\" 2>nul")
                            appendLine("copy /y \"${source.absolutePath}\" \"$path\\\"")
                        }
                    })
                    runElevatedBat(script)
                    for (path in targetPaths) {
                        val destFile = File(File(path), source.name)
                        if (!destFile.exists()) {
                            throw IOException("Elevated copy failed: ${destFile.absolutePath} not found after copy")
                        }
                    }
                } finally {
                    script.delete()
                }
            }
            Platform.MACOS -> {
                val commands = targetPaths.joinToString(" && ") { path ->
                    "mkdir -p '$path' && cp '${source.absolutePath}' '$path/'"
                }
                val process = ProcessBuilder(
                    "osascript", "-e",
                    "do shell script \"$commands\" with administrator privileges"
                ).start()
                process.waitFor()
            }
            Platform.LINUX -> {
                val commands = targetPaths.joinToString(" && ") { path ->
                    "mkdir -p '$path' && cp '${source.absolutePath}' '$path/'"
                }
                val process = ProcessBuilder(
                    "pkexec", "sh", "-c", commands
                ).start()
                process.waitFor()
            }
        }
    }

    private fun batchInstallZipWithElevation(archive: File, targetPaths: List<String>) {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "aejuice-install-${System.currentTimeMillis()}")
        tempDir.mkdirs()
        extractZip(archive, tempDir)

        try {
            when (platform) {
                Platform.WINDOWS -> {
                    val script = File(tempDir, "install.bat")
                    script.writeText(buildString {
                        appendLine("@echo off")
                        for (path in targetPaths) {
                            appendLine("mkdir \"$path\" 2>nul")
                            appendLine("xcopy /s /y /q \"${tempDir.absolutePath}\\*\" \"$path\\\"")
                        }
                    })
                    runElevatedBat(script)
                }
                Platform.MACOS -> {
                    val commands = targetPaths.joinToString(" && ") { path ->
                        "mkdir -p '$path' && cp -R '${tempDir.absolutePath}/'* '$path/'"
                    }
                    val process = ProcessBuilder(
                        "osascript", "-e",
                        "do shell script \"$commands\" with administrator privileges"
                    ).start()
                    process.waitFor()
                }
                Platform.LINUX -> {
                    val commands = targetPaths.joinToString(" && ") { path ->
                        "mkdir -p '$path' && cp -R '${tempDir.absolutePath}/'* '$path/'"
                    }
                    val process = ProcessBuilder(
                        "pkexec", "sh", "-c", commands
                    ).start()
                    process.waitFor()
                }
            }
        } finally {
            tempDir.deleteRecursively()
        }
    }

    private fun extractZip(archive: File, targetDir: File) {
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        ZipFile(archive).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                val outputFile = File(targetDir, entry.name)
                if (entry.isDirectory) {
                    outputFile.mkdirs()
                } else {
                    outputFile.parentFile?.mkdirs()
                    zip.getInputStream(entry).use { input ->
                        outputFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }
    }

    private fun installWithElevation(archive: File, targetDir: File) {
        batchInstallZipWithElevation(archive, listOf(targetDir.absolutePath))
    }

    private fun uninstallWithElevation(targetDir: File, files: List<String>) {
        when (platform) {
            Platform.WINDOWS -> {
                val script = File(System.getProperty("java.io.tmpdir"), "aejuice-uninstall-${System.currentTimeMillis()}.bat")
                try {
                    script.writeText(buildString {
                        appendLine("@echo off")
                        for (fileName in files) {
                            appendLine("del /f \"${File(targetDir, fileName).absolutePath}\"")
                        }
                    })
                    runElevatedBat(script)
                } finally {
                    script.delete()
                }
            }
            Platform.MACOS -> {
                val filesList = files.joinToString(" ") { "'${File(targetDir, it).absolutePath}'" }
                val process = ProcessBuilder(
                    "osascript", "-e",
                    "do shell script \"rm -f $filesList\" with administrator privileges"
                ).start()
                process.waitFor()
            }
            Platform.LINUX -> {
                val filesList = files.joinToString(" ") { "'${File(targetDir, it).absolutePath}'" }
                val process = ProcessBuilder(
                    "pkexec", "sh", "-c", "rm -f $filesList"
                ).start()
                process.waitFor()
            }
        }
    }

    private fun runElevatedBat(script: File) {
        val process = ProcessBuilder(
            "powershell", "-Command",
            "Start-Process", "cmd",
            "-ArgumentList", "'/c','\"${script.absolutePath}\"'",
            "-Verb", "RunAs", "-Wait", "-WindowStyle", "Hidden"
        ).start()
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            Logger.w { "Elevated script exited with code $exitCode: ${script.name}" }
        }
    }

    override fun isProcessRunning(processName: String): Boolean {
        return try {
            val command = when (platform) {
                Platform.WINDOWS -> listOf("tasklist", "/FI", "IMAGENAME eq $processName")
                else -> listOf("pgrep", "-x", processName)
            }
            val process = ProcessBuilder(command).start()
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            when (platform) {
                Platform.WINDOWS -> output.contains(processName, ignoreCase = true)
                else -> process.exitValue() == 0
            }
        } catch (e: Exception) {
            Logger.w { "Failed to check process $processName: ${e.message}" }
            false
        }
    }

    override fun findScriptUIPanelsPaths(): List<String> {
        val paths = mutableListOf<String>()
        when (platform) {
            Platform.WINDOWS -> {
                val adobeDir = File("C:/Program Files/Adobe")
                if (adobeDir.exists()) {
                    adobeDir.listFiles()?.filter {
                        it.isDirectory && it.name.startsWith("Adobe After Effects")
                    }?.forEach { aeFolder ->
                        val scriptsPath = File(aeFolder, "Support Files/Scripts/ScriptUI Panels")
                        if (scriptsPath.exists()) {
                            paths.add(scriptsPath.absolutePath)
                        }
                    }
                }
            }
            Platform.MACOS -> {
                val applicationsDir = File("/Applications")
                applicationsDir.listFiles()?.filter {
                    it.isDirectory && it.name.startsWith("Adobe After Effects")
                }?.forEach { aeFolder ->
                    val scriptsPath = File(aeFolder, "Scripts/ScriptUI Panels")
                    if (scriptsPath.exists()) {
                        paths.add(scriptsPath.absolutePath)
                    }
                }
            }
            Platform.LINUX -> { }
        }
        Logger.d { "Found ${paths.size} ScriptUI Panels paths: $paths" }
        return paths
    }

    override fun fileExistsWithSize(path: String, expectedSize: Long): Boolean {
        val file = File(path)
        if (!file.exists()) return false
        if (expectedSize <= 0) return true
        return file.length() == expectedSize
    }

    private fun determineSystemArchitecture(): SystemArchitecture {
        if (platform == Platform.MACOS) {
            try {
                val process = ProcessBuilder("uname", "-m").start()
                val output = process.inputStream.bufferedReader().readText().trim()
                process.waitFor()
                return when (output) {
                    "arm64" -> SystemArchitecture.AARCH64
                    "x86_64" -> SystemArchitecture.X86_64
                    else -> SystemArchitecture.fromString(System.getProperty("os.arch"))
                }
            } catch (_: Exception) { }
        }
        val osArch = System.getProperty("os.arch") ?: return SystemArchitecture.UNKNOWN
        return SystemArchitecture.fromString(osArch)
    }
}
