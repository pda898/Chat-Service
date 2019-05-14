package ru.nsu.ccfit;

public class User {
    private int id;
    private String username;
    private String signature;

    public User(int id, String name, String signature) {
        this.id = id;
        this.username = name;
        this.signature = signature;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getSignature() {
        return signature;
    }
}
