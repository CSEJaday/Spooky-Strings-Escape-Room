package com.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for TriviaPuzzle class (JUnit 4).
 *
 * These tests verify constructor behavior, getters/setters, and
 * correctness of checkAnswer() logic.
 */
public class TriviaPuzzleTest {

    private TriviaPuzzle puzzle;

    @Before
    public void setUp() {
        // Assuming Difficulty is an enum; replace with valid constructor if not
        puzzle = new TriviaPuzzle(
            "What is the capital of France?",
            "Paris",
            "Geography",
            Difficulty.EASY
        );
    }

    @Test
    public void testConstructorSetsFields() {
        assertEquals("Question should be set correctly",
                "What is the capital of France?", puzzle.getQuestion());
        assertEquals("Answer should be set correctly",
                "Paris", puzzle.getAnswer());
        assertEquals("Category should be set correctly",
                "Geography", puzzle.getCategory());
        assertEquals("Difficulty should be set correctly",
                Difficulty.EASY, puzzle.getDifficulty());
    }

    @Test
    public void testSettersUpdateFields() {
        puzzle.setAnswer("PARIS");
        puzzle.setCategory("Travel");

        assertEquals("Answer should be updated", "PARIS", puzzle.getAnswer());
        assertEquals("Category should be updated", "Travel", puzzle.getCategory());
    }

    @Test
    public void testCheckAnswerExactMatch() {
        assertTrue("Exact match should return true",
                puzzle.checkAnswer("Paris"));
    }

    @Test
    public void testCheckAnswerCaseInsensitive() {
        assertTrue("Case-insensitive answer should return true",
                puzzle.checkAnswer("paris"));
    }

    @Test
    public void testCheckAnswerTrimsWhitespace() {
        assertTrue("Whitespace around answer should be ignored",
                puzzle.checkAnswer("  Paris  "));
    }

    @Test
    public void testCheckAnswerIncorrectReturnsFalse() {
        assertFalse("Different answer should return false",
                puzzle.checkAnswer("London"));
    }

    @Test
    public void testCheckAnswerEmptyStringReturnsFalse() {
        assertFalse("Empty string should not be considered correct",
                puzzle.checkAnswer(""));
    }

    @Test
    public void testCheckAnswerNullInputThrowsException() {
        try {
            puzzle.checkAnswer(null);
            fail("checkAnswer(null) should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected: equalsIgnoreCase will throw NPE
        }
    }
}
