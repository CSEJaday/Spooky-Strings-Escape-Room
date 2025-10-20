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

<<<<<<< HEAD
    // Constructor for users that are already created
    public User(String username, String password, UUID id) 
    {
=======
    /** The user's email address. */
    private String email;

    /**
     * Constructs a new User with the specified username and email.
     *
     * @param username the user's username
     * @param email the user's email address
     */
    public User(String username, String email) {
>>>>>>> 49958707524be3549734851146eabb24250cc901
        this.username = username;
        this.email = email;
    }

<<<<<<< HEAD
    // added overloaded constructor to just read the username and password
    // and create a random id internally for the new account created
    // is this ok?
    public User(String username, String password)
    {
        this.username = username;
        this.password = password;
        this.id = UUID.randomUUID();
        this.characters = new ArrayList<>();
    }

    // Add a character to the user
    public void addCharacter(Character character) 
    {
        if (character != null) 
        {
            characters.add(character);
        }
    }

    // Remove a character by name and avatar
    public boolean deleteCharacter(String name, String avatar) 
    {
        Iterator<Character> iterator = characters.iterator();
        while (iterator.hasNext()) 
        {
            Character character = iterator.next();
            if (character.getName().equals(name) && character.getAvatar().equals(avatar)) 
            {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    // Getters
    public String getUsername() 
    {
        return username;
    }

    public String getPassword() 
    {
        return password;
    }

    public UUID getId() 
    {
        return id;
    }

    public ArrayList<Character> getCharacters() 
    {
        return characters;
    }

    // Characters information output
    @Override
    public String toString() 
    {
        return "User: " + "username: " + username + '\'' + ", id:" + id +
                ", characterCount: " + characters.size();
=======
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
>>>>>>> 49958707524be3549734851146eabb24250cc901
    }
}
