package com.model;

import java.util.*;

public class CharacterList {
    private ArrayList<Character> characters;
    private static CharacterList instance;

    // private constructor for singleton pattern
    public CharacterList() 
    {
        characters = new ArrayList<>();
    }

    // Singleton accessor
    public static CharacterList getInstance()
    {
        if (instance == null) 
        {
            instance = new CharacterList();
        }
        return instance;
    }

    // return all the characters
    public ArrayList<Character> getCharacterInventory() 
    {
        return characters;
    }

    // Add character to the list
    public void addCharacter(Character character) 
    {
        characters.add(character);
    }

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
