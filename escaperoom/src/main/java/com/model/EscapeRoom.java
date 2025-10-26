package com.model;

import java.util.List;

/**
 * Represents a single escape room containing a set of puzzles.
 *
 * This class stores metadata about the room (name, description, level),
 * a flag indicating whether the room is solved, and an optional list of
 * {@code Puzzle} objects the player may attempt.
 */
public class EscapeRoom {
    private String name;
    private String description;
    private boolean isSolved;
    private int level;
    private List<Puzzle> puzzles;

    /**
     * Create a new EscapeRoom.
     *
     * @param name        the display name of the room; must not be null (empty allowed).
     * @param description textual description shown when entering the room; may be empty.
     * @param level       difficulty or progression level for this room (positive integers expected).
     */
    public EscapeRoom(String name, String description, int level) {
        this.name = name;
        this.description = description;
        this.level = level;
        this.isSolved = false;
    }

    /**
     * Create a new EscapeRoom and include the given puzzles.
     *
     * @param name        the display name of the room.
     * @param description description text for the room.
     * @param level       progression level for the room.
     * @param puzzles     list of puzzles for this room; may be null or empty.
     */
    public EscapeRoom(String name, String description, int level, List<Puzzle> puzzles) {
        this(name, description, level);
        this.puzzles = puzzles;
    }

    /**
     * Simulate entering the room (prints the room description).
     *
     * This method currently writes to stdout; callers may override behavior by
     * integrating the game UI instead.
     */
    public void enterRoom() {
        System.out.println("Entering " + name + ": " + description);
    }

    /**
     * Return the first puzzle in the room's puzzle list, or {@code null} if none exist.
     *
     * @return the first {@code Puzzle} or {@code null} when no puzzles are present.
     */
    public Puzzle getPuzzle() {
        if (puzzles == null || puzzles.isEmpty()) return null;
        return puzzles.get(0);
    }

    /**
     * Getters
     */
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isSolved() { return isSolved; }
    public int getLevel() { return level; }
    public List<Puzzle> getPuzzles() { return puzzles; }

    /**
     * Setters
     */
    public void setPuzzles(List<Puzzle> puzzles) { this.puzzles = puzzles; }
    public void setSolved(boolean solved) { isSolved = solved; }

    /**
     * Human-readable representation of the EscapeRoom.
     *
     * @return summary string containing name, level, puzzle count and solved flag.
     */
    @Override
    public String toString() {
        return "EscapeRoom{name='" + name + "', level=" + level + ", puzzles=" + (puzzles == null ? 0 : puzzles.size()) + ", isSolved=" + isSolved + "}";
    }
}
