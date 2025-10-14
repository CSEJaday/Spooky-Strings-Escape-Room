package com.model;

import java.util.Timer;
import java.util.*;

public class GameManager {
    private User user;
    private Timer timer;
    private CharacterList characterList;
    private HintList hintList;

    public User login(String username, String password) {
        return null;
    }

    public User signup(String username, String password) {
        return null;
    }

    public void playGame() {
    }

    public void pauseGame() {
        if (timer != null) 
        {
            timer.cancel(); 
            timer = null;
            System.out.println("Game paused.");
        }
    }

    public void exitGame() {
    }

    public void updateTime() {
    }
}
