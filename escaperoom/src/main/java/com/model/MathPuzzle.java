package com.model;
import java.lang.String;


/**
 * 
 */
public class MathPuzzle extends Puzzle{
    private String equation;
    private int answer;

    /**
     * 
     * @param question
     * @param answer
     * @param difficulty
     */
    public MathPuzzle(String question, int answer, Difficulty difficulty) {
        super(question, difficulty);
        this.equation = question;
        this.answer = answer;
    }//end constructor

    /**
     * is the question different form the equation
     */
    public void generateEquation() {
        return;
    }//end generateEquation()

    /**
     * 
     */
    public boolean checkAnswer(String userAnswer) {
        return userAnswer.equalsIgnoreCase(this.answer);
    }//end checkAnswer()
}//end MathPuzzle
