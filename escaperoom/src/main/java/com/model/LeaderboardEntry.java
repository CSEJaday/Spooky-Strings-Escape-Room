package com.model;

public class LeaderBoardEntry {
    private String playerName;
    private int score;

    public LeaderBoardEntry(String playerName, int score) {
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

    public int compareTo(LeaderBoardEntry other)
    {
        if (this.score > other.getScore()) {
            return 1;
            //does 1 mean this user is higher than the other one
        }
        else if (this.score == other.getScore()) {
            return 0;
            //indicates that theyre tied
        }
        else {
            return -1;
            //indicates this player has a lower score
        }
    }
}
