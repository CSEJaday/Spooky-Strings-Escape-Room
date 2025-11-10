package com.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * JUnit 4 tests for CharacterList class.
 * 
 * These tests reset the singleton instance via reflection between runs
 * so state from one test does not affect others.
 */
public class CharacterListTest {

    private CharacterList list;

    @Before
    public void setUp() throws Exception {
        resetSingleton();
        list = CharacterList.getInstance();
    }

    @After
    public void tearDown() throws Exception {
        resetSingleton();
    }

    @Test
    public void testSingletonReturnsSameInstance() {
        CharacterList first = CharacterList.getInstance();
        CharacterList second = CharacterList.getInstance();
        assertSame("getInstance should always return the same instance", first, second);
    }

    @Test
    public void testCharacterInventoryInitiallyEmpty() {
        assertNotNull("Character inventory should not be null", list.getCharacterInventory());
        assertEquals("Inventory should start empty", 0, list.getCharacterInventory().size());
    }

    @Test
    public void testAddCharacterIncreasesInventory() {
        Character hero = new Character("Hero", 0);
        list.addCharacter(hero);

        ArrayList<Character> inventory = list.getCharacterInventory();
        assertEquals("Inventory should contain one character", 1, inventory.size());
        assertTrue("Inventory should include added character", inventory.contains(hero));
    }

    @Test
    public void testGetCharacterByNameFoundCaseInsensitive() {
        Character mage = new Character("Merlin", 0);
        list.addCharacter(mage);

        String resultLower = list.getCharacterByName("merlin");
        String resultUpper = list.getCharacterByName("MERLIN");

        assertTrue("Result should contain the name (case-insensitive search)", 
                   resultLower.contains("Merlin"));
        assertEquals("Both upper and lower case lookups should match", resultLower, resultUpper);
    }

    @Test
    public void testGetCharacterByNameNotFound() {
        Character rogue = new Character("Shadow", 0);
        list.addCharacter(rogue);

        String result = list.getCharacterByName("Unknown");
        assertEquals("Should return 'Character not found' for missing entries", 
                     "Character not found", result);
    }

    @Test
    public void testGetCharacterInventoryIsMutableReference() {
        ArrayList<Character> inventory = list.getCharacterInventory();
        Character knight = new Character("Knight", 0);
        inventory.add(knight);

        // Because getCharacterInventory returns the internal list, changes reflect in CharacterList
        assertTrue("Internal list should reflect external additions", 
                   list.getCharacterInventory().contains(knight));
    }

    // Helper method to reset private static singleton between tests
    private void resetSingleton() throws Exception {
        Field instanceField = CharacterList.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }
}

