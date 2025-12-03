package com.example;

import javafx.fxml.FXML;
import java.io.IOException;

public class InventoryController {
    private String returnRoomId = "DarkFoyer";

    public void setReturnRoom(String roomId) {
        if (roomId != null && !roomId.isEmpty()) this.returnRoomId = roomId;
    }

    @FXML
    private void back() throws IOException {
        SceneManager.getInstance().showRoom(returnRoomId);
    }
}


