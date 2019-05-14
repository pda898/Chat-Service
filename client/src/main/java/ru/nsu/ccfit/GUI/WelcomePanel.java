package ru.nsu.ccfit.GUI;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class WelcomePanel extends JPanel {

    private JTextField address = new JTextField(), login = new JTextField();
    private JPasswordField password = new JPasswordField();

    public WelcomePanel() {
        super(true);
        this.setLayout(new GridLayout(3, 2, 10, 5));
        this.add(new Label("Chat address:port"));
        this.add(address);
        this.add(new Label("Login"));
        this.add(login);
        this.add(new Label("Password"));
        this.add(password);
    }

    public String getAddress() {
        return address.getText();
    }

    public String getLogin() {
        return login.getText();
    }

    public String getPassword() {
        return Arrays.toString(password.getPassword());
    }
}
