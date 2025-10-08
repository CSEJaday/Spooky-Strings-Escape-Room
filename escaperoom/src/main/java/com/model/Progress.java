package com.model;

public class Progress {
    private int currentLevel;
    private int hintsUsed;
    private int puzzlesSolved;
    private String timeElapsed;

    public Progress() {  
    }

    public void resetProgress() {
    }

    public void advanceLevel() {
    }

    public void updateTime(String time) {
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getPuzzlesSolved() {
        return puzzlesSolved;
    }

    public int getHintsUsed() {
        return hintsUsed;
    }

    public String getTimeElapsed() {
        return timeElapsed;
    }

    @Override
    public String toString() {
        return "";
    }
}
