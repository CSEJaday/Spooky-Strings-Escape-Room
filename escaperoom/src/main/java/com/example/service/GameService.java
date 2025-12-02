package com.example.service;

import com.model.Inventory;
import com.model.Progress;
import com.model.User;
import com.model.UserLoader;
import com.model.UserList;

/**
 * GameService helper wired to backend package com.model
 * - Maps frontend/backed names
 * - Provides small UI helpers
 * - Provides inventory access methods using your backend classes.
 *
 * Note: This method tries several common access paths and falls back to null if none work.
 */
public class GameService {
    private static final GameService INSTANCE = new GameService();
    public static GameService getInstance() { return INSTANCE; }

    public static String mapFrontendToBackendRoom(String frontendId) {
        switch (frontendId) {
            case "WitchesDen": return "DarkFoyer"; // your backend still calls it DarkFoyer
            case "Nightmare": return "ALL";        // frontend Nightmare -> backend ALL
            default: return frontendId;
        }
    }

    public static String mapBackendToFrontendRoom(String backendId) {
        switch (backendId) {
            case "DarkFoyer": return "DarkFoyer";
            case "ALL": return "Nightmare";
            default: return backendId;
        }
    }

    public void showInfoDialog(String message) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            a.setHeaderText(null);
            a.setContentText(message);
            a.showAndWait();
        });
    }

    public void showLockedDialog(String message) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            a.setHeaderText(null);
            a.setContentText(message);
            a.showAndWait();
        });
    }

    /**
     * Returns current inventory using available backend entry points.
     * Tries (in order):
     *  1) Progress.getCurrentProgress().getInventory()
     *  2) UserLoader.getInstance().getCurrentUser().getProgress().getInventory()
     *  3) UserList.getInstance().getCurrentUser().getProgress().getInventory()
     *
     * If none are available, returns null.
     */
    public Inventory getCurrentInventory() {
        // 1) Try Progress.getCurrentProgress()
        try {
            Progress p = Progress.getCurrentProgress(); // many projects expose a static getter; if absent this will throw
            if (p != null) {
                Inventory inv = p.getInventory();
                if (inv != null) return inv;
            }
        } catch (Throwable t) {
            // ignore, try next strategy
        }

        // 2) Try UserLoader singleton
        try {
            UserLoader loader = UserLoader.getInstance();
            if (loader != null) {
                // common names: getCurrentUser() or getLoggedInUser()
                User u = null;
                try { u = loader.getCurrentUser(); } catch (Throwable ignored) {}
                try { if (u == null) u = loader.getLoggedInUser(); } catch (Throwable ignored) {}
                try { if (u == null) u = loader.getUser(); } catch (Throwable ignored) {}
                if (u != null) {
                    try {
                        Progress up = u.getProgress();
                        if (up != null) {
                            Inventory inv = up.getInventory();
                            if (inv != null) return inv;
                        }
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable t) {
            // ignore and try next
        }

        // 3) Try UserList singleton
        try {
            UserList ul = UserList.getInstance();
            if (ul != null) {
                User u = null;
                try { u = ul.getCurrentUser(); } catch (Throwable ignored) {}
                try { if (u == null) u = ul.getLoggedInUser(); } catch (Throwable ignored) {}
                if (u != null) {
                    try {
                        Progress up = u.getProgress();
                        if (up != null) {
                            Inventory inv = up.getInventory();
                            if (inv != null) return inv;
                        }
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable t) {
            // final fallback
        }

        // couldn't locate an inventory
        return null;
    }

    /**
     * Returns true if player's inventory contains a key. Checks ItemName enums via Inventory.has(...)
     * If Inventory can't be found returns false.
     */
    public boolean playerHasKey() {
        Inventory inv = getCurrentInventory();
        if (inv == null) return false;
        try {
            // Try common ItemName enum values (master_key, KEY, MASTER_KEY)
            // We don't reference ItemName directly here; Inventory.has accepts ItemName enum,
            // but we can check quantities map by iterating templates or quantities if needed.
            // Simpler: iterate through Inventory.getQuantities() if available
            try {
                java.util.Map<?, Integer> quantities = inv.getQuantities();
                if (quantities != null) {
                    for (Object k : quantities.keySet()) {
                        if (k == null) continue;
                        String name = k.toString().toLowerCase();
                        if (name.contains("key")) return true;
                    }
                }
            } catch (Throwable ignored) {
            }

            // Fallback: try known item names via Inventory.has if ItemName enum is accessible
            try {
                // try common enum constants using reflection
                Class<?> itemNameClass = Class.forName("com.model.ItemName");
                for (Object enumConst : itemNameClass.getEnumConstants()) {
                    String nm = enumConst.toString().toLowerCase();
                    if (nm.contains("key")) {
                        // call inv.has(enumConst)
                        try {
                            java.lang.reflect.Method hasMethod = inv.getClass().getMethod("has", itemNameClass);
                            Object res = hasMethod.invoke(inv, enumConst);
                            if (res instanceof Boolean && (Boolean) res) return true;
                        } catch (Throwable ignored) {}
                    }
                }
            } catch (Throwable ignored) {}

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }
}

