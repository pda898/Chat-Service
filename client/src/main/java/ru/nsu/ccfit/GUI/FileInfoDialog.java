package ru.nsu.ccfit.GUI;

import ru.nsu.ccfit.Client;
import ru.nsu.ccfit.StoredFile;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class FileInfoDialog extends JDialog {
    private JFrame owner;

    public FileInfoDialog(JFrame owner) {
        super(owner, "File info", true);
        this.owner = owner;
    }

    public void run(int uid, List<StoredFile> fileInfo, String hostname, Client client) {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        for (StoredFile file : fileInfo) {
            this.add(new JLabel("Name: " + file.getOriginalName()));
            this.add(new JLabel("Desc: " + file.getDesc()));
            this.add(new JLabel("Size: " + file.getSize()));
            try {
                URI uri = new URI("http://" + hostname + "/" + uid + "/" + file.getName());
                JButton hyperlink = new JButton("<HTML><FONT color=\"#000099\"><U>" + uri.getPath() + "</U></FONT>\"</HTML>");
                hyperlink.addActionListener(e -> open(uri));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            JButton delete = new JButton("Delete this file");
            delete.addActionListener(e -> {
                if (client.deleteFile(file.getName())) {
                    JOptionPane.showMessageDialog(owner, "File deleted");
                } else {
                    JOptionPane.showMessageDialog(owner, "Either file already deleted or not exist at all");
                }
            });
            this.add(delete);
            this.add(Box.createRigidArea(new Dimension(5, 0)));
        }
    }

    private void open(URI uri) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(uri);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(owner, "Error while handling browser");
            }
        } else {
            JOptionPane.showMessageDialog(owner, "Opening links are not supported");
        }
    }

}
