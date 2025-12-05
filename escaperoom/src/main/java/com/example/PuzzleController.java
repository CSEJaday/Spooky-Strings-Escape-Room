package com.example;

import com.model.DataLoader;
import com.model.RoomLoader;
import com.model.UserList;
import com.model.User;

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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Robust PuzzleController that avoids hard compile-time assumptions about model method names.
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

    // selectedPuzzle kept as Object to avoid compile-time dependency on com.model.Puzzle
    private Object selectedPuzzle;

    // parsed hints from hints.txt: id -> ordered hints
    private final Map<Integer, List<String>> hints = new HashMap<>();

    // penalty map (seconds)
    private static final Map<String, Integer> HINT_PENALTY_SCALED = new HashMap<>();
    static {
        HINT_PENALTY_SCALED.put("easy", 30);
        HINT_PENALTY_SCALED.put("medium", 60);
        HINT_PENALTY_SCALED.put("hard", 120);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (backgroundImageView == null) System.err.println("PuzzleController.initialize(): backgroundImageView is null");
        if (overlay == null) System.err.println("PuzzleController.initialize(): overlay is null");

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

        hintButton.setOnAction(e -> {
            try { onHintRequested(); } catch (Throwable t) { t.printStackTrace(); }
        });

        submitButton.setOnAction(e -> {
            try { onSubmit(); } catch (Throwable t) { t.printStackTrace(); }
        });

        loadHintsFromResources();
    }

    /**
     * Called by SceneManager after loader creates this controller.
     */
    public void setContext(String roomId, int hotspotIndex, String previousRoomId) {
        this.roomId = roomId;
        this.hotspotIndex = hotspotIndex;
        this.previousRoomId = previousRoomId;

        Platform.runLater(() -> {
            loadBackgroundForHotspot();
            pickPuzzleForThisHotspot();
            renderPuzzle();
        });
    }

    private void loadHintsFromResources() {
        String[] candidatePaths = new String[] {
                "/JSON/hints.txt",
                "/hints.txt",
                "/com/model/hints.txt",
                "/com/example/hints.txt"
        };
        boolean found = false;
        for (String p : candidatePaths) {
            try (InputStream is = getClass().getResourceAsStream(p)) {
                if (is == null) continue;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty()) continue;
                        String[] parts = line.split("\\|", 2);
                        if (parts.length < 2) continue;
                        int idx;
                        try { idx = Integer.parseInt(parts[0].trim()); } catch (NumberFormatException ex) { continue; }
                        String[] hintParts = parts[1].split(",");
                        List<String> list = Arrays.stream(hintParts)
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList());
                        if (!list.isEmpty()) hints.put(idx, list);
                    }
                }
                System.out.println("PuzzleController: loaded hints from " + p + " (entries=" + hints.size() + ")");
                found = true;
                break;
            } catch (Exception ex) {
                // try next
            }
        }
        if (!found) System.out.println("PuzzleController: NO hints.txt found.");
    }

    private void loadBackgroundForHotspot() {
        String filename;
        if ("WitchesDen".equals(roomId) || "Witches Den".equalsIgnoreCase(roomId) || "WitchesDen Screen".equalsIgnoreCase(roomId)) {
            filename = (hotspotIndex == 0) ? "WitchesDenSkullRevamp.png" : "WitchesDenPaintingRevamp.png";
        } else if ("CursedRoom".equals(roomId) || "Cursed Room".equalsIgnoreCase(roomId) || "CursedRoom Screen".equalsIgnoreCase(roomId)) {
            filename = (hotspotIndex == 0) ? "CursedRoomKeyRevamp.png" : "CursedRoomClockRevamp.png";
        } else if ("HallOfDoors".equals(roomId) || "Hall Of Doors".equalsIgnoreCase(roomId) || "HallOfDoors Screen".equalsIgnoreCase(roomId)) {
            filename = (hotspotIndex == 0) ? "HallOfDoorsGhostRevamp.png" : "HallOfDoorsPictureRevamp.png";
        } else if ("AlchemyLab".equals(roomId) || "Alchemy Lab".equalsIgnoreCase(roomId) || "AlchemyLab Screen".equalsIgnoreCase(roomId)) {
            filename = (hotspotIndex == 0) ? "AlchemyLabCandleRevamp.png" : "AlchemyLabPotionRevamp.png";
        } else {
            filename = roomId + "_" + hotspotIndex + ".png";
        }
        String[] candidates = new String[] {
                "/com/example/images/" + filename,
                "/images/" + filename,
                "/" + filename
        };
        for (String path : candidates) {
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is == null) continue;
                Image img = new Image(is);
                backgroundImageView.setImage(img);
                backgroundImageView.setFitWidth(1092);
                backgroundImageView.setFitHeight(680);
                backgroundImageView.setPreserveRatio(false);
                System.out.println("PuzzleController loaded background: " + path);
                return;
            } catch (Exception ex) { /* try next */ }
        }
        System.err.println("PuzzleController: background not found for " + filename);
    }

    /**
     * Selects a puzzle for this room/hotspot and respects player's chosen difficulty.
     * Uses safe reflective fallbacks for retrieving last difficulty from Progress.
     */
    private void pickPuzzleForThisHotspot() {
        // compute difficulty first (mutable local)
        String chosenDiff = "easy";

        try {
            RoomLoader rl = new RoomLoader();
            List<?> rooms = rl.loadRooms("JSON/EscapeRoom.json");
            if (rooms == null || rooms.isEmpty()) {
                System.err.println("pickPuzzleForThisHotspot: no rooms loaded");
                return;
            }

            // find the room by name (flexible matching)
            Object foundRoom = null;
            String rid = (roomId == null) ? "" : roomId.replaceAll("\\s", "").toLowerCase();
            for (Object r : rooms) {
                try {
                    Method getName = r.getClass().getMethod("getName");
                    Object nameObj = getName.invoke(r);
                    if (nameObj != null) {
                        String nm = nameObj.toString();
                        String key = nm.replaceAll("\\s","").toLowerCase();
                        if (key.contains(rid) || rid.contains(key) || nm.equalsIgnoreCase(roomId)) { foundRoom = r; break; }
                    }
                } catch (NoSuchMethodException ignore) {}
            }
            if (foundRoom == null) foundRoom = rooms.get(0);

            // get puzzles list reflectively
            Object puzzlesObj = null;
            try {
                Method getPuzzles = foundRoom.getClass().getMethod("getPuzzles");
                puzzlesObj = getPuzzles.invoke(foundRoom);
            } catch (NoSuchMethodException nm) {
                System.err.println("pickPuzzleForThisHotspot: foundRoom.getPuzzles() not found");
            }
            if (!(puzzlesObj instanceof List)) {
                System.err.println("pickPuzzleForThisHotspot: room.getPuzzles() not a List");
                return;
            }
            @SuppressWarnings("unchecked")
            List<Object> puzzles = (List<Object>) puzzlesObj;

            // determine player's chosen difficulty via UserList -> currentUser -> progress safely
            try {
                UserList ul = UserList.getInstance();
                Object current = tryGetCurrentUserReflective(ul);
                if (current != null) {
                    Object prog = tryInvokeNoArg(current, "getProgress");
                    if (prog != null) {
                        Object enumObj = tryInvokeNoArg(prog, "getLastDifficultyAsEnum");
                        if (enumObj != null) chosenDiff = enumObj.toString().toLowerCase();
                        else {
                            Object s = tryInvokeNoArg(prog, "getLastDifficulty");
                            if (s != null) chosenDiff = s.toString().toLowerCase();
                            else {
                                try {
                                    Field f = prog.getClass().getDeclaredField("lastDifficulty");
                                    f.setAccessible(true);
                                    Object val = f.get(prog);
                                    if (val != null) chosenDiff = val.toString().toLowerCase();
                                } catch (Throwable ignore) {}
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                // keep default chosenDiff
            }

            // **Make a final copy for lambda capture**
            final String chosen = (chosenDiff == null) ? "easy" : chosenDiff;

            System.out.println("PuzzleController: chosen difficulty = " + chosen);

            // filter puzzles by difficulty (safe reflection), use final 'chosen' inside lambda
            List<Object> candidates = puzzles.stream().filter(p -> {
                try {
                    Object d = tryInvokeNoArg(p, "getDifficulty");
                    if (d == null) return "easy".equals(chosen);
                    String pd = d.toString().toLowerCase();
                    return pd.equals(chosen);
                } catch (Throwable t) {
                    return true;
                }
            }).collect(Collectors.toList());

            if (candidates.isEmpty()) candidates = puzzles;

            int pick = (hotspotIndex <= 0) ? 0 : Math.min(hotspotIndex, Math.max(0, candidates.size() - 1));
            selectedPuzzle = candidates.get(Math.min(pick, candidates.size()-1));

            System.out.println("PuzzleController: selectedPuzzle -> id=" + safeGetId(selectedPuzzle));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Utility: try to invoke no-arg method reflectively; returns null on any problem.
     */
    private Object tryInvokeNoArg(Object target, String name) {
        if (target == null) return null;
        try {
            Method m = target.getClass().getMethod(name);
            return m.invoke(target);
        } catch (Throwable t) {
            return null;
        }
    }

    private Integer safeGetId(Object p) {
        if (p == null) return null;
        try {
            Method m = p.getClass().getMethod("getId");
            Object o = m.invoke(p);
            if (o == null) return null;
            if (o instanceof Number) return ((Number) o).intValue();
            return Integer.parseInt(o.toString());
        } catch (Throwable t) {
            try {
                Field f = p.getClass().getDeclaredField("id");
                f.setAccessible(true);
                Object o = f.get(p);
                if (o instanceof Number) return ((Number) o).intValue();
                if (o != null) return Integer.parseInt(o.toString());
            } catch (Throwable ignore) {}
        }
        return null;
    }

    private Object tryGetCurrentUserReflective(UserList ul) {
        if (ul == null) return null;
        try {
            for (String mname : new String[] {"getCurrentUser", "getCurrent", "getLoggedInUser"}) {
                try {
                    Method m = ul.getClass().getMethod(mname);
                    Object cu = m.invoke(ul);
                    if (cu != null) return cu;
                } catch (Throwable ignored) {}
            }
            try {
                Method m = ul.getClass().getMethod("getAllUsers");
                Object listObj = m.invoke(ul);
                if (listObj instanceof List) {
                    List<?> list = (List<?>) listObj;
                    if (!list.isEmpty()) return list.get(0);
                }
            } catch (Throwable ignore) {}
        } catch (Throwable t) { /* ignore */ }
        return null;
    }

    private void renderPuzzle() {
        if (selectedPuzzle == null) {
            questionText.setText("No puzzle here.");
            return;
        }
        try {
            Object q = tryInvokeNoArg(selectedPuzzle, "getQuestion");
            if (q == null) questionText.setText(selectedPuzzle.toString());
            else questionText.setText(q.toString());
        } catch (Throwable t) {
            t.printStackTrace();
            questionText.setText(selectedPuzzle.toString());
        }
    }

    /**
     * Handle hint button. Uses hints map and updates Progress safely.
     */
    private void onHintRequested() {
        if (selectedPuzzle == null) {
            new Alert(Alert.AlertType.INFORMATION, "No puzzle selected.").showAndWait();
            return;
        }
        Integer pid = safeGetId(selectedPuzzle);
        if (pid == null) {
            new Alert(Alert.AlertType.INFORMATION, "No hints available for this item.").showAndWait();
            return;
        }
        List<String> local = hints.get(pid);
        Object prog = getCurrentProgressReflective();
        if (prog == null) {
            new Alert(Alert.AlertType.ERROR, "No user progress found.").showAndWait();
            return;
        }

        int used = 0;
        try {
            Method m = prog.getClass().getMethod("getHintsUsedFor", int.class);
            Object r = m.invoke(prog, pid);
            if (r instanceof Number) used = ((Number) r).intValue();
        } catch (Throwable ignored) {
            try {
                Method gm = prog.getClass().getMethod("getHintsUsed");
                Object mapObj = gm.invoke(prog);
                if (mapObj instanceof Map) {
                    Map<?,?> mm = (Map<?,?>) mapObj;
                    if (mm.containsKey(pid)) {
                        Object v = mm.get(pid);
                        if (v instanceof Number) used = ((Number) v).intValue();
                        else used = Integer.parseInt(v.toString());
                    }
                }
            } catch (Throwable ignore2) {}
        }

        String nextHint = null;
        if (local != null && used < local.size()) nextHint = local.get(used);

        if (nextHint == null) {
            new Alert(Alert.AlertType.INFORMATION, "No hints available.").showAndWait();
            return;
        }

        Alert a = new Alert(Alert.AlertType.INFORMATION, nextHint, ButtonType.OK);
        a.setHeaderText("Hint (" + (used+1) + (local != null ? ("/" + local.size()) : "") + ")");
        a.showAndWait();

        boolean incremented = false;
        try {
            Method inc = prog.getClass().getMethod("incrementHintsUsedFor", int.class);
            inc.invoke(prog, pid);
            incremented = true;
        } catch (Throwable ignore) {}

        if (!incremented) {
            try {
                Method getHintsUsed = prog.getClass().getMethod("getHintsUsed");
                Object mapObj = getHintsUsed.invoke(prog);
                if (mapObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<Object,Object> mm = (Map<Object,Object>) mapObj;
                    Object cur = mm.get(pid);
                    int curInt = 0;
                    if (cur instanceof Number) curInt = ((Number) cur).intValue();
                    else if (cur != null) curInt = Integer.parseInt(cur.toString());
                    mm.put(pid, curInt + 1);
                    incremented = true;
                }
            } catch (Throwable ignore) {}
        }

        String pd = "easy";
        try {
            Object d = tryInvokeNoArg(selectedPuzzle, "getDifficulty");
            if (d != null) pd = d.toString().toLowerCase();
        } catch (Throwable ignore) {}
        int penalty = HINT_PENALTY_SCALED.getOrDefault(pd, 30);

        boolean applied = false;
        try {
            Method addTime = prog.getClass().getMethod("addTime", long.class);
            addTime.invoke(prog, (long) penalty);
            applied = true;
        } catch (Throwable ignore) {}

        if (!applied) {
            try {
                Method getTS = prog.getClass().getMethod("getTimeSpent");
                Object curObj = getTS.invoke(prog);
                long cur = 0;
                if (curObj instanceof Number) cur = ((Number)curObj).longValue();
                else if (curObj != null) cur = Long.parseLong(curObj.toString());
                try {
                    Method setTS = prog.getClass().getMethod("setTimeSpent", long.class);
                    setTS.invoke(prog, cur + penalty);
                    applied = true;
                } catch (Throwable ignore2) {
                    try {
                        Field f = prog.getClass().getDeclaredField("timeSpent");
                        f.setAccessible(true);
                        f.setLong(prog, cur + penalty);
                        applied = true;
                    } catch (Throwable ignore3) {}
                }
            } catch (Throwable ignore) {}
        }

        // persist users safely
        try {
            UserList ul = UserList.getInstance();
            try {
                Method allUsers = ul.getClass().getMethod("getAllUsers");
                Object list = allUsers.invoke(ul);
                if (list instanceof List) DataLoader.saveUsers((List) list);
                else DataLoader.saveUsers(UserList.getInstance().getAllUsers());
            } catch (Throwable t2) {
                try { DataLoader.saveUsers(UserList.getInstance().getAllUsers()); } catch (Throwable ignore) { ignore.printStackTrace(); }
            }
        } catch (Throwable t) {
            try { DataLoader.saveUsers(UserList.getInstance().getAllUsers()); } catch (Throwable ex) { ex.printStackTrace(); }
        }
    }

    /**
     * Attempt to validate the answer.
     */
    private void onSubmit() {
        if (selectedPuzzle == null) return;
        String userAnswer = (answerField.getText() == null) ? "" : answerField.getText().trim();
        boolean correct = false;

        try {
            Method check = selectedPuzzle.getClass().getMethod("checkAnswer", String.class);
            Object r = check.invoke(selectedPuzzle, userAnswer);
            if (r instanceof Boolean) correct = (Boolean) r;
        } catch (Throwable ignored) {
            String canonical = null;
            String[] candidateNames = new String[] {"getAnswer", "getCorrectAnswer", "getSolution", "answer", "correctAnswer", "solution"};
            for (String name : candidateNames) {
                if (canonical != null) break;
                try {
                    Method m = selectedPuzzle.getClass().getMethod(name);
                    Object v = m.invoke(selectedPuzzle);
                    if (v != null) canonical = v.toString().trim();
                } catch (NoSuchMethodException ns) {
                    try {
                        Field f = selectedPuzzle.getClass().getDeclaredField(name);
                        f.setAccessible(true);
                        Object v = f.get(selectedPuzzle);
                        if (v != null) canonical = v.toString().trim();
                    } catch (Throwable ignoreField) {}
                } catch (Throwable ignore) {}
            }
            if (canonical != null) correct = canonical.equalsIgnoreCase(userAnswer.trim());
            else correct = false;
        }

        if (correct) {
            new Alert(Alert.AlertType.INFORMATION, "Correct!", ButtonType.OK).showAndWait();
            Object prog = getCurrentProgressReflective();
            if (prog != null) {
                try {
                    Method addQ = prog.getClass().getMethod("addCompletedPuzzle", String.class);
                    Object q = tryInvokeNoArg(selectedPuzzle, "getQuestion");
                    String qStr = (q == null) ? selectedPuzzle.toString() : q.toString();
                    addQ.invoke(prog, qStr);
                } catch (Throwable ignored) {}
                try {
                    Method addId = prog.getClass().getMethod("addCompletedPuzzleId", int.class);
                    Integer id = safeGetId(selectedPuzzle);
                    if (id != null) addId.invoke(prog, id);
                } catch (Throwable ignored) {}

                int pts = 10;
                try {
                    Object d = tryInvokeNoArg(selectedPuzzle, "getDifficulty");
                    if (d != null) {
                        String pd = d.toString().toLowerCase();
                        if ("medium".equals(pd)) pts = 20;
                        else if ("hard".equals(pd)) pts = 30;
                    }
                } catch (Throwable ignore) {}
                try {
                    Method inc = prog.getClass().getMethod("increaseScore", int.class);
                    inc.invoke(prog, pts);
                } catch (Throwable ignored) {}

                try {
                    UserList ul = UserList.getInstance();
                    Method allUsers = ul.getClass().getMethod("getAllUsers");
                    Object list = allUsers.invoke(ul);
                    if (list instanceof List) DataLoader.saveUsers((List) list);
                } catch (Throwable e) {
                    try { DataLoader.saveUsers(UserList.getInstance().getAllUsers()); } catch (Throwable ex) { ex.printStackTrace(); }
                }
            }

            try { SceneManager.getInstance().showRoom(previousRoomId); } catch (Exception e) { e.printStackTrace(); }
        } else {
            new Alert(Alert.AlertType.ERROR, "Incorrect. Try again.", ButtonType.OK).showAndWait();
        }
    }

    // ---------- utilities ----------
    private Object getCurrentProgressReflective() {
        try {
            UserList ul = UserList.getInstance();
            Object cur = tryGetCurrentUserReflective(ul);
            if (cur == null) return null;
            Object prog = tryInvokeNoArg(cur, "getProgress");
            if (prog != null) return prog;
            try {
                Field f = cur.getClass().getDeclaredField("progress");
                f.setAccessible(true);
                return f.get(cur);
            } catch (Throwable ignore) {}
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }
}

