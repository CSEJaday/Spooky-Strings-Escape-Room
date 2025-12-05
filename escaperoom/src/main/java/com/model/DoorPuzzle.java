package com.model;

/**
 * A puzzle where the player must choose the correct numbered door to solve it.
 * The puzzle supports a configurable number of doors, attempt limits, and a
 * blocked state requiring external action or items to clear.
 */
public class DoorPuzzle extends Puzzle {

    private final int numDoors;
    private int correctDoor = 1;        // 1-based index of the correct door
    private int attemptsAllowed = 0;    // 0 = unlimited
    private int attemptsMade = 0;
    private boolean blocked = false;    // e.g., a stuck door that requires a crowbar
    // locked is supported via Puzzle.setLocked(boolean)/isLocked()

    /**
     * Creates a door puzzle with the specified number of doors.
     * 
     * @param numDoors number of door choices; values below 1 are set to 1
     */
    public DoorPuzzle(int numDoors) {
        super("Choose a number (1-" + Math.max(1, numDoors) + "). Answer carefully..", Difficulty.MEDIUM);
        this.numDoors = Math.max(1, numDoors);
    }

    /**
     * Creates a door puzzle with custom parameters.
     * 
     * @param numDoors number of doors available
     * @param correctDoor the 1-based index of the correct door
     * @param attemptsAllowed 0 for unlimited, otherwise max allowed attempts
     * @param difficulty difficulty setting for the puzzle
     */
    public DoorPuzzle(int numDoors, int correctDoor, int attemptsAllowed, Difficulty difficulty) {
        super("Choose a number (1-" + Math.max(1, numDoors) + "). Answer carefully..", difficulty);
        this.numDoors = Math.max(1, numDoors);
        setCorrectDoor(correctDoor);
        this.attemptsAllowed = Math.max(0, attemptsAllowed);
    }

    /**
     * Get the number of door choices.
     *
     * @return number of doors (>= 1).
     */
    public int getNumDoors() {
        return numDoors;
    }

    /**
     * Get the correct door index (1-based).
     *
     * @return the correct door index.
     */
    public int getCorrectDoor() {
        return correctDoor;
    }

    /**
     * Sets the correct door index, clamped between 1 and {@code numDoors}.
     *
     * @param correctDoor the 1-based index to set
     */
    public void setCorrectDoor(int correctDoor) {
        if (correctDoor < 1) correctDoor = 1;
        if (correctDoor > this.numDoors) correctDoor = this.numDoors;
        this.correctDoor = correctDoor;
    }

    /**
     * Get maximum allowed attempts (0 indicates unlimited).
     *
     * @return attempts allowed.
     */
    public int getAttemptsAllowed() {
        return attemptsAllowed;
    }

    /**
     * Set the attempts allowed (negative values are coerced to 0).
     *
     * @param attemptsAllowed new maximum attempts (0 = unlimited).
     */
    public void setAttemptsAllowed(int attemptsAllowed) {
        this.attemptsAllowed = Math.max(0, attemptsAllowed);
    }

    /**
     * Get attempts made so far.
     *
     * @return number of attempts recorded.
     */
    public int getAttemptsMade() {
        return attemptsMade;
    }

    /**
     * Set attempts made (negative values become 0).
     *
     * @param attemptsMade attempts to record.
     */
    public void setAttemptsMade(int attemptsMade) {
        this.attemptsMade = Math.max(0, attemptsMade);
    }

    /**
     * Check whether this door is currently blocked (physically).
     *
     * @return true when blocked and cannot be solved by normal choice.
     */
    public boolean isBlocked() {
        return blocked;
    }

    /**
     * Mark or clear the blocked status.
     *
     * @param blocked true to mark as blocked.
     */
    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    /**
     * Checks the player’s answer by parsing a numeric door choice and comparing it
     * to the correct door. Returns false if locked, blocked, invalid, or over the
     * allowed attempt limit.
     *
     * @param userAnswer player input string
     * @return true if the chosen door is correct and valid; false otherwise
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
