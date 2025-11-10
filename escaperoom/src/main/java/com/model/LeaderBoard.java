package com.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Utility methods to build and display a simple leaderboard for {@code User} objects.
 *
 * Provides sorting by score (descending) and formatted printing to stdout.
 * Methods are static helpers and do not maintain state.
 */
public class LeaderBoard {

    /**
     * Return a new list of users sorted by their progress score in descending order.
     *
     * This method clones the provided list to avoid mutating the caller's list.
     * If {@code users} is null, an empty list is returned.
     *
     * @param users list of users to sort; may be null.
     * @return a new {@code List<User>} sorted from highest to lowest score.
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
     * Print a human-friendly leaderboard to stdout.
     *
     * Shows rank, username, score and time spent. If the input list is empty or null,
     * prints a message indicating there are no entries.
     *
     * @param users list of users to display on the leaderboard; may be null or empty.
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
     *
     * @param seconds number of seconds (non-negative preferred).
     * @return formatted string like "4:05" or "0:00".
     */
    private static String formatSeconds(long seconds) {
        if (seconds < 0) seconds = 0;
        long mins = seconds / 60;
        long sec = seconds % 60;
        return String.format("%d:%02d", mins, sec);
    }
}
