package com.model;

/**
 * Represents the difficulty levels available for puzzles or gameplay.
 * Used for filtering, tracking progress, and difficulty-based settings.
 */
public enum Difficulty {
    EASY, MEDIUM, HARD, ALL;

    /**
     * Converts a string value to its corresponding {@link Difficulty} enum.
     * If the input is null or invalid, defaults to {@code EASY}.
     *
     * @param s the string to convert
     * @return the matching {@link Difficulty}, or {@code EASY} if none match
     */
    public static Difficulty fromString(String s) {
        if (s == null) return EASY;
        try {
            return Difficulty.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EASY;
        }
    }
    
}
