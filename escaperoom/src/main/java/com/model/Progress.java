package com.model;

import java.util.*;

/**
 * Progress tracks:
 *  - timeSpent (seconds)
 *  - score
 *  - completed puzzles (both IDs and questions for compatibility)
 *  - hintsUsed map (globalIndex or id -> count)
 *  - lastDifficulty
 *  - Inventory
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

    public Progress() {
        this.inventory = new Inventory();
    }

    public int getCurrentLevel() {
        return currentLevel;
    }
    
    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = Math.max(1, currentLevel);
    }
    
    // time
    public long getTimeSpent() { return timeSpent; }
    public void addTime(long seconds) { if (seconds > 0) this.timeSpent += seconds; }

    // score
    public int getScore() { return score; }
    public void increaseScore(int delta) { this.score += delta; if (this.score < 0) this.score = 0; }

    // completed by id
    public void addCompletedPuzzleId(int id) { if (id >= 0) completedPuzzleIds.add(id); }
    public boolean hasCompletedPuzzleId(int id) { return id >= 0 && completedPuzzleIds.contains(id); }
    public Set<Integer> getCompletedPuzzleIds() { return Collections.unmodifiableSet(completedPuzzleIds); }

    // completed by question (backwards compatibility)
    public void addCompletedPuzzle(String question) { if (question != null) completedPuzzleQuestions.add(question); }
    public boolean hasCompletedPuzzleQuestion(String q) { return q != null && completedPuzzleQuestions.contains(q); }
    public List<String> getCompletedPuzzles() { return new ArrayList<>(completedPuzzleQuestions); }

    // hints
    public Map<Integer,Integer> getHintsUsed() { return Collections.unmodifiableMap(hintsUsed); }
    public int getHintsUsedFor(int id) { return hintsUsed.getOrDefault(id, 0); }
    public void incrementHintsUsedFor(int id) { hintsUsed.merge(id, 1, Integer::sum); }

    // difficulty
    public void setLastDifficulty(Difficulty d) { if (d != null) this.lastDifficulty = d; }
    public Difficulty getLastDifficultyAsEnum() { return lastDifficulty != null ? lastDifficulty : Difficulty.ALL; }

    // inventory
    public Inventory getInventory() {
        if (this.inventory == null) this.inventory = new Inventory();
        return this.inventory;
    }
    public void setInventory(Inventory inv) { this.inventory = inv; }

    // helpers for migrating/compatibility
    public boolean hasCompletedByEither(int id, String question) {
        return (id >= 0 && completedPuzzleIds.contains(id)) || (question != null && completedPuzzleQuestions.contains(question));
    }
}


