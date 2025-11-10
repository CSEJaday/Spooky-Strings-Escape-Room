package com.model;

/**
 * Tracks a player's in-game progress, including:
 * - Time spent playing
 * - Score
 * - Completed puzzles (by both ID and question text)
 * - Hint usage
 * - Last selected difficulty
 * - Player inventory
 */
public abstract class Puzzle {
    protected String question;
    protected Difficulty difficulty;

    // Optional id (set from JSON)
    private int id = -1;

    // Optional reward
    private ItemName reward;

    // Lock / hidden hint support
    private boolean locked = false;
    private String hiddenHint = null;
    private boolean hiddenHintShown = false;

    /** Creates a new Progress instance with an empty inventory. */
    public Puzzle(String question, Difficulty difficulty) {
        this.question = question == null ? "" : question;
        this.difficulty = difficulty == null ? Difficulty.EASY : difficulty;
    }

    /**
     * Getters/Setters for ID
     */ 
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    /** 
     * Getters/Setters for  Question and Difficulty
     */
    public String getQuestion() { return question; }
    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty d) { this.difficulty = d; }

    /**
     * Getters/Setters for Reward
     */
    public ItemName getReward() { return reward; }
    public void setReward(ItemName reward) { this.reward = reward; }

    /**
     * Getters/Setters for IsLocked
     */
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    /**
     * Getters/Setters for HiddenHint
     */
    public String getHiddenHint() { return hiddenHint; }
    public void setHiddenHint(String hint) { this.hiddenHint = hint; }
    public boolean isHiddenHintShown() { return hiddenHintShown; }
    public void setHiddenHintShown(boolean shown) { this.hiddenHintShown = shown; }

    /**
     * Subclasses must implement answer checking.
     * @param userAnswer player's answer
     * @return true if correct
     */
    public abstract boolean checkAnswer(String userAnswer);

    @Override
    public String toString() {
        return "Puzzle{id=" + id + ", question='" + question + '\'' + ", difficulty=" + difficulty +
                ", reward=" + reward + ", locked=" + locked + "}";
    }
}




   

