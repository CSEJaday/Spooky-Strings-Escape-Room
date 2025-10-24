package com.model;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * DataLoader with progress.lastDifficulty read/write support.
 */
public class DataLoader extends DataConstants {

    public static ArrayList<User> getUsers() {
        ArrayList<User> users = new ArrayList<>();

        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(USER_DATA_FILE)) {
            Object obj = parser.parse(reader);
            if (!(obj instanceof JSONArray)) return users;
            JSONArray usersArray = (JSONArray) obj;

            for (Object o : usersArray) {
                if (!(o instanceof JSONObject)) continue;
                JSONObject userJSON = (JSONObject) o;

                String idStr = (String) userJSON.get(KEY_ID);
                UUID id = idStr != null && !idStr.equals("null") ? UUID.fromString(idStr) : UUID.randomUUID();
                String username = (String) userJSON.get(KEY_USERNAME);
                String password = (String) userJSON.get(KEY_PASSWORD);

                User user = new User(username, password, id);

                // Characters (optional)
                JSONArray charsJSON = (JSONArray) userJSON.get(KEY_CHARACTERS);
                if (charsJSON != null) {
                    for (Object charObj : charsJSON) {
                        if (!(charObj instanceof JSONObject)) continue;
                        JSONObject charJSON = (JSONObject) charObj;
                        String charName = (String) charJSON.get("name");
                        Long levelLong = (Long) charJSON.get("level");
                        String avatar = (String) charJSON.get("avatar");
                        int level = levelLong != null ? levelLong.intValue() : 0;
                        Character character = new Character(charName, level, avatar);
                        user.addCharacter(character);
                    }
                }

                // Progress (optional)
                JSONObject progObj = (JSONObject) userJSON.get("progress");
                if (progObj != null) {
                    Progress progress = new Progress();

                    // score
                    Object scoreObj = progObj.get("score");
                    if (scoreObj instanceof Number) {
                        progress.increaseScore(((Number) scoreObj).intValue());
                    } else if (scoreObj != null) {
                        try {
                            progress.increaseScore(Integer.parseInt(String.valueOf(scoreObj)));
                        } catch (NumberFormatException ignored) {}
                    }

                    // currentLevel
                    Object lvlObj = progObj.get("currentLevel");
                    if (lvlObj instanceof Number) {
                        progress.setCurrentLevel(((Number) lvlObj).intValue());
                    } else if (lvlObj != null) {
                        try {
                            progress.setCurrentLevel(Integer.parseInt(String.valueOf(lvlObj)));
                        } catch (NumberFormatException ignored) {}
                    }

                    // timeSpent
                    Object timeObj = progObj.get("timeSpent");
                    if (timeObj instanceof Number) {
                        progress.setTimeSpent(((Number) timeObj).longValue());
                    } else if (timeObj != null) {
                        try {
                            progress.setTimeSpent(Long.parseLong(String.valueOf(timeObj)));
                        } catch (NumberFormatException ignored) {}
                    }

                    // completedPuzzles
                    JSONArray completed = (JSONArray) progObj.get("completedPuzzles");
                    if (completed != null) {
                        for (Object cp : completed) {
                            if (cp != null) progress.addCompletedPuzzle(String.valueOf(cp));
                        }
                    }

                    // hintsUsed (object map string->number)
                    JSONObject hintsObj = (JSONObject) progObj.get("hintsUsed");
                    if (hintsObj != null) {
                        for (Object key : hintsObj.keySet()) {
                            String k = String.valueOf(key);
                            try {
                                int idx = Integer.parseInt(k);
                                Object val = hintsObj.get(key);
                                if (val instanceof Number) {
                                    progress.setHintsUsedFor(idx, ((Number) val).intValue());
                                } else if (val != null) {
                                    try {
                                        progress.setHintsUsedFor(idx, Integer.parseInt(String.valueOf(val)));
                                    } catch (NumberFormatException ignored) {}
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }

                    // NEW: lastDifficulty (string)
                    Object lastDiffObj = progObj.get("lastDifficulty");
                    if (lastDiffObj != null) {
                        progress.setLastDifficultyString(String.valueOf(lastDiffObj));
                    }

                    user.setProgress(progress);
                } // end progress

                users.add(user);
            }
        } catch (IOException | ParseException e) {
            System.err.println(ERROR_LOADING_DATA + " " + e.getMessage());
        }

        return users;
    }

    public static void saveUsers(ArrayList<User> users) {
        JSONArray usersArray = new JSONArray();
        for (User user : users) {
            JSONObject userJSON = new JSONObject();
            UUID uid = user.getID() != null ? user.getID() : UUID.randomUUID();
            userJSON.put(KEY_ID, uid.toString());
            userJSON.put(KEY_USERNAME, user.getName());
            userJSON.put(KEY_PASSWORD, user.getPassword());

            // characters
            JSONArray charactersArray = new JSONArray();
            Object charsObj = user.getCharacters();
            if (charsObj instanceof Iterable) {
                for (Object ch : (Iterable<?>) charsObj) {
                    if (ch instanceof Character) {
                        Character character = (Character) ch;
                        JSONObject charJSON = new JSONObject();
                        charJSON.put("name", character.getName());
                        charJSON.put("level", character.getLevel());
                        charJSON.put("avatar", character.getAvatar());
                        charactersArray.add(charJSON);
                    }
                }
            } else if (charsObj != null && charsObj.getClass().isArray()) {
                int len = java.lang.reflect.Array.getLength(charsObj);
                for (int i = 0; i < len; i++) {
                    Object ch = java.lang.reflect.Array.get(charsObj, i);
                    if (ch instanceof Character) {
                        Character character = (Character) ch;
                        JSONObject charJSON = new JSONObject();
                        charJSON.put("name", character.getName());
                        charJSON.put("level", character.getLevel());
                        charJSON.put("avatar", character.getAvatar());
                        charactersArray.add(charJSON);
                    }
                }
            }
            userJSON.put(KEY_CHARACTERS, charactersArray);

            // progress block
            Progress p = user.getProgress();
            if (p == null) p = new Progress();
            JSONObject prog = new JSONObject();
            prog.put("score", p.getScore());
            prog.put("currentLevel", p.getCurrentLevel());
            prog.put("timeSpent", p.getTimeSpent());

            JSONArray completed = new JSONArray();
            for (String s : p.getCompletedPuzzles()) completed.add(s);
            prog.put("completedPuzzles", completed);

            JSONObject hintsObj = new JSONObject();
            for (Map.Entry<Integer, Integer> e : p.getHintsUsed().entrySet()) {
                hintsObj.put(String.valueOf(e.getKey()), e.getValue());
            }
            prog.put("hintsUsed", hintsObj);

            // NEW: persist lastDifficulty string
            prog.put("lastDifficulty", p.getLastDifficulty());

            userJSON.put("progress", prog);

            usersArray.add(userJSON);
        }

        try (FileWriter file = new FileWriter(USER_DATA_FILE)) {
            file.write(usersArray.toJSONString());
            file.flush();
        } catch (IOException e) {
            System.err.println("Error saving users!" + e.getMessage());
        }
    }

    // test to see if it works
    public static void main(String[] args) {
        ArrayList<User> users = DataLoader.getUsers();
        for (User user : users) System.out.println(user);
    }
}


