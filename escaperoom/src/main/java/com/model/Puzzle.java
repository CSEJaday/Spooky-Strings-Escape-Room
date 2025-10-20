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

    //public PuzzleMaker getPuzzleMaker() // add constructor to puzzle instead of having puzzle maker
    //{

    //}
    public abstract boolean checkAnswer(String userAnswer);
   


}
