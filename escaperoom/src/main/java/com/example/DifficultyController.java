package com.example;

import java.io.IOException;

import javafx.fxml.FXML;

public class DifficultyController {

    @FXML
    private void switchToBackstoryEasy() throws IOException {
        App.setRoot("backstoryEasy");
    }

    @FXML
    private void switchToBackstoryMedium() throws IOException {
        App.setRoot("backstoryMedium");
    }

    @FXML
    private void switchToBackstoryHard() throws IOException {
        App.setRoot("backstoryHard");
    }

    @FXML
    private void switchToBackstoryNightmare() throws IOException {
        App.setRoot("backstoryNightmare");
    }

    @FXML
    private void switchToLogin() throws IOException {
        App.setRoot("login");
    }
}
