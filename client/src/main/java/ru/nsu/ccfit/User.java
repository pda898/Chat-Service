package ru.nsu.ccfit;

public class User {
    private long id;
    private String username;

    public User(long id, String name) {
        this.id = id;
        this.username = name;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}
