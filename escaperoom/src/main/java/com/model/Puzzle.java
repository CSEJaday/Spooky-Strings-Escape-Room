package com.model;

/**
 * Base Puzzle class with support for:
 *  - numeric id (optional)
 *  - reward (ItemName) optional
 *  - locked flag
 *  - hiddenHint + shown flag
 *
 * Concrete puzzles must implement checkAnswer().
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

    public Puzzle(String question, Difficulty difficulty) {
        this.question = question == null ? "" : question;
        this.difficulty = difficulty == null ? Difficulty.EASY : difficulty;
    }

    // id
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // question / difficulty
    public String getQuestion() { return question; }
    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty d) { this.difficulty = d; }

    // reward
    public ItemName getReward() { return reward; }
    public void setReward(ItemName reward) { this.reward = reward; }

    // lock
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    // hidden hint
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




   

