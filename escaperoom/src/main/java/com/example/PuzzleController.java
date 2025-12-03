package com.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * FULL PuzzleController:
 * - Loads correct background (skull / painting)
 * - Selects the correct puzzle based on difficulty
 * - Provides hints from hints.txt
 * - Tracks hint usage in Progress (reflection-safe)
 * - Handles BACK / SETTINGS / INVENTORY navigation
 * - Handles answer submission
 */
public class PuzzleController implements Initializable {

    @FXML private ImageView backgroundImageView;
    @FXML private AnchorPane overlay;
    @FXML private Button backButton, settingsButton, inventoryButton, hintButton, submitButton;
    @FXML private TextArea questionText;
    @FXML private TextField answerField;

    private String roomId;
    private int hotspotIndex;
    private String previousRoomId;

    private Object selectedPuzzle;
    private Map<Integer, List<String>> hintsMap = new HashMap<>();
    private int hintIndex = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (backgroundImageView == null) System.err.println("PuzzleController.init: backgroundImageView is NULL");

        loadHintsTxt();

        backButton.setOnAction(e -> {
            try { SceneManager.getInstance().showRoom(previousRoomId); }
            catch (Exception ex) { ex.printStackTrace(); }
        });

        settingsButton.setOnAction(e -> {
            try { SceneManager.getInstance().showSettings(previousRoomId); }
            catch (Exception ex) { ex.printStackTrace(); }
        });

        inventoryButton.setOnAction(e -> {
            try { SceneManager.getInstance().showInventory(previousRoomId); }
            catch (Exception ex) { ex.printStackTrace(); }
        });

        hintButton.setOnAction(e -> onHint());

