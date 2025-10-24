package com.model;
import java.util.Random;

public class DoorPuzzle extends Puzzle {
    private int correctDoor;
    private int numDoors;
    private int attempts;
    private boolean isSolved;
    private int id;

    // UML: + DoorPuzzle(numDoors): int
    // (constructors don't return int; UML likely meant constructor taking numDoors)
    public DoorPuzzle(int numDoors) {
        super("Choose a door (1 to " + Math.max(1, numDoors) + ")", Difficulty.MEDIUM);
        this.numDoors = Math.max(1, numDoors);
        this.correctDoor = 1; // default; loader should set this or you can add setter
        this.attempts = 0;
        this.isSolved = false;
    }

    // helper constructor used by loader (keeps UML behavior but allows setting correctDoor/attempts)
    public DoorPuzzle(int numDoors, int correctDoor, int attempts, Difficulty difficulty) {
        super("Choose a door (1 to" + Math.max(1, numDoors) + ")", difficulty);
        this.numDoors = Math.max(1, numDoors);
        this.correctDoor = correctDoor;
        this.attempts = attempts;
        this.isSolved = false;
    }

    // UML: + guessDoor(choice: int): boolean
    public boolean guessDoor(int choice) {
        attempts++;
        boolean ok = (choice == correctDoor);
        if (ok) isSolved = true;
        return ok;
    }

    // UML: + getAttempts(): int
    public int getAttempts() {
        return attempts;
    }

    // UML: + getHint(): String
    public String getHint() {
        // simple hint implementation
        return "There are " + numDoors + " doors. Try to reason about labels/clues.";
    }

    // implement abstract method
    @Override
    public boolean checkAnswer(String userAnswer) {
        if (userAnswer == null) return false;
        try {
            int choice = Integer.parseInt(userAnswer.trim());
            return guessDoor(choice);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public int getNumDoors() { return numDoors; }
    public int getCorrectDoor() { return correctDoor; }
    public boolean isSolved() { return isSolved; }

    @Override
    public String toString() {
        return "DoorPuzzle{numDoors=" + numDoors + ", correctDoor=" + correctDoor + ", attempts=" + attempts + ", solved=" + isSolved + "}";
    }
}
