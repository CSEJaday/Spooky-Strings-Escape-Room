package com.example;

import java.io.IOException;
import javafx.fxml.FXML;

public class BackstoryNightmareController {

    @FXML
    private void continueToDarkFoyer() {
        try {
            SceneManager.getInstance().showRoom("DarkFoyer");   // <- use the generic room viewer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
