package com.model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Utility which parses JSON text into {@code EscapeRoom} and {@code Puzzle} objects.
 *
 * High-level behavior:
 * 
 *   Accepts either a classpath resource or filesystem path.
 *   Parses top-level JSON arrays or single objects.
 *   Supports optional puzzle fields like id, reward, locked and difficulty.
 */
public class RoomLoader {

    /**
     * Load rooms from the specified resource or file path.
     *
     * If the argument begins with '/', it is treated as an absolute classpath resource.
     * Otherwise the loader first attempts a classpath resource (by prefixing with '/'),
     * then falls back to a filesystem path.
     *
     * @param resourceOrPath classpath resource or filesystem path (e.g. "JSON/EscapeRoom.json").
     * @return list of loaded {@code EscapeRoom} objects (possibly empty).
     * @throws IOException if the resource cannot be read or top-level JSON is invalid.
     */
    public List<EscapeRoom> loadRooms(String resourceOrPath) throws IOException {
        String text = readAllText(resourceOrPath);
        if (text == null) throw new FileNotFoundException("Cannot open " + resourceOrPath);
        Object top = JsonSimpleParser.parse(text);

        List<EscapeRoom> rooms = new ArrayList<>();
        if (top instanceof List) {
            List<?> arr = (List<?>) top;
            for (Object o : arr) {
                if (o instanceof Map) rooms.add(parseRoom((Map<String,Object>) o));
            }
        } else if (top instanceof Map) {
            rooms.add(parseRoom((Map<String,Object>) top));
        } else {
            throw new IOException("Top-level JSON must be array or object");
        }
        return rooms;
    }

