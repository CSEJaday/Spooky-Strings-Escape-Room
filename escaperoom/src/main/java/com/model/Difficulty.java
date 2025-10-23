package com.model;

public enum Difficulty {
    EASY, MEDIUM, HARD;

    public static Difficulty fromString(String s) {
        if (s == null) return EASY;
        try {
            return Difficulty.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EASY;
        }
    }
}
