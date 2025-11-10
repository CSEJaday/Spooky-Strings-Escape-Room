package com.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for LeaderBoard utility.
 *
 * These tests use a small amount of reflection when constructing User/Progress instances
 * so they are tolerant to minor differences in the project's constructors/setters.
 */
public class LeaderBoardTest {

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

    /**
     * Helper: create a Progress instance by trying several common constructor patterns
     * then falling back to setting fields via reflection.
     */
    private Object createProgress(int score, long timeSpent) {
        try {
            Class<?> progCls = Class.forName("com.model.Progress");

            // try constructor (int score, long timeSpent)
            try {
                Constructor<?> c = progCls.getConstructor(int.class, long.class);
                return c.newInstance(score, timeSpent);
            } catch (NoSuchMethodException ignored) {}

            // try constructor (int score)
            try {
                Constructor<?> c = progCls.getConstructor(int.class);
                Object p = c.newInstance(score);
                // try to set time field via setter or field
                trySetFieldOrSetter(p, "timeSpent", timeSpent);
                trySetFieldOrSetter(p, "time", timeSpent);
                return p;
            } catch (NoSuchMethodException ignored) {}

            // try default constructor then set fields
            try {
                Constructor<?> c = progCls.getConstructor();
                Object p = c.newInstance();
                trySetFieldOrSetter(p, "score", score);
                trySetFieldOrSetter(p, "timeSpent", timeSpent);
                trySetFieldOrSetter(p, "time", timeSpent);
                return p;
            } catch (NoSuchMethodException ignored) {}

        } catch (Exception e) {
            // fall through to return null
        }
        return null;
    }

    /**
     * Helper: create a User instance and attach a Progress instance (may use reflection).
     */
    private Object createUser(String name, Integer score, Long timeSpent) {
        try {
            Class<?> userCls = Class.forName("com.model.User");

            Object user = null;

            // try constructor User(String name)
            try {
                Constructor<?> c = userCls.getConstructor(String.class);
                user = c.newInstance(name);
            } catch (NoSuchMethodException ignored) {}

            // try constructor User(String name, Progress p)
            if (user == null && score != null) {
                try {
                    Class<?> progCls = Class.forName("com.model.Progress");
                    Constructor<?> c = userCls.getConstructor(String.class, progCls);
                    Object prog = createProgress(score, timeSpent == null ? 0L : timeSpent);
                    if (prog != null) user = c.newInstance(name, prog);
                } catch (NoSuchMethodException ignored) {}
            }

            // fallback default constructor
            if (user == null) {
                try {
                    Constructor<?> c = userCls.getConstructor();
                    user = c.newInstance();
                    trySetFieldOrSetter(user, "name", name);
                } catch (NoSuchMethodException ignored) {}
            }

            if (user == null) return null;

            // attach progress if requested
            if (score != null) {
                Object prog = createProgress(score, timeSpent == null ? 0L : timeSpent);
                if (prog != null) {
                    // try setProgress method
                    trySetFieldOrSetter(user, "progress", prog);
                }
            }

            return user;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Try to set a field or call a setter on the target object. Swallows exceptions.
     */
    private void trySetFieldOrSetter(Object target, String name, Object value) {
        if (target == null || name == null || name.isEmpty()) return;
        Class<?> cls = target.getClass();
    
        // Build setter name safely
        char first = name.charAt(0);
        String setter = "set" + java.lang.Character.toUpperCase(first) + name.substring(1);
    
        // Try setter first
        try {
            for (Method m : cls.getMethods()) {
                if (m.getName().equals(setter) && m.getParameterCount() == 1) {
                    m.invoke(target, value);
                    return;
                }
            }
        } catch (Exception ignored) {}
    
        // Then try setting field directly
        try {
            Field f = cls.getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception ignored) {}
    }
    

    @Test
    public void testSortByScoreDescOrdersCorrectlyAndDoesNotMutateOriginal() {
        // create three users: alice(50), bob(200), charlie(null progress)
        Object alice = createUser("alice", 50, 120L);
        Object bob = createUser("bob", 200, 30L);
        Object charlie = createUser("charlie", null, null);

        assertNotNull(alice);
        assertNotNull(bob);
        assertNotNull(charlie);

        @SuppressWarnings("unchecked")
        List<User> input = new ArrayList<>();
        input.add((User) alice);
        input.add((User) bob);
        input.add((User) charlie);

        List<User> sorted = LeaderBoard.sortByScoreDesc(input);

        // original order must be preserved for input (method clones list)
        assertEquals("alice", input.get(0).getName());
        assertEquals("bob", input.get(1).getName());
        assertEquals("charlie", input.get(2).getName());

        // sorted order should be bob (200), alice (50), charlie (0)
        assertEquals("bob", sorted.get(0).getName());
        assertEquals("alice", sorted.get(1).getName());
        assertEquals("charlie", sorted.get(2).getName());

        // ensure returned list is a new list (not same instance)
        assertNotSame(input, sorted);
    }

    @Test
    public void testSortByScoreDescHandlesNullInputAndNullProgress() {
        // null input -> empty list
        List<User> res = LeaderBoard.sortByScoreDesc(null);
        assertNotNull(res);
        assertTrue(res.isEmpty());

        // user with null progress should be treated as score 0
        Object u1 = createUser("u1", null, null);
        Object u2 = createUser("u2", 1, 0L);
        assertNotNull(u1);
        assertNotNull(u2);

        List<User> list = Arrays.asList((User) u1, (User) u2);
        List<User> sorted = LeaderBoard.sortByScoreDesc(list);

        assertEquals("u2", sorted.get(0).getName());
        assertEquals("u1", sorted.get(1).getName());
    }

    @Test
    public void testPrintLeaderboardOutputsFormattedLines() {
        // Create two users with different times
        Object alice = createUser("alice", 10, 65L);   // 1:05
        Object bob = createUser("bob", 20, 125L);      // 2:05

        assertNotNull(alice);
        assertNotNull(bob);

        List<User> users = Arrays.asList((User) alice, (User) bob);
        LeaderBoard.printLeaderboard(users);

        String out = outContent.toString();

        // header present
        assertTrue(out.contains("=== LEADERBOARD ==="));
        // bob should be listed before alice (higher score)
        assertTrue(out.contains("1. bob"));
        assertTrue(out.contains("2. alice"));
        // check formatted times 2:05 and 1:05
        assertTrue(out.contains("(time: 2:05)"));
        assertTrue(out.contains("(time: 1:05)"));
    }

    @Test
    public void testPrintLeaderboardWithEmptyOrNullListPrintsNoEntriesMessage() {
        LeaderBoard.printLeaderboard(new ArrayList<>());
        String out1 = outContent.toString();
        assertTrue(out1.contains("No entries yet."));

        // reset output capture
        outContent.reset();

        LeaderBoard.printLeaderboard(null);
        String out2 = outContent.toString();
        assertTrue(out2.contains("No entries yet."));
    }
}
