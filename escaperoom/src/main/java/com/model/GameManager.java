package com.model;

import java.util.*;

public class GameManager {
    private User user;
    private Timer timer;
    private CharacterList characterList;
    private HintList hintList;

    public User login(String username, String password) {
        // find the user data in the JSON file matching the username 
        //check that the password is the same
        //return a User object with the saved data
    
        return null;
    }

    /**
     * Creates a new Users when a new player signs up for the first time. The UUID is randomly generated.
     * @param username - the username this User selects
     * @param password - the password this User selects
     * @return - the User object created for this user.
     */
    public User signup(String username, String password) {
        UUID id = UUID.randomUUID();
        User user = new User(username, password, id);
        addToJSON (user);
    }

    public void playGame() {
    }

    public void pauseGame() {
    }

    public void exitGame() {
    }

    public void updateTime() {
    }

    /**
     * Adds the data for this User to JSON
     * @param user the User object to add to the JSON file
     */
    private void addToJSON(User user){
        try {
            FileWriter writer = new FileWriter
        } catch (Exception e) {
        }
    }
}
