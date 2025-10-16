package com.model;

public class RiddlePuzzle extends Puzzle{
    //private String riddle; // duplicate already stored in puzzle class
    private String answer;
    private String category;

    public RiddlePuzzle(String riddle, String answer, String category, Difficulty difficulty)
    {
        super(riddle, difficulty); // stores riddle and question in puzzle
        this.answer = answer;
        this.category = category;
    }

    public String getAnswer()
    {
        return answer;
    }

    public void setAnswer(String answer)
    {
        this.answer = answer;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    @Override
    public boolean checkAnswer(String userAnswer)
    {
        return answer.equalsIgnoreCase(userAnswer.trim());
    }
}
