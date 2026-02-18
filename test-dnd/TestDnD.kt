import java.awt.BorderLayout
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetDropEvent
import java.awt.dnd.DropTargetEvent
import java.awt.dnd.DropTargetListener
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.SwingConstants

fun main() {
    val frame = JFrame("DnD Test")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.setSize(400, 300)

    val label = JLabel("Drop files here", SwingConstants.CENTER)
    frame.contentPane.add(label, BorderLayout.CENTER)

    val listener = object : DropTargetListener {
        override fun dragEnter(e: DropTargetDragEvent) {
            println("SWING: dragEnter")
            e.acceptDrag(DnDConstants.ACTION_COPY)
            label.text = "Dragging over..."
        }
        override fun dragOver(e: DropTargetDragEvent) {
            e.acceptDrag(DnDConstants.ACTION_COPY)
        }
        override fun dropActionChanged(e: DropTargetDragEvent) {}
        override fun dragExit(e: DropTargetEvent) {
            println("SWING: dragExit")
            label.text = "Drop files here"
        }
        override fun drop(e: DropTargetDropEvent) {
            e.acceptDrop(DnDConstants.ACTION_COPY)
            val t = e.transferable
            if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                val files = t.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
                println("SWING: dropped ${files.size} files")
                label.text = "Dropped ${files.size} files!"
            }
            e.dropComplete(true)
        }
    }

    frame.contentPane.dropTarget = DropTarget(frame.contentPane, DnDConstants.ACTION_COPY, listener, true)
    frame.isVisible = true
    println("Swing DnD test window ready")
}
