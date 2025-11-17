package com.example;

import java.io.IOException;

import javafx.fxml.FXML;

public class LoginController {

    @FXML
    private void switchToDifficulty() throws IOException {
        App.setRoot("difficulty");
    }

    @FXML
    private void switchToHome() throws IOException {
        App.setRoot("home");
    } 

}