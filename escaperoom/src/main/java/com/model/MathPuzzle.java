package com.model;

public class MathPuzzle extends Puzzle {
    private String equation;
    private int answer;

    public MathPuzzle(String question, int answer, Difficulty difficulty) {
        super(question, difficulty);
        this.equation = question;
        this.answer = answer;
    }

    public void generateEquation() {
        // optional: generate or parse the equation string into fields
        // left as no-op for now
    }

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

    // optional getter if other code expects it
    public int getAnswer() {
        return this.answer;
    }
}

