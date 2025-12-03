package com.example;

import java.io.IOException;
import javafx.fxml.FXML;

public class HelpController {
    @FXML
    private void backToDarkFoyer() throws IOException {
        SceneManager.getInstance().showRoom("DarkFoyer");
    }
}


