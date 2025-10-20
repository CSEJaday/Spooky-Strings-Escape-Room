package com.model;

import java.util.Timer;
import java.util.TimerTask;
<<<<<<< HEAD
=======
import java.util.UUID;
import java.io.FileWriter;
import java.util.ArrayList;
>>>>>>> 49958707524be3549734851146eabb24250cc901

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
        User user = new User(username, password);
        return user;
    }

    public User getCurrentUser()
    {
        return this.user;
    }

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

    public void pauseGame() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            System.out.println("Game paused.");
        }
    }

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
    }
}
