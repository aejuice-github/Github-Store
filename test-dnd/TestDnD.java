import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;

public class TestDnD {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Java DnD Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLocationRelativeTo(null);

            JLabel label = new JLabel("Drop a file here", SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 20));
            frame.getContentPane().add(label, BorderLayout.CENTER);

            DropTarget dt = new DropTarget(frame.getContentPane(), DnDConstants.ACTION_COPY,
                new DropTargetListener() {
                    public void dragEnter(DropTargetDragEvent e) {
                        System.out.println("dragEnter!");
                        e.acceptDrag(DnDConstants.ACTION_COPY);
                        label.setText("DRAGGING!");
                    }
                    public void dragOver(DropTargetDragEvent e) {
                        e.acceptDrag(DnDConstants.ACTION_COPY);
                    }
                    public void dropActionChanged(DropTargetDragEvent e) {}
                    public void dragExit(DropTargetEvent e) {
                        System.out.println("dragExit");
                        label.setText("Drop a file here");
                    }
                    public void drop(DropTargetDropEvent e) {
                        e.acceptDrop(DnDConstants.ACTION_COPY);
                        try {
                            java.util.List<?> files = (java.util.List<?>)
                                e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                            System.out.println("Dropped " + files.size() + " files!");
                            label.setText("Dropped " + files.size() + " files!");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        e.dropComplete(true);
                    }
                }, true);

            frame.setVisible(true);
            System.out.println("Ready - drag a file onto this window");
        });
    }
}
