package com.example;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * SceneManager singleton for switching scenes.
 * Tries multiple resource locations so it works whether FXMLs are under /fxml/ or /com/example/.
 */
public class SceneManager {
    private static SceneManager instance;
    private final Stage stage;
    private final int WIDTH = 1092;
    private final int HEIGHT = 680;

    private SceneManager(Stage stage) {
        this.stage = stage;
    }

    public static void init(Stage stage) {
        instance = new SceneManager(stage);
    }

    public static SceneManager getInstance() {
        if (instance == null) throw new IllegalStateException("SceneManager not initialized. Call SceneManager.init(stage) first.");
        return instance;
    }

    /**
     * Attempts to locate an FXML resource under multiple candidate paths.
     * Priority:
     *   1) /fxml/<name>.fxml
     *   2) /com/example/<name>.fxml
     *
     * Throws an IOException with helpful diagnostics if nothing is found.
     */
    private Parent loadFXMLFlexible(String resourceName) throws IOException {
        String[] candidates = new String[] {
                "/fxml/" + resourceName + ".fxml",
                "/com/example/" + resourceName + ".fxml"
        };

        URL found = null;
        String foundPath = null;
        for (String p : candidates) {
            URL u = getClass().getResource(p);
            if (u != null) {
                found = u;
                foundPath = p;
                break;
            }
        }

        if (found == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("FXML resource not found. Tried:\n");
            for (String c : candidates) sb.append("  ").append(c).append("\n");
            sb.append("Make sure the FXML files are in src/main/resources and spelled exactly.\n");
            sb.append("Current working dir: ").append(System.getProperty("user.dir")).append("\n");
            throw new IOException(sb.toString());
        }

        FXMLLoader loader = new FXMLLoader(found);
        Parent root = loader.load();
        System.out.println("Loaded FXML: " + foundPath + " -> URL: " + found);
        return root;
    }

    public void showHome() throws IOException {
        Parent root = loadFXMLFlexible("home");
        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.show();
    }

    public void showLogin() throws IOException {
        Parent root = loadFXMLFlexible("login");
        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.show();
    }

    public void showSignUp() throws IOException {
        Parent root = loadFXMLFlexible("signup");
        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.show();
    }

    public void showDifficulty() throws IOException {
        Parent root = loadFXMLFlexible("difficulty");
        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.show();
    }

    public void showLeaderboard() throws IOException {
        Parent root = loadFXMLFlexible("leaderboard");
        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.show();
    }

    // --- NEW: backstory scene wrappers that DifficultyController expects ---
    public void showBackstoryEasy() throws IOException {
        Parent root = loadFXMLFlexible("backstoryEasy");
        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.show();
    }

    public void showBackstoryMedium() throws IOException {
        Parent root = loadFXMLFlexible("backstoryMedium");
        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.show();
    }

    public void showBackstoryHard() throws IOException {
        Parent root = loadFXMLFlexible("backstoryHard");
        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.show();
    }

    public void showBackstoryNightmare() throws IOException {
        Parent root = loadFXMLFlexible("backstoryNightmare");
        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.show();
    }
    // --- end new wrappers ---

    public void showRoom(String backendRoomId) throws IOException {
        // Try flexible loading for a room_view fxml (controller expected)
        Parent root = loadFXMLFlexible("room_view");
        FXMLLoader loader = null;
        try {
            // If we loaded via flexible method we don't have the loader instance.
            // To set controller data, reload with a loader to access controller.
            String[] candidates = new String[] { "/fxml/room_view.fxml", "/com/example/room_view.fxml" };
            URL found = null;
            String foundPath = null;
            for (String p : candidates) {
                URL u = getClass().getResource(p);
                if (u != null) {
                    found = u;
                    foundPath = p;
                    break;
                }
            }
            if (found == null) {
                throw new IOException("room_view.fxml not found in /fxml/ or /com/example/ - cannot open room view.");
            }
            loader = new FXMLLoader(found);
            Parent root2 = loader.load();
            com.example.RoomController ctrl = loader.getController();
            stage.setScene(new Scene(root2, WIDTH, HEIGHT));
            stage.show();
            try { ctrl.setRoomId(backendRoomId); } catch (Throwable t) { System.err.println("Couldn't set room id on RoomController: " + t.getMessage()); }
            return;
        } catch (IOException e) {
            // fallback: if we already had a Parent from flexible load, use it (controller may not be accessible)
            stage.setScene(new Scene(root, WIDTH, HEIGHT));
            stage.show();
            System.err.println("Opened room view without setting room id (controller access failed).");
        }
    }

