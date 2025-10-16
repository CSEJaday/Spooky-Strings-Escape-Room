package com.model;

public abstract class Puzzle {
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

    public abstract boolean checkAnswer(String userAnswer);
   // need to possibly implement a enum class
   // named difficulty, should look like
   // public enum Difficulty {
   //.    EASY, MEDIUM HARD;
   // }


}
