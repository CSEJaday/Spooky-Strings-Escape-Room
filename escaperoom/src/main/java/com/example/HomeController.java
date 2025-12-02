package com.example;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

import java.util.Objects;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.event.EventHandler;

/**
 * HomeController: creates invisible hotspots over the background image
 * and routes clicks to scene manager methods.
 */
public class HomeController {

    @FXML
    private Pane hotspotPane;

    @FXML
    public void initialize() {
        // Ensure pane picks up mouse events on full bounds
        hotspotPane.setPickOnBounds(true);

        // Debug toggle: set -Dhotspot.debug=true when launching the JVM, or set to "true" here.
        boolean debug = Boolean.parseBoolean(System.getProperty("hotspot.debug", "false"));

        /*
        * Hotspot fractions (xFrac, yFrac, wFrac, hFrac) are fractions of pane width/height.
        * These values were tuned for a 1440x900 image:
        *
        * login button   ≈ x=110, y=520, w=260, h=80
        * sign up button ≈ x=400, y=520, w=260, h=80
        * leaderboard    ≈ x=110, y=650, w=600, h=100
        *
        * Fractions calculated as value / 1440 or value / 900.
        */
        addHotspot(0.076, 0.578, 0.181, 0.089, evt -> switchToLogin(), debug);   // login
        addHotspot(0.278, 0.578, 0.181, 0.089, evt -> switchToSignUp(), debug); // sign up
        addHotspot(0.076, 0.722, 0.417, 0.111, evt -> switchToLeaderboard(), debug); // view leaderboard

        // Attach stylesheet at runtime (safe)
        hotspotPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    Scene s = hotspotPane.getScene();
                    if (s != null) {
                        try {
                            String css = Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm();
                            if (!s.getStylesheets().contains(css)) s.getStylesheets().add(css);
                        } catch (Exception e) {
                            System.err.println("styles.css not loaded from HomeController: " + e.getMessage());
                        }
                    }
                });
            }
        });
    }

    private void addHotspot(double xFrac, double yFrac, double wFrac, double hFrac,
        EventHandler<MouseEvent> handler, boolean debug) {
    Rectangle r = new Rectangle();
    r.setFill(debug ? Color.color(1, 0, 0, 0.25) : Color.color(0, 0, 0, 0.0)); // visible only in debug
    r.setStroke(null);

    r.widthProperty().bind(Bindings.createDoubleBinding(
    () -> hotspotPane.getWidth() * wFrac, hotspotPane.widthProperty()));
    r.heightProperty().bind(Bindings.createDoubleBinding(
    () -> hotspotPane.getHeight() * hFrac, hotspotPane.heightProperty()));
    r.xProperty().bind(Bindings.createDoubleBinding(
    () -> hotspotPane.getWidth() * xFrac, hotspotPane.widthProperty()));
    r.yProperty().bind(Bindings.createDoubleBinding(
    () -> hotspotPane.getHeight() * yFrac, hotspotPane.heightProperty()));

    r.setMouseTransparent(false);
    r.setOnMouseClicked(handler);

    hotspotPane.getChildren().add(r);
    }


    // The three navigation actions (they call SceneManager like before)
    private void switchToLogin() {
        try {
            SceneManager.getInstance().showLogin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchToSignUp() {
        try {
            SceneManager.getInstance().showSignUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchToLeaderboard() {
        try {
            SceneManager.getInstance().showLeaderboard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



