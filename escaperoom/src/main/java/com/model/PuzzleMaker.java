package com.model;

/**
 * Factory class for creating puzzles of different types and difficulties.
 * @author
 */
public class PuzzleMaker {

    /**
     * Default constructor.
     */
    public PuzzleMaker() {
    }

    /**
     * Creates a puzzle object based on the given parameters.
     * @param type the type of puzzle (e.g., "riddle", "logic")
     * @param question the puzzle question text
     * @param answer the correct answer for the puzzle
     * @param difficulty the difficulty level of the puzzle
     * @return a new Puzzle object, or null if the type is unknown
     */
    public Puzzle createPuzzle(String type, String question, String answer, Difficulty difficulty) {
        switch (type.toLowerCase()) {
            case "riddle":
                return new RiddlePuzzle(question, answer, difficulty);
            case "math":
                return new MathPuzzle(question, answer, difficulty);
            case "trivia":
                return new TriviaPuzzle(question, answer, difficulty);
            default:
                System.out.println("Unknown puzzle type: " + type);
                return null;
        }
    }
}
