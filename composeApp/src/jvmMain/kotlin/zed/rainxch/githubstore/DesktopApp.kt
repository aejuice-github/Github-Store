package zed.rainxch.githubstore

import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.module
import zed.rainxch.core.data.services.DragDropInstaller
import zed.rainxch.githubstore.app.di.initKoin
import java.awt.datatransfer.DataFlavor
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Relaunch as non-elevated if running as admin (required for DnD)
    if (WindowsDnDFix.relaunchIfElevated()) return

    application {
        initKoin {
            modules(
                module {
                    single<DragDropHandler> {
                        DesktopDragDropHandler(
                            dragDropInstaller = get<DragDropInstaller>()
                        )
                    }
                }
            )
        }

        Window(
            onCloseRequest = ::exitApplication,
            title = "AEJuice Component Manager"
        ) {
            val mainViewModel: MainViewModel = koinViewModel()

            val dragAndDropTarget = remember {
                object : DragAndDropTarget {
                    override fun onStarted(event: DragAndDropEvent) {
                        mainViewModel.onAction(MainAction.OnDragEnter)
                    }

                    override fun onEnded(event: DragAndDropEvent) {
                        mainViewModel.onAction(MainAction.OnDragExit)
                    }

                    override fun onDrop(event: DragAndDropEvent): Boolean {
                        mainViewModel.onAction(MainAction.OnDragExit)

                        val transferable = event.awtTransferable
                        if (!transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                            return false
                        }

                        @Suppress("UNCHECKED_CAST")
                        val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
                        val supportedPaths = files
                            .filter { it.isFile && DragDropInstaller.isSupportedFile(it.absolutePath) }
                            .map { it.absolutePath }

                        if (supportedPaths.isNotEmpty()) {
                            mainViewModel.onAction(MainAction.OnFilesDropped(supportedPaths))
                        }
                        return true
                    }
                }
            }

            val dragDropModifier = Modifier.dragAndDropTarget(
                shouldStartDragAndDrop = { true },
                target = dragAndDropTarget
            )

            val onBrowseFiles: () -> Unit = {
                val chooser = JFileChooser()
                chooser.isMultiSelectionEnabled = true
                chooser.fileFilter = FileNameExtensionFilter(
                    "Plugin files (jsx, jsxbin, aex, ofx, zxp)",
                    "jsx", "jsxbin", "aex", "ofx", "zxp"
                )
                val result = chooser.showOpenDialog(window)
                if (result == JFileChooser.APPROVE_OPTION) {
                    val paths = chooser.selectedFiles.map { it.absolutePath }
                    if (paths.isNotEmpty()) {
                        mainViewModel.onAction(MainAction.OnFilesDropped(paths))
                    }
                }
            }

            App(
                mainViewModel = mainViewModel,
                onBrowseFiles = onBrowseFiles,
                dragDropModifier = dragDropModifier
            )
        }
    }
}
