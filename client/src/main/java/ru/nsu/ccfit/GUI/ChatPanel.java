package ru.nsu.ccfit.GUI;

import ru.nsu.ccfit.Client;
import ru.nsu.ccfit.Message;
import ru.nsu.ccfit.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

public class ChatPanel extends JPanel implements ActionListener {
    private JTextArea chat = new JTextArea();
    private JTextField message = new JTextField();
    private JButton send = new JButton("Send");

    private Client client;

    public ChatPanel(Client client) {
        super(true);
        this.client = client;
        this.setLayout(new BorderLayout());
        this.add(chat);
        chat.setLineWrap(true);
        chat.setWrapStyleWord(true);
        chat.setEditable(false);
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(500, 40));
        panel.add(message);
        message.addActionListener(this);
        message.setPreferredSize(new Dimension(400, 30));
        send.addActionListener(this);
        panel.add(send);
        this.add(panel, BorderLayout.AFTER_LAST_LINE);
    }

    public void updateChat(List<Message> list) {
        for (Message value : list) {
            chat.append(value.getAuthorName() + ":" + value.getMessage() + "\n");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!message.getText().equals("")) {
            try {
                if (message.getText().equals("/list")) {
                    List<User> users = client.getUsers();
                    chat.append("Users online:\n");
                    for (User user : users) {
                        chat.append(user.getUsername() + "\n");
                    }
                } else {
                    client.sendMessage(message.getText());
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Server Error");
            }
        }
        message.setText("");
    }
}
