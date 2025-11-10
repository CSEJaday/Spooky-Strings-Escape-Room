package com.model;

//import java.util.ArrayList;
//import java.util.HashMap;

/**
 * Simple player character data holder.
 *
 * Holds the player's display name, avatar id (string), and progression level.
 */
public class Character {

    private String name;
    private String avatar;
    private int level;
    //private Inventory inventory;
    //private HashMap<Integer, ArrayList<Hint>> hints;

    /**
     * Construct a Character with a name, level and avatar id.
     *
     * @param name   character name shown in-game.
     * @param level  progression level for the character (non-negative integers expected).
     * @param avatar string representing avatar id or path; may be null.
     */
    public Character(String name, int level, String avatar) 
    {
        this.name = name;
        this.level = level;
        this.avatar = avatar;
    }

    /**
     * Convenience constructor without avatar.
     *
     * @param name  character name.
     * @param level initial level.
     */
    public Character(String name, int level) 
    {
        this(name, level, null);
    }

    /**
     * Get the character name.
     *
     * @return name string.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the character's progression level.
     *
     * @return integer level.
     */
    public int getLevel()
    {
        return level;
    }

    /**
     * Get the avatar identifier or path.
     *
     * @return avatar string, may be null.
     */
    public String getAvatar()
    {
        return avatar;
    }
    /* 
    public void unlockNextHint(int level) 
    {

    }

    public ArrayList<Hint> getAllHintsForLevel(int level) 
    {

        return null;
    }

    public String toString() {
        
        return (name + " with Avatar " + avatar + " is on level " + currentLevel);
    }
        return null;
    } */
}
