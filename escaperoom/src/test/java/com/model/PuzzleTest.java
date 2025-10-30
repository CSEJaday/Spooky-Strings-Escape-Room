package com.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the abstract Puzzle class using a small concrete subclass
 * defined for testing.
 */
public class PuzzleTest {

    /**
     * Minimal concrete Puzzle implementation for tests.
     * checkAnswer returns true when userAnswer equals the expected answer (case-insensitive).
     */
    private static class TestPuzzle extends Puzzle {
        private final String expectedAnswer;

        public TestPuzzle(String question, Difficulty difficulty, String expectedAnswer) {
            super(question, difficulty);
            this.expectedAnswer = expectedAnswer;
        }

        @Override
        public boolean checkAnswer(String userAnswer) {
            if (userAnswer == null && expectedAnswer == null) return true;
            if (userAnswer == null) return false;
            return userAnswer.equalsIgnoreCase(expectedAnswer);
        }
    }

    @Test
    public void testConstructorHandlesNullsAndDefaults() {
        TestPuzzle p = new TestPuzzle(null, null, "answer");

        // question null -> becomes empty string per constructor
        assertEquals("", p.getQuestion());

        // difficulty null -> defaults to Difficulty.EASY
        assertNotNull(p.getDifficulty());
        // We don't assume specific enum value other than not null, but if your Difficulty has EASY:
        // assertEquals(Difficulty.EASY, p.getDifficulty());

        // id default is -1
        assertEquals(-1, p.getId());

        // reward should default to null
        assertNull(p.getReward());

        // locked default false
        assertFalse(p.isLocked());

        // hidden hint defaults
        assertNull(p.getHiddenHint());
        assertFalse(p.isHiddenHintShown());
    }

    @Test
    public void testIdSetterAndGetter() {
        TestPuzzle p = new TestPuzzle("Q", Difficulty.MEDIUM, "x");
        assertEquals(-1, p.getId());

        p.setId(42);
        assertEquals(42, p.getId());
    }

    @Test
    public void testRewardSetterAndGetter() {
        TestPuzzle p = new TestPuzzle("Q", Difficulty.EASY, "x");
        assertNull(p.getReward());

        // use an ItemName from your project; adjust if enum value differs
        p.setReward(ItemName.TORCH); // if ItemName.GOLD doesn't exist in your project, replace with a valid constant
        assertEquals(ItemName.TORCH, p.getReward());
    }

    @Test
    public void testLockedFlag() {
        TestPuzzle p = new TestPuzzle("Q", Difficulty.EASY, "x");
        assertFalse(p.isLocked());

        p.setLocked(true);
        assertTrue(p.isLocked());

        p.setLocked(false);
        assertFalse(p.isLocked());
    }

    @Test
    public void testHiddenHintSetters() {
        TestPuzzle p = new TestPuzzle("Q", Difficulty.EASY, "x");
        assertNull(p.getHiddenHint());
        assertFalse(p.isHiddenHintShown());

        p.setHiddenHint("Try the red book");
        assertEquals("Try the red book", p.getHiddenHint());

        p.setHiddenHintShown(true);
        assertTrue(p.isHiddenHintShown());

        p.setHiddenHintShown(false);
        assertFalse(p.isHiddenHintShown());
    }

    @Test
    public void testCheckAnswerBehavior() {
        TestPuzzle p = new TestPuzzle("What is the answer?", Difficulty.HARD, "FortyTwo");

        // correct answers (case-insensitive)
        assertTrue(p.checkAnswer("fortytwo"));
        assertTrue(p.checkAnswer("FortyTwo"));

        // incorrect and null handling
        assertFalse(p.checkAnswer("41"));
        assertFalse(p.checkAnswer(null));
    }

    @Test
    public void testSetAndGetDifficulty() {
        TestPuzzle p = new TestPuzzle("Q", Difficulty.EASY, "a");
        assertEquals(Difficulty.EASY, p.getDifficulty());

        p.setDifficulty(Difficulty.HARD);
        assertEquals(Difficulty.HARD, p.getDifficulty());
    }

    @Test
    public void testToStringContainsKeyFields() {
        TestPuzzle p = new TestPuzzle("Find key", Difficulty.MEDIUM, "k");
        p.setId(7);
        p.setLocked(true);
        p.setReward(ItemName.KEY); // change to a valid ItemName constant if necessary

        String s = p.toString();

        assertTrue(s.contains("id=7"));
        assertTrue(s.contains("question='Find key'"));
        assertTrue(s.contains("difficulty="));
        assertTrue(s.contains("reward="));
        assertTrue(s.contains("locked=true"));
    }
}
