package com.example;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class WitchesDenController {

    @FXML private ImageView backgroundImageView;

    // called automatically after FXML is loaded
    @FXML
    public void initialize() {
        // optional: log to console that init ran
        System.out.println("WitchesDenController initialized. image = " + backgroundImageView);
    }

    @FXML
    private void openSettings() {
        System.out.println("openSettings clicked");
        // TODO: show settings
    }

    @FXML
    private void openBackpack() {
        System.out.println("openBackpack clicked");
        // TODO: show inventory
    }

    @FXML
    private void goBack() throws IOException {
        SceneManager.getInstance().showRoom("DarkFoyer");
        System.out.println("goBack clicked");
    }

    @FXML
    private void hotspotLeftAction() {
        System.out.println("hotspotLeft clicked");
        // TODO: open left hotspot modal
    }

    @FXML
    private void hotspotCenterAction() {
        System.out.println("hotspotCenter clicked");
    }

    @FXML
    private void hotspotRightAction() {
        System.out.println("hotspotRight clicked");
    }
}

