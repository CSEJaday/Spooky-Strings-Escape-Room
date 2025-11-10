package com.model;

/**
 * A trivia-style puzzle that presents a question and requires the player
 * to provide the correct answer. Includes an optional category for organization.
 */
public class TriviaPuzzle extends Puzzle {
    private String answer;
    private String category;

    /**
     * Creates a new TriviaPuzzle instance.
     *
     * @param question   the trivia question text
     * @param answer     the correct answer
     * @param category   category or theme of the question
     * @param difficulty the difficulty level
     */
    public TriviaPuzzle(String question, String answer, String category, Difficulty difficulty) {
        super(question, difficulty); // question stored as puzzle's question
        this.answer = answer;
        this.category = category;
    }

    /** @return the correct answer */
    public String getAnswer() {
        return answer;
    }

    /** Sets the correct answer. */
    public void setAnswer(String answer) {
        this.answer = answer;
    }

    /** @return the category or topic of the trivia question */
    public String getCategory() {
        return category;
    }

    /** Sets the category of this trivia question. */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Checks whether the player’s answer matches the correct answer,
     * ignoring case and trimming whitespace.
     *
     * @param userAnswer the player's response
     * @return true if the answer is correct; false otherwise
     */
    @Override
    public boolean checkAnswer(String userAnswer) {
        return answer.equalsIgnoreCase(userAnswer.trim());
    }
}
