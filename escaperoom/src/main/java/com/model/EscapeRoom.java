package com.model;

public class EscapeRoom {
    private String name;
    private String description;
    private boolean isSolved;
    private int level;

    public EscapeRoom(String name, String description, int level)
    {
        this.name = name;
        this.description = description;
        this.level = level;
        this.isSolved = false;

    }

    public void enterRoom()
    {
        return;
    }

    public Puzzle getPuzzle()
    {
        return null;
    }
}
