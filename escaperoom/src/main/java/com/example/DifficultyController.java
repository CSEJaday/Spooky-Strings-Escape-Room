package com.example;

import java.io.IOException;

import javafx.fxml.FXML;

public class DifficultyController {

    @FXML
    private void switchToBackstoryEasy() throws IOException {
        App.setRoot("backstory");
    }

    @FXML
    private void switchToBackstoryMedium() throws IOException {
        App.setRoot("backstory");
    }

    @FXML
    private void switchToBackstoryHard() throws IOException {
        App.setRoot("backstory");
    }

    @FXML
    private void switchToBackstoryNightmare() throws IOException {
        App.setRoot("backstory");
    }

    @FXML
    private void switchToLogin() throws IOException {
        App.setRoot("login");
    }
}
