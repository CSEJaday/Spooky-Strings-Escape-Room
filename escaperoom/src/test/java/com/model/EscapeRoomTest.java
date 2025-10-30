package com.model;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for EscapeRoom using a small concrete TestPuzzle subclass
 * because Puzzle is abstract.
 */
public class EscapeRoomTest {

    /**
     * Minimal concrete Puzzle implementation for tests.
     * checkAnswer returns true iff the provided answer equals expectedAnswer (case-insensitive).
     */
    private static class TestPuzzle extends Puzzle {
        private final String expectedAnswer;

        public TestPuzzle(String question, Difficulty difficulty) {
            this(question, difficulty, null);
        }

        public TestPuzzle(String question, Difficulty difficulty, String expectedAnswer) {
            super(question, difficulty);
            this.expectedAnswer = expectedAnswer;
        }

        @Override
        public boolean checkAnswer(String userAnswer) {
            if (expectedAnswer == null && userAnswer == null) return true;
            if (expectedAnswer == null) return false;
            if (userAnswer == null) return false;
            return expectedAnswer.equalsIgnoreCase(userAnswer);
        }
    }

    @Test
    public void testConstructorWithoutPuzzles() {
        EscapeRoom room = new EscapeRoom("Lab", "A spooky lab", 2);

        assertEquals("Lab", room.getName());
        assertEquals("A spooky lab", room.getDescription());
        assertEquals(2, room.getLevel());
        assertFalse(room.isSolved());
        assertNull(room.getPuzzles());
    }

    @Test
    public void testConstructorWithPuzzles() {
        List<Puzzle> puzzles = new ArrayList<>();
        puzzles.add(new TestPuzzle("Riddle", Difficulty.EASY, "Solve me!"));

        EscapeRoom room = new EscapeRoom("Library", "Books everywhere", 3, puzzles);

        assertEquals("Library", room.getName());
        assertEquals("Books everywhere", room.getDescription());
        assertEquals(3, room.getLevel());
        assertFalse(room.isSolved());
        assertEquals(1, room.getPuzzles().size());
        assertEquals("Riddle", room.getPuzzles().get(0).getQuestion());
    }

    @Test
    public void testGetPuzzleReturnsFirstPuzzle() {
        List<Puzzle> puzzles = new ArrayList<>();
        puzzles.add(new TestPuzzle("Puzzle1", Difficulty.EASY));
        puzzles.add(new TestPuzzle("Puzzle2", Difficulty.EASY));

        EscapeRoom room = new EscapeRoom("TestRoom", "desc", 1, puzzles);
        Puzzle firstPuzzle = room.getPuzzle();

        assertNotNull(firstPuzzle);
        assertEquals("Puzzle1", firstPuzzle.getQuestion());
    }

    @Test
    public void testGetPuzzleReturnsNullWhenEmpty() {
        EscapeRoom room = new EscapeRoom("EmptyRoom", "No puzzles", 1, new ArrayList<>());
        assertNull(room.getPuzzle());
    }

    @Test
    public void testGetPuzzleReturnsNullWhenListIsNull() {
        EscapeRoom room = new EscapeRoom("NullRoom", "Null puzzles", 1, null);
        assertNull(room.getPuzzle());
    }

    @Test
    public void testSetSolvedChangesState() {
        EscapeRoom room = new EscapeRoom("Hall", "A long hallway", 1);
        assertFalse(room.isSolved());

        room.setSolved(true);
        assertTrue(room.isSolved());
    }

    @Test
    public void testSetPuzzlesUpdatesList() {
        EscapeRoom room = new EscapeRoom("Garage", "Fix the car", 2);
        List<Puzzle> puzzles = new ArrayList<>();
        puzzles.add(new TestPuzzle("Engine Puzzle", Difficulty.MEDIUM));

        room.setPuzzles(puzzles);
        assertEquals(1, room.getPuzzles().size());
        assertEquals("Engine Puzzle", room.getPuzzles().get(0).getQuestion());
    }

    @Test
    public void testToStringContainsAllInfo() {
        List<Puzzle> puzzles = new ArrayList<>();
        puzzles.add(new TestPuzzle("Key Puzzle", Difficulty.MEDIUM));
        EscapeRoom room = new EscapeRoom("Vault", "Locked tight", 4, puzzles);

        String result = room.toString();
        assertTrue(result.contains("Vault"));
        assertTrue(result.contains("level=4"));
        assertTrue(result.contains("puzzles=1"));
        assertTrue(result.contains("isSolved=false"));
    }
}

