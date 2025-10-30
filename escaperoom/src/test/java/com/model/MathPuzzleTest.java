package com.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for MathPuzzle.
 */
public class MathPuzzleTest {

    @Test
    public void testConstructorInitializesFieldsCorrectly() {
        MathPuzzle p = new MathPuzzle("2 + 2", 4, Difficulty.MEDIUM);

        assertEquals("2 + 2", p.getQuestion());
        assertEquals(Difficulty.MEDIUM, p.getDifficulty());
        assertEquals(4, p.getAnswer());
        assertEquals("2 + 2", p.toString().contains("2 + 2") ? "2 + 2" : p.getQuestion()); // just ensure no error
    }

    @Test
    public void testCheckAnswerWithCorrectAnswer() {
        MathPuzzle p = new MathPuzzle("3 * 3", 9, Difficulty.EASY);
        assertTrue(p.checkAnswer("9"));
        assertTrue(p.checkAnswer(" 9 "));  // whitespace trimmed
    }

    @Test
    public void testCheckAnswerWithIncorrectAnswer() {
        MathPuzzle p = new MathPuzzle("5 + 5", 10, Difficulty.HARD);
        assertFalse(p.checkAnswer("11"));
    }

    @Test
    public void testCheckAnswerWithNonNumericInput() {
        MathPuzzle p = new MathPuzzle("1 + 1", 2, Difficulty.EASY);
        assertFalse(p.checkAnswer("two"));
        assertFalse(p.checkAnswer(""));
        assertFalse(p.checkAnswer(" "));
        assertFalse(p.checkAnswer(null));
    }

    @Test
    public void testCheckAnswerWithLargeNumbers() {
        MathPuzzle p = new MathPuzzle("999999 + 1", 1000000, Difficulty.MEDIUM);
        assertTrue(p.checkAnswer("1000000"));
        assertFalse(p.checkAnswer("1000001"));
    }

    @Test
    public void testGenerateEquationDoesNotThrow() {
        MathPuzzle p = new MathPuzzle("1 + 2", 3, Difficulty.EASY);
        p.generateEquation();  // should not throw even if unimplemented
        assertEquals("1 + 2", p.getQuestion());
    }

    @Test
    public void testInheritedPuzzleBehaviorDefaults() {
        MathPuzzle p = new MathPuzzle("10 - 5", 5, Difficulty.HARD);

        // inherited defaults
        assertFalse(p.isLocked());
        assertNull(p.getReward());
        assertFalse(p.isHiddenHintShown());

        // can set inherited fields
        p.setLocked(true);
        p.setHiddenHint("Subtract properly");
        p.setHiddenHintShown(true);

        assertTrue(p.isLocked());
        assertEquals("Subtract properly", p.getHiddenHint());
        assertTrue(p.isHiddenHintShown());
    }
}
