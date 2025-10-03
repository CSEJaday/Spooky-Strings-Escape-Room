package com.model;

public class MathPuzzle extends Puzzle{
    private String equation;
    private int answer;

    public MathPuzzle(String question, int answer, Difficulty difficulty)
    {
        super(question, difficulty);
        this.equation = question;
        this.answer = answer;
    }

    public void generateEquation()
    {
        return;
    }

    public boolean checkAnswer(String userAnswer)
    {
        return false;
    }
}
