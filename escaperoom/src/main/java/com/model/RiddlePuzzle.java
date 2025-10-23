package com.model;

public class RiddlePuzzle extends Puzzle {
    private String answer;
    private String category;
    private boolean isSolved;
    private int id;

    // UML: + RiddlePuzzle(String riddle, String answer, String category, Difficulty difficulty)
    public RiddlePuzzle(String riddle, String answer, String category, Difficulty difficulty) {
        super(riddle, difficulty);
        this.answer = answer;
        this.category = category;
        this.isSolved = false;
    }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    // UML: + checkAnswer(String userAnswer): boolean
    @Override
    public boolean checkAnswer(String userAnswer) {
        if (userAnswer == null || answer == null) return false;
        boolean ok = answer.trim().equalsIgnoreCase(userAnswer.trim());
        if (ok) isSolved = true;
        return ok;
    }

    public boolean isSolved() { return isSolved; }

    @Override
    public String toString() {
        return "RiddlePuzzle{question='" + question + "', answer='" + answer + "', category='" + category + "', solved=" + isSolved + "}";
    }
}
