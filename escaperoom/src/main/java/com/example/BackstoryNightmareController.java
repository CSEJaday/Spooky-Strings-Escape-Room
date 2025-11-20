package com.example;

import java.io.IOException;

import javafx.fxml.FXML;

public class BackstoryNightmareController {
    
    @FXML
    private void switchToHome() throws IOException {
        App.setRoot("home");
    }
}
