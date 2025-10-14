
package com.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Represents a user with login credentials, a unique ID, and a list of characters.
 */
public class User {
    private String username;
    private String password;
    private UUID id;
    private ArrayList<Character> characters;

    // Constructor
    public User(String username, String password, UUID id) {
        this.username = username;
        this.password = password;
        this.id = id;
        this.characters = new ArrayList<>();
    }

    // Add a character to the user
    public void addCharacter(Character character) {
        if (character != null) {
            characters.add(character);
        }
    }

    // Remove a character by name and avatar
    public boolean deleteCharacter(String name, String avatar) {
        Iterator<Character> iterator = characters.iterator();
        while (iterator.hasNext()) {
            Character character = iterator.next();
            if (character.getName().equals(name) && character.getAvatar().equals(avatar)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public UUID getId() {
        return id;
    }

    public ArrayList<Character> getCharacters() {
        return characters;
    }

    // Optional: Debugging output
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", id=" + id +
                ", characterCount=" + characters.size() +
                '}';
    }
}

