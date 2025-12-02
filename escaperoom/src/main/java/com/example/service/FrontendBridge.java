package com.example.service;

import com.model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * FrontendBridge adapts core functionality from EscapeRoomGameUI for JavaFX controllers.
 * It intentionally duplicates small utility code (leaderboard read/write, hint lookup,
 * puzzle flattening) so the GUI can call simple methods.
 */
public class FrontendBridge {
    public static final Map<String,Integer> POINTS = new HashMap<>();
    public static final Map<String,Integer> HINT_PENALTY_SCALED = new HashMap<>();
    public static final String HINT_PENALTY_MODE = "SCALED";
    public static final int HINT_FIXED_SECONDS = 60;
    public static final String JSON_DIR = System.getProperty("user.dir") + "/JSON";
    public static final String LEADERBOARD_PATH = JSON_DIR + "/leaderboard.json";
    public static final String DEFAULT_ROOM_JSON = "JSON/EscapeRoom.json";
    public static final String EXPLICIT_HINTS_PATH = System.getProperty("user.dir") + "/escaperoom/src/main/java/com/model/hints.txt";

    private final RoomLoader roomLoader = new RoomLoader();
    private final HintList hintList = new HintList();
    private final Map<Integer,List<String>> localHints = new HashMap<>();

    static {
        POINTS.put("easy",10);
        POINTS.put("medium",20);
        POINTS.put("hard",30);

        HINT_PENALTY_SCALED.put("easy",30);
        HINT_PENALTY_SCALED.put("medium",60);
        HINT_PENALTY_SCALED.put("hard",120);
    }

    public FrontendBridge() {
        // try to load explicit hints file (same lookup as console)
        try {
            Files.createDirectories(Paths.get(JSON_DIR));
        } catch (IOException ignored) {}
        try {
            if (Files.exists(Paths.get(EXPLICIT_HINTS_PATH))) {
                hintList.load(EXPLICIT_HINTS_PATH);
            } else {
                // fallback: try default locations if present
                String jsonCandidate = System.getProperty("user.dir") + "/JSON/hints.txt";
                String projectCandidate = System.getProperty("user.dir") + "/hints.txt";
                String modelCandidate = System.getProperty("user.dir") + "/escaperoom/src/main/java/com/model/hints.txt";
                if (Files.exists(Paths.get(jsonCandidate))) loadLocalHints(jsonCandidate);
                else if (Files.exists(Paths.get(projectCandidate))) loadLocalHints(projectCandidate);
                else if (Files.exists(Paths.get(modelCandidate))) loadLocalHints(modelCandidate);
            }
        } catch (Throwable t) {
            System.err.println("Hint load failed: " + t.getMessage());
        }
    }

