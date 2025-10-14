package com.model;
import java.util.ArrayList;
/**
 * 
 * @author
 */
public class Progress {
    private int currentLevel;
    private ArrayList<String> completedPuzzles;
    //private int totalScore;
    private long timeSpent;
    private String playerName;      //is this the same as username?
    private int score;

    /*
     * Constructor for the Progress object that tracks a user's progress through the game.
     * @param level - the level the player is at in the game. If this is a first time user, progress is set to 0
     * @param puzzles - the ArrayList of Strings that contains the puzzles the user has completed. If this is the first
     *                  time creating Progress, this will be null
     * @param score - the current number of points this user has earned. For new users, this will be 0
     * @param time - the long value of the time the user has spent solving puzzles in the game. For new users, this will be 0
     * @param name - this is the username for the player  - might want to use UUID?
     */
    public Progress(int level, ArrayList<String> puzzles, int score, long time, String name) {
        currentLevel = level;
        completedPuzzles = puzzles;
        timeSpent = time;
        playerName = name;  //should we use UUID instead?
        this.score = score;

    }//end constructor

    /*
     * Adds a new puzzle name to the list of completed puzzles.
     * @param puzzleName the name of the newly completed puzzle.
     */
    public void addCompletedPuzzle(String puzzleName) {
        completedPuzzles.add(puzzleName);   //check to see if we have to verify that there is space in the ArrayList

    }//end addCompletedPuzzle()

    /* Increases the user's score by the provided amount. This will be used when a user completes a level and is 
    awarded points 
    @param points - the number of points to add to the current score.
    */
    public void increaseScore(int points) {
        score = score + points;
    }//end increaseScore()

    /*  Advances the level by one. To be used when the user completes a level and moves forward in the game
    */
    public void advanceLevel() {
        currentLevel++;

    }//end advanceLevel()

    /*
        Adds time to the local timeSpent tracking. This would be called when a user is either penalized with a time penalty,
        pauses a level in progress, or when a user completes a level and needs to  update their progress
    */
    public void addTime(long seconds) {
        timeSpent = timeSpent + seconds;

    }//end addTime()

    /**
     * Returns the current level of player progress.
     * @return an int value representing the level of progress.
     * 
     */
    public int getCurrentLevel() {
        return currentLevel;
    }//end getCurrentLevel()

    /**
     * Returns the list of completed puzzles
     * @return an ArrayList of Strings containing the names of the completed puzzles
     */
    public ArrayList<String> getCompletedPuzzles() {
        return completedPuzzles;
    }//end getCompletedPuzzles()

    /**
     * REMOVE THIS FROM UML
     */
    //public int getTotalScore() {
        //return score;
    //}//end getTotalScore()

    /** 
     * Returns the total time spent working through puzzles
     * @return a long with the total time spent working through puzzles
     */
    public long getTimeSpent() {
        return timeSpent;
    }//end getTimeSpent()

    /**
     * Returns the player name who owns this progress
     * @return The name of the player who owns this progress instance
     */
    public String getPlayerName() {
        return playerName;
    }//end getPlayerName()

    /**
     * Returns the Score for this progress instance
     * @return an int representing the number of points earned
     */
    public int getScore() {
        return score;
    }//end getScore()

    /**
     * Compares the score for this progress instance to that of the other User provided. Returns a 1 if this score is higher.
     * Returns 0 if the scores are equal. Returns -1 if the other User has a higher score.
     * @param other - the User object who's progress will be compared to this progress
     * @return
     */
    public int compareTo(User other) {  //might want to pass in Progress and have the calling code get the progress from the User
        int value = 0;
        if (this.score > other.getProgress().getScore()){
            value = 1;
        } else if (this.score < other.getProgress().getScore())
            value = -1;
        return value;
    }//end compareTo()
    
}//end Progress
