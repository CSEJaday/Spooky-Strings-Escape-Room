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

/**
 * Generic cabinet controller used for multiple cabinet-like screens.
 * SceneManager.showCabinet(previousRoomId) calls setContext(previousRoomId)
 * which instructs this controller which background image to load.
 */
public class CabinetController implements Initializable {

    @FXML private ImageView backgroundImage;
    @FXML private Button settingsButton;
    @FXML private Button inventoryButton;
    @FXML private Button backButton;

    private String previousRoomId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (backgroundImage == null) System.err.println("CabinetController: backgroundImage == null");
        if (settingsButton == null) System.err.println("CabinetController: settingsButton == null");
        // Buttons will be wired to handlers in FXML.
    }

    /**
     * Called by SceneManager to provide context (the room we came from).
     * The controller uses that to choose which background image to load.
     */
    public void setContext(String previousRoomId) {
        this.previousRoomId = previousRoomId;
        loadBackgroundFor(previousRoomId);
    }

    private void loadBackgroundFor(String roomId) {
        String imageName = imageNameFor(roomId);
        // Try a set of likely paths (mirrors RoomController style)
        String[] candidates = new String[] {
            "/com/example/images/" + imageName,
            "/images/" + imageName,
            "/" + imageName
        };

        boolean loaded = false;
        for (String p : candidates) {
            try (InputStream is = getClass().getResourceAsStream(p)) {
                if (is == null) continue;
                Image img = new Image(is);
                backgroundImage.setImage(img);
                backgroundImage.setFitWidth(1092);
                backgroundImage.setFitHeight(680);
                backgroundImage.setPreserveRatio(false);
                System.out.println("CabinetController: loaded background from " + p);
                loaded = true;
                break;
            } catch (Exception ex) {
                // try next
            }
        }

        if (!loaded) {
            System.err.println("CabinetController: failed to load image for room '" + roomId + "'. Tried:");
            for (String p : candidates) System.err.println("  " + p);
        }
    }

    /**
     * Map previousRoomId (or other keys) to an image filename.
     * Extend this mapping if you have more room-specific cabinet/shelf art.
     */
    private String imageNameFor(String roomId) {
        if (roomId == null) return "WitchesDenCabinet.png";
        switch (roomId) {
            case "WitchesDen": return "WitchesDenCabinet.png";
            case "AlchemyLab": return "AlchemyLabBookShelf.png";
            // add more mappings here if needed
            default: return "WitchesDenCabinet.png";
        }
    }

    @FXML
    private void onSettings() {
        try {
            SceneManager.getInstance().showSettings(previousRoomId);
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Unable to open settings.").showAndWait();
        }
    }

    @FXML
    private void onInventory() {
        try {
            SceneManager.getInstance().showInventory(previousRoomId);
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Unable to open inventory.").showAndWait();
        }
    }

    @FXML
    private void onBack() {
        try {
            SceneManager.getInstance().showRoom(previousRoomId);
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Unable to go back.").showAndWait();
        }
    }
}




