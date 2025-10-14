package com.model;

import java.util.ArrayList;

/**
 * Singleton class that maintains a listing of players and their current scores relative to each other.
 * @author
 */
public class LeaderBoard {
    private static Leaderboard leaderboard;
    private ArrayList<LeaderBoardEntry> entries;

    /**
     * Private constructor that builds the leaderboard from data stored in a JSON file
     */
    private LeaderBoard() {
        //build ArrayList of users
    }//end constructor

    /**
     * Public method that checks for the existence of a LeaderBoard.
     * If one does not exist, it calls the private constructor and creates one and returns it.
     * Otherwise, it returns the current instance of the LeaderBoard
     * @return
     */
    public static Leaderboard getInstance() {
        if (leaderboard == null) {
          leaderboard = new LeaderBoard();  
        } 
        return leaderboard;
    }//end getInstance()

    /**
     * Creates a new LeaderBoardEntry with the data provided and adds it to the ArrayList of players
     * @param name
     * @param score
     */
    public void addEntry(String name, int score) {
        LeaderBoardEntry entry = new LeaderBoardEntry(name, score);
        entries.add(entry);
    }//end addEntry()

    /**
     * Returns an ArrayList of the top specified number of entries in the ArrayList.
     * @param limit
     * @return
     */
    public ArrayList<LeaderBoardEntry> getTopEntries(int limit) {
        return entries;
    }//end getTopEntries()

    /**
     * 
     */
    public void clear() {
        entries.clear();
    }//end clear
}//end LeaderBoard
