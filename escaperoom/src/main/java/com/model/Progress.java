package com.model;

import java.util.*;

/**
 * Tracks a player's in-game progress, including:
 * - Time spent playing
 * - Score
 * - Completed puzzles (by both ID and question text)
 * - Hint usage
 * - Last selected difficulty
 * - Player inventory
 */
public class Progress {

    private long timeSpent = 0L;// seconds
    private int score = 0;
    private int currentLevel = 1;

    // completed puzzles: store both id set and question text set for compatibility
    private Set<Integer> completedPuzzleIds = new LinkedHashSet<>();
    private Set<String> completedPuzzleQuestions = new LinkedHashSet<>();

    private Map<Integer, Integer> hintsUsed = new HashMap<>(); // id/globalIndex -> count

    private Difficulty lastDifficulty = Difficulty.ALL;

    private Inventory inventory;

     /** 
      * Creates a new Progress instance with an empty inventory. 
      */
    public Progress() {
        this.inventory = new Inventory();
    }

    /** 
     * @return the current level number (minimum 1)
     */
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    /**
     * Sets the current level; coerced to at least 1.
     *
     * @param currentLevel the level number to set
     */
    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = Math.max(1, currentLevel);
    }

    /** 
     * @return total time spent in seconds 
     */
    public long getTimeSpent() { return timeSpent; }

    /**
     * Adds playtime to the total.
     *
     * @param seconds seconds to add (ignored if ≤ 0)
     */
    public void addTime(long seconds) { if (seconds > 0) this.timeSpent += seconds; }

    /** 
     * @return current player score 
     */
    public int getScore() { return score; }

    /**
     * Adjusts the player's score by a delta amount.
     * Prevents the score from going below zero.
     *
     * @param delta score change amount
     */
    public void increaseScore(int delta) { this.score += delta; if (this.score < 0) this.score = 0; }



    /** Marks a puzzle (by ID) as completed. */
    public void addCompletedPuzzleId(int id) { if (id >= 0) completedPuzzleIds.add(id); }

    /** Checks if a puzzle (by ID) has been completed. */
    public boolean hasCompletedPuzzleId(int id) { return id >= 0 && completedPuzzleIds.contains(id); }

     /** @return an unmodifiable set of completed puzzle IDs */
    public Set<Integer> getCompletedPuzzleIds() { return Collections.unmodifiableSet(completedPuzzleIds); }



     /** Adds a completed puzzle by its question text (legacy support). */
    public void addCompletedPuzzle(String question) { if (question != null) completedPuzzleQuestions.add(question); }
    
    /** Checks if a puzzle has been completed based on its question text. */
    public boolean hasCompletedPuzzleQuestion(String q) { return q != null && completedPuzzleQuestions.contains(q); }
   
    /** @return a list of completed puzzle question strings */
    public List<String> getCompletedPuzzles() { return new ArrayList<>(completedPuzzleQuestions); }



   /** @return an unmodifiable map of hints used (puzzle ID → count) */
    public Map<Integer,Integer> getHintsUsed() { return Collections.unmodifiableMap(hintsUsed); }

    /** @return the number of hints used for a given puzzle ID */
    public int getHintsUsedFor(int id) { return hintsUsed.getOrDefault(id, 0); }

    /** Increments the hint count for the given puzzle ID. */
    public void incrementHintsUsedFor(int id) { hintsUsed.merge(id, 1, Integer::sum); }



    /** Sets the last difficulty played (defaults to ALL if null). */
    public void setLastDifficulty(Difficulty d) { if (d != null) this.lastDifficulty = d; }

    /** @return the last recorded difficulty level */
    public Difficulty getLastDifficultyAsEnum() { return lastDifficulty != null ? lastDifficulty : Difficulty.ALL; }



    /** @return the current inventory, creating one if null */
    public Inventory getInventory() {
        if (this.inventory == null) this.inventory = new Inventory();
        return this.inventory;
    }

    /**
     * Replaces the player's inventory.
     *
     * @param inv new inventory object
     */
    public void setInventory(Inventory inv) { this.inventory = inv; }



    /**
     * Checks if a puzzle is marked completed either by its ID or question text.
     *
     * @param id       puzzle ID
     * @param question puzzle question text
     * @return true if completed by either identifier
     */
    public boolean hasCompletedByEither(int id, String question) {
        return (id >= 0 && completedPuzzleIds.contains(id)) || (question != null && completedPuzzleQuestions.contains(question));
    }
}


