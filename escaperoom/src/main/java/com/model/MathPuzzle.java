package com.model;
public class MathPuzzle extends Puzzle {
    private String equation;
    private int answer;
    private boolean isSolved;
    private int id;

    // UML: + MathPuzzle(String question, Int answer, Difficulty difficulty)
    public MathPuzzle(String question, int answer, Difficulty difficulty) {
        super(question, difficulty);
        this.equation = null;
        this.answer = answer;
        this.isSolved = false;
    }

    // UML: + generateEquation(): void
    public void generateEquation() {
        // Minimal placeholder: store the question as the equation if null.
        if (equation == null) equation = question;
        // You can replace this with a generator based on Difficulty later.
    }

    // UML: + checkAnswer(String userAnswer): boolean
    @Override
    public boolean checkAnswer(String userAnswer) {
        if (userAnswer == null) return false;
        try {
            int v = Integer.parseInt(userAnswer.trim());
            boolean ok = (v == answer);
            if (ok) isSolved = true;
            return ok;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String getEquation() { return equation; }
    public int getAnswer() { return answer; }
    public boolean isSolved() { return isSolved; }

    @Override
    public String toString() {
        return "MathPuzzle{question='" + question + "', equation='" + equation + "', answer=" + answer + ", solved=" + isSolved + "}";
    }
}

