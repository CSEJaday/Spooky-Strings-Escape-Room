package com.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

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
    private String password;
    private UUID id;
    ArrayList<Character> characters;

    // Constructor for users that are already created
    public User(String username, String password, UUID id) 
    {
        this.username = username;
        this.password = password;
        this.id = id;
        this.characters = new ArrayList<>();
    }


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
        return "User: username=" + username + ", id=" + id + ", characterCount=" + characters.size();
    }


    public static User fromString(String line) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromString'");
    }
}