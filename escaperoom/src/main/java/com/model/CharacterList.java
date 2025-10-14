package com.model;

import java.util.*;

public class CharacterList {
    private ArrayList<Character> characters;

    public CharacterList() {
        characters = new ArrayList<>();
    }

    public void addCharacter(Character c) {
    }

    public ArrayList<Character> getCharacterInventory() {
        return characters;
    }

    public Character getCharacterByUsername(String name) {
        return null;
    }
    public String toString() 
    {
        return name + " (" + avatar + ") - Level " + currentLevel;
}

}
