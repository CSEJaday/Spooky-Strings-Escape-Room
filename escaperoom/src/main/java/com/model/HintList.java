package com.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Loads hints.txt and provides lookup by global puzzle index.
 *
 * Expected hints.txt format (UTF-8):
 *  <index>|hint1, hint2, hint3
 * Example:
 *  1|Think about black and white keys,Piano is an instrument
 *  2|It must be broken to be used,Think breakfast food with a shell
 *
 * Index is 1-based and corresponds to the global puzzle order that RoomLoader loads.
 */
public class HintList {
    private final Map<Integer, Hint> hints = new HashMap<>();
    private final String[] defaultCandidates;

    public HintList() {
        String jsonDir = System.getProperty("user.dir") + "/JSON";
        defaultCandidates = new String[] {
            jsonDir + "/hints.txt",
            System.getProperty("user.dir") + "/hints.txt",
            "hints.txt"
        };
    }

    /**
     * Load using a specific explicit path. Returns the path if loaded successfully, otherwise null.
     */
    public String load(String path) {
        if (path == null) return null;
        if (!Files.exists(Paths.get(path))) return null;
        return loadFromPath(path);
    }

    /**
     * Existing load behavior (tries a few default locations). Returns the path loaded or null.
     */
    public String load() {
        for (String cand : defaultCandidates) {
            try {
                if (Files.exists(Paths.get(cand))) {
                    String loaded = loadFromPath(cand);
                    if (loaded != null) return loaded;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    /**
     * Internal helper: parse file at path into hints map.
     * Returns path on success or null on failure.
     */
    private String loadFromPath(String path) {
        Map<Integer, Hint> tmp = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|", 2);
                if (parts.length < 2) continue;
                int idx;
                try { idx = Integer.parseInt(parts[0].trim()); }
                catch (NumberFormatException ex) { continue; }
                String[] pieces = parts[1].split(",");
                List<String> list = new ArrayList<>();
                for (String p : pieces) {
                    String t = p.trim();
                    if (!t.isEmpty()) list.add(t);
                }
                if (!list.isEmpty()) {
                    Hint h = new Hint(idx, list);
                    tmp.put(idx, h);
                }
            }
            // commit parsed hints only if we parsed at least one
            if (!tmp.isEmpty()) {
                hints.clear();
                hints.putAll(tmp);
                return path;
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Get the Hint object for a global index or null.
     */
    public Hint getHint(int globalIndex) {
        return hints.get(globalIndex);
    }

    /**
     * Convenience: return the next hint string for globalIndex, given alreadyUsed count.
     */
    public String getNextHintFor(int globalIndex, int alreadyUsed) {
        Hint h = getHint(globalIndex);
        if (h == null) return null;
        return h.getNextHint(alreadyUsed);
    }

    /** Returns number of hint entries loaded. */
    public int size() { return hints.size(); }
}
