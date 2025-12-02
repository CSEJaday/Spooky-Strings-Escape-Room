package com.example;

import java.io.IOException;
import javafx.fxml.FXML;

public class BackstoryNightmareController {

    @FXML
    private void switchToHome() {
        try {
            SceneManager.getInstance().showHome();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
