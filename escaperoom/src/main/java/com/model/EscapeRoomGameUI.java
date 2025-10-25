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
 * EscapeRoomGameUI — console backend with non-linear puzzle selection, inventory and item effects.
 */
public class EscapeRoomGameUI {
    private final Scanner in = new Scanner(System.in);
    private final UserList userList = UserList.getInstance();
    private final RoomLoader roomLoader = new RoomLoader();

    public static void main(String[] args) {
        EscapeRoomGameUI ui = new EscapeRoomGameUI();
        ui.run();
    }

    private static final String JSON_DIR = System.getProperty("user.dir") + "/JSON";
    private static final String LEADERBOARD_PATH = JSON_DIR + "/leaderboard.json";
    private static final String EXPLICIT_HINTS_PATH = System.getProperty("user.dir")
            + "/escaperoom/src/main/java/com/model/hints.txt";

    private static final Map<String, Integer> POINTS;
    static {
        POINTS = new HashMap<>();
        POINTS.put("easy", 10);
        POINTS.put("medium", 20);
        POINTS.put("hard", 30);
    }

    private static final String HINT_PENALTY_MODE = "SCALED";
    private static final int HINT_FIXED_SECONDS = 60;
    private static final Map<String, Integer> HINT_PENALTY_SCALED;
    static {
        HINT_PENALTY_SCALED = new HashMap<>();
        HINT_PENALTY_SCALED.put("easy", 30);
        HINT_PENALTY_SCALED.put("medium", 60);
        HINT_PENALTY_SCALED.put("hard", 120);
    }

    private final HintList hintList = new HintList();
    private final Map<Integer, List<String>> hints = new HashMap<>();

    public void run() {
        try { Files.createDirectories(Paths.get(JSON_DIR)); } catch (IOException ignored) {}

        try {
            String loadedPath = null;
            if (Files.exists(Paths.get(EXPLICIT_HINTS_PATH))) {
                loadedPath = hintList.load(EXPLICIT_HINTS_PATH);
                if (loadedPath != null) System.out.println("Hints loaded from explicit path: " + loadedPath + " (entries: " + hintList.size() + ")");
            }
            if (loadedPath == null) {
                loadedPath = hintList.load();
                if (loadedPath != null) System.out.println("Hints loaded from HintList: " + loadedPath + " (entries: " + hintList.size() + ")");
                else loadHintsFromDefaults();
            }
        } catch (Throwable t) {
            System.err.println("HintList.load() threw: " + t.getMessage() + " — falling back.");
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
                    System.out.println("Exiting.");
                    DataLoader.saveUsers(userList.getAllUsers());
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void loadHintsFromDefaults() {
        String jsonCandidate = System.getProperty("user.dir") + "/JSON/hints.txt";
        String projectCandidate = System.getProperty("user.dir") + "/hints.txt";
        String modelCandidate = System.getProperty("user.dir") + "/escaperoom/src/main/java/com/model/hints.txt";

        if (Files.exists(Paths.get(jsonCandidate))) { loadHintsFromFile(jsonCandidate); System.out.println("Hints loaded from: " + jsonCandidate); return; }
        if (Files.exists(Paths.get(projectCandidate))) { loadHintsFromFile(projectCandidate); System.out.println("Hints loaded from: " + projectCandidate); return; }
        if (Files.exists(Paths.get(modelCandidate))) { loadHintsFromFile(modelCandidate); System.out.println("Hints loaded from: " + modelCandidate); return; }
    }

    private void loadHintsFromFile(String path) {
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
                if (!list.isEmpty()) hints.put(idx, list);
            }
        } catch (IOException e) { System.err.println("Failed to load hints from " + path + ": " + e.getMessage()); }
    }

    private void loginFlow() {
        System.out.println("\n== LOGIN ==");
        System.out.print("Username: ");
        String username = in.nextLine().trim();
        User user = userList.getUserByName(username);
        if (user == null) { System.out.println("User not found."); return; }
        System.out.print("Password: ");
        String pwd = in.nextLine();
        if (!pwd.equals(user.getPassword())) { System.out.println("Incorrect password."); return; }
        System.out.println("Login successful! Welcome " + user.getName() + "!");
        displayUserProgress(user);
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
        // *** NEW: save users now so lastDifficulty is persisted immediately ***
        DataLoader.saveUsers(userList.getAllUsers());
    
        playSession(newUser, chosen);
    }
    

