package com.model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Pure-Java loader that uses JsonSimpleParser to parse JSON and instantiate UML objects.
 */
public class RoomLoader {

    /**
     * Load rooms. resourceOrPath may be "/EscapeRoom.json" (classpath) or "EscapeRoom.json" (filesystem).
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

    private String readAllText(String resourceOrPath) throws IOException {
        // try classpath
        InputStream in = RoomLoader.class.getResourceAsStream(resourceOrPath.startsWith("/") ? resourceOrPath : "/" + resourceOrPath);
        if (in != null) {
            try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return readReaderToString(r);
            }
        }
        // filesystem fallback
        if (!Files.exists(Paths.get(resourceOrPath))) return null;
        try (Reader r = Files.newBufferedReader(Paths.get(resourceOrPath), StandardCharsets.UTF_8)) {
            return readReaderToString(r);
        }
    }

    private String readReaderToString(Reader r) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[4096];
        int n;
        while ((n = r.read(buf)) != -1) sb.append(buf, 0, n);
        return sb.toString();
    }

    // convert raw Map to EscapeRoom
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

    private Puzzle parsePuzzle(Map<String,Object> p) {
        String type = optString(p, "type", "riddle").toLowerCase(Locale.ROOT);
        String question = optString(p, "question", "");
        Difficulty difficulty = Difficulty.fromString(optString(p, "difficulty", "EASY"));

        switch (type) {
            case "math": {
                int ans = optInt(p, "answer", 0);
                MathPuzzle mp = new MathPuzzle(question, ans, difficulty);
                // optional: set reward if declared in JSON
                String rewardStr = optString(p, "reward", null);
                if (rewardStr != null) {
                    try {
                        mp.setReward(ItemName.valueOf(rewardStr.trim().toUpperCase()));
                    } catch (IllegalArgumentException ignore) { /* invalid reward name -> ignore */ }
                }
                return mp;
            }
            case "door": {
                int numDoors = optInt(p, "numDoors", 2);
                Puzzle created;
                if (p.containsKey("correctDoor") || p.containsKey("attempts") || p.containsKey("difficulty")) {
                    int correctDoor = optInt(p, "correctDoor", 1);
                    int attempts = optInt(p, "attempts", 0);
                    Difficulty diff = Difficulty.fromString(optString(p, "difficulty", "MEDIUM"));
                    created = new DoorPuzzle(numDoors, correctDoor, attempts, diff);
                } else {
                    created = new DoorPuzzle(numDoors);
                }
                String rewardStr = optString(p, "reward", null);
                if (rewardStr != null) {
                    try {
                        created.setReward(ItemName.valueOf(rewardStr.trim().toUpperCase()));
                    } catch (IllegalArgumentException ignore) {}
                }
                return created;
            }
            case "riddle":
            default: {
                String answer = optString(p, "answer", null);
                String category = optString(p, "category", null);
                RiddlePuzzle rp = new RiddlePuzzle(question, answer, category, difficulty);
                String rewardStr = optString(p, "reward", null);
                if (rewardStr != null) {
                    try {
                        rp.setReward(ItemName.valueOf(rewardStr.trim().toUpperCase()));
                    } catch (IllegalArgumentException ignore) {}
                }
                return rp;
            }
            case "trivia": {
                String answer = optString(p, "answer", null);
                String category = optString(p, "category", null);
                TriviaPuzzle rp = new TriviaPuzzle(question, answer, category, difficulty);
                String rewardStr = optString(p, "reward", null);
                if (rewardStr != null) {
                    try {
                        rp.setReward(ItemName.valueOf(rewardStr.trim().toUpperCase()));
                    } catch (IllegalArgumentException ignore) {}
                }
                return rp;
            }
        }

    }

    // helpers to extract typed values from underlying Map (which contains parser output)
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
