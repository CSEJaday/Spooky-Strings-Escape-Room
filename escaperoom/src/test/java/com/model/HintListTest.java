package com.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class HintListTest {

    private Path tmpDir;
    private String originalUserDir;

    @Before
    public void before() throws IOException {
        tmpDir = Files.createTempDirectory("hintlist_test_dir");
        originalUserDir = System.getProperty("user.dir");
    }

    @After
    public void after() throws IOException {
        // restore user.dir
        System.setProperty("user.dir", originalUserDir);
        // cleanup temp dir
        if (tmpDir != null) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(tmpDir)) {
                for (Path p : ds) Files.deleteIfExists(p);
            } catch (IOException ignored) {}
            Files.deleteIfExists(tmpDir);
        }
    }

    private Path writeHints(String name, String content) throws IOException {
        Path file = tmpDir.resolve(name);
        Files.write(file, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return file;
    }

    @Test
    public void testLoadFromExplicitPath_parsesCorrectly() throws IOException {
        String content =
                "1| a , b\n" +
                "2|only\n" +
                "bad|skip\n" +
                "3|x\n";
        Path file = writeHints("hints_explicit.txt", content);

        HintList hl = new HintList();
        String loaded = hl.load(file.toString());
        assertEquals(file.toString(), loaded);
        assertEquals(3, hl.size());
        assertEquals("a", hl.getNextHintFor(1, 0));
        assertEquals("b", hl.getNextHintFor(1, 1));
    }

    @Test
    public void testLoad_defaultCandidates_usesUserDirFileIfPresent() throws IOException {
        // create a hints.txt in the tmpDir which we will set as user.dir
        String content = "5|alpha\n";
        Path hints = writeHints("hints.txt", content);

        // set user.dir so HintList.defaultCandidates will include tmpDir/hints.txt
        System.setProperty("user.dir", tmpDir.toAbsolutePath().toString());

        HintList hl = new HintList();
        String loaded = hl.load();
        assertEquals(hints.toString(), loaded);
        assertEquals(1, hl.size());
        assertEquals("alpha", hl.getNextHintFor(5, 0));
    }

    @Test
    public void testLoad_commitsOnlyIfAtLeastOneValidEntry() throws IOException {
        Path f = writeHints("empty_hints.txt", "badline\n|noIndex\n1|   \n");
        HintList hl = new HintList();
        String loaded = hl.load(f.toString());
        // should return null since no valid entries
        assertEquals(null, loaded);
        assertEquals(0, hl.size());
    }

    @Test
    public void testLoad_handlesIoException_returnsNull() throws IOException {
        // point to a directory (not readable as file) to trigger an IOException in FileReader path
        Path dir = tmpDir.resolve("adir");
        Files.createDirectory(dir);
        HintList hl = new HintList();
        // attempting to load a directory path should return null
        assertEquals(null, hl.load(dir.toString()));
    }
}
