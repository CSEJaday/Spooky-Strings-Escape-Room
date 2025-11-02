package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class RiddlePuzzleTest {

    private RiddlePuzzle puzzle;

    @Before
    public void setUp() {
        puzzle = new RiddlePuzzle(
                "What has keys but can't open locks?",
                "piano",
                "objects",
                Difficulty.EASY
        );
    }

    @Test
    public void testConstructor_initialState() {
        assertEquals("piano", puzzle.getAnswer());
        assertEquals("objects", puzzle.getCategory());
        assertFalse(puzzle.isSolved());
        // inherited question field from Puzzle superclass
        assertTrue(puzzle.toString().contains("What has keys"));
    }

    @Test
    public void testSetters_updateFields() {
        puzzle.setAnswer("door");
        puzzle.setCategory("home");
        assertEquals("door", puzzle.getAnswer());
        assertEquals("home", puzzle.getCategory());
    }

    @Test
    public void testCheckAnswer_exactMatch_marksSolved() {
        assertTrue(puzzle.checkAnswer("piano"));
        assertTrue(puzzle.isSolved());
    }

    @Test
    public void testCheckAnswer_caseInsensitiveAndTrimmed() {
        assertTrue(puzzle.checkAnswer("  PIANO  "));
        assertTrue(puzzle.isSolved());
    }

    @Test
    public void testCheckAnswer_incorrectDoesNotSolve() {
        assertFalse(puzzle.checkAnswer("violin"));
        assertFalse(puzzle.isSolved());
    }

    @Test
    public void testCheckAnswer_nullUserOrNullAnswer_returnsFalse() {
        puzzle.setAnswer(null);
        assertFalse(puzzle.checkAnswer("anything"));

        puzzle.setAnswer("yes");
        assertFalse(puzzle.checkAnswer(null));
    }

    @Test
    public void testToString_containsAllFields() {
        String s = puzzle.toString();
        assertTrue(s.contains("question="));
        assertTrue(s.contains("answer="));
        assertTrue(s.contains("category="));
    }
}
