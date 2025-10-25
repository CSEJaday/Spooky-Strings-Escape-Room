package com.model;

/**
 * DoorPuzzle: player chooses a numbered door. If they pick the correct door, the puzzle is solved.
 * Supports:
 *  - variable number of doors (numDoors)
 *  - correctDoor (1-based index)
 *  - optional attemptsAllowed (0 = unlimited)
 *  - optional difficulty
 * The checkAnswer method expects the player's input to be parseable to an integer.
 */
public class DoorPuzzle extends Puzzle {

    private final int numDoors;
    private int correctDoor = 1;        // 1-based index of the correct door
    private int attemptsAllowed = 0;    // 0 = unlimited
    private int attemptsMade = 0;
    private boolean blocked = false;    // e.g., a stuck door that requires a crowbar
    // locked is supported via Puzzle.setLocked(boolean)/isLocked()

    /**
     * Simple constructor with number of doors. correctDoor defaults to 1.
     */
    public DoorPuzzle(int numDoors) {
        super("Choose a door (1-" + Math.max(1, numDoors) + ")", Difficulty.MEDIUM);
        this.numDoors = Math.max(1, numDoors);
    }

    /**
     * Full constructor allowing correct door, attempts allowed and difficulty.
     * @param numDoors number of choices
     * @param correctDoor 1-based correct door index
     * @param attemptsAllowed 0 = unlimited, otherwise max attempts allowed before lockout or fail
     * @param difficulty difficulty of puzzle
     */
    public DoorPuzzle(int numDoors, int correctDoor, int attemptsAllowed, Difficulty difficulty) {
        super("Choose a door (1-" + Math.max(1, numDoors) + ")", difficulty);
        this.numDoors = Math.max(1, numDoors);
        setCorrectDoor(correctDoor);
        this.attemptsAllowed = Math.max(0, attemptsAllowed);
    }

    public int getNumDoors() {
        return numDoors;
    }

    public int getCorrectDoor() {
        return correctDoor;
    }

    /**
     * Setter used by RoomLoader when it wants to override the correct door after constructing.
     */
    public void setCorrectDoor(int correctDoor) {
        if (correctDoor < 1) correctDoor = 1;
        if (correctDoor > this.numDoors) correctDoor = this.numDoors;
        this.correctDoor = correctDoor;
    }

    public int getAttemptsAllowed() {
        return attemptsAllowed;
    }

    public void setAttemptsAllowed(int attemptsAllowed) {
        this.attemptsAllowed = Math.max(0, attemptsAllowed);
    }

    public int getAttemptsMade() {
        return attemptsMade;
    }

    public void setAttemptsMade(int attemptsMade) {
        this.attemptsMade = Math.max(0, attemptsMade);
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    /**
     * Check answer: expects an integer string; returns true if matches correctDoor.
     * If attemptsAllowed > 0 and attemptsMade exceeds allowed, returns false and will not accept more attempts.
     */
    @Override
    public boolean checkAnswer(String userAnswer) {
        if (isLocked()) {
            // If puzzle is locked, it cannot be solved by answering
            return false;
        }
        if (blocked) {
            // If physically blocked (requires crowbar), cannot be solved by answering
            return false;
        }

        if (userAnswer == null) return false;
        String trimmed = userAnswer.trim();
        if (trimmed.isEmpty()) return false;

        // Try to parse an integer choice; allow inputs like "2" or "door 2" (take last token)
        String token = trimmed;
        if (!trimmed.matches("^-?\\d+$")) {
            // attempt to extract trailing number
            String[] toks = trimmed.split("\\s+");
            token = toks[toks.length - 1];
        }

        int choice;
        try {
            choice = Integer.parseInt(token);
        } catch (NumberFormatException e) {
            return false;
        }

        // enforce range
        if (choice < 1 || choice > numDoors) return false;

        attemptsMade++;
        // If attemptsAllowed > 0 and we've exceeded, don't allow further success
        if (attemptsAllowed > 0 && attemptsMade > attemptsAllowed) {
            return false;
        }

        return choice == correctDoor;
    }

    @Override
    public String toString() {
        return "DoorPuzzle{numDoors=" + numDoors + ", correctDoor=" + correctDoor +
                ", attemptsAllowed=" + attemptsAllowed + ", attemptsMade=" + attemptsMade +
                ", locked=" + isLocked() + ", blocked=" + blocked + "}";
    }
}