    /**
     * Read all text from the given resource or file path and return it as a string.
     *
     * @param resourceOrPath path or resource.
     * @return file contents as a string, or null when resource not found on disk or classpath.
     * @throws IOException on read failures.
     */
    private String readAllText(String resourceOrPath) throws IOException {
        InputStream in = RoomLoader.class.getResourceAsStream(resourceOrPath.startsWith("/") ? resourceOrPath : "/" + resourceOrPath);
        if (in != null) {
            try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return readReaderToString(r);
            }
        }
        if (!Files.exists(Paths.get(resourceOrPath))) return null;
        try (Reader r = Files.newBufferedReader(Paths.get(resourceOrPath), StandardCharsets.UTF_8)) {
            return readReaderToString(r);
        }
    }

    /**
     * Read all text from the given resource or file path and return it as a string.
     * 
     * @param r path or resource.
     * @return file contents as a string, or null when resource not found on disk or classpath.
     * @throws IOException on read failures.
     */
    private String readReaderToString(Reader r) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[4096];
        int n;
        while ((n = r.read(buf)) != -1) sb.append(buf, 0, n);
        return sb.toString();
    }

    /**
     * Convert a {@code Map} representing a room into an {@code EscapeRoom}.
     *
     * @param obj parsed JSON map for a room object.
     * @return constructed {@code EscapeRoom}.
     */
    private EscapeRoom parseRoom(Map<String,Object> obj) {
        String name = optString(obj, "name", optString(obj, "roomName", "Unnamed Room"));
        String description = optString(obj, "description", "");
        boolean isSolved = optBoolean(obj, "isSolved", false);
        int level = optInt(obj, "level", 1);

        List<Puzzle> puzzles = new ArrayList<>();
        Object pObj = obj.containsKey("puzzle") ? obj.get("puzzle") : obj.get("puzzles");
        if (pObj instanceof List) {
            for (Object pv : (List<?>) pObj) {
                if (pv instanceof Map) {
                    puzzles.add(parsePuzzle((Map<String,Object>) pv));
                }
            }
        }
        EscapeRoom r = new EscapeRoom(name, description, level, puzzles);
        r.setSolved(isSolved);
        return r;
    }

    /**
     * Parse a puzzle JSON map into a typed {@code Puzzle} instance.
     *
     * Supported types: "math", "door", "trivia", "riddle" (default).
     *
     * @param p map representing the puzzle JSON object.
     * @return typed Puzzle implementation.
     */
    private Puzzle parsePuzzle(Map<String,Object> p) {
        String type = optString(p, "type", "riddle").toLowerCase(Locale.ROOT);
        String question = optString(p, "question", "");
        Difficulty difficulty = Difficulty.fromString(optString(p, "difficulty", "EASY"));

        switch (type) {
            case "math": {
                int ans = optInt(p, "answer", 0);
                MathPuzzle mp = new MathPuzzle(question, ans, difficulty);
                applyCommonOptionalFields(mp, p);
                return mp;
            }
            case "door": {
                int numDoors = optInt(p, "numDoors", 2);

                // prefer the richer constructor if JSON provides fields
                if (p.containsKey("correctDoor") || p.containsKey("attempts") || p.containsKey("difficulty")) {
                    int correctDoor = optInt(p, "correctDoor", 1);
                    int attempts = optInt(p, "attempts", 0);
                    DoorPuzzle dp = new DoorPuzzle(numDoors, correctDoor, attempts, difficulty);
                    applyCommonOptionalFields(dp, p);
                    return dp;
                } else {
                    DoorPuzzle dp = new DoorPuzzle(numDoors);
                    dp.setDifficulty(difficulty); // honor JSON difficulty if present
                    applyCommonOptionalFields(dp, p);
                    return dp;
                }
            }
            case "trivia": {
                String answer = optString(p, "answer", null);
                String category = optString(p, "category", null);
                TriviaPuzzle rp = new TriviaPuzzle(question, answer, category, difficulty);
                applyCommonOptionalFields(rp, p);
                return rp;
            }
            case "riddle":
            default: {
                String answer = optString(p, "answer", null);
                String category = optString(p, "category", null);
                RiddlePuzzle rp = new RiddlePuzzle(question, answer, category, difficulty);
                applyCommonOptionalFields(rp, p);
                return rp;
            }
        }
    }

    /**
     * Apply optional fields common to all puzzle types (id, reward, locked, hiddenHint).
     *
     * @param puzzle the puzzle instance to mutate with optional fields.
     * @param p      map from parsed JSON that may contain optional keys.
     */
    private void applyCommonOptionalFields(Puzzle puzzle, Map<String,Object> p) {
        if (puzzle == null || p == null) return;

        // id
        int idVal = optInt(p, "id", -1);
        if (idVal >= 0) puzzle.setId(idVal);

        // reward
        String rewardStr = optString(p, "reward", null);
        if (rewardStr != null && !rewardStr.trim().isEmpty()) {
            try {
                puzzle.setReward(ItemName.valueOf(rewardStr.trim().toUpperCase()));
            } catch (IllegalArgumentException ignore) {}
        }

        // locked
        boolean locked = optBoolean(p, "locked", false);
        puzzle.setLocked(locked);

        // hidden hint
        String hidden = optString(p, "hiddenHint", null);
        if (hidden != null && !hidden.trim().isEmpty()) puzzle.setHiddenHint(hidden);

        // additional optional fields can be applied here
    }

    /**
     * Helpers
     */
    private static String optString(Map<String,Object> m, String key, String def) {
        Object v = m.get(key);
        if (v == null) return def;
        return v.toString();
    }
    private static boolean optBoolean(Map<String,Object> m, String key, boolean def) {
        Object v = m.get(key);
        if (v == null) return def;
        if (v instanceof Boolean) return (Boolean) v;
        String s = v.toString().toLowerCase();
        if ("true".equals(s)) return true;
        if ("false".equals(s)) return false;
        return def;
    }
    private static int optInt(Map<String,Object> m, String key, int def) {
        Object v = m.get(key);
        if (v == null) return def;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v.toString()); } catch (NumberFormatException e) { return def; }
    }
}


