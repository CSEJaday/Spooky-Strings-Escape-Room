package com.example;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the win screen. Buttons call existing SceneManager methods.
 */
public class WinController implements Initializable {

    @FXML private Button viewLeaderboardBtn;
    @FXML private Button continueBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (viewLeaderboardBtn == null) System.err.println("WinController: viewLeaderboardBtn == null");
        if (continueBtn == null) System.err.println("WinController: continueBtn == null");
    }

    @FXML
    private void onViewLeaderboard() {
        try {
            SceneManager.getInstance().showLeaderboard();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Unable to open leaderboard.").showAndWait();
        }
    }

    @FXML
    private void onContinue() {
        try {
            SceneManager.getInstance().showHome();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Unable to return home.").showAndWait();
        }
    }
}
