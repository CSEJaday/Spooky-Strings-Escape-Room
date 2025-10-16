package com.model;

import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;

public class GameManager {
    private User user;
    private Timer timer;
    private CharacterList characterList;
    private HintList hintList;

    public User login(String username, String password) {
        // Simple placeholder login logic
        System.out.println("Attempting login for user: " + username);
        // In a real system, you'd check credentials here
    this.user = new User(username, password, null);
        return this.user;
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

        // Simple placeholder signup logic
        System.out.println("Creating new user: " + username);
        this.user = new User(username, password, null);
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
