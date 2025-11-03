package com.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.junit.Assert.*;

/**
 * Tests for EscapeRoomGameUI focusing on private helpers and deterministic behavior.
 *
 * Notes:
 * - Uses reflection to invoke private methods and to set the private Scanner input stream.
 * - Does NOT run the main loop or any persistent leaderboard writes.
 */
public class EscapeRoomGameUITest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    @Before
    public void setUpStreams() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    public void testFormatSeconds() throws Exception {
        Method format = EscapeRoomGameUI.class.getDeclaredMethod("formatSeconds", long.class);
        format.setAccessible(true);

        assertEquals("0:00", (String) format.invoke(null, -5L));
        assertEquals("0:00", (String) format.invoke(null, 0L));
        assertEquals("0:05", (String) format.invoke(null, 5L));
        assertEquals("2:05", (String) format.invoke(null, 125L));
        assertEquals("10:00", (String) format.invoke(null, 600L));
    }

    @Test
    public void testComputeHintPenaltySecondsScaledMode() throws Exception {
        Method m = EscapeRoomGameUI.class.getDeclaredMethod("computeHintPenaltySeconds", String.class);
        m.setAccessible(true);

        assertEquals(30, ((Integer) m.invoke(null, "easy")).intValue());
        assertEquals(60, ((Integer) m.invoke(null, "medium")).intValue());
        assertEquals(120, ((Integer) m.invoke(null, "hard")).intValue());
        // unknown difficulty falls back to default 30 (per code)
        assertEquals(30, ((Integer) m.invoke(null, "unknown")).intValue());
    }

    @Test
    public void testGetPointsForDifficulty() throws Exception {
        Method m = EscapeRoomGameUI.class.getDeclaredMethod("getPointsForDifficulty", String.class);
        m.setAccessible(true);

        assertEquals(10, ((Integer) m.invoke(null, (Object) null)).intValue());        // null -> easy default
        assertEquals(10, ((Integer) m.invoke(null, "easy")).intValue());
        assertEquals(20, ((Integer) m.invoke(null, "medium")).intValue());
        assertEquals(30, ((Integer) m.invoke(null, "hard")).intValue());
        assertEquals(10, ((Integer) m.invoke(null, "nonsense")).intValue());           // unknown -> easy default
        assertEquals(10, ((Integer) m.invoke(null, "EASY")).intValue());              // case-insensitive via toLowerCase
    }

    @Test
    public void testAskDifficultyReadsFromInjectedScanner() throws Exception {
        // Prepare an instance and inject a Scanner that returns "2" (Medium)
        EscapeRoomGameUI ui = new EscapeRoomGameUI();

        Field inField = EscapeRoomGameUI.class.getDeclaredField("in");
        inField.setAccessible(true);
        Scanner testScanner = new Scanner(new ByteArrayInputStream("2\n".getBytes()));
        // override the private final field
        inField.set(ui, testScanner);

        Method ask = EscapeRoomGameUI.class.getDeclaredMethod("askDifficulty");
        ask.setAccessible(true);
        Object res = ask.invoke(ui);
        assertNotNull(res);
        assertEquals(Difficulty.MEDIUM, res);
    }

    @Test
    public void testPrintPuzzleIntroFallbackByType_riddleVariants() throws Exception {
        // Create a UI instance to invoke the instance method on
        EscapeRoomGameUI ui = new EscapeRoomGameUI();

        // Riddle with keyword "zombie"
        RiddlePuzzle r1 = new RiddlePuzzle("A zombie stalks the halls", "brains", null, Difficulty.EASY);
        RiddlePuzzle r2 = new RiddlePuzzle("A spoo-key riddle about keys", "key", null, Difficulty.EASY);
        RiddlePuzzle r3 = new RiddlePuzzle("Some other riddle", "x", null, Difficulty.EASY);

        Method fallback = EscapeRoomGameUI.class.getDeclaredMethod("printPuzzleIntroFallbackByType", Puzzle.class);
        fallback.setAccessible(true);

        // r1: contains "zombie" -> undead message
        outContent.reset();
        fallback.invoke(ui, r1);
        String out1 = outContent.toString();
        assertTrue(out1.toLowerCase().contains("undead") || out1.toLowerCase().contains("shuffling"));

        // r2: contains "spoo-key" or 'key opens a haunted house' path -> key backstory
        outContent.reset();
        fallback.invoke(ui, r2);
        String out2 = outContent.toString();
        assertTrue(out2.toLowerCase().contains("rusted key") || out2.toLowerCase().contains("rusted"));

        // r3: generic riddle fallback
        outContent.reset();
        fallback.invoke(ui, r3);
        String out3 = outContent.toString();
        assertTrue(out3.toLowerCase().contains("riddles") || out3.toLowerCase().contains("daring you"));
    }

    @Test
    public void testPrintPuzzleIntroFallbackByType_otherTypes() throws Exception {
        EscapeRoomGameUI ui = new EscapeRoomGameUI();
        Method fallback = EscapeRoomGameUI.class.getDeclaredMethod("printPuzzleIntroFallbackByType", Puzzle.class);
        fallback.setAccessible(true);

        // DoorPuzzle fallback
        DoorPuzzle dp = new DoorPuzzle(3);
        outContent.reset();
        fallback.invoke(ui, dp);
        String out = outContent.toString();
        assertTrue(out.toLowerCase().contains("hallway") || out.toLowerCase().contains("doors"));

        // MathPuzzle fallback
        MathPuzzle mp = new MathPuzzle("1+1", 2, Difficulty.MEDIUM);
        outContent.reset();
        fallback.invoke(ui, mp);
        String outM = outContent.toString();
        assertTrue(outM.toLowerCase().contains("symbols") || outM.toLowerCase().contains("numbers"));

        // TriviaPuzzle fallback
        TriviaPuzzle tp = new TriviaPuzzle("Capital?", "Paris", "geo", Difficulty.EASY);
        outContent.reset();
        fallback.invoke(ui, tp);
        String outT = outContent.toString();
        assertTrue(outT.toLowerCase().contains("dusty tome") || outT.toLowerCase().contains("knowledge"));
    }

    @Test
    public void testLoadHintsFromFilePopulatesHintsMap() throws Exception {
        // Create a temp hints file with a few well-formed lines and some malformed ones
        Path tmp = Files.createTempFile("hints-test-", ".txt");
        String content = ""
                + "1|first hint, second hint\n"
                + "2|only one hint\n"
                + "notanint|bad line\n"
                + "3|   \n"                   // empty hints should be ignored
                + "4|a, b, , c\n";
        Files.write(tmp, content.getBytes());
        tmp.toFile().deleteOnExit();

        EscapeRoomGameUI ui = new EscapeRoomGameUI();

        Method loadMethod = EscapeRoomGameUI.class.getDeclaredMethod("loadHintsFromFile", String.class);
        loadMethod.setAccessible(true);
        loadMethod.invoke(ui, tmp.toString());

        // read private hints map
        Field hintsField = EscapeRoomGameUI.class.getDeclaredField("hints");
        hintsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Integer, List<String>> hints = (Map<Integer, List<String>>) hintsField.get(ui);

        assertNotNull(hints);
        assertTrue(hints.containsKey(1));
        assertEquals(2, hints.get(1).size());
        assertTrue(hints.containsKey(2));
        assertEquals(1, hints.get(2).size());
        // 3 should not appear (empty list)
        assertFalse(hints.containsKey(3));
        assertTrue(hints.containsKey(4));
        assertEquals(3, hints.get(4).size()); // "a","b","c" -> empty tokens ignored
    }
}


