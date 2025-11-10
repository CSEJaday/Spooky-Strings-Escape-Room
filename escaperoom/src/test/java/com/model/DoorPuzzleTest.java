package com.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for DoorPuzzle.
 */
public class DoorPuzzleTest {

    @Test
    public void testConstructorClampsNumDoorsToAtLeastOne() {
        DoorPuzzle p = new DoorPuzzle(0); // should clamp to 1
        assertEquals(1, p.getNumDoors());
        assertEquals(1, p.getCorrectDoor()); // default correctDoor initialized to 1
    }

    @Test
    public void testSetCorrectDoorClampsWithinRange() {
        DoorPuzzle p = new DoorPuzzle(3); // numDoors = 3
        p.setCorrectDoor(0); // too small -> clamp to 1
        assertEquals(1, p.getCorrectDoor());

        p.setCorrectDoor(5); // too large -> clamp to numDoors (3)
        assertEquals(3, p.getCorrectDoor());

        p.setCorrectDoor(2);
        assertEquals(2, p.getCorrectDoor());
    }

    @Test
    public void testAttemptsAllowedSetterCoercesNegativeToZeroAndSetAttemptsMade() {
        DoorPuzzle p = new DoorPuzzle(3);
        p.setAttemptsAllowed(-5);
        assertEquals(0, p.getAttemptsAllowed()); // 0 = unlimited

        p.setAttemptsAllowed(2);
        assertEquals(2, p.getAttemptsAllowed());

        p.setAttemptsMade(-7);
        assertEquals(0, p.getAttemptsMade());
        p.setAttemptsMade(5);
        assertEquals(5, p.getAttemptsMade());
    }

    @Test
    public void testCheckAnswerParsesSimpleAndPhraseInputs() {
        DoorPuzzle p = new DoorPuzzle(4);
        p.setCorrectDoor(3);

        // simple numeric string
        assertTrue(p.checkAnswer("3"));
        assertEquals(1, p.getAttemptsMade()); // incremented

        // whitespace trimmed
        assertTrue(p.checkAnswer(" 3 "));
        assertEquals(2, p.getAttemptsMade());

        // phrase ending with number
        assertTrue(p.checkAnswer("open door 3"));
        assertEquals(3, p.getAttemptsMade());

        // incorrect choice within range increments attempts and returns false
        assertFalse(p.checkAnswer("2"));
        assertEquals(4, p.getAttemptsMade());
    }

    @Test
    public void testCheckAnswerRejectsInvalidOrOutOfRangeWithoutIncrement() {
        DoorPuzzle p = new DoorPuzzle(3);
        p.setCorrectDoor(1);
        int before = p.getAttemptsMade();

        // null input -> false, no increment
        assertFalse(p.checkAnswer(null));
        assertEquals(before, p.getAttemptsMade());

        // empty input -> false, no increment
        assertFalse(p.checkAnswer("   "));
        assertEquals(before, p.getAttemptsMade());

        // non-numeric trailing token -> false, no increment
        assertFalse(p.checkAnswer("pick the blue"));
        assertEquals(before, p.getAttemptsMade());

        // numeric but out of range -> false, no increment
        assertFalse(p.checkAnswer("10"));
        assertEquals(before, p.getAttemptsMade());
    }

    @Test
    public void testBlockedOrLockedPreventSolvingAndDoNotIncrementAttempts() {
        DoorPuzzle p = new DoorPuzzle(2);
        p.setCorrectDoor(2);

        // locked
        p.setLocked(true);
        int beforeLocked = p.getAttemptsMade();
        assertFalse(p.checkAnswer("2"));
        assertEquals(beforeLocked, p.getAttemptsMade());
        p.setLocked(false);

        // blocked
        p.setBlocked(true);
        int beforeBlocked = p.getAttemptsMade();
        assertFalse(p.checkAnswer("2"));
        assertEquals(beforeBlocked, p.getAttemptsMade());
        p.setBlocked(false);
    }

    @Test
    public void testAttemptsAllowedEnforced() {
        DoorPuzzle p = new DoorPuzzle(3);
        p.setCorrectDoor(2);
        p.setAttemptsAllowed(2); // allow 2 attempts only

        // first wrong attempt (within range) increments and returns false
        assertFalse(p.checkAnswer("1"));
        assertEquals(1, p.getAttemptsMade());

        // second wrong attempt
        assertFalse(p.checkAnswer("3"));
        assertEquals(2, p.getAttemptsMade());

        // third attempt (would be allowed if attemptsAllowed==0) now exceeds limit:
        // attemptsMade will increment to 3 then the method returns false because attemptsMade > attemptsAllowed
        // According to implementation, attemptsMade increments before checking exceed; so result should be false
        assertFalse(p.checkAnswer("2")); // even though correctDoor==2, we've exceeded allowed attempts
        assertEquals(3, p.getAttemptsMade());

        // reset behavior: setting attemptsAllowed to 0 => unlimited
        p.setAttemptsAllowed(0);
        // now a correct attempt should succeed (attemptsMade will increment again)
        assertTrue(p.checkAnswer("2"));
    }

    @Test
    public void testToStringContainsKeyFields() {
        DoorPuzzle p = new DoorPuzzle(5, 4, 3, Difficulty.HARD);
        p.setBlocked(true);
        p.setAttemptsMade(1);
        p.setLocked(true);

        String s = p.toString();
        assertTrue(s.contains("numDoors=5"));
        assertTrue(s.contains("correctDoor=4"));
        assertTrue(s.contains("attemptsAllowed=3"));
        assertTrue(s.contains("attemptsMade=1"));
        assertTrue(s.contains("blocked=true"));
        assertTrue(s.contains("locked=true"));
    }
}

