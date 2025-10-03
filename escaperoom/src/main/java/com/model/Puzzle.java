package com.model;

public class Puzzle {
    private String question;
    private Difficulty difficulty;

    public Puzzle(String question, Difficulty difficulty)
    {
        this.question = question;
        this.difficulty = difficulty;
    }

    public String getQuestion()
    {
        return question;
    }

    public Difficulty getDifficulty()
    {
        return difficulty;
    }

    public abstract boolean checkAnswer(String userAnswer)
    {
        return false;
    }
}
