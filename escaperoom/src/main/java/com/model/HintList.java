package com.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Loads and provides lookup for hint entries read from a hints.txt file.
 *
 * Expected hints.txt format (UTF-8):
 *  index|hint1, hint2, hint3
 *
 * Index is 1-based and should match the global puzzle ordering used by RoomLoader.
 */
public class HintList {
    private final Map<Integer, Hint> hints = new HashMap<>();
    private final String[] defaultCandidates;

    /**
     * Constructs a HintList and initializes default candidate paths to search for hints.txt.
     */
    public HintList() {
        String jsonDir = System.getProperty("user.dir") + "/JSON";
        defaultCandidates = new String[] {
            jsonDir + "/hints.txt",
            System.getProperty("user.dir") + "/hints.txt",
            "hints.txt"
        };
    }

    /**
     * Attempts to load hints from the explicit path provided.
     *
     * @param path the file system path to a hints file
     * @return the path that was successfully loaded, or null if loading failed
     */
    public String load(String path) {
        if (path == null) return null;
        if (!Files.exists(Paths.get(path))) return null;
        return loadFromPath(path);
    }

    /**
     * Attempts to load hints by trying a set of default locations.
     *
     * @return the path that was successfully loaded, or null if no file was found or parsed
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
     * Parse the file at the given path and populate the internal hints map.
     * Commits parsed hints only if at least one valid entry was found.
     *
     * @param path path to the hints file
     * @return the same path on success, or null on failure
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
     * Retrieve the Hint object associated with the given global puzzle index.
     *
     * @param globalIndex 1-based global puzzle index
     * @return the {@link Hint} if present, otherwise null
     */
    public Hint getHint(int globalIndex) {
        return hints.get(globalIndex);
    }

    /**
     * Convenience method to obtain the next hint string for a puzzle given how many hints
     * have already been used.
     *
     * @param globalIndex 1-based global puzzle index
     * @param alreadyUsed number of hints already consumed for this puzzle
     * @return the next hint string, or null if no hint is available
     */
    public String getNextHintFor(int globalIndex, int alreadyUsed) {
        Hint h = getHint(globalIndex);
        if (h == null) return null;
        return h.getNextHint(alreadyUsed);
    }

    /**
     * @return number of hint entries currently loaded
     */
    public int size() { return hints.size(); }
}
