package com.example;

import java.io.IOException;
import javafx.fxml.FXML;

public class DifficultyController {

    @FXML
    private void switchToBackstoryEasy() throws IOException {
        GameState.get().setChosenDifficulty(com.model.Difficulty.EASY);
        SceneManager.getInstance().showBackstoryEasy();
    }

    @FXML
    private void switchToBackstoryMedium() throws IOException {
        GameState.get().setChosenDifficulty(com.model.Difficulty.MEDIUM);
        SceneManager.getInstance().showBackstoryMedium();
    }

    @FXML
    private void switchToBackstoryHard() throws IOException {
        GameState.get().setChosenDifficulty(com.model.Difficulty.HARD);
        SceneManager.getInstance().showBackstoryHard();
    }

    @FXML
    private void switchToBackstoryNightmare() throws IOException {
        GameState.get().setChosenDifficulty(com.model.Difficulty.ALL);
        SceneManager.getInstance().showBackstoryNightmare();
    }

    @FXML
    private void switchToLogin() throws IOException {
        SceneManager.getInstance().showLogin();
    }
}


