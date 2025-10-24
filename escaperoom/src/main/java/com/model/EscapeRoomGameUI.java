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
 * EscapeRoomGameUI (updated)
 *
 * - Awards different points per puzzle difficulty (easy/medium/hard)
 * - Deduplicates / updates leaderboard entries in JSON/leaderboard.json
 *
 * This class uses:
 * - UserList, DataLoader, RoomLoader, Progress (existing model classes)
 * - And a simple JSON-based leaderboard stored in project-relative JSON/leaderboard.json
 */
public class EscapeRoomGameUI {
    private final Scanner in = new Scanner(System.in);
    private final UserList userList = UserList.getInstance();
    private final RoomLoader roomLoader = new RoomLoader();

    // project-relative JSON dir for leaderboard updates
    private static final String JSON_DIR = System.getProperty("user.dir") + "/JSON";
    private static final String LEADERBOARD_PATH = JSON_DIR + "/leaderboard.json";

    // scoring mapping (points per puzzle difficulty)
    private static final Map<String, Integer> POINTS;
    static {
        POINTS = new HashMap<>();
        POINTS.put("easy", 10);
        POINTS.put("medium", 20);
        POINTS.put("hard", 30);
    }

    public static void main(String[] args) {
        EscapeRoomGameUI ui = new EscapeRoomGameUI();
        ui.run();
    }

    public void run() {
        // Ensure JSON folder exists (so leaderboard file can be created)
        try {
            Files.createDirectories(Paths.get(JSON_DIR));
        } catch (IOException ignored) {}

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
        playSession(user);
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
        playSession(newUser);
    }

    private void showLeaderboard() {
        // read leaderboard JSON and print sorted by score desc
        JSONArray arr = loadLeaderboardJson();
        if (arr == null || arr.isEmpty()) {
            System.out.println("\n=== LEADERBOARD ===\nNo entries yet.");
            return;
        }

        List<JSONObject> list = new ArrayList<>();
        for (Object o : arr) list.add((JSONObject) o);
        list.sort((a, b) -> Long.compare(
                ((Number) b.getOrDefault("score", 0)).longValue(),
                ((Number) a.getOrDefault("score", 0)).longValue()));

        System.out.println("\n=== LEADERBOARD ===");
        int rank = 1;
        for (JSONObject e : list) {
            String user = String.valueOf(e.getOrDefault("username", "unknown"));
            long score = ((Number) e.getOrDefault("score", 0)).longValue();
            String diff = String.valueOf(e.getOrDefault("difficulty", "N/A"));
            String ts = String.valueOf(e.getOrDefault("timestamp", ""));
            System.out.printf("%2d. %s — %d pts [%s] (%s)%n", rank++, user, score, diff.toUpperCase(), ts);
        }
    }

