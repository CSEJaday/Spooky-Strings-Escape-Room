package com.example;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Loads and serves hints from hints.txt. Each line is:
 *   <id>|hint1, hint2, ...
 *
 * Commas separate multiple hints for the same puzzle.
 */
public class HintManager {
    private static HintManager instance;
    private final Map<Integer, List<String>> hints = new HashMap<>();

    private HintManager() {
        loadHints();
    }

    public static synchronized HintManager getInstance() {
        if (instance == null) instance = new HintManager();
        return instance;
    }

    /**
     * Try multiple plausible locations for hints.txt:
     *  - resource "/hints.txt"
     *  - resource "/com/example/hints.txt"
     *  - file "JSON/hints.txt" (project runtime)
     *  - file "hints.txt" (cwd)
     */
    private void loadHints() {
        List<String> tried = new ArrayList<>();
        // try resource paths first
        String[] resourcePaths = new String[] { "/hints.txt", "/com/example/hints.txt", "/model/hints.txt" };
        for (String rp : resourcePaths) {
            InputStream is = getClass().getResourceAsStream(rp);
            if (is != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                    parseHintsFromReader(br);
                    System.out.println("HintManager: loaded hints from resource " + rp);
                    return;
                } catch (IOException e) { System.err.println("HintManager: failed to read " + rp + " : " + e.getMessage()); }
            }
            tried.add("resource:" + rp);
        }

        // try known files
        String[] filePaths = new String[] {
            System.getProperty("user.dir") + "/JSON/hints.txt",
            System.getProperty("user.dir") + "/hints.txt",
            System.getProperty("user.dir") + "/escaperoom/src/main/java/com/model/hints.txt"
        };
        for (String fp : filePaths) {
            try {
                if (Files.exists(Paths.get(fp))) {
                    try (BufferedReader br = Files.newBufferedReader(Paths.get(fp))) {
                        parseHintsFromReader(br);
                        System.out.println("HintManager: loaded hints from file " + fp);
                        return;
                    }
                }
            } catch (IOException e) { System.err.println("HintManager: failed to read " + fp + " : " + e.getMessage()); }
            tried.add("file:" + fp);
        }

        System.err.println("HintManager: no hints.txt found (tried: " + String.join(", ", tried) + ").");
    }

    private void parseHintsFromReader(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\|", 2);
            if (parts.length < 2) continue;
            int id;
            try { id = Integer.parseInt(parts[0].trim()); }
            catch (NumberFormatException ex) { continue; }
            String[] partsHints = parts[1].split(",");
            List<String> list = new ArrayList<>();
            for (String h : partsHints) {
                String t = h.trim();
                if (!t.isEmpty()) list.add(t);
            }
            if (!list.isEmpty()) hints.put(id, list);
        }
    }

    /**
     * Return the total number of hints available for a puzzle, or 0 if none.
     */
    public int availableCount(int puzzleId) {
        List<String> l = hints.get(puzzleId);
        return l == null ? 0 : l.size();
    }

    /**
     * Return the hint at index (0-based) or null if not present.
     */
    public String hintAt(int puzzleId, int index) {
        List<String> l = hints.get(puzzleId);
        if (l == null || index < 0 || index >= l.size()) return null;
        return l.get(index);
    }

    /**
     * Convenience: get the next hint given how many have already been used.
     * If usedCount < available, returns the next hint; else returns null.
     */
    public String getNextHint(int puzzleId, int usedCount) {
        return hintAt(puzzleId, usedCount);
    }
}

