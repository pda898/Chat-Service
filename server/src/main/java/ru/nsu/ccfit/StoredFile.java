package ru.nsu.ccfit;

public class StoredFile {
    private String name;
    private String originalName;
    private int size;
    private String desc;

    public StoredFile(String name, String originalName, int size, String desc) {
        this.name = name;
        this.originalName = originalName;
        this.size = size;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public String getOriginalName() {
        return originalName;
    }

    public int getSize() {
        return size;
    }

    public String getDesc() {
        return desc;
    }
}
