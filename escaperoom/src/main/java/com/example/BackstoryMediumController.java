package com.example;

import java.io.IOException;

import javafx.fxml.FXML;

public class BackstoryMediumController {
    
    @FXML
    private void switchToHome() throws IOException {
        App.setRoot("home");
    }
}