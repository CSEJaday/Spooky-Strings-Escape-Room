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
    public static void printLeaderboard(List<User> users) {
        List<User> sorted = sortByScoreDesc(users);
        System.out.println("\n=== LEADERBOARD ===");
        if (sorted.isEmpty()) {
            System.out.println("No entries yet.");
            return;
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
    private static String formatSeconds(long seconds) {
        if (seconds < 0) seconds = 0;
        long mins = seconds / 60;
        long sec = seconds % 60;
        return String.format("%d:%02d", mins, sec);
    }
}
