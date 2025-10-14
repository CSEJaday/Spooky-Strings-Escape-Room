package com.model;

import java.util.ArrayList;

/**
 * Singleton class that maintains a listing of players and their current scores relative to each other.
 * @author
 */
public class LeaderBoard {
    private static Leaderboard leaderboard;
    private ArrayList<LeaderBoardEntry> entries;
    private ArrayList<User> entries;

    /**
     * Private constructor that builds the leaderboard from data stored in a JSON file
     */
<<<<<<< HEAD
    private Leaderboard() {
        //build ArrayList of users from JSON file
        //Retrieve the data for a user in the JSON file and create a User object.
        User user = new User(name, password, UUID, characterList, settings, progress);
        //add the user to the entries ArrayList
        entries.add(user);

=======
    private LeaderBoard() {
        //build ArrayList of users
        
>>>>>>> 8d259037a6e192cc6408702797ade55f840f91c4
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
     * Creates a new User entry with the data provided and adds it to the ArrayList of players
     * @param name
     * @param score
     */
<<<<<<< HEAD
    public void addEntry(String name, int score) {  //should we use UUID to identify the user?
        // retrieve the user information from the JSON file and update with the new score and level.
        User user = new User(name, score);
        entries.add(user);
=======
    public void addEntry(String name, int score) {
        LeaderBoardEntry entry = new LeaderBoardEntry(name, score);
        entries.add(entry);
>>>>>>> 8d259037a6e192cc6408702797ade55f840f91c4
    }//end addEntry()

    /**
     * Returns an ArrayList of the top specified number of entries in the ArrayList.
     * @param limit
     * @return
     */
<<<<<<< HEAD
    public ArrayList<User> getTopEntries(int limit) {
=======
    public ArrayList<LeaderBoardEntry> getTopEntries(int limit) {
>>>>>>> 8d259037a6e192cc6408702797ade55f840f91c4
        return entries;
    }//end getTopEntries()

    /**
     * 
     */
    public void clear() {
        entries.clear();
    }//end clear
}//end LeaderBoard
