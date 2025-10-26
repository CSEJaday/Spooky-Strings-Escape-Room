package com.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Simple leaderboard helper utilities.
 * Provides methods to sort users by score and print a leaderboard.
 */
public class LeaderBoard {

    /**
     * Return a new list of users sorted by progress score in descending order.
     */
    public static List<User> sortByScoreDesc(List<User> users) {
        List<User> copy = new ArrayList<>();
        if (users != null) copy.addAll(users);
        // Use a lambda so type inference is explicit and compatible with Java 17
        copy.sort(Comparator.comparingInt((User u) -> {
            Progress p = u.getProgress();
            return p == null ? 0 : p.getScore();
        }).reversed());
        return copy;
    }

    /**
     * Print a simple leaderboard to stdout for the supplied users list.
     * Shows rank, username, score, and total time (formatted).
     */
<<<<<<< HEAD
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
                return;
            }
=======
    public static void printLeaderboard(List<User> users) {
        List<User> sorted = sortByScoreDesc(users);
        System.out.println("\n=== LEADERBOARD ===");
        if (sorted.isEmpty()) {
            System.out.println("No entries yet.");
            return;
>>>>>>> 2c0099efdaadf9e881fc0f0f3ad451f04ec46680
        }

        int rank = 1;
        for (User u : sorted) {
            Progress p = u.getProgress();
            int score = p == null ? 0 : p.getScore();
            long time = p == null ? 0L : p.getTimeSpent();
            String timeStr = formatSeconds(time);
            System.out.printf("%2d. %s — %d pts (time: %s)%n", rank++, u.getName(), score, timeStr);
        }
    }

    /**
     * Format seconds into M:SS (minutes:seconds).
     */
<<<<<<< HEAD
    public ArrayList<User> getTopEntries(int limit) {
        return entries;
    }//end getTopEntries()

    /**
     * Clears the console
     */
    public void clear() {
        System.out.print("\033[H\033[2J");     
    }//end clear

    public String toString() {
        String returnString = ("Username\t Level\t Score\t Time spent\n");
        for (int i = 0; i < entries.size(); i++) {
            User localUser = entries.get(i);
            returnString += (localUser.getName() + "\t" + localUser.getProgress().getCurrentLevel() + "\t" + 
            localUser.getProgress().getScore() + localUser.getProgress().getTimeSpent() + "\n");
        }
        return returnString;
        
    }//end toString()
}//end LeaderBoard
=======
    private static String formatSeconds(long seconds) {
        if (seconds < 0) seconds = 0;
        long mins = seconds / 60;
        long sec = seconds % 60;
        return String.format("%d:%02d", mins, sec);
    }
}
>>>>>>> 2c0099efdaadf9e881fc0f0f3ad451f04ec46680
