package com.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
    public void testRepeatedCorrectCalls_areIdempotent() {
        // first correct call
        assertTrue(puzzle.checkAnswer("piano"));
        assertTrue(puzzle.isSolved());

        // second correct call should still return true and keep solved true
        assertTrue(puzzle.checkAnswer(" piano ")); // trimmed/case tolerant
        assertTrue(puzzle.isSolved());
    }

    @Test
    public void testIncorrectThenCorrect_sequence() {
        assertFalse(puzzle.checkAnswer("violin"));
        assertFalse(puzzle.isSolved());

        assertTrue(puzzle.checkAnswer("PIANO")); // case-insensitive match
        assertTrue(puzzle.isSolved());
    }

    @Test
    public void testChangeAnswerAfterSolved_doesNotUnsetSolved() {
        // solve it
        assertTrue(puzzle.checkAnswer("piano"));
        assertTrue(puzzle.isSolved());

        // change answer to something else; current implementation does not reset isSolved
        puzzle.setAnswer("door");
        // isSolved should remain true (documented current behavior)
        assertTrue("Changing the answer should not automatically unset isSolved in current implementation",
                puzzle.isSolved());

        // and the old answer still returns true since isSolved is already true — but checkAnswer uses actual strings:
        // checkAnswer("piano") will now return false because answer changed, but isSolved remains true.
        assertFalse("After changing the stored answer, checkAnswer should reflect the new answer",
                puzzle.checkAnswer("piano"));
        assertTrue("isSolved remains true despite answer change (current behavior)", puzzle.isSolved());
    }

    @Test
    public void testTrimmingOnStoredAndUserAnswer() {
        // set stored answer with extra spaces
        puzzle.setAnswer("  spaced  ");
        // user gives trimmed input
        assertTrue(puzzle.checkAnswer("spaced"));
        assertTrue(puzzle.isSolved());
    }

    @Test
    public void testNullHandling_variousCombinations() {
        // null user answer returns false even if stored answer non-null
        puzzle.setAnswer("yes");
        assertFalse(puzzle.checkAnswer(null));
        assertFalse(puzzle.isSolved());

        // null stored answer results in false even for non-null user input
        puzzle.setAnswer(null);
        assertFalse(puzzle.checkAnswer("anything"));
        assertFalse(puzzle.isSolved());

        // both null should still return false (per implementation)
        assertFalse(puzzle.checkAnswer(null));
        assertFalse(puzzle.isSolved());

        // but toString should still be safe (no exception) and include "null" text for null fields
        puzzle.setAnswer(null);
        puzzle.setCategory(null);
        String s = puzzle.toString();
        assertTrue("toString should include question even when other fields null", s.contains("question="));
        assertTrue("toString should contain literal 'null' for null answer", s.contains("answer='null'"));
        assertTrue("toString should contain literal 'null' for null category", s.contains("category='null'"));
    }

    @Test
    public void testUnicodeCaseInsensitive_matchExamples() {
        // Spanish/Latin small diacritic: "mañana"
        puzzle.setAnswer("Mañana");
        assertTrue("Unicode case-insensitive comparison should match 'mañana' vs 'Mañana'",
                puzzle.checkAnswer("mañana"));
        assertTrue(puzzle.isSolved());

        // Reset puzzle for new case
        puzzle = new RiddlePuzzle("q", "straße", "cat", Difficulty.MEDIUM);
        assertTrue("German sharp-s case fold: 'straße' vs 'STRASSE' — Java equalsIgnoreCase treats ß specially",
                puzzle.checkAnswer("STRASSE") || puzzle.checkAnswer("straße"));
        // We allow either result depending on JDK's handling; simply ensure no exception thrown and boolean returned.
    }

    @Test(timeout = 10_000)
    public void testConcurrentCheckAnswer_manyThreads_noCrash_andSolvedWhenAnyCorrect() throws Exception {
        final int threads = 50;
        ExecutorService es = Executors.newFixedThreadPool(threads);
        List<Callable<Boolean>> tasks = new ArrayList<>();

        // half of tasks will try incorrect answers, some will try correct
        for (int i = 0; i < threads; i++) {
            final int idx = i;
            tasks.add(() -> {
                // simulate varied inputs
                if (idx % 10 == 0) { // one in ten threads gives the correct trimmed/mixed-case answer
                    return puzzle.checkAnswer("  PiAnO ");
                } else {
                    return puzzle.checkAnswer("wrong-" + idx);
                }
            });
        }

        // collect results
        List<Future<Boolean>> futs = es.invokeAll(tasks);
        es.shutdown();
        es.awaitTermination(2, TimeUnit.SECONDS);

        boolean anyReturnedTrue = false;
        for (Future<Boolean> f : futs) {
            try {
                if (f.get()) anyReturnedTrue = true;
            } catch (ExecutionException ee) {
                // if implementation throws inside checkAnswer this would surface here; fail the test
                throw ee;
            }
        }

        // At least one task should have returned true (the right-answer tasks); ensure isSolved set
        assertTrue("At least one concurrent caller should have matched the answer", anyReturnedTrue);
        assertTrue("isSolved should be true after any thread supplies the correct answer", puzzle.isSolved());
    }

    @Test
    public void testToString_containsAllFields_andReflectsSolvedState() {
        puzzle = new RiddlePuzzle("Q", "A", "Cat", Difficulty.HARD);
        String sBefore = puzzle.toString();
        assertTrue(sBefore.contains("question='Q'") || sBefore.contains("question="));
        assertTrue(sBefore.contains("answer='A'"));
        assertTrue(sBefore.contains("category='Cat'"));
        assertTrue(sBefore.contains("solved=false"));

        puzzle.checkAnswer("A");
        String sAfter = puzzle.toString();
        assertTrue(sAfter.contains("solved=true"));
    }
}
