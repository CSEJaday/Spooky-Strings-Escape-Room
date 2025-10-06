package com.model;

/**
 * 
 */
public class CharacterMaker {
    /**
     * 
     * @param name
     * @param avatar
     * @return
     */
    public Character createCharacter(String name, String avatar) {
        return new Character(name, avatar);
    }//end createCharacter()
}//ened CharacterMaker
