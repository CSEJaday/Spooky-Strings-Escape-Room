package com.example;

import java.io.IOException;
import javafx.fxml.FXML;

/**
 * DifficultyController: sets game-wide chosen difficulty and persists it into the model
 * before navigating to the backstory screens.
 */
public class DifficultyController {

    @FXML
    private void switchToBackstoryEasy() throws IOException {
        GameState.get().setChosenDifficulty(com.model.Difficulty.EASY);
        GameState.get().persistChosenDifficultyToModel(); // <-- persist to current user's Progress
        SceneManager.getInstance().showBackstoryEasy();
    }

    @FXML
    private void switchToBackstoryMedium() throws IOException {
        GameState.get().setChosenDifficulty(com.model.Difficulty.MEDIUM);
        GameState.get().persistChosenDifficultyToModel();
        SceneManager.getInstance().showBackstoryMedium();
    }

    @FXML
    private void switchToBackstoryHard() throws IOException {
        GameState.get().setChosenDifficulty(com.model.Difficulty.HARD);
        GameState.get().persistChosenDifficultyToModel();
        SceneManager.getInstance().showBackstoryHard();
    }

    @FXML
    private void switchToBackstoryNightmare() throws IOException {
        // if "Nightmare" is represented by ALL or another enum value, pick accordingly
        GameState.get().setChosenDifficulty(com.model.Difficulty.ALL);
        GameState.get().persistChosenDifficultyToModel();
        SceneManager.getInstance().showBackstoryNightmare();
    }

    @FXML
    private void switchToLogin() throws IOException {
        SceneManager.getInstance().showLogin();
    }
}