    public void showDarkFoyer() throws IOException {
        Parent root = loadFXMLFlexible("darkfoyer");   // expects darkfoyer.fxml to exist
        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.show();
    }

    /**
     * Generic helper to load an FXML via FXMLLoader and return the loader (so caller can access controller).
     * Tries the same flexible candidate locations as loadFXMLFlexible and calls loader.load() so controller is created.
     */
    private FXMLLoader loadFXMLLoaderFlexible(String resourceName) throws IOException {
        String[] candidates = new String[] {
            "/fxml/" + resourceName + ".fxml",
            "/com/example/" + resourceName + ".fxml"
        };

        URL found = null;
        String foundPath = null;
        for (String p : candidates) {
            URL u = getClass().getResource(p);
            if (u != null) {
                found = u;
                foundPath = p;
                break;
            }
        }

        if (found == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("FXML resource not found. Tried:\n");
            for (String c : candidates) sb.append("  ").append(c).append("\n");
            sb.append("Make sure the FXML files are in src/main/resources and spelled exactly.\n");
            throw new IOException(sb.toString());
        }

        FXMLLoader loader = new FXMLLoader(found);
        loader.load(); // load so controller and root are created
        System.out.println("Loaded FXML (loader): " + foundPath + " -> URL: " + found);
        return loader;
    }

    /** Show Help screen. Pass the current/previous room id so controller can return if needed. */
    public void showHelp(String returnRoomId) throws IOException {
        FXMLLoader loader = loadFXMLLoaderFlexible("help");
        Object controller = loader.getController();
        if (controller != null) {
            try {
                // call setReturnRoom(...) if controller implements it
                controller.getClass().getMethod("setReturnRoom", String.class).invoke(controller, returnRoomId);
            } catch (NoSuchMethodException ignored) {
                // controller doesn't accept returnRoom: ignore
            } catch (Exception e) {
                System.err.println("Failed to set return room on help controller: " + e.getMessage());
            }
        }
        Parent root = loader.getRoot();
        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.show();
    }

    /** Show Settings screen and pass the return room id so Settings->Back returns to previous room. */
    public void showSettings(String returnRoomId) throws IOException {
        FXMLLoader loader = loadFXMLLoaderFlexible("settings");
        Object controller = loader.getController();
        if (controller != null) {
            try {
                controller.getClass().getMethod("setReturnRoom", String.class).invoke(controller, returnRoomId);
            } catch (NoSuchMethodException ignored) {}
            catch (Exception e) { System.err.println("Failed to set return room on settings controller: " + e.getMessage()); }
        }
        Parent root = loader.getRoot();
        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.show();
    }

    /** Show Inventory screen and pass the return room id so Inventory->Back returns to previous room. */
    public void showInventory(String returnRoomId) throws IOException {
        FXMLLoader loader = loadFXMLLoaderFlexible("inventory");
        Object controller = loader.getController();
        if (controller != null) {
            try {
                controller.getClass().getMethod("setReturnRoom", String.class).invoke(controller, returnRoomId);
            } catch (NoSuchMethodException ignored) {}
            catch (Exception e) { System.err.println("Failed to set return room on inventory controller: " + e.getMessage()); }
        }
        Parent root = loader.getRoot();
        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.show();
    }

    public void showPuzzle(String roomId, int hotspotIndex, String previousRoomId) throws IOException {
        // flexible loader you already have
        FXMLLoader loader = loadFXMLLoaderFlexible("puzzle_view");
        Object ctrl = loader.getController();
        if (ctrl != null) {
            try {
                // call setContext(roomId, hotspotIndex, previousRoomId)
                java.lang.reflect.Method m = ctrl.getClass()
                        .getMethod("setContext", String.class, int.class, String.class);
                m.invoke(ctrl, roomId, hotspotIndex, previousRoomId);
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) { e.printStackTrace(); }
        }
        Parent root = loader.getRoot();
        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.show();
    }

    public void showCabinet(String previousRoomId) throws IOException {
        FXMLLoader loader = loadFXMLLoaderFlexible("cabinet_view");
        Object ctrl = loader.getController();
        if (ctrl != null) {
            try {
                ctrl.getClass().getMethod("setContext", String.class)
                    .invoke(ctrl, previousRoomId);
            } catch (Exception ignored) {}
        }
        Parent root = loader.getRoot();
        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.show();
    }    
    
}



