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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * RoomController for room_view.fxml
 * - loads "<roomId> Screen.png" from resources
 * - creates overlayPane whose size/bounds follow the backgroundImageView so hotspots always align
 * - provides pixel and percentage hotspot helpers
 *
 * This version normalizes incoming room ids (strips "PartN", "Screen", collapses separators)
 * and switches on a canonical key so filenames and case variants don't break hotspot selection.
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

    private void loadBackgroundFor(String roomId) {
        if (backgroundImageView == null) return;

        // --- 1. Normalize the roomId: remove Part#, strip "Screen", collapse spaces ---
        String base = roomId == null ? "" : roomId;
        base = base.replaceAll("(?i)screen", "");         // remove "Screen"
        base = base.replaceAll("(?i)part\\d+", "");       // remove Part1 Part2 etc
        base = base.replaceAll("[_\\-]", " ");            // underscores/hyphens become spaces
        base = base.trim();

        // camelCase → spaced words (HallOfDoors → Hall Of Doors)
        base = base.replaceAll("([a-z])([A-Z])", "$1 $2").trim();

        // Title-case version (best guess)
        String titleBase = Arrays.stream(base.split("\\s+"))
                .filter(s -> !s.isBlank())
                .map(s -> s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase())
                .reduce((a,b) -> a + " " + b)
                .orElse(base);

        // --- 2. Assemble candidate filenames in preferred order ---
        String[] names = new String[] {
            roomId + " Screen.png",
            titleBase + " Screen.png",                // Hall Of Doors Screen.png
            titleBase.replace(" ", "") + " Screen.png", // HallOfDoors Screen.png
            base.toLowerCase().replace(" ","") + " screen.png", // hallofdoors screen.png
            base.replace(" ", "_") + " Screen.png",   // Hall_Of_Doors Screen.png
        };

        // --- 3. Try each filename in both resource folders ---
        for (String filename : names) {
            String[] paths = new String[] {
                "/com/example/images/" + filename,
                "/images/" + filename,
                "/" + filename
            };

            for (String p : paths) {
                try (InputStream is = getClass().getResourceAsStream(p)) {
                    if (is != null) {
                        Image img = new Image(is);
                        backgroundImageView.setImage(img);
                        backgroundImageView.setFitWidth(1092);
                        backgroundImageView.setFitHeight(680);
                        backgroundImageView.setPreserveRatio(false);

                        System.out.println("RoomController: loaded background from " + p);
                        return;
                    }
                } catch (Exception ignored) {}
            }
        }

        // --- 4. If nothing found: log all attempted names ---
        System.err.println("RoomController: background image NOT found for '" + roomId + "'");
        System.err.println("Tried:");
        for (String n : names) {
            System.err.println("  /com/example/images/" + n);
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
        addHotspotPercent(0.50 - (160.0/1092.0)/2.0, 0.89, 160.0/1092.0, 44.0/680.0, () -> {
            try {
                SceneManager.getInstance().showRoom("DarkFoyer");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "-fx-background-color: transparent; -fx-border-color: transparent;");

        // bottom-right question/help icon (example)
        addHotspotPercent(0.934, 0.86, 50.0/1092.0, 80.0/680.0, () -> {
            try {
                SceneManager.getInstance().showHelp(roomId);
            } catch (Exception e) { e.printStackTrace(); }
        }, "-fx-background-color: rgba(255,255,255,0.0);");

        // Normalize key so we match logical room names (HallOfDoorsPart1 -> HallOfDoors)
        String key = normalizeRoomKey(roomId);
        boolean matched = true;

        switch (key) {
            case "DarkFoyer" -> {
                addHotspotPercent(210.0/1092.0, 180.0/680.0, 160.0/1092.0, 380.0/680.0,
                        () -> safeShowRoom("WitchesDen"),
                        "-fx-background-color: transparent;");
                addHotspotPercent(435.0/1092.0, 180.0/680.0, 160.0/1092.0, 380.0/680.0,
                        () -> safeShowRoom("CursedRoom"),
                        "-fx-background-color: transparent;");
                addHotspotPercent(680.0/1092.0, 180.0/680.0, 160.0/1092.0, 380.0/680.0,
                        () -> safeShowRoom("LockedDoor"),
                        "-fx-background-color: transparent;");
                addHotspotPercent(930.0/1092.0, 180.0/680.0, 160.0/1092.0, 380.0/680.0,
                        () -> safeShowRoom("HallOfDoorsPart1"),
                        "-fx-background-color: transparent;");
            }
            case "WitchesDen" -> {
                addHotspotPercent(0.07, 0.39, 0.05, 0.06, () -> {
                    try { SceneManager.getInstance().showPuzzle("WitchesDen", 1, "WitchesDen"); }
                    catch (Exception e) { e.printStackTrace(); }
                }, "-fx-background-color: transparent;");
                // cabinet
                addHotspotPercent(0.62, 0.55, 0.05, 0.06, () -> {
                    try { SceneManager.getInstance().showCabinet("WitchesDen"); }
                    catch (Exception e) { e.printStackTrace(); }
                }, "-fx-background-color: transparent; -fx-cursor: hand;");
                addHotspotPercent(0.84, 0.28, 0.05, 0.06, () -> {
                    try { SceneManager.getInstance().showPuzzle("WitchesDen", 0, "WitchesDen"); }
                    catch (Exception e) { e.printStackTrace(); }
                }, "-fx-background-color: transparent;");
            }
            case "CursedRoom" -> {
                addHotspotPercent(0.30, 0.40, 0.06, 0.08, () -> {
                    try { SceneManager.getInstance().showPuzzle("CursedRoom", 0, "CursedRoom"); }
                    catch (Exception e) { e.printStackTrace(); }
                }, "-fx-background-color: transparent;");

                addHotspotPercent(0.72, 0.78, 0.06, 0.08, () -> {
                    try { SceneManager.getInstance().showPuzzle("CursedRoom", 1, "CursedRoom"); }
                    catch (Exception e) { e.printStackTrace(); }
                }, "-fx-background-color: transparent;");
            }

            case "HallOfDoors" -> {
                addHotspotPercent(0.24, 0.37, 0.06, 0.09, () -> {
                    try { SceneManager.getInstance().showPuzzle("HallOfDoors", 1, "HallOfDoors"); }
                    catch (Exception e) { e.printStackTrace(); }
                }, "-fx-background-color: transparent;");

                addHotspotPercent(0.64, 0.49, 0.06, 0.09, () -> {
                    try { SceneManager.getInstance().showPuzzle("HallOfDoors", 0, "HallOfDoors"); }
                    catch (Exception e) { e.printStackTrace(); }
                }, "-fx-background-color: transparent;");

                addHotspotPercent(0.88, 0.42, 0.06, 0.09, () -> {
                    // open Alchemy Lab room (use safeShowRoom to avoid checked exceptions bubble)
                    safeShowRoom("AlchemyLab");
                }, "-fx-background-color: transparent;");
            }

            case "AlchemyLab" -> {
                addHotspotPercent(0.36, 0.51, 0.06, 0.06, () -> {
                    try { SceneManager.getInstance().showPuzzle("AlchemyLab", 1, "AlchemyLab"); }
                    catch (Exception e) { e.printStackTrace(); }
                }, "-fx-background-color: transparent;");

                addHotspotPercent(0.735, 0.815, 0.06, 0.06, () -> {
                    try { SceneManager.getInstance().showPuzzle("AlchemyLab", 0, "AlchemyLab"); }
                    catch (Exception e) { e.printStackTrace(); }
                }, "-fx-background-color: transparent;");

                // bookshelf
                addHotspotPercent(0.51, 0.175, 0.06, 0.08, () -> {
                    try { SceneManager.getInstance().showCabinet("AlchemyLab"); }
                    catch (Exception e) { e.printStackTrace(); }
                }, "-fx-background-color: transparent; -fx-cursor: hand;");

                // Go to "Win Screen"
                addHotspotPercent(0.80, 0.50, 0.11, 0.08, () -> {
                    try { SceneManager.getInstance().showWin(); }
                    catch (Exception e) { e.printStackTrace(); }
                }, "-fx-background-color: transparent;");

            }

            default -> {
                // fallback: intentionally do not add purple debug hotspot when a normalized key exists but we didn't match
                matched = false;
            }
        }

        // ensure overlay stays on top
        Platform.runLater(() -> {
            try { overlayPane.toFront(); } catch (Throwable ignored) {}
        });

        System.out.println("buildHotspots called for room: " + roomId + " (normalized -> " + key + ")");
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

    /**
     * Normalize a backend room id into a canonical key used for matching hotspot sets.
     * Examples:
     *   "HallOfDoorsPart1" -> "HallOfDoors"
     *   "HallOfDoors Part1" -> "HallOfDoors"
     *   "hallofdoors" -> "HallOfDoors"
     */
    private String normalizeRoomKey(String rawRoomId) {
        if (rawRoomId == null) return "";
        String s = rawRoomId;
        // remove "screen" and "part#" (case-insensitive)
        s = s.replaceAll("(?i)screen", "");
        s = s.replaceAll("(?i)part\\d+", "");
        // collapse separators to single space
        s = s.replaceAll("[_\\-]+", " ").trim();
        // preserve camelcase by inserting spaces then remove spaces for compact key
        s = s.replaceAll("([a-z])([A-Z])", "$1 $2").trim();
        // build TitleCase words then join without spaces to produce keys like "HallOfDoors"
        String key = Arrays.stream(s.split("\\s+"))
                .filter(w -> !w.isBlank())
                .map(w -> w.substring(0,1).toUpperCase() + (w.length()>1 ? w.substring(1).toLowerCase() : ""))
                .reduce((a,b) -> a + b)
                .orElse(s);
        return key;
    }
}










