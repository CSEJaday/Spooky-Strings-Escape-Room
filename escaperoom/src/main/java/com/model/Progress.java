package com.model;

/**
 * 
 * @author
 */
public class Progress implements Comparable<Progress> {
    private int currentLevel;
    private ArrayList<String> completedPuzzles;
    private int totalScore;
    private long timeSpent;
    private String playerName;
    private int score;

    public Progress() 
    {
        this.currentLevel = 1;
        this.completedPuzzles = new ArrayList<>();
        this.totalScore = 0;
        this.timeSpent = 0;
        this.playerName = "Unknown"; //players username should be here
        this.score = 0;
    }//end constructor

    public void addCompletedPuzzle(String puzzleName) {

    }//end addCompletedPuzzle()

    public void increaseScore(int points) {
        totalScore = totalScore + points;
    }//end increaseScore()

    public void advanceLevel() {

    }//end advanceLevel()

    public void addTime(long seconds) 
    {
        this.timeSpent += seconds;
    }//end addTime()

    public int getCurrentLevel() {
        return currentLevel;
    }//end getCurrentLevel()

    public ArrayList<String> getCompletedPuzzles() {
        return completedPuzzles;
    }//end getCompletedPuzzles()

    public int getTotalScore() {
        return totalScore;
    }//end getTotalScore()

    public long getTimeSpent() 
    {
        return timeSpent;
    }//end getTimeSpent()

    public String getPlayerName() {
        return playerName;
    }//end getPlayerName()

    public int getScore() {
        return score;
    }//end getScore()

    public int compareTo(other) {
        return other;
    }//end compareTo()
    
}//end Progress
