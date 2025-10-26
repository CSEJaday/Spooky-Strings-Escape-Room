package com.model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.speech.Speek;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Console-driven user interface and game loop for the Escape Room application.
 *
 * Responsibilities:
 *  - bootstraps resources (hints, rooms, leaderboard)
 *  - presents login and signup flows
 *  - runs the main session loop where players select and solve puzzles
 *  - handles per-puzzle actions such as requesting hints, using inventory items,
 *    scoring, and persisting progress/leaderboard entries
 *
 * This class is intentionally imperative and interacts with the console Scanner.
 * It persists user progress through {@link DataLoader} and updates a simple
 * leaderboard JSON file under the JSON directory.
 */
public class EscapeRoomGameUI {
    private final Scanner in = new Scanner(System.in);
    private final UserList userList = UserList.getInstance();
    private final RoomLoader roomLoader = new RoomLoader();

    /**
     * Entry point for running the console UI.
     */
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

    /**
     * Start the main UI loop. Ensures JSON directory exists, attempts to load hints,
     * and repeatedly shows the main menu until the user exits.
     */
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

    /**
     * Attempt loading hints from a small set of default locations.
     * The first existing file found will be parsed into the local hints map.
     */
    private void loadHintsFromDefaults() {
        String jsonCandidate = System.getProperty("user.dir") + "/JSON/hints.txt";
        String projectCandidate = System.getProperty("user.dir") + "/hints.txt";
        String modelCandidate = System.getProperty("user.dir") + "/escaperoom/src/main/java/com/model/hints.txt";

        if (Files.exists(Paths.get(jsonCandidate))) { loadHintsFromFile(jsonCandidate); System.out.println("Hints loaded from: " + jsonCandidate); return; }
        if (Files.exists(Paths.get(projectCandidate))) { loadHintsFromFile(projectCandidate); System.out.println("Hints loaded from: " + projectCandidate); return; }
        if (Files.exists(Paths.get(modelCandidate))) { loadHintsFromFile(modelCandidate); System.out.println("Hints loaded from: " + modelCandidate); return; }
    }

    /**
     * Parse a hints file and populate the in-memory hints map.
     *
     * @param path path to a readable hints file using the format "index|hint1, hint2,..."
     */
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

    /**
     * Handle user login flow: prompt for username and password, then resume a play session.
     * Successful login will display user progress before starting a session.
     */
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

    /**
     * Handle sign-up flow: create account, persist the chosen difficulty immediately,
     * then start a play session for the new user.
     */
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
    
        System.out.println(
        "          .-.\n" +
        "         (o o) boo!\n" +
        "          |O|   _  _   _  _  _  _  _  _\n" +
        "         /   \\  \\`/ \\_/ \\\\/ \\/ \\/ \\/ \\/ \\\n" +
        "        /_____\\  \\              HAUNTED\n" +
        "       /  /|\\  \\  \\   MANSION   MANSION\n" +
        "      /__/ | \\__\\  \\  ______  ______  __\n" +
        "     /  \\  |  /  \\  / /  __ \\/  __  \\/  \\\n" +
        "    /____\\ | /____\\/ /  /  \\/  /  \\  /\\  \\\n" +
        "   /      \\|/      /_/__/\\___/__/\\__\\/__\\_\\\n" +
        "   |  .--.  |  .--.  .--.  .--.  .--.  .--. |\n" +
        "   | (    ) | (    )(    )(    )(    )(    )|\n" +
        "   |  `--'  |  `--'  `--'  `--'  `--'  `--' |\n" +
        "   |                                       |\n" +
        "   |   Beware. The Wilkes Mansion is old   |\n" +
        "   |   and hungry for riddles, doors, and  |\n" +
        "   |   brave souls. Solve if you dare...   |\n" +
        "   |_______________________________________|\n" +
        "       \\_________________________________/\n" +
        "        /  /  /  /  /  /  /  /  /  /  /  /\n" +
        "       /__/_ /__/_ /__/_ /__/_ /__/_ /__/\n"
        );


        // persist their choice immediately so it shows on next login too
        newUser.getProgress().setLastDifficulty(chosen);
        // *** NEW: save users now so lastDifficulty is persisted immediately ***
        DataLoader.saveUsers(userList.getAllUsers());
    
