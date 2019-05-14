package ru.nsu.ccfit;

public class Message {
    private int id;
    private String message;
    private long authorID;
    private String authorName;

    public Message(int id, String message, long authorID, String author) {
        this.id = id;
        this.message = message;
        this.authorID = authorID;
        this.authorName = author;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public long getAuthorID() {
        return authorID;
    }

    public String getAuthorName() {
        return authorName;
    }
}
