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

        if (requiresAdmin) {
            installWithElevation(archive, targetDir)
        } else {
            extractZip(archive, targetDir)
        }

        Logger.i { "Installed ${archive.name} to $installPath" }
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
        val tempDir = File(System.getProperty("java.io.tmpdir"), "aejuice-install-${System.currentTimeMillis()}")
        tempDir.mkdirs()

        extractZip(archive, tempDir)

        try {
            when (platform) {
                Platform.WINDOWS -> {
                    val script = File(tempDir, "install.bat")
                    script.writeText(buildString {
                        appendLine("@echo off")
                        appendLine("mkdir \"${targetDir.absolutePath}\" 2>nul")
                        appendLine("xcopy /s /y /q \"${tempDir.absolutePath}\\*\" \"${targetDir.absolutePath}\\\"")
                    })
                    val process = ProcessBuilder(
                        "powershell", "-Command",
                        "Start-Process", "cmd", "-ArgumentList", "'/c','${script.absolutePath}'",
                        "-Verb", "RunAs", "-Wait"
                    ).start()
                    process.waitFor()
                }
                Platform.MACOS -> {
                    val command = "mkdir -p '${targetDir.absolutePath}' && cp -R '${tempDir.absolutePath}/'* '${targetDir.absolutePath}/'"
                    val process = ProcessBuilder(
                        "osascript", "-e",
                        "do shell script \"$command\" with administrator privileges"
                    ).start()
                    process.waitFor()
                }
                Platform.LINUX -> {
                    val process = ProcessBuilder(
                        "pkexec", "sh", "-c",
                        "mkdir -p '${targetDir.absolutePath}' && cp -R '${tempDir.absolutePath}/'* '${targetDir.absolutePath}/'"
                    ).start()
                    process.waitFor()
                }
            }
        } finally {
            tempDir.deleteRecursively()
        }
    }

    private fun uninstallWithElevation(targetDir: File, files: List<String>) {
        val filesList = files.joinToString(" ") { "'${File(targetDir, it).absolutePath}'" }

        when (platform) {
            Platform.WINDOWS -> {
                val deleteCommands = files.joinToString(" & ") { "del /f \"${File(targetDir, it).absolutePath}\"" }
                val process = ProcessBuilder(
                    "powershell", "-Command",
                    "Start-Process", "cmd", "-ArgumentList", "'/c','$deleteCommands'",
                    "-Verb", "RunAs", "-Wait"
                ).start()
                process.waitFor()
            }
            Platform.MACOS -> {
                val command = "rm -f $filesList"
                val process = ProcessBuilder(
                    "osascript", "-e",
                    "do shell script \"$command\" with administrator privileges"
                ).start()
                process.waitFor()
            }
            Platform.LINUX -> {
                val process = ProcessBuilder(
                    "pkexec", "sh", "-c", "rm -f $filesList"
                ).start()
                process.waitFor()
            }
        }
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