    private void showLeaderboard() {
        JSONArray arr = loadLeaderboardJson();
        if (arr == null || arr.isEmpty()) { System.out.println("\n=== LEADERBOARD ===\nNo entries yet."); return; }
        List<JSONObject> list = new ArrayList<>();
        for (Object o : arr) if (o instanceof JSONObject) list.add((JSONObject) o);
        list.sort((a, b) -> {
            long scoreA = ((Number) a.getOrDefault("score", 0)).longValue();
            long scoreB = ((Number) b.getOrDefault("score", 0)).longValue();
            if (scoreA != scoreB) return Long.compare(scoreB, scoreA);
            long timeA = ((Number) a.getOrDefault("timeSpent", Long.MAX_VALUE)).longValue();
            long timeB = ((Number) b.getOrDefault("timeSpent", Long.MAX_VALUE)).longValue();
            return Long.compare(timeA, timeB);
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
        if (chosen == null) { System.out.println("No difficulty selected. Aborting session."); return; }

        printBackstory();

        List<EscapeRoom> rooms;
        try {
            rooms = roomLoader.loadRooms("JSON/EscapeRoom.json");
        } catch (IOException e) {
            System.err.println("Failed to load rooms: " + e.getMessage());
            return;
        }

        long startTimeTotal = currentUser.getProgress().getTimeSpent();
        final long wallStart = System.currentTimeMillis() / 1000L;

        // Flatten puzzles and build id map
        Map<Integer, Puzzle> puzzleById = new LinkedHashMap<>();
        int generatedId = 1;
        for (EscapeRoom room : rooms) {
            List<Puzzle> pz = room.getPuzzles();
            if (pz == null) continue;
            for (Puzzle pu : pz) {
                int pid = pu.getId();
                if (pid < 0) {
                    while (puzzleById.containsKey(generatedId)) generatedId++;
                    pid = generatedId++;
                    pu.setId(pid);
                }
                puzzleById.put(pid, pu);
            }
        }

        boolean loggedOut = false;
        int puzzlesSolved = 0;
        int sessionHintsUsed = 0;

        System.out.println("\nYou may tackle puzzles in any order. Type 'list' to show available puzzles.");
        Runnable listAvailable = () -> {
            System.out.println("\n=== AVAILABLE PUZZLES ===");
            for (Map.Entry<Integer, Puzzle> e : puzzleById.entrySet()) {
                int pid = e.getKey();
                Puzzle pu = e.getValue();
                String pd = pu.getDifficulty() == null ? "easy" : pu.getDifficulty().name().toLowerCase();
                if (chosen != Difficulty.ALL && !pd.equals(chosen.name().toLowerCase())) continue;
                if (currentUser.getProgress().hasCompletedByEither(pu.getId(), pu.getQuestion())) continue;
                String lock = pu.isLocked() ? " (LOCKED)" : "";
                System.out.printf(" %3d : %s [%s]%s%n", pid, pu.getQuestion(), pd.toUpperCase(), lock);
            }
            System.out.println("Commands: enter puzzle id to play it, 'list', 'inventory', 'logout', 'leaderboard'.");
        };

        listAvailable.run();

        selectionLoop:
        while (true) {
            System.out.print("\nSelect> ");
            String cmd = in.nextLine().trim();
            if (cmd.isEmpty()) continue;
            if (cmd.equalsIgnoreCase("list")) { listAvailable.run(); continue; }
            if (cmd.equalsIgnoreCase("inventory")) { System.out.println(currentUser.getProgress().getInventory().toString()); continue; }
            if (cmd.equalsIgnoreCase("leaderboard")) { showLeaderboard(); continue; }
            if (cmd.equalsIgnoreCase("logout") || cmd.equalsIgnoreCase("exit")) {
                long wallElapsed = (System.currentTimeMillis() / 1000L) - wallStart;
                if (wallElapsed > 0) currentUser.getProgress().addTime(wallElapsed);
                DataLoader.saveUsers(userList.getAllUsers());
                System.out.println("Saved progress. Logged out.");
                loggedOut = true;
                break selectionLoop;
            }
            int selId;
            try { selId = Integer.parseInt(cmd); } catch (NumberFormatException ex) { System.out.println("Unknown command. Type 'list' or enter a puzzle id."); continue; }
            Puzzle p = puzzleById.get(selId);
            if (p == null) { System.out.println("No puzzle with id: " + selId); continue; }
            String pdRaw = p.getDifficulty() == null ? "easy" : p.getDifficulty().name().toLowerCase();
            if (chosen != Difficulty.ALL && !pdRaw.equals(chosen.name().toLowerCase())) { System.out.println("This puzzle is not available for the chosen difficulty."); continue; }
            if (currentUser.getProgress().hasCompletedByEither(p.getId(), p.getQuestion())) { System.out.println("You already completed this puzzle."); continue; }

            // Per-puzzle interactive loop
            System.out.println("\nPuzzle: " + p.getQuestion());
            boolean solvedCurrent = false;
            while (!solvedCurrent) {
                System.out.println("(type your answer, 'hint' for a hint, 'use <item>', or 'back' to return)");
                System.out.print("> ");
                String answer = in.nextLine().trim();
                if (answer.equalsIgnoreCase("back")) break;
                /*if (answer.toLowerCase().startsWith("pickup ")) {
                    String token = answer.substring(7).trim().toUpperCase().replaceAll("\\s+", "_");
                    try {
                        ItemName name = ItemName.valueOf(token);
                        Item template = switch (name) {
                            case KEY -> new Item(ItemName.KEY, "A small iron key. Might open a lock.", true, true, "You used the key.");
                            case TORCH -> new Item(ItemName.TORCH, "A wooden torch to light dark places.", true, false, "You light the torch; shadows recede.");
                            case POTION -> new Item(ItemName.POTION, "A mysterious potion. Drink to heal.", true, true, "You drink the potion; you feel better.");
                            default -> new Item(name, "An item: " + name.name(), false, false, "");
                        };
                        currentUser.getProgress().getInventory().addItem(template, 1);
                        System.out.println("You picked up: " + name.name());
                    } catch (IllegalArgumentException iae) {
                        System.out.println("Unknown item: " + token);
                    }
                    continue;
                }*/
                if (answer.toLowerCase().startsWith("use ")) {
                    String token = answer.substring(4).trim().toUpperCase().replaceAll("\\s+", "_");
                    try {
                        ItemName name = ItemName.valueOf(token);
                        Inventory inv = currentUser.getProgress().getInventory();
                        if (!inv.has(name)) { System.out.println("You don't have " + name.name() + " in your inventory."); }
                        else {
                            Item template = inv.getTemplate(name);
                            boolean used = inv.useItem(name);
                            if (used) {
                                System.out.println(template != null ? template.getUseText() : "You use the " + name.name() + ".");
                                // effects
                                if (name == ItemName.KEY) {
                                    if (p.isLocked()) { p.setLocked(false); System.out.println("The key turns — the lock clicks open. You can now attempt the puzzle."); }
                                    else System.out.println("There is nothing to use the key on here.");
                                }
                                if (name == ItemName.TORCH) {
                                    if (!p.isHiddenHintShown()) {
                                        String hidden = p.getHiddenHint();
                                        if (hidden != null && !hidden.isEmpty()) { System.out.println("[TORCH] Revealed: " + hidden); p.setHiddenHintShown(true); }
                                        else System.out.println("The torch lights the room but reveals nothing new.");
                                    } else System.out.println("You already revealed the hidden details here.");
                                }
                                if (name == ItemName.POTION) {
                                    System.out.println("You feel invigorated! (+10 points)");
                                    currentUser.getProgress().increaseScore(10);
                                }
                            } else {
                                System.out.println("Failed to use " + name.name() + ".");
                            }
                        }
                    } catch (IllegalArgumentException iae) { System.out.println("Unknown item: " + token); }
                    continue;
                }
                if (answer.equalsIgnoreCase("hint")) {
                    int used = currentUser.getProgress().getHintsUsedFor(p.getId());
                    String nextHint = null;
                    try { nextHint = hintList.getNextHintFor(p.getId(), used); } catch (Throwable ignore) {}
                    if (nextHint == null) {
                        List<String> local = hints.get(p.getId());
                        if (local != null && used < local.size()) nextHint = local.get(used);
                    }
                    if (nextHint == null) { System.out.println("No hints available."); }
                    else {
                        currentUser.getProgress().incrementHintsUsedFor(p.getId());
                        sessionHintsUsed++;
                        int penalty = computeHintPenaltySeconds(pdRaw);
                        currentUser.getProgress().addTime(penalty);
                        System.out.println("[HINT] " + nextHint + " (Penalty +" + formatSeconds(penalty) + ")");
                        DataLoader.saveUsers(userList.getAllUsers());
                    }
                    continue;
                }

                // attempt answer (respect lock)
                if (p.isLocked()) { System.out.println("This puzzle is locked. Try to 'use KEY' first."); continue; }
                boolean correct = false;
                try { correct = p.checkAnswer(answer); } catch (Exception e) { correct = false; }
                if (correct) {
                    System.out.println("Correct!");
                    currentUser.getProgress().addCompletedPuzzle(p.getQuestion());
                    currentUser.getProgress().addCompletedPuzzleId(p.getId());
                    int pts = getPointsForDifficulty(pdRaw);
                    currentUser.getProgress().increaseScore(pts);
                    puzzlesSolved++;
                    DataLoader.saveUsers(userList.getAllUsers());

                    // conditional reward
                    ItemName rewardName = p.getReward();
                    if (rewardName != null) {
                        Item reward = switch (rewardName) {
                            case KEY -> new Item(ItemName.KEY, "A small iron key, probably opens a nearby door.", true, true, "You used the key.");
                            case TORCH -> new Item(ItemName.TORCH, "A wooden torch to light dark places.", true, false, "You light the torch; shadows recede.");
                            case POTION -> new Item(ItemName.POTION, "A mysterious potion. Drink to heal.", true, true, "You drink the potion; you feel better.");
                            default -> new Item(rewardName, "A found item: " + rewardName.name(), false, false, "");
                        };
                        currentUser.getProgress().getInventory().addItem(reward);
                        System.out.println("You found a " + rewardName.name() + " as you solve the puzzle! It has been added to your inventory.");
                    }

                    solvedCurrent = true;
                } else {
                    System.out.println("Incorrect. Try again, or use 'hint' / 'use <item>' / 'back'.");
                }
            } // per-puzzle loop

            // check if there are any puzzles left for chosen difficulty
            boolean anyLeft = false;
            for (Puzzle pu : puzzleById.values()) {
                String pd = pu.getDifficulty() == null ? "easy" : pu.getDifficulty().name().toLowerCase();
                if (chosen != Difficulty.ALL && !pd.equals(chosen.name().toLowerCase())) continue;
                if (!currentUser.getProgress().hasCompletedByEither(pu.getId(), pu.getQuestion())) { anyLeft = true; break; }
            }
            if (!anyLeft) { System.out.println("All puzzles for the chosen difficulty are complete!"); break selectionLoop; }
        } // selection loop

        if (!loggedOut) {
            long wallElapsed = (System.currentTimeMillis() / 1000L) - wallStart;
            if (wallElapsed > 0) currentUser.getProgress().addTime(wallElapsed);

            System.out.println("\n===                 Haunted House Escaped                 ===");
            System.out.println("\nYOU'VE ESCAPED ME FOR NOW BUT THIS IS ONLY THE BEGINNING.....");
            System.out.println("You solved " + puzzlesSolved + " puzzles!");
            long endTime = currentUser.getProgress().getTimeSpent();
            long sessionSeconds = Math.max(0, endTime - startTimeTotal);

            int totalHints = 0;
            for (int c : currentUser.getProgress().getHintsUsed().values()) totalHints += c;

            System.out.println("\nSession summary:");
            System.out.println(" - Hints used this session: " + sessionHintsUsed);
            System.out.println(" - Total hints used: " + totalHints);
            System.out.println(" - Session time: " + formatSeconds(sessionSeconds));
            System.out.println(" - Total time (all sessions): " + formatSeconds(endTime));
            System.out.println(" - Difficulty played on: " + chosen.name());
            System.out.println(" - Total score: " + currentUser.getProgress().getScore());

            String diffStr = chosen == Difficulty.ALL ? "all" : chosen.name().toLowerCase();
            long totalTime = currentUser.getProgress().getTimeSpent();
            updateLeaderboardJson(currentUser.getName(), currentUser.getProgress().getScore(), diffStr, totalTime);
            DataLoader.saveUsers(userList.getAllUsers());
            System.out.println("Progress saved.");
        }
    }

    private static int computeHintPenaltySeconds(String pdiffLower) {
        if ("FIXED".equalsIgnoreCase(HINT_PENALTY_MODE)) return HINT_FIXED_SECONDS;
        return HINT_PENALTY_SCALED.getOrDefault(pdiffLower.toLowerCase(), 30);
    }

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

    private void displayUserProgress(User user) {
        if (user == null) return;
        Progress prog = user.getProgress();
        if (prog == null) { System.out.println("No progress recorded yet."); return; }

        int totalPuzzles = 0;
        List<EscapeRoom> rooms = Collections.emptyList();
        try {
            rooms = roomLoader.loadRooms("JSON/EscapeRoom.json");
            for (EscapeRoom r : rooms) { List<Puzzle> pz = r.getPuzzles(); if (pz != null) totalPuzzles += pz.size(); }
        } catch (IOException e) {}

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
        if (prog.getCompletedPuzzles().isEmpty()) System.out.println("  (none)");
        else for (String q : prog.getCompletedPuzzles()) System.out.println("  - " + q);

        System.out.println("\nHints used:");
        if (prog.getHintsUsed().isEmpty()) System.out.println("  (no hints used)");
        else {
            for (Map.Entry<Integer, Integer> e : prog.getHintsUsed().entrySet()) {
                int idx = e.getKey();
                int count = e.getValue();
                System.out.printf("  - Puzzle index %d : %d hint(s) used%n", idx, count);
                try {
                    Hint h = hintList.getHint(idx);
                    if (h != null) {
                        for (int i = 0; i < Math.min(count, h.getCount()); i++) System.out.printf("Hint %d: %s%n", i+1, h.getNextHint(i));
                        if (count > h.getCount()) System.out.printf("(user used %d hints; only %d hints are in hints.txt)%n", count, h.getCount());
                    } else {
                        List<String> local = hints.get(idx);
                        if (local != null && !local.isEmpty()) {
                            for (int i = 0; i < Math.min(count, local.size()); i++) System.out.printf("Hint %d: %s%n", i+1, local.get(i));
                            if (count > local.size()) System.out.printf("(user used %d hints; only %d hints are in hints.txt)%n", count, local.size());
                        } else {
                            System.out.println("(no hint texts available for this puzzle)");
                        }
                    }
                } catch (Throwable ignore) { System.out.println("(hint text lookup failed)"); }
            }
        }
    }

    // Leaderboard helpers
    private JSONArray loadLeaderboardJson() {
        JSONParser parser = new JSONParser();
        try (FileReader fr = new FileReader(LEADERBOARD_PATH)) {
            Object obj = parser.parse(fr);
            if (obj instanceof JSONArray) return (JSONArray) obj;
            else return new JSONArray();
        } catch (FileNotFoundException e) { return new JSONArray(); }
        catch (IOException | ParseException e) { System.err.println("Failed to read leaderboard.json: " + e.getMessage()); return new JSONArray(); }
    }

    @SuppressWarnings("unchecked")
    private void updateLeaderboardJson(String username, long newScore, String difficulty, long newTimeSpent) {
        if (username == null) return;
        JSONArray arr = loadLeaderboardJson();
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


