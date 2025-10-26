package com.model;

/**
 * A numerical puzzle where the player must solve a math equation and provide the correct answer.
 * The correct answer is stored as an integer and validated against the player's input.
 */
public class MathPuzzle extends Puzzle {
    private String equation;
    private int answer;

    /**
     * Constructs a new MathPuzzle with the given equation, answer, and difficulty.
     *
     * @param question   the equation or math problem text
     * @param answer     the correct integer answer
     * @param difficulty the difficulty level of the puzzle
     */
    public MathPuzzle(String question, int answer, Difficulty difficulty) {
        super(question, difficulty);
        this.equation = question;
        this.answer = answer;
    }

    /**
     * Optional method for generating or parsing the equation dynamically.
     * Currently unused but available for extensions.
     */
    public void generateEquation() {
        // optional: generate or parse the equation string into fields
    }

    /**
     * Checks whether the player's numeric answer matches the correct answer.
     *
     * @param userAnswer string entered by the player
     * @return true if the answer is a valid integer and equals the stored solution
     */
    @Override
    public boolean checkAnswer(String userAnswer) {
        if (userAnswer == null) return false;
        try {
            long provided = Long.parseLong(userAnswer.trim());
            return provided == (long) this.answer;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** 
     * @return the correct numeric answer for this puzzle 
     */
    public int getAnswer() {
        return this.answer;
    }
}

