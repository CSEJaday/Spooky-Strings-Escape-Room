package com.model;

/**
 * Represents a trivia-style puzzle in the Escape Room game.
 * Extends Puzzle and includes an additional category field.
 * @author
 */
public class TriviaPuzzle extends Puzzle {
    private String answer;
    private String category;

    /**
     * Constructs a new TriviaPuzzle object.
     * @param question   the trivia question
     * @param answer     the correct answer
     * @param category   the category of the trivia question
     * @param difficulty the difficulty level of the puzzle
     */
    public TriviaPuzzle(String question, String answer, String category, Difficulty difficulty) {
        super(question, difficulty); // question stored as puzzle's question
        this.answer = answer;
        this.category = category;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public boolean checkAnswer(String userAnswer) {
        return answer.equalsIgnoreCase(userAnswer.trim());
    }
}
