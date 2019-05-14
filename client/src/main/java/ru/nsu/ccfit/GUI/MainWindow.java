package ru.nsu.ccfit.GUI;

import ru.nsu.ccfit.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class MainWindow extends JFrame implements ActionListener {
    private JPanel buttonGroup = new JPanel();
    private WelcomePanel welcomePanel = new WelcomePanel();
    private ChatPanel chatPanel;
    private Client client = new Client();
    private Timer updateTimer = new Timer(500, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                chatPanel.updateChat(client.getMessages());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    });

    public static void main(String[] args) {
        new MainWindow().run();
    }

    public void run() {
        this.setTitle("Chat");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout(5, 5));
        this.setJMenuBar(new JMenuBar());
        this.add(welcomePanel);
        JButton button = new JButton("Register");
        button.setActionCommand("register");
        button.addActionListener(this);
        buttonGroup.add(button);
        button = new JButton("Login");
        button.setActionCommand("login");
        button.addActionListener(this);
        buttonGroup.add(button);
        this.add(buttonGroup, BorderLayout.AFTER_LAST_LINE);
        this.pack();
        this.setMinimumSize(this.getSize());
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private JMenuItem createMenuItem(String text, String command) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(this);
        item.setActionCommand(command);
        return item;
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.setLayout(new FlowLayout(FlowLayout.LEFT));
        bar.add(createMenuItem("Upload file", "upload"));
        bar.add(createMenuItem("File info", "info"));
        bar.add(createMenuItem("Disconnect", "quit"));
        return bar;
    }

    private void changeToChat() {
        this.remove(buttonGroup);
        this.remove(welcomePanel);
        this.setPreferredSize(new Dimension(500, 600));
        chatPanel = new ChatPanel(client);
        this.add(chatPanel);
        this.setJMenuBar(createMenuBar());
        this.pack();
        this.setMinimumSize(this.getSize());
        updateTimer.start();
        this.setLocationRelativeTo(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand());
        switch (e.getActionCommand()) {
            case "register":
                client.setServer(welcomePanel.getAddress());
                try {
                    if (client.register(welcomePanel.getLogin(), welcomePanel.getPassword())) {
                        JOptionPane.showMessageDialog(this, "Registration succeed");
                    } else {
                        JOptionPane.showMessageDialog(this, "That user is already exist");
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Cannot connect to the server", "Error", JOptionPane.ERROR_MESSAGE);
                }
                break;
            case "login":
                client.setServer(welcomePanel.getAddress());
                try {
                    if (client.login(welcomePanel.getLogin(), welcomePanel.getPassword())) {
                        changeToChat();
                    } else {
                        JOptionPane.showMessageDialog(this, "Incorrect login/password");
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Cannot connect to the server", "Error", JOptionPane.ERROR_MESSAGE);
                }
                break;
            case "upload":
                JFileChooser dialog = new JFileChooser();
                int result = dialog.showSaveDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = dialog.getSelectedFile();
                    String desc = JOptionPane.showInputDialog("File Description");
                    if (desc != null) {
                        try {
                            if (client.uploadFile(file, desc)) {
                                JOptionPane.showMessageDialog(this, "Success");
                            } else {
                                JOptionPane.showMessageDialog(this, "You reached file limits");
                            }
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(this, "Server error");
                        }
                    }
                }
                break;
            case "info":
                try {
                    new FileInfoDialog(this).run(client.getUID(), client.getFiles(), client.getHost(), client);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Server error");
                }
                break;
            case "quit":
                try {
                    client.disconnect();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "There was a error during disconnecting, session can linger.");
                }
                dispose();
        }
    }
}