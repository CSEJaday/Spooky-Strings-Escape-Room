package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Controller for leaderboard.fxml
 */
public class LeaderboardController {

    @FXML private ListView<String> entriesList;
    @FXML private Button backBtn;
    @FXML private Label titleLabel;

    private static final String LEADERBOARD_PATH = System.getProperty("user.dir") + "/JSON/leaderboard.json";

    @FXML
    public void initialize() {
        loadAndPopulate();
    }

    private void loadAndPopulate() {
        JSONArray arr = readLeaderboardArray();
        List<JSONObject> list = new ArrayList<>();
        if (arr != null) {
            for (Object o : arr) if (o instanceof JSONObject) list.add((JSONObject) o);
        }

        // Sort: score desc, time asc
        list.sort((a, b) -> {
            long scoreA = ((Number) a.getOrDefault("score", 0)).longValue();
            long scoreB = ((Number) b.getOrDefault("score", 0)).longValue();
            if (scoreA != scoreB) return Long.compare(scoreB, scoreA);
            long timeA = ((Number) a.getOrDefault("timeSpent", Long.MAX_VALUE)).longValue();
            long timeB = ((Number) b.getOrDefault("timeSpent", Long.MAX_VALUE)).longValue();
            return Long.compare(timeA, timeB);
        });

        ObservableList<String> rows = FXCollections.observableArrayList();
        int rank = 1;
        for (JSONObject e : list) {
            String user = String.valueOf(e.getOrDefault("username", "unknown"));
            long score = ((Number) e.getOrDefault("score", 0)).longValue();
            String diff = String.valueOf(e.getOrDefault("difficulty", "all")).toUpperCase();
            long timeSpent = ((Number) e.getOrDefault("timeSpent", 0)).longValue();
            String timeStr = formatSeconds(timeSpent);
            String row = String.format("%d. %s — %d pts [%s] (%s)", rank++, user, score, diff, timeStr);
            rows.add(row);
        }

        // Add placeholder if empty
        if (rows.isEmpty()) rows.add("(no leaderboard entries found)");

        entriesList.setItems(rows);
        // Make selection disabled (optional)
        entriesList.setMouseTransparent(true);
        entriesList.setFocusTraversable(false);
    }

    private JSONArray readLeaderboardArray() {
        JSONParser parser = new JSONParser();
        File f = new File(LEADERBOARD_PATH);
        if (!f.exists()) return new JSONArray();
        try (FileReader fr = new FileReader(f)) {
            Object obj = parser.parse(fr);
            if (obj instanceof JSONArray) return (JSONArray) obj;
            return new JSONArray();
        } catch (IOException | ParseException ex) {
            System.err.println("Failed to read leaderboard.json: " + ex.getMessage());
            return new JSONArray();
        }
    }

    private static String formatSeconds(long seconds) {
        if (seconds < 0) seconds = 0;
        long mins = seconds / 60;
        long sec = seconds % 60;
        return String.format("%d:%02d", mins, sec);
    }

    @FXML
    private void onBack() {
        try {
            // return to home screen via your SceneManager convenience
            SceneManager.getInstance().showHome();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}



