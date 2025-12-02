package com.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.util.Objects;

/**
 * RoomController - handles per-room UI interactions (hotspots, pickups).
 *
 * Drop-in replacement: adjust buildHotspots(...) to integrate with your actual hotspot drawing code.
 */
public class RoomController {

    // backendRoomId is the string id used by your backend for this room (e.g. "DarkFoyer")
    private String backendRoomId;

    /**
     * Called by SceneManager (or whoever) to set which backend room this controller represents.
     *
     * @param backendRoomId backend room id
     */
    public void setRoomId(String backendRoomId) {
        this.backendRoomId = backendRoomId;
        // Build hotspots / UI for the room immediately
        Platform.runLater(() -> {
            try {
                buildHotspots(backendRoomId);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /**
     * Example stub: (re)create hotspots/UI elements for the supplied room id.
     *
     * Replace or expand this method with your actual code that places clickable
     * hotspots, binds them to actions (pickup/use/open), and so on.
     *
     * @param backendRoomId backend id for the room to render
     */
    private void buildHotspots(String backendRoomId) {
        // TODO: integrate with your existing UI hotspot code.
        // This stub exists so pickupItem can call it to refresh the scene after pickup.
        System.out.println("buildHotspots called for room: " + backendRoomId);
    }

    /**
     * Display an informational dialog to the player.
     * Replaces calls to a gameService dialog to make this controller self-contained.
     *
     * @param title dialog title
     * @param msg   dialog message
     */
    private void showInfoDialog(String title, String msg) {
        Platform.runLater(() -> {
            try {
                Alert a = new Alert(AlertType.INFORMATION);
                a.setTitle(title == null ? "Info" : title);
                a.setHeaderText(null);
                a.setContentText(Objects.toString(msg, ""));
                a.showAndWait();
            } catch (Throwable t) {
                // If UI fails (headless/test) fallback to stdout
                System.out.println((title == null ? "" : title + ": ") + msg);
            }
        });
    }

    /**
     * Attempt to pick up an item and add it to the current player's inventory.
     *
     * This method:
     *  - normalizes the incoming itemId and tries to resolve it to an enum com.model.ItemName
     *  - constructs a com.model.Item and adds it to the current user's Inventory
     *  - persists users via com.model.DataLoader.saveUsers(...)
     *  - refreshes room hotspots on the FX thread
     *
     * @param itemId textual id/name of the item (e.g. "KEY", "master_key", "Key")
     */
    private void pickupItem(String itemId) {
        if (itemId == null || itemId.trim().isEmpty()) {
            showInfoDialog("Pickup", "No item specified to pick up.");
            return;
        }

        try {
            // Normalize token (prefer enum-style names)
            String norm = itemId.trim().toUpperCase().replaceAll("[^A-Z0-9]", "_");

            // 1) Try direct enum lookup
            com.model.ItemName foundName = null;
            try {
                foundName = com.model.ItemName.valueOf(norm);
            } catch (IllegalArgumentException ignored) {
            }

            // 2) Fallback to scanning enum values for a close match
            if (foundName == null) {
                for (com.model.ItemName n : com.model.ItemName.values()) {
                    if (n.name().equalsIgnoreCase(itemId)
                            || n.name().toLowerCase().contains(itemId.toLowerCase())
                            || itemId.toLowerCase().contains(n.name().toLowerCase())) {
                        foundName = n;
                        break;
                    }
                }
            }

            if (foundName == null) {
                // Unknown item type: inform user and return
                showInfoDialog("Picked up", "Picked up item (unknown type): " + itemId);
                return;
            }

            // 3) Construct a com.model.Item.
            com.model.Item newItem;
            try {
                // Prefer the simple constructor (Item(ItemName, String))
                newItem = new com.model.Item(foundName, "Picked up from room: " + backendRoomId);
            } catch (Throwable t) {
                // Fallback to full constructor if available (Item(ItemName, String, boolean, boolean, String))
                newItem = new com.model.Item(foundName, "Picked up from room: " + backendRoomId, false, false, "");
            }

            // 4) Locate current user's inventory and add the item
            com.model.User currentUser = null;
            try {
                // Try UserLoader first (if available in your project)
                try {
                    com.model.UserLoader ul = com.model.UserLoader.getInstance();
                    currentUser = ul.getCurrentUser();
                    if (currentUser == null) {
                        // sometimes the API differs
                        try { currentUser = ul.getLoggedInUser(); } catch (Throwable ignored) {}
                    }
                } catch (Throwable ignored) { /* no UserLoader or it failed */ }
            } catch (Throwable ignored) {}

            // fallback to UserList
            if (currentUser == null) {
                try {
                    com.model.UserList ul2 = com.model.UserList.getInstance();
                    currentUser = ul2.getCurrentUser();
                    if (currentUser == null) {
                        try { currentUser = ul2.getLoggedInUser(); } catch (Throwable ignored) {}
                    }
                } catch (Throwable ignored) {}
            }

            // If still null, add to the first user in the list (best-effort)
            if (currentUser == null) {
                try {
                    com.model.UserList ul3 = com.model.UserList.getInstance();
                    if (!ul3.getAllUsers().isEmpty()) currentUser = ul3.getAllUsers().get(0);
                } catch (Throwable ignored) {}
            }

            if (currentUser == null) {
                // No user available; still show picked up but cannot persist
                showInfoDialog("Picked up", "Picked up: " + foundName.name() + " (no active user to attach to).");
            } else {
                com.model.Inventory inv = currentUser.getProgress() != null ? currentUser.getProgress().getInventory() : null;
                if (inv == null) {
                    // If inventory is null try to ensure progress exists and get inventory
                    if (currentUser.getProgress() == null) currentUser.setProgress(new com.model.Progress());
                    inv = currentUser.getProgress().getInventory();
                }

                if (inv != null) {
                    inv.addItem(newItem);
                    showInfoDialog("Picked up", "Picked up: " + foundName.name());

                    // Persist all users (DataLoader used elsewhere in your project)
                    try {
                        com.model.UserList ul = com.model.UserList.getInstance();
                        com.model.DataLoader.saveUsers(ul.getAllUsers());
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                } else {
                    showInfoDialog("Picked up", "Picked up: " + foundName.name() + " (inventory not available).");
                }
            }

            // 5) Refresh UI/hotspots on FX thread
            Platform.runLater(() -> {
                try {
                    // If you have a helper to map backend->frontend room name, use it;
                    // otherwise re-build the hotspots for the current backendRoomId directly.
                    try {
                        // Try to use backend helper if available
                        String frontendRoom = backendRoomId;
                        try {
                            // If com.model.GameService.mapBackendToFrontendRoom exists, use it
                            frontendRoom = com.model.GameService.mapBackendToFrontendRoom(backendRoomId);
                        } catch (Throwable ignored) {}
                        buildHotspots(frontendRoom);
                    } catch (Throwable t) {
                        // final fallback: build by backendRoomId
                        buildHotspots(backendRoomId);
                    }
                } catch (Throwable ignored) {
                }
            });

        } catch (Throwable e) {
            e.printStackTrace();
            showInfoDialog("Pickup error", "Picked up item (unexpected error).");
        }
    }

    // ---------- Example public API to trigger pickup from the UI ----------
    // You can call this from a hotspot event handler (button click, etc).
    // e.g. pickupButton.setOnAction(evt -> onPickupButton("KEY"));
    @FXML
    private void onPickupExample() {
        // Example: pick up a KEY when some UI hotspot is clicked.
        pickupItem("KEY");
    }
}