        playSession(newUser, chosen);
    }
    
    /**
     * Print the leaderboard sorted by score (desc) and time (asc).
     * Reads leaderboard JSON and displays a formatted ranking.
     */
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

    /**
     * Print an atmospheric intro for the supplied puzzle. If the puzzle has a positive id,
     * a handcrafted backstory for that id is used when available. Otherwise, a type-based
     * fallback description is printed.
     *
     * @param p puzzle to introduce
     */
    private void printPuzzleIntro(Puzzle p) {
        if (p == null) return;
    
        System.out.println();
        System.out.println("~~~ Atmosphere ~~~");
    
        // If puzzle has a valid id, prefer the id-based handcrafted backstory.
        int id = p.getId();
        if (id > 0) {
            switch (id) {
                // Dark Foyer (1–6)
                case 1 -> {
                    String darkFoyer1 = "You stand before a massive iron door. A ghostly draft seeps through its keyhole, whispering the punchline you dread to speak aloud.";
                    System.out.println(darkFoyer1);
                    Speek.speak(darkFoyer1);
                }
                case 2 -> {
                    String darkFoyer2 = "Shadows flicker across the walls, shaped like the undead in casual conversation. They laugh at a joke only the dead would understand.";
                    System.out.println(darkFoyer2);
                    Speek.speak(darkFoyer2);
                }
                case 3 -> {
                    String darkFoyer3 = "The chandelier rattles above; something unseen rushes by. The air itself challenges you to name it.";
                    System.out.println(darkFoyer3);
                    Speek.speak(darkFoyer3);
                }
                case 4 -> {
                    String darkFoyer4 = "A polished coffin rests in the corner. Its lid trembles as if expecting company—whatever it is, it didn't want to be made.";
                    System.out.println(darkFoyer4);
                    Speek.speak(darkFoyer4);
                }
                case 5 -> {
                    String darkFoyer5 = "An ancient door sways in still air. Each slow groan sounds like a warning; the hinges remember old laments.";
                    System.out.println(darkFoyer5);
                    Speek.speak(darkFoyer5);
                }
                case 6 -> {
                    String darkFoyer6 = "Skeletons in the alcoves grin wider as you enter. One begins to chatter its jaw rhythmically—it's waiting for an answer.";
                    System.out.println(darkFoyer6);
                    Speek.speak(darkFoyer6);
                }

                // Hall of Doors (7–12)
                case 7 -> {
                    String hall7 = "Three doors stand before you. Behind one you hear chains, another hides a heartbeat, the last breathes an unnatural silence.";
                    System.out.println(hall7);
                    Speek.speak(hall7);
                }
                case 8 -> {
                    String hall8 = "Five doors shimmer faintly. One glows warmer than the rest — but in this place, warmth can lie.";
                    System.out.println(hall8);
                    Speek.speak(hall8);
                }
                case 9 -> {
                    String hall9 = "The corridor stretches to infinity. Ten doors leer at you, engraved with symbols that rearrange when you blink.";
                    System.out.println(hall9);
                    Speek.speak(hall9);
                }
                case 10 -> {
                    String hall10 = "Soft whispers count from behind every door—different numbers, same terrified voice. Choose the one that keeps calm.";
                    System.out.println(hall10);
                    Speek.speak(hall10);
                }
                case 11 -> {
                    String hall11 = "Thirty doors rise like tombstones. The air grows heavy with indecision; take too long and the corridor remembers you.";
                    System.out.println(hall11);
                    Speek.speak(hall11);
                }
                case 12 -> {
                    String hall12 = "The hallway folds into itself. Each door watches you as if it learned your name; one will not send you back.";
                    System.out.println(hall12);
                    Speek.speak(hall12);
                }

                // Cursed Room (Math 13–18)
                case 13 -> {
                    String math13 = "Equations crawl across candlelit walls like living things. The numbers shimmer, rearranging themselves in mockery.";
                    System.out.println(math13);
                    Speek.speak(math13);
                }
                case 14 -> {
                    String math14 = "A chalkboard hisses as invisible hands scrawl fresh formulas—each wrong answer seems to shudder the room.";
                    System.out.println(math14);
                    Speek.speak(math14);
                }
                case 15 -> {
                    String math15 = "A dusty abacus counts itself with spectral fingers. It dares you to out-calculate the dead before the beads freeze.";
                    System.out.println(math15);
                    Speek.speak(math15);
                }
                case 16 -> {
                    String math16 = "Books drift lazily in the air, flipping pages of half-burned equations. One number seems to stare back, waiting.";
                    System.out.println(math16);
                    Speek.speak(math16);
                }
                case 17 -> {
                    String math17 = "Runes of calculation pulse in the dark. The numbers whisper in chorus — finish the math or be counted among them.";
                    System.out.println(math17);
                    Speek.speak(math17);
                }
                case 18 -> {
                    String math18 = "The equation glows and burns itself into the plaster. Solve it before the light becomes a brand on your memory.";
                    System.out.println(math18);
                    Speek.speak(math18);
                }

                // Alchemy Lab (Trivia 19–24)
                case 19 -> {
                    String alchemy19 = "A glass jar trembles on a shelf. Something small and unnatural peers out, guarding its master's secrets with jealous eyes.";
                    System.out.println(alchemy19);
                    Speek.speak(alchemy19);
                }
                case 20 -> {
                    String alchemy20 = "The cauldron boils on its own; steam curls into letters you almost read. The scent of trouble hangs heavy in the air.";
                    System.out.println(alchemy20);
                    Speek.speak(alchemy20);
                }
                case 21 -> {
                    String alchemy21 = "A sealed vial hums softly. The glow inside flickers like a heartbeat — whatever is trapped knows you noticed it.";
                    System.out.println(alchemy21);
                    Speek.speak(alchemy21);
                }
                case 22 -> {
                    String alchemy22 = "Blue flames dance in flasks across the bench. Each one exhales a whisper that smells faintly of phosphorus and regret.";
                    System.out.println(alchemy22);
                    Speek.speak(alchemy22);
                }
                case 23 -> {
                    String alchemy23 = "A cracked symbol for Mercury shimmers on a battered chart. It seems to melt and grin back at you from the page.";
                    System.out.println(alchemy23);
                    Speek.speak(alchemy23);
                }
                case 24 -> {
                    String alchemy24 = "An obsidian mirror catches your reflection—and for a flash it isn't yours. Lead hums softly in the shadows.";
                    System.out.println(alchemy24);
                    Speek.speak(alchemy24);
                }
    
                // default for any id not handled
                default -> {
                    // fallback to type-based text below if id unknown
                    printPuzzleIntroFallbackByType(p);
                    System.out.println("~~~~~~~~~~~~~~~~~~");
                    //System.out.println();
                    return;
                }
            }
    
            System.out.println("~~~~~~~~~~~~~~~~~~");
            //System.out.println();
            return;
        }
    
        // If no valid id, or id <= 0, fallback to type-based intros (previous behavior).
        printPuzzleIntroFallbackByType(p);
        System.out.println("~~~~~~~~~~~~~~~~~~");
        //System.out.println();
    }
    
    /**
     * Fallback flavor text selected by puzzle type when no id-specific text exists.
     *
     * @param p puzzle instance
     */
    private void printPuzzleIntroFallbackByType(Puzzle p) {
        String q = p.getQuestion() == null ? "" : p.getQuestion().toLowerCase();
    
        if (p instanceof RiddlePuzzle) {
            if (q.contains("key opens a haunted house") || q.contains("spoo-key")) {
                System.out.println("A cold wind passes through the foyer. A rusted key lies on a dusty table, glinting faintly under ghostlight.");
            } else if (q.contains("zombie") || q.contains("mummy") || q.contains("skeleton")) {
                System.out.println("You hear shuffling footsteps somewhere close. Something undead seems amused by your presence...");
            } else {
                System.out.println("You feel a presence whispering riddles in your ear, daring you to speak the answer aloud.");
            }
        } else if (p instanceof DoorPuzzle) {
            System.out.println("The hallway stretches endlessly before you. Countless doors line the walls—some whisper, some breathe.");
            System.out.println("Only one will let you pass. Choose carefully; the wrong one may close forever.");
        } else if (p instanceof MathPuzzle) {
            System.out.println("Symbols pulse faintly across the walls of this cursed study. Numbers twist and rearrange themselves like ghosts of logic.");
            System.out.println("The air smells of ink, chalk, and something metallic—solve before the formula consumes you.");
        } else if (p instanceof TriviaPuzzle) {
            System.out.println("You find a dusty tome on a pedestal. Each page whispers a question—knowledge itself feels haunted here.");
        } else {
            System.out.println("The mansion shifts around you. Another mystery emerges from the darkness...");
        }
    }        

    /**
     * Run a play session for the given user at the selected difficulty. This method:
     *  - loads rooms and flattens puzzles into an id map
     *  - allows the player to choose puzzles, request hints, use inventory, and submit answers
     *  - awards points, time penalties, and optional item rewards
     *  - persists progress and updates the leaderboard on session completion
     *
     * @param currentUser user playing the session
     * @param chosen difficulty selected for this session (cannot be null)
     */
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
            printPuzzleIntro(p);
            System.out.println("\nPuzzle: " + p.getQuestion());
            String puzzleMessage = "\nPuzzle: " + p.getQuestion();
            Speek.speak(puzzleMessage);
            boolean solvedCurrent = false;
            while (!solvedCurrent) {
                System.out.println("(type your answer, 'hint' for a hint, 'use <item>', or 'back' to return)");
                String answerMessage = "type your answer, 'hint' for a hint, 'use <item>', or 'back' to return";
                Speek.speak(answerMessage);
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
                        String hintMessage = "[HINT] " + nextHint + " (Penalty +" + formatSeconds(penalty) + ")";
                        Speek.speak(hintMessage);
                        DataLoader.saveUsers(userList.getAllUsers());
                    }
                    continue;
                }

                // attempt answer (respect lock)
                if (p.isLocked()) { 
                    System.out.println("This puzzle is locked. Try to 'use KEY' first.");
                    String lockedMessage = "This puzzle is locked. Try to 'use KEY' first.";
                    Speek.speak(lockedMessage);
                    continue;
                }
                boolean correct = false;
                try { correct = p.checkAnswer(answer); } catch (Exception e) { correct = false; }
                if (correct) {
                    System.out.println("Correct!");
                    String correctMessage = "Correct!";
                    Speek.speak(correctMessage);
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
                        String item = "You found a " + rewardName.name() + " as you solve the puzzle! It has been added to your inventory.";
                        Speek.speak(item);
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
            System.out.println("""
                        .-.
                     .-(o o)-.
                    /   (_)   \\
                    |  .---.  |
                    | (     ) |
                    |  `-.-'  |
                    |         |
                    | |     | |
                    | |     | |
                    |_|     |_|
                    (__)    (__)
                    
                    MWAHHAHHAA!
            """);

            System.out.println("");
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

    /**
     * Compute the time penalty (in seconds) applied when a hint is taken.
     *
     * @param pdiffLower difficulty as lowercase string
     * @return penalty seconds
     */
    private static int computeHintPenaltySeconds(String pdiffLower) {
        if ("FIXED".equalsIgnoreCase(HINT_PENALTY_MODE)) return HINT_FIXED_SECONDS;
        return HINT_PENALTY_SCALED.getOrDefault(pdiffLower.toLowerCase(), 30);
    }

    /**
     * Format a seconds count into minutes:seconds for human display.
     *
     * @param seconds seconds to format
     * @return formatted string like "3:05"
     */
    private static String formatSeconds(long seconds) {
        if (seconds < 0) seconds = 0;
        long mins = seconds / 60;
        long sec = seconds % 60;
        return String.format("%d:%02d", mins, sec);
    }

    /**
     * Lookup points awarded for a puzzle based on difficulty key.
     *
     * @param pdiffLower difficulty string in lower case
     * @return point value
     */
    private static int getPointsForDifficulty(String pdiffLower) {
        if (pdiffLower == null) return POINTS.getOrDefault("easy", 10);
        return POINTS.getOrDefault(pdiffLower.toLowerCase(), POINTS.get("easy"));
    }

    /**
     * Prompt the user to choose a difficulty and return the corresponding enum.
     *
     * @return selected Difficulty or null when canceled/invalid
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

    /**
     * Print the game's backstory and invoke speech output via Speek.
     */
    private void printBackstory() {
        System.out.println("\n--- SPOOKY BACKSTORY ---");
        System.out.println("You awaken in the Wilkes Mansion, a place long abandoned and whispered about in ghost stories.");
        System.out.println("The air hums with a strange energy—doors creak even when still, and the portraits seem to watch your every move.");
        System.out.println();
        System.out.println("Once the home of the eccentric inventor Dr. Lucien Wilkes, the mansion was said to hold his greatest creation:");
        System.out.println("a series of “living puzzles” designed to trap the curious and test the worthy.");
        System.out.println("Riddles echo through dark halls, alchemical fumes drift from the old laboratory,");
        System.out.println("and enchanted locks guard rooms that have not opened in a century.");
        System.out.println();
        System.out.println("To escape, you must solve the mansion’s mysteries—crack the haunted riddles, unseal cursed doors,");
        System.out.println("and master the forbidden math and alchemy that fuel its magic.");
        System.out.println("But beware: each puzzle draws you deeper into the house’s restless heart.");
        System.out.println();
        System.out.println("The mansion does not like to be solved.");
        System.out.println("------------------------");
        String message = 
        "SPOOKY BACKSTORY\n" +
        "You awaken in the Wilkes Mansion, a place long abandoned and whispered about in ghost stories.\n" +
        "The air hums with a strange energy—doors creak even when still, and the portraits seem to watch your every move.\n" +
        "\n" +
        "Once the home of the eccentric inventor Dr. Lucien Wilkes, the mansion was said to hold his greatest creation:\n" +
        "a series of “living puzzles” designed to trap the curious and test the worthy.\n" +
        "Riddles echo through dark halls, alchemical fumes drift from the old laboratory,\n" +
        "and enchanted locks guard rooms that have not opened in a century.\n" +
        "\n" +
        "To escape, you must solve the mansion’s mysteries—crack the haunted riddles, unseal cursed doors,\n" +
        "and master the forbidden math and alchemy that fuel its magic.\n" +
        "But beware: each puzzle draws you deeper into the house’s restless heart.\n" +
        "\n" +
        "The mansion does not like to be solved.";
        Speek.speak(message);
    }
    

    /**
     * Display a summary of the provided user's saved progress including percent complete
     * for the last chosen difficulty, score, time, and used hints.
     *
     * @param user the user whose progress will be displayed
     */
    private void displayUserProgress(User user) {
        if (user == null) return;
        Progress prog = user.getProgress();
        if (prog == null) {
            System.out.println("No progress recorded yet.");
            return;
        }
    
        // Determine the difficulty to evaluate against (user's last chosen, default ALL)
        Difficulty chosen = prog.getLastDifficultyAsEnum();
        if (chosen == null) chosen = Difficulty.ALL;
    
        // Load rooms to determine total puzzle count for the chosen difficulty
        int totalPuzzlesForDifficulty = 0;
        int completedCountForDifficulty = 0;
        List<EscapeRoom> rooms = Collections.emptyList();
        try {
            rooms = roomLoader.loadRooms("JSON/EscapeRoom.json");
            for (EscapeRoom r : rooms) {
                List<Puzzle> pz = r.getPuzzles();
                if (pz == null) continue;
                for (Puzzle pu : pz) {
                    String pd = pu.getDifficulty() == null ? "easy" : pu.getDifficulty().name().toLowerCase();
                    boolean matches = (chosen == Difficulty.ALL) || pd.equals(chosen.name().toLowerCase());
                    if (matches) {
                        totalPuzzlesForDifficulty++;
                        // count as completed if progress contains the puzzle id OR the question string (compatibility)
                        if (prog.hasCompletedByEither(pu.getId(), pu.getQuestion())) {
                            completedCountForDifficulty++;
                        }
                    }
                }
            }
        } catch (IOException e) {
            // If rooms can't be loaded, we will still show a conservative summary below
            System.err.println("Warning: couldn't load rooms to compute progress: " + e.getMessage());
        }
    
        double percent = 0.0;
        if (totalPuzzlesForDifficulty > 0) {
            percent = (completedCountForDifficulty * 100.0) / totalPuzzlesForDifficulty;
        }
    
        System.out.println("\n=== YOUR SAVED PROGRESS ===");
        System.out.printf("Player: %s%n", user.getName());
        if (totalPuzzlesForDifficulty > 0) {
            System.out.printf("Progress: %d / %d puzzles completed (%.1f%%) [based on %s difficulty]%n",
                    completedCountForDifficulty, totalPuzzlesForDifficulty, percent, chosen.name());
        } else {
            // fallback: show total completed questions (by question list) and indicate unknown total
            int completedByQuestion = prog.getCompletedPuzzles().size();
            System.out.printf("Progress: %d puzzles completed (total puzzles unknown for difficulty %s)%n",
                    completedByQuestion, chosen.name());
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
            // each entry: puzzle id -> count
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
    

    // Leaderboard helpers

    /**
     * Read the leaderboard JSON file and return a JSONArray (empty if missing or unreadable).
     *
     * @return parsed leaderboard array or an empty JSONArray
     */
    private JSONArray loadLeaderboardJson() {
        JSONParser parser = new JSONParser();
        try (FileReader fr = new FileReader(LEADERBOARD_PATH)) {
            Object obj = parser.parse(fr);
            if (obj instanceof JSONArray) return (JSONArray) obj;
            else return new JSONArray();
        } catch (FileNotFoundException e) { return new JSONArray(); }
        catch (IOException | ParseException e) { System.err.println("Failed to read leaderboard.json: " + e.getMessage()); return new JSONArray(); }
    }

    /**
     * Update or append an entry in the leaderboard file. If an entry for the same user
     * and difficulty exists, it is replaced only if the new score is higher or the same
     * score was achieved in less time.
     *
     * @param username player's name
     * @param newScore player's score
     * @param difficulty difficulty key (lowercase or "all")
     * @param newTimeSpent total time in seconds for this player
     */
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
