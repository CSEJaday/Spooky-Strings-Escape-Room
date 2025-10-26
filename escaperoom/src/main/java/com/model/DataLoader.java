package com.model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;

/**
 * DataLoader - read + write users.json including Progress inventory and completed puzzle ids.
 *
 * Save format is compatible with saveUsers(List<User>) implemented previously.
 */
public final class DataLoader {

    private static final String JSON_DIR = System.getProperty("user.dir") + "/JSON";
    private static final String USERS_PATH = JSON_DIR + "/users.json";

    private DataLoader() {}

    // -----------------------
    // Saving (same as provided earlier)
    // -----------------------
    @SuppressWarnings("unchecked")
    public static void saveUsers(List<User> users) {
        if (users == null) users = List.of();

        JSONArray arr = new JSONArray();

        for (User u : users) {
            JSONObject obj = new JSONObject();
            obj.put("username", safeString(u.getName()));
            obj.put("password", safeString(u.getPassword()));

            // Progress
            Progress p = u.getProgress();
            if (p != null) {
                JSONObject pj = new JSONObject();
                pj.put("timeSpent", p.getTimeSpent());
                pj.put("score", p.getScore());

                // completed puzzle ids (if available)
                JSONArray ids = new JSONArray();
                try {
                    for (Integer id : p.getCompletedPuzzleIds()) {
                        if (id != null) ids.add(id);
                    }
                } catch (Throwable ignore) { /* ignore if method not present or returns null */ }
                pj.put("completedPuzzleIds", ids);

                // completed puzzles by question (backwards compat)
                JSONArray questions = new JSONArray();
                try {
                    for (String q : p.getCompletedPuzzles()) {
                        if (q != null) questions.add(q);
                    }
                } catch (Throwable ignore) {}
                pj.put("completedPuzzles", questions);

                // hintsUsed map
                JSONObject hintsObj = new JSONObject();
                try {
                    for (Map.Entry<Integer,Integer> e : p.getHintsUsed().entrySet()) {
                        Integer key = e.getKey();
                        Integer val = e.getValue();
                        if (key != null && val != null) hintsObj.put(String.valueOf(key), val);
                    }
                } catch (Throwable ignore) {}
                pj.put("hintsUsed", hintsObj);

                // lastDifficulty
                try {
                    Difficulty d = p.getLastDifficultyAsEnum();
                    pj.put("lastDifficulty", d == null ? "ALL" : d.name());
                } catch (Throwable ignore) {
                    pj.put("lastDifficulty", "ALL");
                }

                // inventory: serialize quantities as map ITEM_NAME -> qty
                JSONObject invObj = new JSONObject();
                try {
                    Inventory inv = p.getInventory();
                    if (inv != null) {
                        Map<ItemName,Integer> qty = inv.getQuantities();
                        for (Map.Entry<ItemName,Integer> e : qty.entrySet()) {
                            ItemName name = e.getKey();
                            Integer qn = e.getValue();
                            if (name != null && qn != null && qn > 0) {
                                invObj.put(name.name(), qn);
                            }
                        }
                    }
                } catch (Throwable ignore) {}
                pj.put("inventory", invObj);

                obj.put("progress", pj);
            } else {
                // no progress -> insert an empty progress object for future-proofing
                JSONObject pj = new JSONObject();
                pj.put("timeSpent", 0);
                pj.put("score", 0);
                pj.put("completedPuzzleIds", new JSONArray());
                pj.put("completedPuzzles", new JSONArray());
                pj.put("hintsUsed", new JSONObject());
                pj.put("lastDifficulty", "ALL");
                pj.put("inventory", new JSONObject());
                obj.put("progress", pj);
            }

            arr.add(obj);
        }

        // Ensure directory exists
        try {
            File dir = new File(JSON_DIR);
            if (!dir.exists()) dir.mkdirs();
        } catch (Throwable ignore) {}

        // Write file
        try (FileWriter fw = new FileWriter(USERS_PATH)) {
            fw.write(arr.toJSONString());
            fw.flush();
        } catch (IOException e) {
            System.err.println("Failed to write users.json: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // -----------------------
    // Loading: reconstruct Users and Progress (inventory + completed ids)
    // -----------------------
    public static ArrayList<User> getUsers() {
        ArrayList<User> out = new ArrayList<>();

        File f = new File(USERS_PATH);
        if (!f.exists()) {
            // Nothing saved yet -> return empty list
            return out;
        }

        JSONParser parser = new JSONParser();
        try (FileReader fr = new FileReader(f)) {
            Object top = parser.parse(fr);
            if (!(top instanceof JSONArray)) return out;
            JSONArray arr = (JSONArray) top;
            for (Object o : arr) {
                if (!(o instanceof JSONObject)) continue;
                JSONObject jo = (JSONObject) o;
                String username = safeString((String) jo.getOrDefault("username", ""));
                String password = safeString((String) jo.getOrDefault("password", ""));
                // Try to read id if present (some older files won't have it)
                UUID uuid = null;
                try {
                    Object idObj = jo.get("id");
                    if (idObj != null) {
                        String idStr = idObj.toString();
                        uuid = UUID.fromString(idStr);
                    }
                } catch (Throwable ignore) {}

                // Create user with provided constructor (this sets up default Progress)
                User user = new User(username, password, uuid);

                // Parse progress object if present
                try {
                    Object progObj = jo.get("progress");
                    Progress prog = new Progress(); // fresh progress to populate
                    if (progObj instanceof JSONObject) {
                        JSONObject pj = (JSONObject) progObj;

                        // timeSpent
                        try {
                            Object t = pj.get("timeSpent");
                            if (t instanceof Number) prog.addTime(((Number) t).longValue());
                            else if (t != null) prog.addTime(Long.parseLong(t.toString()));
                        } catch (Throwable ignore) {}

                        // score
                        try {
                            Object sc = pj.get("score");
                            if (sc instanceof Number) prog.increaseScore(((Number) sc).intValue());
                            else if (sc != null) prog.increaseScore(Integer.parseInt(sc.toString()));
                        } catch (Throwable ignore) {}

                        // completedPuzzleIds (array)
                        try {
                            Object idsObj = pj.get("completedPuzzleIds");
                            if (idsObj instanceof JSONArray) {
                                JSONArray idsArr = (JSONArray) idsObj;
                                for (Object ido : idsArr) {
                                    try {
                                        int idVal = -1;
                                        if (ido instanceof Number) idVal = ((Number) ido).intValue();
                                        else if (ido != null) idVal = Integer.parseInt(ido.toString());
                                        if (idVal >= 0) prog.addCompletedPuzzleId(idVal);
                                    } catch (Throwable ignore) {}
                                }
                            }
                        } catch (Throwable ignore) {}

                        // completedPuzzles (strings) - backwards compat
                        try {
                            Object qObj = pj.get("completedPuzzles");
                            if (qObj instanceof JSONArray) {
                                JSONArray qArr = (JSONArray) qObj;
                                for (Object qq : qArr) {
                                    if (qq != null) prog.addCompletedPuzzle(qq.toString());
                                }
                            }
                        } catch (Throwable ignore) {}

                        // hintsUsed (object map)
                        try {
                            Object hu = pj.get("hintsUsed");
                            if (hu instanceof JSONObject) {
                                JSONObject huz = (JSONObject) hu;
                                for (Object key : huz.keySet()) {
                                    try {
                                        String kstr = key.toString();
                                        int kid = Integer.parseInt(kstr);
                                        Object v = huz.get(key);
                                        int count = 0;
                                        if (v instanceof Number) count = ((Number) v).intValue();
                                        else if (v != null) count = Integer.parseInt(v.toString());
                                        // increment that many times (Progress API exposes increment)
                                        for (int i = 0; i < count; i++) prog.incrementHintsUsedFor(kid);
                                    } catch (Throwable ignore) {}
                                }
                            }
                        } catch (Throwable ignore) {}

                        // lastDifficulty
                        try {
                            Object ld = pj.get("lastDifficulty");
                            if (ld != null) {
                                Difficulty d = Difficulty.fromString(ld.toString());
                                if (d != null) prog.setLastDifficulty(d);
                            }
                        } catch (Throwable ignore) {}

                        // inventory: object map ITEM_NAME -> qty
                        try {
                            Object invObj = pj.get("inventory");
                            if (invObj instanceof JSONObject) {
                                JSONObject invJson = (JSONObject) invObj;
                                Inventory inv = prog.getInventory();
                                for (Object k : invJson.keySet()) {
                                    try {
                                        String name = k.toString();
                                        int qty = 0;
                                        Object v = invJson.get(k);
                                        if (v instanceof Number) qty = ((Number) v).intValue();
                                        else if (v != null) qty = Integer.parseInt(v.toString());
                                        if (qty <= 0) continue;

                                        // Map string to ItemName if possible
                                        try {
                                            ItemName iname = ItemName.valueOf(name.trim().toUpperCase());
                                            // Create a simple template matching known items (sensible defaults)
                                            Item template = switch (iname) {
                                                case KEY -> new Item(ItemName.KEY, "A small iron key. Might open a lock.", true, true, "You used the key.");
                                                case TORCH -> new Item(ItemName.TORCH, "A wooden torch to light dark places.", true, false, "You light the torch; shadows recede.");
                                                case POTION -> new Item(ItemName.POTION, "A mysterious potion. Drink to heal.", true, true, "You drink the potion; you feel better.");
                                                default -> new Item(iname, "An item: " + iname.name(), false, false, "");
                                            };
                                            // Use Inventory's convenience method to add by name with template if new
                                            inv.addItemByName(iname, qty, template);
                                        } catch (IllegalArgumentException iae) {
                                            // unknown item name -> skip
                                        }
                                    } catch (Throwable ignore) {}
                                }
                            }
                        } catch (Throwable ignore) {}
                    }
                    // Attach the reconstructed progress to the user
                    user.setProgress(prog);
                } catch (Throwable t) {
                    // If anything went wrong populating progress, keep the default Progress created by User constructor
                    // but print a debug note
                    System.err.println("Warning: failed to fully parse progress for user " + username + " : " + t.getMessage());
                }

                out.add(user);
            }
        } catch (IOException | ParseException e) {
            System.err.println("Failed to read users.json: " + e.getMessage());
            e.printStackTrace();
            // return whatever we've parsed so far (maybe empty)
        }

        return out;
    }

    // small helper
    private static String safeString(String s) {
        return s == null ? "" : s;
    }
}



