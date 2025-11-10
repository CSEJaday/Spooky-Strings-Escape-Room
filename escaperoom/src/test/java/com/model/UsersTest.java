package com.model;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class UsersTest {

    // Note: these helper classes must exist in your project:
    // - Character with a constructor Character(String name)
    // - Progress with a default constructor
    // If their constructors differ, adjust below.

    private User userWithNullId;
    private User userFull;
    private UUID explicitId;

    @Before
    public void setUp() {
        // constructor 1: null id -> should generate one
        userWithNullId = new User("alice", "pw123", null);

        // constructor 2: provide lists/settings/progress
        explicitId = UUID.randomUUID();
        ArrayList<Character> chars = new ArrayList<>();
        Character c = new Character("Hero", 0);
        chars.add(c);
        HashMap<String, String> settings = new HashMap<>();
        settings.put("volume", "80");
        Progress prg = new Progress();

        // Note: second constructor signature uses raw HashMap and ArrayList types in User class;
        // Java generics warnings aside, this matches your implementation.
        userFull = new User("bob", "secret", explicitId, chars, settings, prg);
    }

    @Test
    public void testConstructorGeneratesIdWhenNull() {
        assertNotNull("ID should be created when null passed", userWithNullId.getID());
        assertEquals("username should be set", "alice", userWithNullId.getUsername());
        assertEquals("password should be set", "pw123", userWithNullId.getPassword());
    }

    @Test
    public void testSecondConstructorSetsProvidedFieldsOrDefaults() {
        assertEquals("username preserved", "bob", userFull.getUsername());
        assertEquals("password preserved", "secret", userFull.getPassword());
        assertEquals("explicit ID preserved", explicitId, userFull.getID());

        List<Character> chars = userFull.getCharacters();
        assertNotNull("characters list should not be null", chars);
        assertTrue("characters list should contain the provided character", chars.stream().anyMatch(ch -> ch.toString().contains("Hero") || ch.toString().contains("Hero")));

        Map settings = userFull.getSettings();
        assertNotNull("settings should not be null", settings);
        assertTrue("settings should contain provided entry", settings.containsKey("volume"));

        assertNotNull("progress should not be null", userFull.getProgress());
    }

    @Test
    public void testGetCharactersAlwaysNonNullAndMutableFromCaller() {
        User u = new User("carol", "p", UUID.randomUUID());
        List<Character> list = u.getCharacters();
        assertNotNull("getCharacters must never return null", list);

        Character newChar = new Character("Ranger", 0);
        // modify returned list directly and ensure user reflects it (User returns internal list)
        list.add(newChar);
        assertTrue("user's internal characters should contain newly added Character", u.getCharacters().contains(newChar));
    }

    @Test
    public void testAddAndDeleteCharacter() {
        User u = new User("dave", "pw", UUID.randomUUID());
        Character a = new Character("A", 0);
        Character b = new Character("B", 0);

        u.addCharacter(a);
        assertTrue("after addCharacter, a should be present", u.getCharacters().contains(a));
        assertFalse("b not added yet", u.getCharacters().contains(b));

        u.deleteCharacter(a);
        assertFalse("after deleteCharacter, a should be removed", u.getCharacters().contains(a));

        // deleting a not-present character should not throw and should be safe
        u.deleteCharacter(b); // expect no exception
    }

    @Test
    public void testUpdateAndGetSettings() {
        User u = new User("erin", "pw", UUID.randomUUID());
        HashMap<String, Integer> newSettings = new HashMap<>();
        newSettings.put("difficulty", 3);
        u.updateSettings(newSettings);

        HashMap returned = u.getSettings();
        assertNotNull("settings should not be null after update", returned);
        assertTrue("settings should contain updated key", returned.containsKey("difficulty"));
        assertEquals("settings value preserved", 3, ((Integer)returned.get("difficulty")).intValue());
    }

    @Test
    public void testSetAndGetProgressObject() {
        User u = new User("frank", "pw", UUID.randomUUID());
        Progress custom = new Progress();
        u.setProgress(custom);
        assertSame("setProgress should replace the Progress instance", custom, u.getProgress());
    }

    @Test
    public void testGetNameAndGetUsernameConsistency() {
        User u = new User("gina", "pw", UUID.randomUUID());
        assertEquals("getName and getUsername should match", u.getName(), u.getUsername());
    }

    @Test
    public void testToStringContainsKeyFieldsAndPrints() {
        User u = new User("henry", "pass", UUID.randomUUID());
        // capture System.out since toString prints
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(out));
            String s = u.toString(); // this also prints via System.out.println inside toString
            String printed = out.toString();

            assertNotNull("toString should not return null", s);
            assertTrue("toString returned string should contain username", s.contains("henry"));
            assertTrue("returned or printed output should contain username", printed.contains("henry"));
            assertTrue("toString should include password", s.contains("pass"));
            assertNotNull("ID string should be present in toString", s.contains(u.getID().toString()) ? u.getID().toString() : null);
        } finally {
            System.setOut(originalOut);
        }
    }
}
