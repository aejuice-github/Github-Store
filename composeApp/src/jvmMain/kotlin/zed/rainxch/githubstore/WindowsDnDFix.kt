package zed.rainxch.githubstore

import java.io.File

/**
 * On Windows, OLE2 drag-and-drop (used by Explorer) is blocked by UIPI when
 * the app runs elevated (as admin). There is NO API to allow it through.
 * The only fix is to run the app as non-elevated.
 * This relaunches the process with normal privileges if running elevated.
 */
object WindowsDnDFix {

    fun relaunchIfElevated(): Boolean {
        if (!System.getProperty("os.name", "").lowercase().contains("win")) return false
        if (!isElevated()) return false

        println("WindowsDnDFix: running elevated, relaunching as normal user for DnD support...")

        val java = ProcessHandle.current().info().command().orElse("java")
        val classpath = System.getProperty("java.class.path")
        val mainClass = "zed.rainxch.githubstore.DesktopAppKt"

        // Write a temp batch file to avoid Windows command line length limit
        val batFile = File.createTempFile("aejuice-relaunch-", ".bat")
        batFile.writeText("@echo off\r\n\"$java\" -cp \"$classpath\" $mainClass\r\n")

        // Use explorer.exe to launch the batch file - explorer always runs at
        // medium integrity (non-elevated), so the launched process inherits that
        ProcessBuilder("explorer.exe", batFile.absolutePath)
            .start()

        return true
    }

    private fun isElevated(): Boolean {
        return try {
            val process = ProcessBuilder("net", "session")
                .redirectErrorStream(true)
                .start()
            process.inputStream.readAllBytes()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }
}
