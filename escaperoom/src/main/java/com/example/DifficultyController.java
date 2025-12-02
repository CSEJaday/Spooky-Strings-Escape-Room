package com.example;

import java.io.IOException;
import javafx.fxml.FXML;

public class DifficultyController {

    @FXML
    private void switchToBackstoryEasy() throws IOException {
        SceneManager.getInstance().showBackstoryEasy();
    }

    @FXML
    private void switchToBackstoryMedium() throws IOException {
        SceneManager.getInstance().showBackstoryMedium();
    }

    @FXML
    private void switchToBackstoryHard() throws IOException {
        SceneManager.getInstance().showBackstoryHard();
    }

    @FXML
    private void switchToBackstoryNightmare() throws IOException {
        SceneManager.getInstance().showBackstoryNightmare();
    }

    @FXML
    private void switchToLogin() throws IOException {
        SceneManager.getInstance().showLogin();
    }
}

