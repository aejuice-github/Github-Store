package zed.rainxch.githubstore

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import zed.rainxch.githubstore.app.di.initKoin

fun main() = application {
    initKoin()

    Window(
        onCloseRequest = ::exitApplication,
        title = "AEJuice Component Manager"
    ) {
        App()
    }
}
