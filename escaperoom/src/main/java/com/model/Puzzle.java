package com.model;

/**
 * Base Puzzle class.
 * Concrete puzzles extend this (MathPuzzle, RiddlePuzzle, DoorPuzzle, TriviaPuzzle).
 */
public abstract class Puzzle {
    protected String question;
    protected Difficulty difficulty;

    // Optional reward for completing this puzzle (null if none)
    private ItemName reward;

    // Lock / hidden hint support
    private boolean locked = false;
    private String hiddenHint = null;
    private boolean hiddenHintShown = false;

    public Puzzle(String question, Difficulty difficulty) {
        this.question = question == null ? "" : question;
        this.difficulty = difficulty == null ? Difficulty.EASY : difficulty;
    }

    public String getQuestion() {
        return question;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty d) {
        this.difficulty = d;
    }

    // Reward related
    public void setReward(ItemName reward) {
        this.reward = reward;
    }

    public ItemName getReward() {
        return reward;
    }

    // Lock-related
    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    // Hidden hint
    public String getHiddenHint() {
        return hiddenHint;
    }

    public void setHiddenHint(String hint) {
        this.hiddenHint = hint;
    }

    public boolean isHiddenHintShown() {
        return hiddenHintShown;
    }

    public void setHiddenHintShown(boolean shown) {
        this.hiddenHintShown = shown;
    }

    /**
     * Subclasses must implement answer checking.
     * @param userAnswer player's answer text
     * @return true if correct
     */
    public abstract boolean checkAnswer(String userAnswer);
}



   

