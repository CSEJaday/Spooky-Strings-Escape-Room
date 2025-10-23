package com.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

/**
 * Singleton class that maintains a listing of players and their current scores relative to each other.
 * @author
 */
public class LeaderBoard {
    private static LeaderBoard leaderboard;
    private ArrayList<User> entries;

    /**
     * Private constructor that builds the leaderboard from data stored in a JSON file
     */
    private LeaderBoard() {
        //build ArrayList of users from JSON file
        entries = DataLoader.getUsers();
        //sort entries by user score
        //entries.sort(Comparator.comparingInt(User.getProgress().getScore()));
        entries.sort(Comparator.comparing(User::getProgress::getScore));
    }//end constructor

    /**
     * Public method that checks for the existence of a LeaderBoard.
     * If one does not exist, it calls the private constructor and creates one and returns it.
     * Otherwise, it returns the current instance of the LeaderBoard
     * @return
     */
    public static LeaderBoard getInstance() {
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
    public void addEntry(String name, String password, Progress progress) {  
        UUID id = UUID.randomUUID();
        User user = new User(name, password, id);
        user.setProgress(progress);
        entries.add(user);
    }//end addEntry()

    /**
     * Updates a User's progress in the entries ArrayList for the Leaderboard
     * @param name the user's name
     * @param progress the updated progress object for the user
     */
    public void updateEntry(String name, Progress progress) {
        for (int i = 0; i < entries.size(); i++) {
            if (name.equals( entries.get(i).getName())) {
                entries.get(i).setProgress(progress);
                entries.sort(Comparator.comparingInt(User.getProgress().getScore()));
                return 0;
            }
        }
    }//end updateEntry() 

    /**
     * Returns an ArrayList of the top specified number of entries in the ArrayList.
     * @param limit
     * @return
     */
    public ArrayList<User> getTopEntries(int limit) {
        return entries;
    }//end getTopEntries()

    /**
     * Clears the console
     */
    public void clear() {
        System.out.print("\033[H\033[2J");     
    }//end clear
}//end LeaderBoard