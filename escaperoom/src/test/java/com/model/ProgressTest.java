package com.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    public void testConstructor_defaults() {
        assertEquals(1, p.getCurrentLevel());
        assertEquals(0L, p.getTimeSpent());
        assertEquals(0, p.getScore());
        assertEquals(Difficulty.ALL, p.getLastDifficultyAsEnum());
        assertTrue(p.getInventory() != null);
    }

    @Test
    public void testSetCurrentLevel_edgeCases() {
        p.setCurrentLevel(10);
        assertEquals(10, p.getCurrentLevel());

        p.setCurrentLevel(1);
        assertEquals(1, p.getCurrentLevel());

        p.setCurrentLevel(0);
        assertEquals(1, p.getCurrentLevel());

        p.setCurrentLevel(-999);
        assertEquals(1, p.getCurrentLevel());
    }

    @Test
    public void testAddTime_andConcurrency() throws Exception {
        // single-thread behavior
        p.addTime(5);
        assertEquals(5L, p.getTimeSpent());
        p.addTime(0);   // ignored
        p.addTime(-2);  // ignored
        assertEquals(5L, p.getTimeSpent());

        // concurrent adds
        int threads = 20;
        ExecutorService es = Executors.newFixedThreadPool(threads);
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            tasks.add(() -> { p.addTime(1); return null; });
        }
        es.invokeAll(tasks);
        es.shutdown();
        es.awaitTermination(1, TimeUnit.SECONDS);

        // total should have increased by threads
        assertEquals(5L + threads, p.getTimeSpent());
    }

    @Test
    public void testIncreaseScore_andConcurrency_and_floorAtZero() throws Exception {
        p.increaseScore(10);
        assertEquals(10, p.getScore());

        p.increaseScore(-3);
        assertEquals(7, p.getScore());

        // concurrent deltas (mix of positive and negative)
        ExecutorService es = Executors.newFixedThreadPool(10);
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            final int delta = (i % 3 == 0) ? -5 : 2;
            tasks.add(() -> { p.increaseScore(delta); return null; });
        }
        es.invokeAll(tasks);
        es.shutdown();
        es.awaitTermination(2, TimeUnit.SECONDS);

        // score must never be negative
        assertTrue(p.getScore() >= 0);
    }

    @Test
    public void testCompletedPuzzleIds_andImmutability() {
        p.addCompletedPuzzleId(0);
        p.addCompletedPuzzleId(5);
        p.addCompletedPuzzleId(-1); // invalid — ignored
        assertTrue(p.hasCompletedPuzzleId(0));
        assertTrue(p.hasCompletedPuzzleId(5));
        assertFalse(p.hasCompletedPuzzleId(-1));

        Set<Integer> ids = p.getCompletedPuzzleIds();
        assertEquals(2, ids.size());
        try {
            ids.add(99);
            throw new AssertionError("Expected unmodifiable set");
        } catch (UnsupportedOperationException expected) { /* ok */ }
    }

    @Test
    public void testCompletedPuzzleQuestions_orderAndCopy() {
        p.addCompletedPuzzle("A");
        p.addCompletedPuzzle("B");
        p.addCompletedPuzzle(null); // ignored
        List<String> q = p.getCompletedPuzzles();
        assertEquals(2, q.size());
        assertEquals("A", q.get(0));
        assertEquals("B", q.get(1));

        // modifying returned list shouldn't affect internal state
        q.add("X");
        List<String> q2 = p.getCompletedPuzzles();
        assertEquals(2, q2.size());
    }

    @Test
    public void testHintsUsed_incrementAndViewBehavior() {
        assertEquals(0, p.getHintsUsedFor(10));
        p.incrementHintsUsedFor(10);
        p.incrementHintsUsedFor(10);
        assertEquals(2, p.getHintsUsedFor(10));

        Map<Integer,Integer> m = p.getHintsUsed();
        assertEquals(Integer.valueOf(2), m.get(10));

        try {
            m.put(3, 1);
            throw new AssertionError("Expected unmodifiable hints map view");
        } catch (UnsupportedOperationException expected) { /* ok */ }

        // increments after getting view are visible in subsequent calls
        p.incrementHintsUsedFor(10);
        assertEquals(3, p.getHintsUsedFor(10));
    }

    @Test
    public void testInventory_setNullAndGetCreates() {
        p.setInventory(null);
        Inventory inv = p.getInventory();
        assertTrue(inv != null);

        Inventory custom = new Inventory();
        p.setInventory(custom);
        assertEquals(custom, p.getInventory());
    }

    @Test
    public void testHasCompletedByEither_variousCombos() {
        p.addCompletedPuzzleId(42);
        p.addCompletedPuzzle("Legacy-Q");

        assertTrue(p.hasCompletedByEither(42, null));
        assertTrue(p.hasCompletedByEither(1, "Legacy-Q"));
        assertFalse(p.hasCompletedByEither(999, "not"));
        assertFalse(p.hasCompletedByEither(-1, null));
    }
}
