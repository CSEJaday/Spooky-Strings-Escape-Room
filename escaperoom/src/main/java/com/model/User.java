package com.model;

/**
 * The User class represents a simple user with a username and email address.
 * It provides methods for converting between User objects and a simple
 * text-based representation.
 *
 */
public class User {
    /** The username. */
    private String username;

    /** The user's email address. */
    private String email;

    /**
     * Constructs a new User with the specified username and email.
     *
     * @param username the user's username
     * @param email the user's email address
     */
    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    /**
     * Returns the username of this user.
     *
     * @return the username as a String
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the email address of this user.
     *
     * @return the email address as a String
     */
    public String getEmail() {
        return email;
    }

    /**
     * Converts this User object into a single line of text
     *
     * @return the user data as a single String line
     */
    @Override
    public String toString() {
        return username + "," + email;
    }

    /**
     * Converts a line of text back into a User object.
     *
     * @param line a line of text representing a user
     * @return a User object, or null if the line is invalid
     */
    public static User fromString(String line) {
        String[] parts = line.split(",");
        if (parts.length == 2) {
            String username = parts[0].trim();
            String email = parts[1].trim();
            return new User(username, email);
        }
        return null;
    }
}
