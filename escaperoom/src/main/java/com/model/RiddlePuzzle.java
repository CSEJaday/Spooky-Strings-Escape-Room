package com.model;

public class RiddlePuzzle extends Puzzle{
    private String riddle;
    private String answer;
    private String category;

    public RiddlePuzzle(String riddle, String answer, String category, Difficulty difficulty)
    {
        super(riddle, difficulty);
        this.riddle = riddle;
        this.answer = answer;
        this.category = category;
    }

    public void setRiddle(String riddle)
    {
        this.riddle = riddle;
    }

    public String getRiddle()
    {
        return riddle;
    }

    public boolean checkAnswer(String userAnswer)
    {
        return false;
    }
}
