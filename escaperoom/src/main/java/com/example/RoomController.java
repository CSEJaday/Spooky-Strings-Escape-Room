package com.example;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * RoomController for room_view.fxml
 * - loads "<roomId> Screen.png" from resources
 * - creates overlayPane whose size/bounds follow the backgroundImageView so hotspots always align
 * - provides pixel and percentage hotspot helpers
 */
public class RoomController implements Initializable {

    @FXML private AnchorPane rootPane;               // fx:id in FXML
    @FXML private ImageView backgroundImageView;     // fx:id in FXML
    @FXML private StackPane hotspotsLayer;           // fx:id in FXML

    // overlay inside hotspotsLayer used for absolute positioning of hotspots
    private AnchorPane overlayPane;

    private String backendRoomId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (rootPane == null) System.err.println("RoomController.initialize(): rootPane is null");
        if (backgroundImageView == null) System.err.println("RoomController.initialize(): backgroundImageView is null");
        if (hotspotsLayer == null) System.err.println("RoomController.initialize(): hotspotsLayer is null");

        // create overlay and add to hotspotsLayer
        overlayPane = new AnchorPane();
        overlayPane.setPickOnBounds(false);
        overlayPane.setMouseTransparent(false); // accept mouse events for children
        hotspotsLayer.getChildren().add(overlayPane);

        // listen to background image bounds changes and reposition/resize overlay accordingly
        ChangeListener<Bounds> boundsListener = (obs, oldB, newB) -> {
            if (newB == null || overlayPane == null) return;
            // position overlayPane to coincide with image bounds (in parent coordinates)
            Platform.runLater(() -> alignOverlayToImage(newB));
            // reposition any percent-based hotspots (they will be re-laid out by addHotspotPercent which stores no state;
            // here we rely on each addHotspotPercent already using backgroundImageView bounds at creation and on image
            // bounds changes we can iterate children and call layout helper — we do that inside alignOverlayToImage)
        };
        backgroundImageView.boundsInParentProperty().addListener(boundsListener);
        hotspotsLayer.boundsInParentProperty().addListener((o,oldv,newv) -> {
            Bounds b = backgroundImageView.getBoundsInParent();
            if (b != null) Platform.runLater(() -> alignOverlayToImage(b));
        });

