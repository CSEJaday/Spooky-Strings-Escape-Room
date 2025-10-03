package com.model;

public class LeaderboardEntry {

    private String playerName;
    private int score;


    public LeaderboardEntry() {
       
    }

    public LeaderboardEntry(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
    }

   
    String getPlayerName() {

        return null;
    }

    public int getScore() {
        
        return 0;
    }

    public int compareTo(LeaderboardEntry other) {

        return 0;
    }
}
