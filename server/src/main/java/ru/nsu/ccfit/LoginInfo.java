package ru.nsu.ccfit;

public class LoginInfo {

    private String token;
    private int id;

    public LoginInfo(String token, int id) {
        this.token = token;
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public int getId() {
        return id;
    }
}
