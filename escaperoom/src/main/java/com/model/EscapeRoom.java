package com.model;

import java.util.List;

public class EscapeRoom {
    private String name;
    private String description;
    private boolean isSolved;
    private int level;
    private List<Puzzle> puzzles;

    // UML: + EscapeRoom(name: String, description: String, level: int)
    public EscapeRoom(String name, String description, int level) {
        this.name = name;
        this.description = description;
        this.level = level;
        this.isSolved = false;
    }

    // convenience constructor to include puzzles when loading
    public EscapeRoom(String name, String description, int level, List<Puzzle> puzzles) {
        this(name, description, level);
        this.puzzles = puzzles;
    }

    // UML: + enterRoom(): void
    public void enterRoom() {
        System.out.println("Entering " + name + ": " + description);
    }

    // UML: + getPuzzle(): Puzzle
    // returns the first puzzle (UML shows single Puzzle return). This keeps UML while allowing multiple puzzles.
    public Puzzle getPuzzle() {
        if (puzzles == null || puzzles.isEmpty()) return null;
        return puzzles.get(0);
    }

    // extra getters used by code
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isSolved() { return isSolved; }
    public int getLevel() { return level; }
    public List<Puzzle> getPuzzles() { return puzzles; }

    public void setPuzzles(List<Puzzle> puzzles) { this.puzzles = puzzles; }
    public void setSolved(boolean solved) { isSolved = solved; }

    @Override
    public String toString() {
        return "EscapeRoom{name='" + name + "', level=" + level + ", puzzles=" + (puzzles == null ? 0 : puzzles.size()) + ", isSolved=" + isSolved + "}";
    }
}
