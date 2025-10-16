package com.model;

//import java.util.ArrayList;
//import java.util.HashMap;

public class Character {

    private String name;
    private String avatar;
    private int level;
    //private Inventory inventory;
    //private HashMap<Integer, ArrayList<Hint>> hints;

<<<<<<< HEAD
    public Character(String name, String avatar) {
        this.name = name;
        this.avatar = avatar;
=======
    public Character(String name, int level, String avatar) 
    {
        this.name = name;
        this.level = level;
        this.avatar = avatar;
    }

    public Character(String name, int level) 
    {
        this(name, level, null);
    }

    public String getName()
    {
        return name;
    }

    public int getLevel()
    {
        return level;
    }

    public String getAvatar()
    {
        return avatar;
    }
    /* 
    public void unlockNextHint(int level) 
    {
>>>>>>> 8d259037a6e192cc6408702797ade55f840f91c4

    }

    public ArrayList<Hint> getAllHintsForLevel(int level) 
    {

        return null;
    }

    public String toString() {
        
<<<<<<< HEAD
        return (name + " with Avatar " + avatar + " is on level " + currentLevel);
    }
=======
        return null;
    } */
>>>>>>> 8d259037a6e192cc6408702797ade55f840f91c4
}
