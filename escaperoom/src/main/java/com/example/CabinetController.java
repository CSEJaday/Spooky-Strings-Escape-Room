package com.example;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class CabinetController implements Initializable {

    @FXML private ImageView backgroundImage;
    @FXML private Button settingsButton;
    @FXML private Button inventoryButton;
    @FXML private Button backButton;

    private String previousRoomId = "WitchesDen"; // default fallback

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadBackgroundImage();
    }

    /**
     * SceneManager will call this right after loading FXML.
     */
    public void setContext(String previousRoomId) {
        this.previousRoomId = previousRoomId;
    }

    private void loadBackgroundImage() {
        String path = "/com/example/images/WitchesDenCabinet.png";

        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("CabinetController: Image not found: " + path);
                return;
            }
            backgroundImage.setImage(new Image(is));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ----------------- BUTTON HANDLERS -----------------

    @FXML
    private void onSettings() {
        try {
            SceneManager.getInstance().showSettings(previousRoomId);
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not open Settings.").showAndWait();
        }
    }

    @FXML
    private void onInventory() {
        try {
            SceneManager.getInstance().showInventory(previousRoomId);
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not open Inventory.").showAndWait();
        }
    }

    @FXML
    private void onBack() {
        try {
            SceneManager.getInstance().showRoom(previousRoomId);
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not return.").showAndWait();
        }
    }
}



