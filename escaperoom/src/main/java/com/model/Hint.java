package com.model;

public class Hint {
    private int level;
    private String description;

    public Hint(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
