package com.model;

import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;

/**
 * 
 * @author
 */
public class GameManager {
    private User user;
    private Timer timer;
    private CharacterList characterList;
    private HintList hintList;

    /**
     * 
     * @param username
     * @param password
     * @return
     */
    public User login(String username, String password) {
        // find the user data in the JSON file matching the username 
        //check that the password is the same
        //return a User object with the saved data
    
        return null;
    }

    /**
<<<<<<< HEAD
     * 
     * @param username
     * @param password
     * @return
=======
     * Creates a new Users when a new player signs up for the first time. The UUID is randomly generated.
     * @param username - the username this User selects
     * @param password - the password this User selects
     * @return - the User object created for this user.
>>>>>>> 377cb9732b672968dc17d60a67d52e5877576a8e
     */
    public User signup(String username, String password) {
        UUID id = UUID.randomUUID();
        User user = new User(username, password, id);
        addToJSON (user);
    }

    /**
     * 
     */
    public void playGame() {
        System.out.println("Game started.");
        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateTimeUI();
                }
            }, 0, 1000); // Update every second
        }
    }

    /**
     * 
     */
    public void pauseGame() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            System.out.println("Game paused.");
        }
    }

<<<<<<< HEAD
    /**
     * 
     */
    public void exitGame() {
    }

    /**
     * 
     */
    public void updateTime() {
=======
    public void quitGame() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        System.out.println("Game exited.");
    }

    public void updateTimeUI() {
        // Just a simple console message for now
        System.out.println("Updating time UI...");
>>>>>>> 377cb9732b672968dc17d60a67d52e5877576a8e
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
