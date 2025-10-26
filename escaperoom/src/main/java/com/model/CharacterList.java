package com.model;

import java.util.*;

public class CharacterList {
    private ArrayList<Character> characters;
    private static CharacterList instance;

    /**
     * Manages a collection of Character objects using the Singleton pattern.
     * Provides global access to the game's character list for lookups and updates.
     */
    public CharacterList() 
    {
        characters = new ArrayList<>();
    }

    /**
     * Returns the single instance of CharacterList.
     *
     * @return the shared CharacterList instance
     */
    public static CharacterList getInstance()
    {
        if (instance == null) 
        {
            instance = new CharacterList();
        }
        return instance;
    }

    /**
     * Returns the list of all characters currently stored.
     *
     * @return list of Character objects
     */
    public ArrayList<Character> getCharacterInventory() 
    {
        return characters;
    }

    /**
     * Adds a character to the list.
     *
     * @param character the Character to add
     */
    public void addCharacter(Character character) 
    {
        characters.add(character);
    }

    /**
     * Finds a character by name (case-insensitive) and returns its string representation.
     *
     * @param name the name of the character to search for
     * @return character description if found, otherwise "Character not found"
     */
    public String getCharacterByName(String name)
    {
        for (Character character : characters)
        {
            if (character.getName().equalsIgnoreCase(name))
            {
                return character.toString();
            }
        }
        return "Character not found";
    }
    

}
