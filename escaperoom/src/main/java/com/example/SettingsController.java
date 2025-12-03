package com.example;

import javafx.fxml.FXML;
import java.io.IOException;

public class SettingsController {
    private String returnRoomId = "DarkFoyer";

    public void setReturnRoom(String roomId) {
        if (roomId != null && !roomId.isEmpty()) this.returnRoomId = roomId;
    }

    @FXML
    private void logout() throws IOException {
        // navigate to Home / login screen, depending on your flow
        SceneManager.getInstance().showHome();
    }

    @FXML
    private void back() throws IOException {
        // go back to the room we were in
        SceneManager.getInstance().showRoom(returnRoomId);
    }
}


