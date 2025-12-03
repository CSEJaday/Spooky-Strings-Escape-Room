package com.example;

import java.io.IOException;
import javafx.fxml.FXML;

public class DarkFoyerController {

    // These IDs are the backend room identifiers passed to SceneManager.showRoom(...)
    // Change the strings below if your SceneManager expects different IDs.

    @FXML
    private void openWitchesDen() throws IOException {
        SceneManager.getInstance().showRoom("WitchesDen"); // or "witches_den" if that's what you use
    }

    @FXML
    private void openCursedRoom() throws IOException {
        SceneManager.getInstance().showRoom("CursedRoom");
    }

    @FXML
    private void openLockedDoor() throws IOException {
        SceneManager.getInstance().showRoom("LockedDoor");
    }

    @FXML
    private void openHallOfDoorsPart1() throws IOException {
        SceneManager.getInstance().showRoom("HallOfDoorsPart1");
    }

    @FXML
    private void openBackpack() {
        System.out.println("Backpack clicked");
        // TODO: show inventory pane
    }

    @FXML
    private void openSettings() {
        System.out.println("Settings clicked");
        // TODO: show settings dialog
    }

    @FXML
    private void openHelp() {
        System.out.println("Help clicked");
        // TODO: show help/hint UI
    }

    @FXML
    private void switchToHome() {
        try {
            SceneManager.getInstance().showHome();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

