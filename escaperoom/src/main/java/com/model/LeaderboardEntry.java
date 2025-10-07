package com.model;

public class LeaderboardEntry {
    private String playerName;
    private int score;

    public LeaderboardEntry(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
    }//end constructor

    public String getPlayerName()
    {
        return playerName;
    }

    public int getScore()
    {
        return score;
    }

    public int compareTo(LeaderboardEntry other)
    {
        return Integer.compare(other.score, this.score);
    }

}
