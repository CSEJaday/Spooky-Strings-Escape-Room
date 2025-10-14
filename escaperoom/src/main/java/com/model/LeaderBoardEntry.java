
public class LeaderBoardEntry implements Comparable<LeaderBoardEntry> 
{
    public String name;
    public int score;

    public LeaderBoardEntry(String name, int score) 
    {
        this.name = name;
        this.score = score;
    }

    public int getScore() 
    {
        return score;
    }

    public int compareTo(LeaderBoardEntry other) 
    {
        return Integer.compare(other.score, this.score); 
    }
}