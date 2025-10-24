package com.model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * EscapeRoomGameUI with HintList integration (falls back to local hints map).
 */
public class EscapeRoomGameUI {
    private final Scanner in = new Scanner(System.in);
    private final UserList userList = UserList.getInstance();
    private final RoomLoader roomLoader = new RoomLoader();

    /*
     * Main method to test the game
     */
    public static void main(String[] args) {
        EscapeRoomGameUI ui = new EscapeRoomGameUI();
        ui.run();
    }

    // project-relative JSON dir for leaderboard/optional hints file
    private static final String JSON_DIR = System.getProperty("user.dir") + "/JSON";
    private static final String LEADERBOARD_PATH = JSON_DIR + "/leaderboard.json";

    // also check explicit path you provided (project-specific source path)
    private static final String EXPLICIT_HINTS_PATH = System.getProperty("user.dir")
            + "/escaperoom/src/main/java/com/model/hints.txt";

    // scoring mapping (points per puzzle difficulty)
    private static final Map<String, Integer> POINTS;
    static {
        POINTS = new HashMap<>();
        POINTS.put("easy", 10);
        POINTS.put("medium", 20);
        POINTS.put("hard", 30);
    }

    // Hint penalty configuration
    private static final String HINT_PENALTY_MODE = "SCALED"; // "FIXED" or "SCALED"
    private static final int HINT_FIXED_SECONDS = 60; // used if mode == FIXED

    private static final Map<String, Integer> HINT_PENALTY_SCALED;
    static {
        HINT_PENALTY_SCALED = new HashMap<>();
        HINT_PENALTY_SCALED.put("easy", 30);   // seconds
        HINT_PENALTY_SCALED.put("medium", 60); // seconds
        HINT_PENALTY_SCALED.put("hard", 120);  // seconds
    }

    // Primary approach: HintList (preferred)
    private final HintList hintList = new HintList();

    // fallback: local map of hints (globalIndex -> list)
    private final Map<Integer, List<String>> hints = new HashMap<>();