    private void playSession(User currentUser) {
        Difficulty chosen = askDifficulty();
        if (chosen == null) {
            System.out.println("Returning to main menu.");
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

        System.out.println("\nEntering the mansion... solving " + chosen + " puzzles only.\n");
        boolean loggedOut = false;
        int puzzlesSolvedThisSession = 0;

        outer:
        for (EscapeRoom room : rooms) {
            System.out.println("Room: " + room.getName() + " - " + room.getDescription());
            List<Puzzle> puzzles = room.getPuzzles();
            if (puzzles == null || puzzles.isEmpty()) continue;
            for (Puzzle p : puzzles) {
                // Normalize puzzle difficulty (from puzzle object)
                String pdRaw = p.getDifficulty() == null ? "easy" : p.getDifficulty().name().toLowerCase();
                // if session chosen is not matching and not "all", skip
                if (chosen != Difficulty.ALL && !pdRaw.equals(chosen.name().toLowerCase())) continue;

                // skip if user already completed this puzzle
                if (currentUser.getProgress().getCompletedPuzzles().contains(p.getQuestion())) continue;

                System.out.println("\nPuzzle: " + p.getQuestion());
                System.out.println("(type your answer, or type 'logout' to save and return to main menu)");
                System.out.print("> ");
                String answer = in.nextLine();
                if (answer.equalsIgnoreCase("logout")) {
                    DataLoader.saveUsers(userList.getAllUsers());
                    System.out.println("Saved progress. Logged out.");
                    loggedOut = true;
                    break outer;
                }

                boolean correct;
                try {
                    correct = p.checkAnswer(answer);
                } catch (Exception e) {
                    System.out.println("Error evaluating answer: " + e.getMessage());
                    correct = false;
                }

                if (correct) {
                    System.out.println("Correct!");
                    currentUser.getProgress().addCompletedPuzzle(p.getQuestion());
                    // award points based on puzzle difficulty
                    int pts = getPointsForDifficulty(pdRaw);
                    currentUser.getProgress().increaseScore(pts);
                    puzzlesSolvedThisSession++;
                    DataLoader.saveUsers(userList.getAllUsers());
                } else {
                    System.out.println("Incorrect. Try next puzzle (or try again later).");
                }
            }
        }

        if (!loggedOut) {
            System.out.println("\n=== SESSION COMPLETE ===");
            if (puzzlesSolvedThisSession > 0) {
                System.out.println("You solved " + puzzlesSolvedThisSession + " puzzles this session!");
                System.out.println("\nYou escaped the mansion! Congratulations!");
            } else {
                System.out.println("No puzzles of that difficulty were found or solved.");
            }

            // Update leaderboard JSON: deduplicate by username + difficulty (keep higher score)
            long finalScore = currentUser.getProgress().getScore();
            String difficultyStr = chosen == Difficulty.ALL ? "all" : chosen.name().toLowerCase();
            updateLeaderboardJson(currentUser.getName(), finalScore, difficultyStr);

            DataLoader.saveUsers(userList.getAllUsers());
            System.out.println("Progress saved.");
        }
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

    private static int getPointsForDifficulty(String pdiffLower) {
        if (pdiffLower == null) return POINTS.getOrDefault("easy", 10);
        return POINTS.getOrDefault(pdiffLower.toLowerCase(), POINTS.get("easy"));
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
     * If an entry exists and newScore > oldScore -> update entry and timestamp.
     * If no entry exists -> append.
     */
    @SuppressWarnings("unchecked")
    private void updateLeaderboardJson(String username, long newScore, String difficulty) {
        if (username == null) return;
        JSONArray arr = loadLeaderboardJson();
        boolean updated = false;
        for (Object o : arr) {
            if (!(o instanceof JSONObject)) continue;
            JSONObject entry = (JSONObject) o;
            String user = String.valueOf(entry.getOrDefault("username", ""));
            String diff = String.valueOf(entry.getOrDefault("difficulty", "all")).toLowerCase();
            if (user.equals(username) && diff.equals(difficulty.toLowerCase())) {
                // compare scores
                long old = ((Number) entry.getOrDefault("score", 0)).longValue();
                if (newScore > old) {
                    entry.put("score", newScore);
                    entry.put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
                    updated = true;
                }
                // if newScore <= old, do not overwrite
                break;
            }
        }
        if (!updated) {
            // ensure we don't already have an entry with same username+difficulty
            boolean found = false;
            for (Object o : arr) {
                if (!(o instanceof JSONObject)) continue;
                JSONObject entry = (JSONObject) o;
                String user = String.valueOf(entry.getOrDefault("username", ""));
                String diff = String.valueOf(entry.getOrDefault("difficulty", "all")).toLowerCase();
                if (user.equals(username) && diff.equals(difficulty.toLowerCase())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                JSONObject newEntry = new JSONObject();
                newEntry.put("username", username);
                newEntry.put("score", newScore);
                newEntry.put("difficulty", difficulty.toLowerCase());
                newEntry.put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
                arr.add(newEntry);
                updated = true;
            }
        }

        // save back if changed (or if arr previously empty and we've added)
        try (FileWriter fw = new FileWriter(LEADERBOARD_PATH)) {
            fw.write(arr.toJSONString());
            fw.flush();
        } catch (IOException e) {
            System.err.println("Failed to write leaderboard.json: " + e.getMessage());
        }
    }
}

