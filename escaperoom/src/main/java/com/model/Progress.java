package com.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Tracks user progress including score, completed puzzles, time, hints used,
 * and last chosen difficulty (persisted as a string).
 */
public class Progress implements Comparable<Progress> {
    private int currentLevel;
    private ArrayList<String> completedPuzzles;
    private long timeSpent; // seconds
    private String playerName;      // optional
    private int score;
    private Inventory inventory;

    // persistent hint-tracking map (globalPuzzleIndex -> hint count)
    private Map<Integer, Integer> hintsUsed;

    // NEW: store last chosen difficulty as lowercase string ("easy","medium","hard","all")
    private String lastDifficulty;

    public Progress() {
        this.currentLevel = 1;
        this.completedPuzzles = new ArrayList<>();
        this.score = 0;
        this.timeSpent = 0L;
        this.playerName = "Unknown";
        this.hintsUsed = new HashMap<>();
        this.lastDifficulty = "all"; // default
        this.inventory = new Inventory();

    }

    // ---------------------------
    // basic progress methods
    // ---------------------------

    public void addCompletedPuzzle(String puzzleName) {
        if (puzzleName == null) return;
        if (!completedPuzzles.contains(puzzleName)) completedPuzzles.add(puzzleName);
    }

    public void increaseScore(int points) {
        this.score += points;
        if (this.score < 0) this.score = 0;
    }

    public void advanceLevel() {
        currentLevel++;
    }

    public void addTime(long seconds) {
        if (seconds <= 0) return;
        this.timeSpent += seconds;
    }

    public void setTimeSpent(long seconds) {
        this.timeSpent = Math.max(0L, seconds);
    }

    public long getTimeSpent() {
        return timeSpent;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int level) {
        this.currentLevel = Math.max(1, level);
    }

    public ArrayList<String> getCompletedPuzzles() {
        if (this.completedPuzzles == null) this.completedPuzzles = new ArrayList<>();
        return completedPuzzles;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String name) {
        this.playerName = name;
    }

    public int getScore() {
        return score;
    }

    public Inventory getInventory() {
        if (this.inventory == null) this.inventory = new Inventory();
        return this.inventory;
    }
    
    public void setInventory(Inventory inv) {
        this.inventory = inv;
    }
    
    public Map<String, Integer> exportInventoryAsMap() {
        Map<String,Integer> out = new HashMap<>();
        Map<ItemName,Integer> q = this.inventory.getQuantities();
        for (Map.Entry<ItemName,Integer> e : q.entrySet()) {
            out.put(e.getKey().name(), e.getValue());
        }
        return out;
    }

    // ---------------------------
    // Hints tracking
    // ---------------------------

    public Map<Integer, Integer> getHintsUsed() {
        if (this.hintsUsed == null) this.hintsUsed = new HashMap<>();
        return hintsUsed;
    }

    public int getHintsUsedFor(int globalIndex) {
        return getHintsUsed().getOrDefault(globalIndex, 0);
    }

    public void incrementHintsUsedFor(int globalIndex) {
        int prev = getHintsUsed().getOrDefault(globalIndex, 0);
        getHintsUsed().put(globalIndex, prev + 1);
    }

    public void setHintsUsedFor(int globalIndex, int count) {
        if (count <= 0) getHintsUsed().remove(globalIndex);
        else getHintsUsed().put(globalIndex, count);
    }

    // ---------------------------
    // Last difficulty (persistence)
    // ---------------------------
    /**
     * Returns the stored difficulty string (lowercase) — e.g. "easy","medium","hard","all"
     */
    public String getLastDifficulty() {
        if (this.lastDifficulty == null) this.lastDifficulty = "all";
        return this.lastDifficulty;
    }

    /**
     * Set last difficulty using a lowercase string. If null or unrecognized -> "all".
     */
    public void setLastDifficultyString(String diff) {
        if (diff == null) {
            this.lastDifficulty = "all";
            return;
        }
        String d = diff.trim().toLowerCase();
        switch (d) {
            case "easy", "medium", "hard", "all" -> this.lastDifficulty = d;
            default -> this.lastDifficulty = "all";
        }
    }

    /**
     * Convenience: set last difficulty using the Difficulty enum.
     */
    public void setLastDifficulty(Difficulty d) {
        if (d == null) this.lastDifficulty = "all";
        else this.lastDifficulty = d.name().toLowerCase();
    }

    /**
     * Returns the last difficulty as a Difficulty enum (default ALL).
     */
    public Difficulty getLastDifficultyAsEnum() {
        String d = getLastDifficulty();
        if (d == null) return Difficulty.ALL;
        return switch (d.toLowerCase()) {
            case "easy" -> Difficulty.EASY;
            case "medium" -> Difficulty.MEDIUM;
            case "hard" -> Difficulty.HARD;
            default -> Difficulty.ALL;
        };
    }

    // ---------------------------
    // Comparison helpers
    // ---------------------------

    public int compareTo(User other) {
        if (other == null || other.getProgress() == null) return 1;
        return Integer.compare(this.score, other.getProgress().getScore());
    }

    @Override
    public int compareTo(Progress arg0) {
        if (arg0 == null) return 1;
        return Integer.compare(this.score, arg0.score);
    }

    @Override
    public String toString() {
        return "Progress[level=" + currentLevel +
                ", score=" + score +
                ", timeSpent(s)=" + timeSpent +
                ", completed=" + completedPuzzles +
                ", hints=" + hintsUsed +
                ", lastDifficulty=" + lastDifficulty + "]";
    }
}