        submitButton.setOnAction(e -> onSubmit());
    }

    /** Called reflectively from SceneManager */
    public void setContext(String roomId, int hotspotIndex, String previousRoomId) {
        this.roomId = roomId;
        this.hotspotIndex = hotspotIndex;
        this.previousRoomId = previousRoomId;
        this.hintIndex = 0;

        Platform.runLater(() -> {
            loadBackgroundForHotspot();
            pickPuzzle();
            renderPuzzle();
        });
    }

    // ---------------------------------------------------------
    //  LOAD HINTS.TXT
    // ---------------------------------------------------------
    private void loadHintsTxt() {
        try {
            InputStream is = getClass().getResourceAsStream("/com/example/hints.txt");
            if (is == null) {
                System.err.println("PuzzleController: NO hints.txt found.");
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.contains("|")) continue;
                String[] parts = line.split("\\|", 2);
                int id = Integer.parseInt(parts[0].trim());
                List<String> hints = new ArrayList<>();
                for (String piece : parts[1].split(",")) {
                    if (!piece.trim().isEmpty()) hints.add(piece.trim());
                }
                hintsMap.put(id, hints);
            }
            br.close();
        } catch (Exception e) {
            System.err.println("Failed to load hints.txt: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    //  LOAD BACKGROUND IMAGE
    // ---------------------------------------------------------
    private void loadBackgroundForHotspot() {
        String filename;
        if ("WitchesDen".equals(roomId)) {
            filename = (hotspotIndex == 0) ? "WitchesDenSkull.png" : "WitchesDenPainting.png";
        } else {
            filename = roomId + "_" + hotspotIndex + ".png";
        }

        String[] testPaths = {
                "/com/example/images/" + filename,
                "/images/" + filename,
                "/" + filename
        };

        for (String test : testPaths) {
            InputStream is = getClass().getResourceAsStream(test);
            if (is != null) {
                Image img = new Image(is);
                backgroundImageView.setImage(img);
                backgroundImageView.setFitWidth(1092);
                backgroundImageView.setFitHeight(680);
                backgroundImageView.setPreserveRatio(false);
                System.out.println("PuzzleController loaded background: " + test);
                return;
            }
        }

        System.err.println("PuzzleController: FAILED to load background: " + filename);
    }

    // ---------------------------------------------------------
    //  PICK PUZZLE BASED ON DIFFICULTY
    // ---------------------------------------------------------
    private void pickPuzzle() {
        try {
            Class<?> rlClass = Class.forName("com.model.RoomLoader");
            Object rl = rlClass.getDeclaredConstructor().newInstance();
            Method loadRooms = rlClass.getMethod("loadRooms", String.class);
            @SuppressWarnings("unchecked")
            List<Object> rooms = (List<Object>) loadRooms.invoke(rl, "JSON/EscapeRoom.json");

            if (rooms == null || rooms.isEmpty()) return;

            Object targetRoom = null;
            String match = roomId.replaceAll("\\s", "").toLowerCase();

            for (Object r : rooms) {
                Method getName = r.getClass().getMethod("getName");
                String name = getName.invoke(r).toString().replaceAll("\\s","").toLowerCase();
                if (name.contains(match) || match.contains(name)) {
                    targetRoom = r;
                    break;
                }
            }
            if (targetRoom == null) targetRoom = rooms.get(0);

            Method getPuzzles = targetRoom.getClass().getMethod("getPuzzles");
            @SuppressWarnings("unchecked")
            List<Object> puzzles = (List<Object>) getPuzzles.invoke(targetRoom);
            if (puzzles == null) return;

            String difficulty = getUserDifficulty();

            List<Object> filtered = new ArrayList<>();
            for (Object p : puzzles) {
                Method getDiff = p.getClass().getMethod("getDifficulty");
                Object d = getDiff.invoke(p);
                if (d != null && d.toString().equalsIgnoreCase(difficulty)) {
                    filtered.add(p);
                }
            }
            if (filtered.isEmpty()) filtered = puzzles;

            selectedPuzzle = filtered.get(Math.min(hotspotIndex, filtered.size() - 1));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getUserDifficulty() {
        try {
            Class<?> ulClass = Class.forName("com.model.UserList");
            Object ul = ulClass.getMethod("getInstance").invoke(null);

            Object user = null;
            for (String method : new String[]{"getCurrentUser", "getCurrent", "getLoggedInUser"}) {
                try {
                    user = ulClass.getMethod(method).invoke(ul);
                    if (user != null) break;
                } catch (Exception ignored) {}
            }
            if (user == null) return "easy";

            Method getProg = user.getClass().getMethod("getProgress");
            Object prog = getProg.invoke(user);

            Method getDiff = prog.getClass().getMethod("getLastDifficultyAsEnum");
            Object diffEnum = getDiff.invoke(prog);

            return diffEnum == null ? "easy" : diffEnum.toString().toLowerCase();

        } catch (Exception e) {
            return "easy";
        }
    }

    // ---------------------------------------------------------
    //  RENDER PUZZLE
    // ---------------------------------------------------------
    private void renderPuzzle() {
        try {
            Method getQ = selectedPuzzle.getClass().getMethod("getQuestion");
            questionText.setText(getQ.invoke(selectedPuzzle).toString());
        } catch (Exception e) {
            questionText.setText("Puzzle failed to load question.");
        }
    }

    // ---------------------------------------------------------
    //  HINT BUTTON
    // ---------------------------------------------------------
    private void onHint() {
        try {
            int id = getPuzzleId();
            List<String> hints = hintsMap.get(id);

            if (hints == null || hints.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION, "No hints available.", ButtonType.OK).showAndWait();
                return;
            }

            // show next hint
            String hint = hints.get(Math.min(hintIndex, hints.size() - 1));
            new Alert(Alert.AlertType.INFORMATION, hint, ButtonType.OK).showAndWait();

            hintIndex++;

            updateHintUsageInProgress(id);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getPuzzleId() throws Exception {
        Method getId = selectedPuzzle.getClass().getMethod("getId");
        Object idObj = getId.invoke(selectedPuzzle);
        return (idObj instanceof Number) ? ((Number) idObj).intValue() : Integer.parseInt(idObj.toString());
    }

    private void updateHintUsageInProgress(int puzzleId) {
        try {
            Class<?> ulClass = Class.forName("com.model.UserList");
            Object ul = ulClass.getMethod("getInstance").invoke(null);

            Object user = null;
            for (String m : new String[]{"getCurrentUser", "getCurrent", "getLoggedInUser"}) {
                try {
                    user = ulClass.getMethod(m).invoke(ul);
                    if (user != null) break;
                } catch (Exception ignored) {}
            }
            if (user == null) return;

            Method getProg = user.getClass().getMethod("getProgress");
            Object progress = getProg.invoke(user);

            tryIncrementHintsUsed(progress, puzzleId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // SAFELY TRY MULTIPLE METHOD SIGNATURES
    private void tryIncrementHintsUsed(Object progressObj, int id) {
        String[] names = {
                "incrementHintsUsedFor",
                "incrementHintsUsed",
                "incrementHintCountFor",
                "useHintFor"
        };

        for (String n : names) {
            try {
                Method m = progressObj.getClass().getMethod(n, int.class);
                m.invoke(progressObj, id);
                return;
            } catch (Exception ignored) {}
        }

        // fallback: nothing found
        System.err.println("No increment-hint method found on Progress.");
    }

    // ---------------------------------------------------------
    //  SUBMIT ANSWER
    // ---------------------------------------------------------
    private void onSubmit() {
        try {
            String userAnswer = answerField.getText().trim();
            boolean correct = false;

            // try checkAnswer()
            try {
                Method check = selectedPuzzle.getClass().getMethod("checkAnswer", String.class);
                correct = (boolean) check.invoke(selectedPuzzle, userAnswer);
            } catch (Exception ignored) {}

            // try getAnswer() fallback
            if (!correct) {
                try {
                    Method getA = selectedPuzzle.getClass().getMethod("getAnswer");
                    String right = getA.invoke(selectedPuzzle).toString().trim().toLowerCase();
                    correct = right.equals(userAnswer.toLowerCase());
                } catch (Exception ignored) {}
            }

            if (!correct) {
                new Alert(Alert.AlertType.ERROR, "Incorrect. Try again.", ButtonType.OK).showAndWait();
                return;
            }

            new Alert(Alert.AlertType.INFORMATION, "Correct!", ButtonType.OK).showAndWait();
            markPuzzleCompleted();

            SceneManager.getInstance().showRoom(previousRoomId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void markPuzzleCompleted() {
        try {
            Class<?> ulClass = Class.forName("com.model.UserList");
            Object ul = ulClass.getMethod("getInstance").invoke(null);
            Object user = ulClass.getMethod("getCurrentUser").invoke(ul);

            Method getProg = user.getClass().getMethod("getProgress");
            Object prog = getProg.invoke(user);

            Method getQ = selectedPuzzle.getClass().getMethod("getQuestion");
            Method getId = selectedPuzzle.getClass().getMethod("getId");

            prog.getClass().getMethod("addCompletedPuzzle", String.class)
                    .invoke(prog, getQ.invoke(selectedPuzzle).toString());

            prog.getClass().getMethod("addCompletedPuzzleId", int.class)
                    .invoke(prog, ((Number) getId.invoke(selectedPuzzle)).intValue());

        } catch (Exception ignored) {}
    }
}




