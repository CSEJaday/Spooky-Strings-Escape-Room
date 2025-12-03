package com.example;

import com.model.Difficulty; // adjust package if your Difficulty enum is elsewhere

/**
 * Small app-wide state holder used by the JavaFX UI to share transient choices
 * (currently: chosenDifficulty) between controllers.
 */
public final class GameState {
    private static final GameState INSTANCE = new GameState();

    // default to ALL (or EASY) depending on desired default behaviour
    private com.model.Difficulty chosenDifficulty = com.model.Difficulty.ALL;

    private GameState() {}

    public static GameState get() {
        return INSTANCE;
    }

    public com.model.Difficulty getChosenDifficulty() {
        return chosenDifficulty;
    }

    public void setChosenDifficulty(com.model.Difficulty d) {
        if (d != null) this.chosenDifficulty = d;
    }
}

