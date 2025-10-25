package com.model;

public abstract class Puzzle {
    protected String question;
    protected Difficulty difficulty;
    private ItemName reward;

    // UML: + Puzzle(String Question, Difficulty difficulty)
    public Puzzle(String question, Difficulty difficulty) {
        this.question = question;
        this.difficulty = difficulty;
    }

    // UML: + getQuestion(): String
    public String getQuestion() {
        return question;
    }

    // UML: + getDifficulty(): Difficulty
    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setReward(ItemName reward) {
        this.reward = reward;
    }

    public ItemName getReward() {
        return reward;
    }

    // UML: + checkAnswer(userAnswer: String): boolean
    public abstract boolean checkAnswer(String userAnswer);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{question='" + question + "', difficulty=" + difficulty + "}";
    }
}


   

