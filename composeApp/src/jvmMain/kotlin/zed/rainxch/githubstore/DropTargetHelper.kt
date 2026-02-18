package zed.rainxch.githubstore

import zed.rainxch.core.data.services.DragDropInstaller
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetDropEvent
import java.awt.dnd.DropTargetEvent
import java.awt.dnd.DropTargetListener
import java.io.File
import javax.swing.JFrame
import javax.swing.JPanel

class DropTargetHelper(
    private val onDragEnter: () -> Unit,
    private val onDragExit: () -> Unit,
    private val onFilesDropped: (List<String>) -> Unit
) : DropTargetListener {

    override fun dragEnter(event: DropTargetDragEvent) {
        event.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE)
        onDragEnter()
        println("DropTargetHelper: dragEnter")
    }

    override fun dragOver(event: DropTargetDragEvent) {
        event.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE)
    }

    override fun dropActionChanged(event: DropTargetDragEvent) {
        event.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE)
    }

    override fun dragExit(event: DropTargetEvent) {
        onDragExit()
        println("DropTargetHelper: dragExit")
    }

    override fun drop(event: DropTargetDropEvent) {
        try {
            event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE)

            val transferable = event.transferable
            if (!transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                println("DropTargetHelper: no file list flavor")
                event.dropComplete(false)
                onDragExit()
                return
            }

            @Suppress("UNCHECKED_CAST")
            val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
            println("DropTargetHelper: dropped ${files.size} files: ${files.map { it.name }}")

            val supportedPaths = files
                .filter { it.isFile && DragDropInstaller.isSupportedFile(it.absolutePath) }
                .map { it.absolutePath }

            event.dropComplete(true)
            onDragExit()

            if (supportedPaths.isNotEmpty()) {
                onFilesDropped(supportedPaths)
            } else {
                println("DropTargetHelper: no supported files in drop")
            }
        } catch (e: Exception) {
            println("DropTargetHelper: drop error: ${e.message}")
            event.dropComplete(false)
            onDragExit()
        }
    }

    companion object {
        fun attachToWindow(
            window: java.awt.Window,
            onDragEnter: () -> Unit,
            onDragExit: () -> Unit,
            onFilesDropped: (List<String>) -> Unit
        ) {
            if (window !is JFrame) return

            val listener = DropTargetHelper(onDragEnter, onDragExit, onFilesDropped)

            // Use glass pane - it sits on top of all Swing/Compose layers
            val glassPane = JPanel()
            glassPane.isOpaque = false
            glassPane.layout = null
            window.glassPane = glassPane
            glassPane.isVisible = true

            glassPane.dropTarget = DropTarget(
                glassPane, DnDConstants.ACTION_COPY_OR_MOVE, listener, true
            )
            println("DropTargetHelper: attached via glass pane")
        }
    }
}