    public void run() {
        // Ensure JSON folder exists (so leaderboard file can be created)
        try {
            Files.createDirectories(Paths.get(JSON_DIR));
        } catch (IOException ignored) {}

        // Try explicit path first (your provided location), then fall back to default HintList.load()
        try {
            String loadedPath = null;
            // If explicit file exists, try to load it through HintList (so HintList validates/parses it)
            if (Files.exists(Paths.get(EXPLICIT_HINTS_PATH))) {
                loadedPath = hintList.load(EXPLICIT_HINTS_PATH);
                if (loadedPath != null) {
                    System.out.println("Hints loaded from explicit path: " + loadedPath + " (entries: " + hintList.size() + ")");
                }
            }
            // If explicit didn't load, try default HintList locations
            if (loadedPath == null) {
                loadedPath = hintList.load();
                if (loadedPath != null) {
                    System.out.println("Hints loaded from HintList: " + loadedPath + " (entries: " + hintList.size() + ")");
                } else {
                    // final fallback: try earlier default loader into the old local hints map
                    loadHintsFromDefaults();
                }
            }
        } catch (Throwable t) {
            System.err.println("HintList.load() threw: " + t.getMessage() + " — falling back to file loader.");
            loadHintsFromDefaults();
        }


        while (true) {
            System.out.println("\n==== WELCOME TO THE SPOOKY STRINGS: HAUNTED MANSION ====");
            System.out.println("\nMain Menu: [1] Login  [2] Sign Up  [3] Show Leaderboard  [0] Exit");
            System.out.print("> ");
            String choice = in.nextLine().trim();

            switch (choice) {
                case "1" -> loginFlow();
                case "2" -> signUpFlow();
                case "3" -> showLeaderboard();
                case "0" -> {
                    System.out.println("Exiting. Goodbye.");
                    DataLoader.saveUsers(userList.getAllUsers());
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // Try a few default likely locations for hints.txt (JSON/hints.txt, project root, model folder)
    private void loadHintsFromDefaults() {
        String jsonCandidate = System.getProperty("user.dir") + "/JSON/hints.txt";
        String projectCandidate = System.getProperty("user.dir") + "/hints.txt";
        String modelCandidate = System.getProperty("user.dir") + "/escaperoom/src/main/java/com/model/hints.txt";

        if (Files.exists(Paths.get(jsonCandidate))) {
            loadHintsFromFile(jsonCandidate);
            System.out.println("Hints loaded from: " + jsonCandidate + " (" + hints.size() + " entries)");
            return;
        }
        if (Files.exists(Paths.get(projectCandidate))) {
            loadHintsFromFile(projectCandidate);
            System.out.println("Hints loaded from: " + projectCandidate + " (" + hints.size() + " entries)");
            return;
        }
        if (Files.exists(Paths.get(modelCandidate))) {
            loadHintsFromFile(modelCandidate);
            System.out.println("Hints loaded from: " + modelCandidate + " (" + hints.size() + " entries)");
            return;
        }
        // no hints found; that's fine
    }

    // Generic file loader into the local hints map (format: index|hint1, hint2, hint3)
    private void loadHintsFromFile(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|", 2);
                if (parts.length < 2) continue;
                int idx;
                try {
                    idx = Integer.parseInt(parts[0].trim());
                } catch (NumberFormatException ex) {
                    continue;
                }
                String[] hintParts = parts[1].split(",");
                List<String> list = new ArrayList<>();
                for (String h : hintParts) {
                    String t = h.trim();
                    if (!t.isEmpty()) list.add(t);
                }
                if (!list.isEmpty()) hints.put(idx, list);
            }
        } catch (IOException e) {
            System.err.println("Failed to load hints from " + path + ": " + e.getMessage());
        }
    }

    private void loginFlow() {
        System.out.println("\n== LOGIN ==");
        System.out.print("Username: ");
        String username = in.nextLine().trim();
        User user = userList.getUserByName(username);
        if (user == null) {
            System.out.println("User not found.");
            return;
        }
        System.out.print("Password: ");
        String pwd = in.nextLine();
        if (!pwd.equals(user.getPassword())) {
            System.out.println("Incorrect password.");
            return;
        }
        System.out.println("Login successful! Welcome " + user.getName() + "!");
    
        // show their saved progress before resuming
        displayUserProgress(user);
    
        // get saved difficulty (default ALL) and use it
        Difficulty chosen = user.getProgress() != null ? user.getProgress().getLastDifficultyAsEnum() : Difficulty.ALL;
        System.out.println("Resuming with difficulty: " + chosen.name());
    
        playSession(user, chosen);
    }
    
    private void signUpFlow() {
        System.out.println("\n== SIGN UP ==");
        System.out.print("Choose username: ");
        String username = in.nextLine().trim();
        System.out.print("Choose a password: ");
        String pwd = in.nextLine();
    
        boolean ok = userList.createAccount(username, pwd);
        if (!ok) {
            System.out.println("Username already exists. Try logging in or pick a different username.");
            return;
        }
        User newUser = userList.getUserByName(username);
        if (newUser == null) {
            System.out.println("Sign up failed unexpectedly.");
            return;
        }
        System.out.println("Sign up successful! Logged in as " + username);

        Difficulty chosen = askDifficulty();
        if (chosen == null) {
            System.out.println("No difficulty chosen — returning to main menu.");
            return;
        }

        // persist their choice immediately so it shows on next login too
        newUser.getProgress().setLastDifficulty(chosen);

        playSession(newUser, chosen);

    }
    

    private void showLeaderboard() {
        // read leaderboard JSON and print sorted by score desc then time asc
        JSONArray arr = loadLeaderboardJson();
        if (arr == null || arr.isEmpty()) {
            System.out.println("\n=== LEADERBOARD ===\nNo entries yet.");
            return;
        }

        List<JSONObject> list = new ArrayList<>();
        for (Object o : arr) if (o instanceof JSONObject) list.add((JSONObject) o);

        // Sort: score desc, then timeSpent asc (lower time is better)
        list.sort((a, b) -> {
            long scoreA = ((Number) a.getOrDefault("score", 0)).longValue();
            long scoreB = ((Number) b.getOrDefault("score", 0)).longValue();
            if (scoreA != scoreB) return Long.compare(scoreB, scoreA); // desc
            long timeA = ((Number) a.getOrDefault("timeSpent", Long.MAX_VALUE)).longValue();
            long timeB = ((Number) b.getOrDefault("timeSpent", Long.MAX_VALUE)).longValue();
            return Long.compare(timeA, timeB); // asc (lower is better)
        });

        System.out.println("\n=== LEADERBOARD ===");
        int rank = 1;
        for (JSONObject e : list) {
            String user = String.valueOf(e.getOrDefault("username", "unknown"));
            long score = ((Number) e.getOrDefault("score", 0)).longValue();
            String diff = String.valueOf(e.getOrDefault("difficulty", "N/A"));
            long timeSpent = ((Number) e.getOrDefault("timeSpent", 0)).longValue();
            String timeStr = formatSeconds(timeSpent);
            System.out.printf("%2d. %s — %d pts [%s]  (%s)%n", rank++, user, score, diff.toUpperCase(), timeStr);
        }
    }

    private void playSession(User currentUser, Difficulty chosen) {
        if (chosen == null) {
            System.out.println("No difficulty selected. Aborting session.");
            return;
        }
    
        printBackstory();
    
        List<EscapeRoom> rooms;
        try {
            rooms = roomLoader.loadRooms("JSON/EscapeRoom.json");
        } catch (IOException e) {
            System.err.println("Failed to load rooms: " + e.getMessage());
            return;
        }
    
        // record starting totals for session comparison
        long startTimeTotal = currentUser.getProgress().getTimeSpent();
    
        // record wall-clock start for the session (seconds)
        final long wallStart = System.currentTimeMillis() / 1000L;
    
        List<Puzzle> globalPuzzleList = new ArrayList<>();
        for (EscapeRoom room : rooms) {
            List<Puzzle> pz = room.getPuzzles();
            if (pz != null) globalPuzzleList.addAll(pz);
        }
    
        boolean loggedOut = false;
        int puzzlesSolved = 0;
        int sessionHintsUsed = 0;
        int globalIndex = 0;
    
        outer:
        for (EscapeRoom room : rooms) {
            System.out.println("Room: " + room.getName() + " - " + room.getDescription());
            List<Puzzle> puzzles = room.getPuzzles();
            if (puzzles == null) continue;
            for (Puzzle p : puzzles) {
                globalIndex++;
    
                String pdRaw = p.getDifficulty() == null ? "easy" : p.getDifficulty().name().toLowerCase();
                if (chosen != Difficulty.ALL && !pdRaw.equals(chosen.name().toLowerCase())) continue;
                if (currentUser.getProgress().getCompletedPuzzles().contains(p.getQuestion())) continue;
    
                System.out.println("\nPuzzle: " + p.getQuestion());
                System.out.println("(type your answer, 'hint' for a hint, 'logout' to save and exit)");
                System.out.print("> ");
    
                while (true) {
                    String answer = in.nextLine().trim();
                    if (answer.equalsIgnoreCase("logout")) {
                        // add wall-clock elapsed time to user's progress before saving
                        long wallElapsed = (System.currentTimeMillis() / 1000L) - wallStart;
                        if (wallElapsed > 0) currentUser.getProgress().addTime(wallElapsed);
    
                        DataLoader.saveUsers(userList.getAllUsers());
                        System.out.println("Saved progress. Logged out.");
                        loggedOut = true;
                        break outer;
                    } else if (answer.equalsIgnoreCase("hint")) {
                        // FIRST: try HintList (preferred)
                        int used = currentUser.getProgress().getHintsUsedFor(globalIndex);
                        String nextHint = null;
                        try {
                            nextHint = hintList.getNextHintFor(globalIndex, used);
                        } catch (Throwable ignore) {
                            nextHint = null;
                        }
                        // SECOND: fallback to local hints map if HintList had nothing
                        if (nextHint == null) {
                            List<String> hintListLocal = hints.get(globalIndex);
                            if (hintListLocal != null && !hintListLocal.isEmpty() && used < hintListLocal.size()) {
                                nextHint = hintListLocal.get(used);
                            }
                        }
    
                        if (nextHint == null) {
                            System.out.println("No hints available for this puzzle or no more hints.");
                        } else {
                            // persist hint usage and apply penalty
                            currentUser.getProgress().incrementHintsUsedFor(globalIndex);
                            sessionHintsUsed++;
                            int penalty = computeHintPenaltySeconds(pdRaw);
                            currentUser.getProgress().addTime(penalty);
                            System.out.println("[HINT] " + nextHint + "  (Time penalty: +" + formatSeconds(penalty) + ")");
                            DataLoader.saveUsers(userList.getAllUsers());
                        }
                        System.out.print("> ");
                        continue;
                    } else {
                        boolean correct;
                        try {
                            correct = p.checkAnswer(answer);
                        } catch (Exception e) {
                            System.out.println("Error: " + e.getMessage());
                            correct = false;
                        }
                        if (correct) {
                            System.out.println("Correct!");
                            currentUser.getProgress().addCompletedPuzzle(p.getQuestion());
                            int pts = getPointsForDifficulty(pdRaw);
                            currentUser.getProgress().increaseScore(pts);
                            puzzlesSolved++;
                            DataLoader.saveUsers(userList.getAllUsers());
                            break;
                        } else {
                            System.out.println("Incorrect. Try again or type 'hint'.");
                            System.out.print("> ");
                        }
                    }
                }
            }
        }
    
        if (!loggedOut) {
            // session finished naturally — add wall-clock elapsed time to user's progress
            long wallElapsed = (System.currentTimeMillis() / 1000L) - wallStart;
            if (wallElapsed > 0) currentUser.getProgress().addTime(wallElapsed);
    
            System.out.println("\n=== SESSION COMPLETE ===");
            if (puzzlesSolved > 0)
                System.out.println("You solved " + puzzlesSolved + " puzzles!");
            else
                System.out.println("No puzzles solved this time.");
    
            // compute session-only time delta (includes wall-clock elapsed + any hint-time penalties applied during the session)
            long endTime = currentUser.getProgress().getTimeSpent();
            long sessionSeconds = Math.max(0, endTime - startTimeTotal);
    
            int totalHints = 0;
            for (int c : currentUser.getProgress().getHintsUsed().values()) totalHints += c;
    
            System.out.println("\nSession summary:");
            System.out.println(" - Hints used this session: " + sessionHintsUsed);
            System.out.println(" - Total hints used: " + totalHints);
            System.out.println(" - Session time: " + formatSeconds(sessionSeconds));
            System.out.println(" - Total time (all sessions): " + formatSeconds(endTime));
    
            String diffStr = chosen == Difficulty.ALL ? "all" : chosen.name().toLowerCase();
            long totalTime = currentUser.getProgress().getTimeSpent();
            updateLeaderboardJson(currentUser.getName(),
                    currentUser.getProgress().getScore(), diffStr, totalTime);
            DataLoader.saveUsers(userList.getAllUsers());
            System.out.println("Progress saved.");
        }
    }    

    private static int computeHintPenaltySeconds(String pdiffLower) {
        if ("FIXED".equalsIgnoreCase(HINT_PENALTY_MODE)) {
            return HINT_FIXED_SECONDS;
        } else {
            return HINT_PENALTY_SCALED.getOrDefault(pdiffLower.toLowerCase(), 30);
        }
    }

    /**
     * Format seconds into M:SS (minutes:seconds) string.
     */
    private static String formatSeconds(long seconds) {
        if (seconds < 0) seconds = 0;
        long mins = seconds / 60;
        long sec = seconds % 60;
        return String.format("%d:%02d", mins, sec);
    }


    private static int getPointsForDifficulty(String pdiffLower) {
        if (pdiffLower == null) return POINTS.getOrDefault("easy", 10);
        return POINTS.getOrDefault(pdiffLower.toLowerCase(), POINTS.get("easy"));
    }

    /**
     * Ask player which difficulty to play. Supports an "All" mode by adding
     * a new enum constant in Difficulty (EASY/MEDIUM/HARD/ALL).
     */
    private Difficulty askDifficulty() {
        System.out.println("\nChoose difficulty: [1] Easy  [2] Medium  [3] Hard  [4] All  [0] Cancel");
        System.out.print("> ");
        String sel = in.nextLine().trim();
        return switch (sel) {
            case "1" -> Difficulty.EASY;
            case "2" -> Difficulty.MEDIUM;
            case "3" -> Difficulty.HARD;
            case "4" -> Difficulty.ALL;
            default -> null;
        };
    }

    private void printBackstory() {
        System.out.println("\n--- SPOOKY BACKSTORY ---");
        System.out.println("You stand before the old Wilkes mansion. Fog curls across the crooked path.");
        System.out.println("Legends say the mansion traps clever souls in games of wit. Tonight, the doors creak open...");
        System.out.println("Solve the haunted puzzles and escape before the clock runs out.");
        System.out.println("------------------------\n");
    }

    /**
     * Display a logged-in user's stored progress:
     *  - percent through the game (based on total puzzles in EscapeRoom.json)
     *  - which questions they answered (completed puzzles)
     *  - hints they used and how many per puzzle (with hint text when available)
     * Also prints the current users.json content so you can inspect persisted data.
     */
    private void displayUserProgress(User user) {
        if (user == null) return;
        Progress prog = user.getProgress();
        if (prog == null) {
            System.out.println("No progress recorded yet.");
            return;
        }

        // Load rooms to determine total puzzle count
        int totalPuzzles = 0;
        List<EscapeRoom> rooms = Collections.emptyList();
        try {
            rooms = roomLoader.loadRooms("JSON/EscapeRoom.json");
            for (EscapeRoom r : rooms) {
                List<Puzzle> pz = r.getPuzzles();
                if (pz != null) totalPuzzles += pz.size();
            }
        } catch (IOException e) {
            // if rooms can't be loaded, we'll still show available progress details
        }

        int completedCount = prog.getCompletedPuzzles().size();
        double percent = (totalPuzzles > 0) ? (completedCount * 100.0 / totalPuzzles) : 0.0;

        System.out.println("\n=== YOUR SAVED PROGRESS ===");
        System.out.printf("Player: %s%n", user.getName());
        if (totalPuzzles > 0) {
            System.out.printf("Progress: %d / %d puzzles completed (%.1f%%)%n", completedCount, totalPuzzles, percent);
        } else {
            System.out.printf("Progress: %d puzzles completed (total puzzles unknown)%n", completedCount);
        }
        System.out.printf("Score: %d%n", prog.getScore());
        System.out.printf("Total time (all sessions): %s%n", formatSeconds(prog.getTimeSpent()));

        System.out.println("\nAnswered questions (completed):");
        if (prog.getCompletedPuzzles().isEmpty()) {
            System.out.println("  (none)");
        } else {
            for (String q : prog.getCompletedPuzzles()) {
                System.out.println("  - " + q);
            }
        }

        System.out.println("\nHints used:");
        if (prog.getHintsUsed().isEmpty()) {
            System.out.println("  (no hints used)");
        } else {
            // each entry: globalIndex -> count
            for (Map.Entry<Integer, Integer> e : prog.getHintsUsed().entrySet()) {
                int idx = e.getKey();
                int count = e.getValue();
                System.out.printf("  - Puzzle index %d : %d hint(s) used%n", idx, count);
                // If we have a HintList entry, print the actual hints that were used (up to 'count')
                try {
                    Hint h = hintList.getHint(idx);
                    if (h != null) {
                        for (int i = 0; i < Math.min(count, h.getCount()); i++) {
                            System.out.printf("Hint %d: %s%n", i+1, h.getNextHint(i));
                        }
                        // If used hints exceed stored hint count, note that
                        if (count > h.getCount()) {
                            System.out.printf("(user used %d hints; only %d hints are in hints.txt)%n", count, h.getCount());
                        }
                    } else {
                        // try fallback local hints map
                        List<String> local = hints.get(idx);
                        if (local != null && !local.isEmpty()) {
                            for (int i = 0; i < Math.min(count, local.size()); i++) {
                                System.out.printf("Hint %d: %s%n", i+1, local.get(i));
                            }
                            if (count > local.size()) {
                                System.out.printf("(user used %d hints; only %d hints are in hints.txt)%n", count, local.size());
                            }
                        } else {
                            System.out.println("(no hint texts available for this puzzle)");
                        }
                    }
                } catch (Throwable ignore) {
                    System.out.println("(hint text lookup failed)");
                }
            }
        }
    }


    // ---------------------------
    // Leaderboard JSON helpers
    // ---------------------------

    /**
     * Load leaderboard JSON (array) from LEADERBOARD_PATH.
     */
    private JSONArray loadLeaderboardJson() {
        JSONParser parser = new JSONParser();
        try (FileReader fr = new FileReader(LEADERBOARD_PATH)) {
            Object obj = parser.parse(fr);
            if (obj instanceof JSONArray) {
                return (JSONArray) obj;
            } else {
                return new JSONArray();
            }
        } catch (FileNotFoundException e) {
            return new JSONArray();
        } catch (IOException | ParseException e) {
            System.err.println("Failed to read leaderboard.json: " + e.getMessage());
            return new JSONArray();
        }
    }

    /**
     * Update leaderboard JSON to deduplicate by (username, difficulty).
     * If an entry exists:
     *   - replace it when newScore > oldScore
     *   - OR when newScore == oldScore AND newTime < oldTime (faster wins)
     * If no entry exists -> append.
     */
    @SuppressWarnings("unchecked")
    private void updateLeaderboardJson(String username, long newScore, String difficulty, long newTimeSpent) {
        if (username == null) return;
        JSONArray arr = loadLeaderboardJson();
        boolean updatedOrAdded = false;

        // Try to find existing entry for username + difficulty
        for (Object o : arr) {
            if (!(o instanceof JSONObject)) continue;
            JSONObject entry = (JSONObject) o;
            String user = String.valueOf(entry.getOrDefault("username", ""));
            String diff = String.valueOf(entry.getOrDefault("difficulty", "all")).toLowerCase();
            if (user.equals(username) && diff.equals(difficulty.toLowerCase())) {
                long oldScore = ((Number) entry.getOrDefault("score", 0)).longValue();
                long oldTime = ((Number) entry.getOrDefault("timeSpent", Long.MAX_VALUE)).longValue();
                // Replace if better score OR same score but faster time
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
            // Not found — add new entry
            JSONObject newEntry = new JSONObject();
            newEntry.put("username", username);
            newEntry.put("score", newScore);
            newEntry.put("difficulty", difficulty.toLowerCase());
            newEntry.put("timeSpent", newTimeSpent);
            newEntry.put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
            arr.add(newEntry);
        }

        // save back to file
        try (FileWriter fw = new FileWriter(LEADERBOARD_PATH)) {
            fw.write(arr.toJSONString());
            fw.flush();
        } catch (IOException e) {
            System.err.println("Failed to write leaderboard.json: " + e.getMessage());
        }
    }
}
