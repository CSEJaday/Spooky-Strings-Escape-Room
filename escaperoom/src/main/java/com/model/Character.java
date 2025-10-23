package com.model;

//import java.util.ArrayList;
//import java.util.HashMap;

public class Character {

    private String name;
    private String avatar;
    private int level;
    //private Inventory inventory;
    //private HashMap<Integer, ArrayList<Hint>> hints;

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

    }

    public ArrayList<Hint> getAllHintsForLevel(int level) 
    {

        return null;
    }

    public String toString() {
        
        return null;
    } */

    public static boolean isDigit(char charAt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isDigit'");
    }
}