        // initial alignment on next pulse
        Platform.runLater(() -> {
            Bounds b = backgroundImageView.getBoundsInParent();
            if (b != null) alignOverlayToImage(b);
        });
    }

    /**
     * Called by SceneManager after loading room_view and obtaining controller instance.
     */
    public void setRoomId(String roomId) {
        this.backendRoomId = roomId;
        Platform.runLater(() -> {
            try {
                loadBackgroundFor(roomId);
                // wait a tick for image to be set and layout to compute, then build hotspots
                Platform.runLater(() -> buildHotspots(roomId));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /**
     * Robust background loader: tries multiple filename variants so small naming differences
     * (spaces, underscores, hyphens, lowercase) won't break things.
     *
     * Example roomId values: "DarkFoyer", "Dark Foyer", "WitchesDen"
     * Example filenames tried: "DarkFoyer Screen.png", "Dark Foyer Screen.png",
     *                         "Dark_Foyer Screen.png", "Dark-Foyer Screen.png",
     *                         "darkfoyer Screen.png", etc.
     */
    private void loadBackgroundFor(String roomId) {
        if (backgroundImageView == null) return;
        // base filename suffix used in your project
        final String suffix = " Screen.png";

        // helper: make "DarkFoyer" -> "Dark Foyer" by inserting spaces before uppercase letters (except first)
        String withSpaces = roomId.replaceAll("(?<=.)(?=[A-Z])", " ");

        // helper lowercased
        String lower = roomId.toLowerCase();

        // build candidate names (most-likely first)
        String[] namesToTry = new String[] {
                roomId + suffix,                     // DarkFoyer Screen.png
                withSpaces + suffix,                 // Dark Foyer Screen.png
                roomId.replace(" ", "_") + suffix,   // DarkFoyer_Screen.png or Dark_Foyer Screen.png
                withSpaces.replace(" ", "_") + suffix,
                roomId.replace(" ", "-") + suffix,
                withSpaces.replace(" ", "-") + suffix,
                lower + suffix,                      // darkfoyer Screen.png
                lower.replace(" ", "_") + suffix,
                lower.replace(" ", "-") + suffix
        };

        String[] candidates = new String[namesToTry.length * 3];
        int idx = 0;
        // try common resource folders you already used
        for (String name : namesToTry) {
            candidates[idx++] = "/com/example/images/" + name;
            candidates[idx++] = "/images/" + name;
            candidates[idx++] = "/" + name;
        }

        InputStream is = null;
        boolean found = false;
        String foundPath = null;
        for (String p : candidates) {
            if (p == null) continue;
            is = getClass().getResourceAsStream(p);
            if (is != null) {
                found = true;
                foundPath = p;
                try {
                    Image img = new Image(is);
                    backgroundImageView.setImage(img);
                    // keep the fixed size you want
                    backgroundImageView.setFitWidth(1092);
                    backgroundImageView.setFitHeight(680);
                    backgroundImageView.setPreserveRatio(false);
                } finally {
                    try { is.close(); } catch (Exception ignored) {}
                }
                System.out.println("RoomController: loaded background from " + p);
                break;
            }
        }

        if (!found) {
            backgroundImageView.setImage(null);
            System.err.println("RoomController: background image not found for '" + roomId + "' (tried these candidates):");
            for (String p : candidates) System.err.println("  " + p);
            System.err.println("Make sure the image file is placed in src/main/resources/com/example/images and spelled exactly.");
        }
    }

    /**
     * Align overlayPane to the background image bounds (so absolute pixel coords match image)
     */
    private void alignOverlayToImage(Bounds imageBounds) {
        if (overlayPane == null || imageBounds == null) return;

        // Set overlayPane size to match image bounds in parent's coordinate system.
        overlayPane.setPrefWidth(imageBounds.getWidth());
        overlayPane.setPrefHeight(imageBounds.getHeight());

        // Position overlayPane inside hotspotsLayer by translating it so its top-left matches imageBounds.minX/Y
        // Use layoutX/layoutY because overlayPane is a direct child of StackPane (centering rules apply)
        overlayPane.setLayoutX(imageBounds.getMinX());
        overlayPane.setLayoutY(imageBounds.getMinY());

        // Re-layout any percent-based hotspots: we place each child that has properties set via userData
        for (javafx.scene.Node n : overlayPane.getChildren()) {
            Object ud = n.getUserData();
            if (ud instanceof HotspotPercent) {
                HotspotPercent hp = (HotspotPercent) ud;
                layoutHotspotPercent(n, hp.px, hp.py, hp.pw, hp.ph);
            }
        }
    }

    /**
     * Clear old hotspots and add new ones for the given roomId.
     * Coordinates tuned to 1092x680 image, but percent hotspots follow image bounds even if scaled.
     */
    private void buildHotspots(String roomId) {
        if (overlayPane == null) {
            System.err.println("buildHotspots: overlayPane is null");
            return;
        }
        overlayPane.getChildren().clear();

        // Add common icons (we prefer percent placement so they track the image)
        // settings (top-right)
        addHotspotPercent(0.912, 0.02, 0.08, 0.11, () -> {
            System.out.println("Settings clicked (room: " + roomId + ")");
            try {
                SceneManager.getInstance().showSettings(roomId);
            } catch (Exception e) { e.printStackTrace(); }
        }, "-fx-background-color: rgba(0,0,0,0.0); -fx-border-color: rgba(0,0,0,0.0);");

        // backpack (left of settings)
        addHotspotPercent(0.927, 0.17, 0.05, 0.11, () -> {
            System.out.println("Backpack clicked (room: " + roomId + ")");
            try {
                SceneManager.getInstance().showInventory(roomId);
            } catch (Exception e) { e.printStackTrace(); }
        }, "-fx-background-color: rgba(0,0,0,0.0);");

        // bottom-center BACK button — go to DarkFoyer (NOT backstory)
        addHotspotPercent(0.50 - (160.0/1092.0)/2.0, 0.91, 160.0/1092.0, 44.0/680.0, () -> {
            try {
                SceneManager.getInstance().showRoom("DarkFoyer");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "-fx-background-color: rgba(0,255,0,0.06); -fx-border-color: transparent;");

        // bottom-right question/help icon (example)
        addHotspotPercent(0.934, 0.86, 50.0/1092.0, 80.0/680.0, () -> {
            try {
                SceneManager.getInstance().showHelp(roomId);
            } catch (Exception e) { e.printStackTrace(); }
        }, "-fx-background-color: rgba(255,255,255,0.0);");

        // Room specific hotspots
        switch (roomId) {
            case "DarkFoyer" -> {
                // Four doors left->right: these coordinates are tuned for 1092x680; change percentages if needed
                // left-most door:
                addHotspotPercent(210.0/1092.0, 180.0/680.0, 160.0/1092.0, 380.0/680.0,
                        () -> safeShowRoom("WitchesDen"),
                        "-fx-background-color: rgba(0,255,0,0.06);");
                // next door
                addHotspotPercent(435.0/1092.0, 180.0/680.0, 160.0/1092.0, 380.0/680.0,
                        () -> safeShowRoom("CursedRoom"),
                        "-fx-background-color: rgba(0,255,0,0.06);");
                // locked door
                addHotspotPercent(680.0/1092.0, 180.0/680.0, 160.0/1092.0, 380.0/680.0,
                        () -> safeShowRoom("LockedDoor"),
                        "-fx-background-color: rgba(0,255,0,0.06);");
                // farthest right -> HallOfDoorsPart1
                addHotspotPercent(930.0/1092.0, 180.0/680.0, 160.0/1092.0, 380.0/680.0,
                        () -> safeShowRoom("HallOfDoorsPart1"),
                        "-fx-background-color: rgba(0,255,0,0.06);");
            }
            case "WitchesDen" -> {
                // WitchesDen: add six magnifying glass hotspots. Use percent positions (tweak until perfect)
                addHotspotPercent(0.07, 0.38, 0.05, 0.06, () -> System.out.println("Painting clicked"), "-fx-background-color: rgba(255,255,0,0.35);");
                addHotspotPercent(0.62, 0.55, 0.05, 0.06, () -> System.out.println("Cabinet clicked"), "-fx-background-color: rgba(255,255,0,0.35);");
                addHotspotPercent(0.84, 0.28, 0.05, 0.06, () -> System.out.println("Skull clicked"), "-fx-background-color: rgba(255,255,0,0.35);");

                // settings/backpack/back already added above (percent), so no duplication necessary
            }
            default -> {
                // fallback debug hotspot top-left
                addHotspotPercent(0.02, 0.02, 24.0/1092.0, 24.0/680.0, () -> System.out.println("Debug hotspot"), "-fx-background-color: rgba(255,0,255,0.12);");
            }
        }

        System.out.println("buildHotspots called for room: " + roomId);
    }

    /** Convenience wrapper that calls SceneManager.showRoom and prints exceptions */
    private void safeShowRoom(String r) {
        try { SceneManager.getInstance().showRoom(r); } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Pixel-based helper — keeps backward compatibility with your old addHotspot calls.
     * left/top/width/height are in pixels relative to the image (1092x680).
     * This converts to a percent and calls addHotspotPercent so hotspots will track image bounds.
     */
    private void addHotspot(double leftPx, double topPx, double widthPx, double heightPx, Runnable action, String style) {
        double px = leftPx / 1092.0;
        double py = topPx / 680.0;
        double pw = widthPx / 1092.0;
        double ph = heightPx / 680.0;
        addHotspotPercent(px, py, pw, ph, action, style);
    }

    /**
     * Percent-based hotspot helper. px,py,pw,ph are fractions 0..1 relative to the image box.
     * The created node's userData is set so we can relayout on image bounds changes.
     */
    private void addHotspotPercent(double px, double py, double pw, double ph, Runnable action, String style) {
        if (overlayPane == null) return;

        Button btn = new Button();
        btn.setText("");
        btn.setFocusTraversable(false);
        btn.setStyle(style == null ? "-fx-background-color: rgba(255,255,0,0.25);" : style);

        // store percent spec so alignOverlayToImage can relayout on resize
        btn.setUserData(new HotspotPercent(px, py, pw, ph));

        btn.setOnAction(e -> {
            try { if (action != null) action.run(); } catch (Throwable t) { t.printStackTrace(); }
        });

        // initial layout (we will compute absolute px/py when image bounds are known)
        overlayPane.getChildren().add(btn);
        Bounds b = backgroundImageView.getBoundsInParent();
        if (b != null) layoutHotspotPercent(btn, px, py, pw, ph);
    }

    /**
     * Compute absolute pixel placement inside overlayPane (which is sized & placed to the image).
     * This positions node by setting AnchorPane.left/top anchors.
     */
    private void layoutHotspotPercent(javafx.scene.Node node, double px, double py, double pw, double ph) {
        if (overlayPane == null) return;
        double overlayW = overlayPane.getPrefWidth();
        double overlayH = overlayPane.getPrefHeight();

        // If overlay's prefWidth/Height are zero (not yet laid out), retrieve actual bounds
        if (overlayW <= 0 || overlayH <= 0) {
            Bounds b = overlayPane.getBoundsInParent();
            if (b != null) {
                overlayW = b.getWidth();
                overlayH = b.getHeight();
            }
        }
        if (overlayW <= 0 || overlayH <= 0) return;

        double left = Math.round(px * overlayW);
        double top = Math.round(py * overlayH);
        double w = Math.round(pw * overlayW);
        double h = Math.round(ph * overlayH);

        if (node instanceof Region) {
            ((Region) node).setPrefWidth(w);
            ((Region) node).setPrefHeight(h);
        }

        AnchorPane.setLeftAnchor(node, left);
        AnchorPane.setTopAnchor(node, top);
    }

    /** small holder so we can re-layout percent-based hotspots when the image changes size */
    private static class HotspotPercent {
        final double px, py, pw, ph;
        HotspotPercent(double px, double py, double pw, double ph) { this.px = px; this.py = py; this.pw = pw; this.ph = ph; }
    }
}







