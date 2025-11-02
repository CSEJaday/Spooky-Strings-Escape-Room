package com.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class ProgressTest {

    private Progress p;

    @Before
    public void setUp() {
        p = new Progress();
    }

    @Test
    public void testConstructor_initialState() {
        // default values
        assertEquals(1, p.getCurrentLevel());
        assertEquals(0L, p.getTimeSpent());
        assertEquals(0, p.getScore());
        // last difficulty defaults to ALL (as code uses Difficulty.ALL)
        assertEquals(Difficulty.ALL, p.getLastDifficultyAsEnum());
        // inventory should be non-null
        assertTrue(p.getInventory() != null);
    }

    @Test
    public void testSetCurrentLevel_coercesToAtLeastOne() {
        p.setCurrentLevel(5);
        assertEquals(5, p.getCurrentLevel());

        p.setCurrentLevel(0); // should coerce to 1
        assertEquals(1, p.getCurrentLevel());

        p.setCurrentLevel(-10); // also coerced
        assertEquals(1, p.getCurrentLevel());
    }

    @Test
    public void testAddTime_ignoresNonPositive() {
        p.addTime(10);
        assertEquals(10L, p.getTimeSpent());

        p.addTime(0);   // ignored
        assertEquals(10L, p.getTimeSpent());

        p.addTime(-5);  // ignored
        assertEquals(10L, p.getTimeSpent());
    }

    @Test
    public void testIncreaseScore_neverBelowZero() {
        p.increaseScore(15);
        assertEquals(15, p.getScore());

        p.increaseScore(-5);
        assertEquals(10, p.getScore());

        p.increaseScore(-20); // would go negative but should floor to 0
        assertEquals(0, p.getScore());
    }

    @Test
    public void testCompletedPuzzleIds_addAndHas_andUnmodifiableView() {
        p.addCompletedPuzzleId(1);
        p.addCompletedPuzzleId(2);
        assertTrue(p.hasCompletedPuzzleId(1));
        assertTrue(p.hasCompletedPuzzleId(2));
        assertFalse(p.hasCompletedPuzzleId(3));
        assertFalse(p.hasCompletedPuzzleId(-1)); // negative ID is considered invalid

        Set<Integer> ids = p.getCompletedPuzzleIds();
        // ensure content is present
        assertTrue(ids.contains(1));
        assertTrue(ids.contains(2));

        // returned set should be unmodifiable
        try {
            ids.add(5);
            // if it didn't throw, fail
            throw new AssertionError("Expected UnsupportedOperationException when modifying returned set");
        } catch (UnsupportedOperationException expected) { /* pass */ }
    }

    @Test
    public void testCompletedPuzzleQuestions_addHasAndListOrder() {
        p.addCompletedPuzzle("What is 2+2?");
        p.addCompletedPuzzle("Name the capital of France");
        assertTrue(p.hasCompletedPuzzleQuestion("What is 2+2?"));
        assertTrue(p.hasCompletedPuzzleQuestion("Name the capital of France"));
        assertFalse(p.hasCompletedPuzzleQuestion("Some other question"));
        assertFalse(p.hasCompletedPuzzleQuestion(null)); // null checks

        List<String> questions = p.getCompletedPuzzles();
        // insertion order should be preserved (LinkedHashSet)
        assertEquals(2, questions.size());
        assertEquals("What is 2+2?", questions.get(0));
        assertEquals("Name the capital of France", questions.get(1));
    }

    @Test
    public void testHintsUsed_incrementAndGetAndUnmodifiableMap() {
        // initially zero for any id
        assertEquals(0, p.getHintsUsedFor(7));

        // increment
        p.incrementHintsUsedFor(7);
        assertEquals(1, p.getHintsUsedFor(7));

        p.incrementHintsUsedFor(7);
        assertEquals(2, p.getHintsUsedFor(7));

        // different id still zero
        assertEquals(0, p.getHintsUsedFor(8));

        Map<Integer, Integer> hints = p.getHintsUsed();
        // map contains the entry for id 7
        assertTrue(hints.containsKey(7));
        assertEquals(Integer.valueOf(2), hints.get(7));

        // returned map should be unmodifiable
        try {
            hints.put(9, 1);
            throw new AssertionError("Expected UnsupportedOperationException when modifying returned map");
        } catch (UnsupportedOperationException expected) { /* pass */ }

        // Ensure subsequent increments are reflected in the unmodifiable view (it's a view)
        p.incrementHintsUsedFor(7);
        assertEquals(Integer.valueOf(3), p.getHintsUsed().get(7));
    }

    @Test
    public void testLastDifficulty_setAndNullIgnored() {
        // default ALL
        assertEquals(Difficulty.ALL, p.getLastDifficultyAsEnum());

        // set a difficulty (use ALL again or another available enum constant)
        p.setLastDifficulty(Difficulty.ALL);
        assertEquals(Difficulty.ALL, p.getLastDifficultyAsEnum());

        // setting null should be ignored (leave as previous)
        p.setLastDifficulty(null);
        assertEquals(Difficulty.ALL, p.getLastDifficultyAsEnum());
    }

    @Test
    public void testInventory_setNull_thenGetCreatesNew() {
        // set inventory to null
        p.setInventory(null);
        // getInventory should create a new non-null inventory
        assertTrue(p.getInventory() != null);

        // set a custom inventory and ensure it is returned
        Inventory inv = new Inventory();
        p.setInventory(inv);
        assertEquals(inv, p.getInventory());
    }

    @Test
    public void testHasCompletedByEither_checksBothIdAndQuestion() {
        p.addCompletedPuzzleId(100);
        p.addCompletedPuzzle("Legacy Q");

        // match by id
        assertTrue(p.hasCompletedByEither(100, null));
        // match by question
        assertTrue(p.hasCompletedByEither(-1, "Legacy Q"));
        // neither matches
        assertFalse(p.hasCompletedByEither(200, "Other Q"));
        // invalid id negative and null question should be false
        assertFalse(p.hasCompletedByEither(-5, null));
    }
}


