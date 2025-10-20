package com.model;
import java.util.Random;

/**
 * @author
 */
public class DoorPuzzle {
    private int correctDoor;
    private int numDoors;
    private int attempts;

    /**
     * 
     * @param numDoors
     */
    public DoorPuzzle(int numDoors) 
    {
        this.numDoors = numDoors;
        this.correctDoor = new Random().nextInt(numDoors) + 1;
        this.attempts = 0;
    }

    /*
     * 
     */
    public boolean guessDoor(int choice) 
    {
        attempts++;
        return choice == correctDoor;
    }

    /**
     * 
     * @return
     */
    public int getAttempts() 
    {
        return attempts;
    }

    /**
     * 
     * @return
     */
    public String getHint() 
    {
        return "Only one door will help you make it through! Mwah-ha-ha";
    }

    /**
     * 
     * @return
     */
    public int getNumDoors()
    {
        return numDoors;
    }
    
}