    private void loadLocalHints(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|", 2);
                if (parts.length < 2) continue;
                int idx;
                try { idx = Integer.parseInt(parts[0].trim()); } catch (NumberFormatException ex) { continue; }
                String[] hintParts = parts[1].split(",");
                List<String> list = new ArrayList<>();
                for (String h : hintParts) { String t = h.trim(); if (!t.isEmpty()) list.add(t); }
                if (!list.isEmpty()) localHints.put(idx, list);
            }
        } catch (IOException e) { System.err.println("Failed to load hints from " + path + ": " + e.getMessage()); }
    }

    /**
     * Load rooms from the EscapeRoom.json path (use default if null).
     */
    public List<EscapeRoom> loadRooms(String path) throws IOException {
        String p = (path == null || path.isBlank()) ? DEFAULT_ROOM_JSON : path;
        return roomLoader.loadRooms(p);
    }

    public List<EscapeRoom> loadRooms() throws IOException { return loadRooms(null); }

    /**
     * Flatten puzzles into a predictable linked map and ensure each puzzle has an id (mimics console behavior).
     */
    public Map<Integer, Puzzle> buildPuzzleMap(List<EscapeRoom> rooms) {
        Map<Integer,Puzzle> map = new LinkedHashMap<>();
        int generatedId = 1;
        for (EscapeRoom room : rooms) {
            List<Puzzle> pz = room.getPuzzles();
            if (pz == null) continue;
            for (Puzzle pu : pz) {
                int pid = pu.getId();
                if (pid < 0) {
                    while (map.containsKey(generatedId)) generatedId++;
                    pid = generatedId++;
                    pu.setId(pid);
                }
                map.put(pid, pu);
            }
        }
        return map;
    }

    /**
     * Return the next hint text for a puzzle id given how many hints user has already used.
     * Tries HintList first, then local hints loaded from hints.txt
     */
    public String getNextHintFor(int puzzleId, int alreadyUsed) {
        try {
            String next = hintList.getNextHintFor(puzzleId, alreadyUsed);
            if (next != null) return next;
        } catch (Throwable ignored) {}

        List<String> local = localHints.get(puzzleId);
        if (local != null && alreadyUsed < local.size()) return local.get(alreadyUsed);
        return null;
    }

    public static int computeHintPenaltySeconds(String pdiffLower) {
        if ("FIXED".equalsIgnoreCase(HINT_PENALTY_MODE)) return HINT_FIXED_SECONDS;
        return HINT_PENALTY_SCALED.getOrDefault(pdiffLower == null ? "easy" : pdiffLower.toLowerCase(), 30);
    }

    public static int pointsForDifficulty(String pdiffLower) {
        if (pdiffLower == null) return POINTS.getOrDefault("easy", 10);
        return POINTS.getOrDefault(pdiffLower.toLowerCase(), POINTS.get("easy"));
    }

    // Leaderboard read/write logic (same structure as console UI)
    public JSONArray readLeaderboard() {
        JSONParser parser = new JSONParser();
        try (FileReader fr = new FileReader(LEADERBOARD_PATH)) {
            Object obj = parser.parse(fr);
            if (obj instanceof JSONArray) return (JSONArray) obj;
            else return new JSONArray();
        } catch (FileNotFoundException e) { return new JSONArray(); }
        catch (Exception e) { System.err.println("Failed to read leaderboard.json: " + e.getMessage()); return new JSONArray(); }
    }

    @SuppressWarnings("unchecked")
    public void updateLeaderboard(String username, long newScore, String difficulty, long newTimeSpent) {
        if (username == null) return;
        JSONArray arr = readLeaderboard();
        boolean updatedOrAdded = false;
        for (Object o : arr) {
            if (!(o instanceof JSONObject)) continue;
            JSONObject entry = (JSONObject) o;
            String user = String.valueOf(entry.getOrDefault("username", ""));
            String diff = String.valueOf(entry.getOrDefault("difficulty", "all")).toLowerCase();
            if (user.equals(username) && diff.equals(difficulty.toLowerCase())) {
                long oldScore = ((Number) entry.getOrDefault("score", 0)).longValue();
                long oldTime = ((Number) entry.getOrDefault("timeSpent", Long.MAX_VALUE)).longValue();
                if (newScore > oldScore || (newScore == oldScore && newTimeSpent < oldTime)) {
                    entry.put("score", newScore);
                    entry.put("timeSpent", newTimeSpent);
                    entry.put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
                }
                updatedOrAdded = true;
                break;
            }
        }
        if (!updatedOrAdded) {
            JSONObject newEntry = new JSONObject();
            newEntry.put("username", username);
            newEntry.put("score", newScore);
            newEntry.put("difficulty", difficulty.toLowerCase());
            newEntry.put("timeSpent", newTimeSpent);
            newEntry.put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
            arr.add(newEntry);
        }
        try (FileWriter fw = new FileWriter(LEADERBOARD_PATH)) {
            fw.write(arr.toJSONString()); fw.flush();
        } catch (IOException e) { System.err.println("Failed to write leaderboard.json: " + e.getMessage()); }
    }
}

