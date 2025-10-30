package com.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Tests for com.model.UserLoader
 *
 * Notes:
 * - User.fromString(...) in your earlier User implementation throws UnsupportedOperationException.
 *   To avoid that breaking tests we ensure loadUsers() is only exercised when the users.json
 *   file does not exist (so loadUsers returns an empty list).
 *
 * - The tests backup any existing users.json in the current working directory and restore it
 *   after tests so they won't clobber real data.
 */
public class UserLoaderTest {

    private static final String FILENAME = "users.json";
    private Path backup = null;

    @Before
    public void setUp() throws Exception {
        // Backup existing file if present
        Path file = Path.of(FILENAME);
        if (Files.exists(file)) {
            backup = Files.createTempFile("users_backup_", ".json");
            Files.copy(file, backup, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Files.delete(file);
        }

        // Ensure singleton is cleared before each test
        resetSingleton();
    }

    @After
    public void tearDown() throws Exception {
        // Delete the file created by tests (if any)
        Path file = Path.of(FILENAME);
        try {
            if (Files.exists(file)) Files.delete(file);
        } catch (IOException ignored) {}

        // Restore backup if there was one
        if (backup != null && Files.exists(backup)) {
            Files.copy(backup, Path.of(FILENAME), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Files.delete(backup);
            backup = null;
        }

        // Reset singleton again
        resetSingleton();
    }

    @Test
    public void testSingletonInstanceSame() {
        UserLoader l1 = UserLoader.getInstance();
        UserLoader l2 = UserLoader.getInstance();
        assertSame("getInstance should return same singleton instance", l1, l2);
    }

    @Test
    public void testNewInstanceWithNoFileHasEmptyUsers() throws Exception {
        // Ensure file does not exist, reset singleton, then create new instance
        Path file = Path.of(FILENAME);
        if (Files.exists(file)) Files.delete(file);

        resetSingleton();
        UserLoader loader = UserLoader.getInstance();
        assertNotNull("getUsers should not return null", loader.getUsers());
        assertEquals("No users file -> list should be empty", 0, loader.getUsers().size());
    }

    @Test
    public void testAddUserAddsToMemoryAndWritesFile() throws Exception {
        resetSingleton();
        UserLoader loader = UserLoader.getInstance();

        // ensure starting clean
        loader.clearUsers();

        User u = new User("alice", "pw", UUID.randomUUID());
        loader.addUser(u); // this writes the whole users list to FILE_PATH

        // In-memory must contain the user
        assertTrue("in-memory list should contain added user", loader.getUsers().contains(u));

        // file should exist and contain at least one line equal to u.toString()
        Path file = Path.of(FILENAME);
        assertTrue("users.json should exist after addUser", Files.exists(file));

        // read file and ensure the toString representation is present on a line
        boolean found = false;
        try (BufferedReader r = Files.newBufferedReader(file)) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.equals(u.toString())) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue("file should contain the user's toString() line", found);
    }

    @Test
    public void testAddUserToJSONAppendsAndUpdatesMemory() throws Exception {
        resetSingleton();
        UserLoader loader = UserLoader.getInstance();

        // ensure starting clean
        loader.clearUsers();

        User a = new User("bob", "pw1", UUID.randomUUID());
        User b = new User("carol", "pw2", UUID.randomUUID());

        // Use addUserToJSON which appends directly to file and adds to in-memory list
        loader.addUserToJSON(a);
        loader.addUserToJSON(b);

        // memory should contain both
        assertTrue(loader.getUsers().contains(a));
        assertTrue(loader.getUsers().contains(b));
        assertEquals("in-memory list size should be 2", 2, loader.getUsers().size());

        // file should contain two lines corresponding to the appended entries (order preserved)
        Path file = Path.of(FILENAME);
        assertTrue("users.json should exist after addUserToJSON", Files.exists(file));

        int lines = 0;
        try (BufferedReader r = Files.newBufferedReader(file)) {
            while (r.readLine() != null) lines++;
        }
        assertEquals("file should have two lines after two addUserToJSON calls", 2, lines);
    }

    @Test
    public void testClearUsersClearsMemoryAndWritesEmptyFile() throws Exception {
        resetSingleton();
        UserLoader loader = UserLoader.getInstance();

        // add a user via addUser (which will overwrite file)
        loader.clearUsers();
        User u = new User("dave", "pw", UUID.randomUUID());
        loader.addUser(u);
        assertTrue(loader.getUsers().size() >= 1);

        // now clear
        loader.clearUsers();
        assertEquals("after clearUsers in-memory list must be empty", 0, loader.getUsers().size());

        // file should exist (saveUsers overwrote) and be empty (0 lines)
        Path file = Path.of(FILENAME);
        assertTrue("users.json should exist after clearUsers", Files.exists(file));
        try (BufferedReader r = Files.newBufferedReader(file)) {
            assertNull("file should be empty after clearUsers", r.readLine());
        }
    }

    /**
     * Reset the private static 'instance' field on UserLoader so tests get a fresh loader.
     */
    private void resetSingleton() throws Exception {
        Field f = UserLoader.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);
    }
}
