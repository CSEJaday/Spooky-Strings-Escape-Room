package com.model;

public class RiddlePuzzle extends Puzzle {
    private String answer;
    private String category;
    private boolean isSolved;
    private int id;

    /**
     * A text-based puzzle where the player must answer a riddle correctly.
     * Comparison is case-insensitive and ignores extra whitespace.
     */
    public RiddlePuzzle(String riddle, String answer, String category, Difficulty difficulty) {
        super(riddle, difficulty);
        this.answer = answer;
        this.category = category;
        this.isSolved = false;
    }

    /**
     * Getters/Setter for Answer
     */
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    /**
     * Getters/Setter for Category
     */
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    /**
     * Checks whether the player’s answer matches the correct answer.
     *
     * @param userAnswer the player’s input
     * @return true if the answer matches; false otherwise
     */
    @Override
    public boolean checkAnswer(String userAnswer) {
        if (userAnswer == null || answer == null) return false;
        boolean ok = answer.trim().equalsIgnoreCase(userAnswer.trim());
        if (ok) isSolved = true;
        return ok;
    }

     /** @return true if the riddle has been solved */
    public boolean isSolved() { return isSolved; }

    @Override
    public String toString() {
        return "RiddlePuzzle{question='" + question + "', answer='" + answer + "', category='" + category + "', solved=" + isSolved + "}";
    }
}
