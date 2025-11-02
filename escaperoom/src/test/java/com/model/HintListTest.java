package com.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class HintListTest {

    /**
     * Helper to create a temp file with provided content (UTF-8).
     * The file will be deleted on exit and also attempted to be deleted immediately in the test finally block.
     */
    private Path writeTempHintsFile(String content) throws IOException {
        Path tmp = Files.createTempFile("hints_test_", ".txt");
        Files.write(tmp, content.getBytes(StandardCharsets.UTF_8));
        tmp.toFile().deleteOnExit();
        return tmp;
    }

    @Test
    public void testLoad_nullAndNonexistent() {
        HintList hl = new HintList();
        // null path should return null
        assertEquals(null, hl.load((String) null));

        // non-existent path should return null
        assertEquals(null, hl.load("this_path_should_not_exist_hopefully_12345.txt"));

        // size remains zero
        assertEquals(0, hl.size());

        // getting a hint when none loaded should return null
        assertEquals(null, hl.getHint(1));
        assertEquals(null, hl.getNextHintFor(1, 0));
    }

    @Test
    public void testLoad_validFile_parsesAndProvidesHints() throws IOException {
        String content =
                "1|first hint, second hint  \n" +
                " 2 | onlyone\n" +
                "# a comment line should be ignored because it doesn't match format\n" +
                "3|a, , , b ,c\n" +   // extra commas and spaces -> should parse a, b, c
                "bad|skipme\n" +     // invalid index, skipped
                "\n" +               // empty line ignored
                "4|   single   \n";  // trimming preserved content

        Path tmp = writeTempHintsFile(content);
        try {
            HintList hl = new HintList();
            String loaded = hl.load(tmp.toString());
            // load should return the same path on success
            assertEquals(tmp.toString(), loaded);

            // size should reflect number of valid parsed entries (1,2,3,4) => 4
            assertEquals(4, hl.size());

            // index 1: two hints
            assertEquals("first hint", hl.getNextHintFor(1, 0));
            assertEquals("second hint", hl.getNextHintFor(1, 1));
            assertEquals(null, hl.getNextHintFor(1, 2)); // no third hint

            // index 2: single hint (trimmed)
            assertEquals("onlyone", hl.getNextHintFor(2, 0));
            assertEquals(null, hl.getNextHintFor(2, 1));

            // index 3: should have parsed "a","b","c" (empty pieces ignored)
            assertEquals("a", hl.getNextHintFor(3, 0));
            assertEquals("b", hl.getNextHintFor(3, 1));
            assertEquals("c", hl.getNextHintFor(3, 2));
            assertEquals(null, hl.getNextHintFor(3, 3));

            // index 4: trimmed single
            assertEquals("single", hl.getNextHintFor(4, 0));

            // unknown index returns null
            assertEquals(null, hl.getHint(999));
            assertEquals(null, hl.getNextHintFor(999, 0));
        } finally {
            try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
        }
    }

    @Test
    public void testLoad_fileWithNoValidEntries_returnsNullAndNoCommit() throws IOException {
        // Lines either malformed or with empty hint lists -> no valid entries
        String content =
                "notvalidline\n" +
                "abc|   \n" +    // second part empty after trim -> no entries
                "|noindex\n" +   // missing index
                "\n";

        Path tmp = writeTempHintsFile(content);
        try {
            HintList hl = new HintList();
            String loaded = hl.load(tmp.toString());
            // load should return null because no valid entry parsed
            assertEquals(null, loaded);

            // size should still be zero
            assertEquals(0, hl.size());

            // getNextHintFor should return null
            assertEquals(null, hl.getNextHintFor(1, 0));
        } finally {
            try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
        }
    }

    @Test
    public void testDuplicateIndices_lastOneWins() throws IOException {
        String content =
                "1|one\n" +
                "1|two\n"; // same index repeated; parser stores last into the map

        Path tmp = writeTempHintsFile(content);
        try {
            HintList hl = new HintList();
            String loaded = hl.load(tmp.toString());
            assertEquals(tmp.toString(), loaded);

            // size should be 1 (only index 1)
            assertEquals(1, hl.size());

            // the last line for index 1 ("two") should be the committed Hint
            assertEquals("two", hl.getNextHintFor(1, 0));
            assertEquals(null, hl.getNextHintFor(1, 1));
        } finally {
            try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
        }
    }
}


