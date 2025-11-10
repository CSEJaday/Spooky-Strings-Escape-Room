package com.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the Character class (JUnit4).
 *
 * Tests the two constructors and the getters.
 * Note: Character currently does not override equals() or toString(), and does not validate level.
 * These tests assert the current behavior; if you later add equals()/toString()/validation, update tests accordingly.
 */
public class CharacterTest {

    private Character c1;
    private Character c2;
    private Character cWithAvatar;

    @Before
    public void setUp() {
        // use the two-arg constructor (no avatar)
        c1 = new Character("Alice", 1);
        // three-arg constructor with avatar
        cWithAvatar = new Character("Bob", 5, "avatar_01.png");
        // another instance with same fields as c1 to show identity vs equality
        c2 = new Character("Alice", 1);
    }

    @Test
    public void testConstructorWithoutAvatarSetsFields() {
        assertEquals("Name should be stored", "Alice", c1.getName());
        assertEquals("Level should be stored", 1, c1.getLevel());
        assertNull("Avatar should be null when not provided", c1.getAvatar());
    }

    @Test
    public void testConstructorWithAvatarSetsFields() {
        assertEquals("Name should be stored", "Bob", cWithAvatar.getName());
        assertEquals("Level should be stored", 5, cWithAvatar.getLevel());
        assertEquals("Avatar should be stored", "avatar_01.png", cWithAvatar.getAvatar());
    }

    @Test
    public void testDistinctInstancesAreNotSameAndNotEqualByReference() {
        assertNotSame("Different instances should not be the same object", c1, c2);
        // There is no .equals override, so default equals is identity; assert that explicitly:
        assertFalse("Different instances with same field values should not be equal by default",
                c1.equals(c2));
    }

    @Test
    public void testNegativeLevelIsAccepted() {
        Character negative = new Character("Neg", -3, null);
        assertEquals("Negative level is stored as-is (no validation currently)", -3, negative.getLevel());
    }

    @Test
    public void testLargeLevelValue() {
        Character high = new Character("Max", Integer.MAX_VALUE, "big");
        assertEquals("Large levels should be retrievable", Integer.MAX_VALUE, high.getLevel());
    }
}

