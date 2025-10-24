package com.model;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
//import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This class loads data from the repository (i.e. JSON file) and returns it to
 * be used in the Escape Room classes.
 */
public class DataLoader extends DataConstants {

    public static ArrayList<User> getUsers() 
    {
        ArrayList<User> users = new ArrayList<>();

        JSONParser parser = new JSONParser();
        try(FileReader reader = new FileReader(USER_DATA_FILE))
        {
            Object obj = parser.parse(reader);
            JSONArray usersArray = (JSONArray) obj;

            for (Object o: usersArray)
            {
                JSONObject userJSON = (JSONObject) o;

                String idStr = (String) userJSON.get(KEY_ID);
                UUID id = idStr != null && !idStr.equals("null") ? UUID.fromString(idStr) : null;
                String username = (String) userJSON.get(KEY_USERNAME);
                String password = (String) userJSON.get(KEY_PASSWORD);

                User user = new User(username, password, id);

                JSONArray charsJSON = (JSONArray) userJSON.get(KEY_CHARACTERS);
                if (charsJSON != null)
                {
                    for (Object charObj : charsJSON)
                    {
                        JSONObject charJSON = (JSONObject) charObj;
                        String charName = (String) charJSON.get("name");
                        Long levelLong = (Long) charJSON.get("level");
                        String avatar = (String) charJSON.get("avatar");
                        int level = levelLong != null ? levelLong.intValue() : 0;
                        Character character = new Character(charName, level, avatar);
                        user.addCharacter(character);
                    }
                }

                users.add(user);

            }
        } catch (IOException | ParseException e) {
            System.err.println(ERROR_LOADING_DATA + " " + e.getMessage());
        }

        return users;
    }

    // created method to save the newly created users
    // check with professor to see if this is correct
    public static void saveUsers(ArrayList<User> users)
    {
        JSONArray usersArray = new JSONArray();
        for (User user : users)
        {
            JSONObject userJSON = new JSONObject();
            userJSON.put(KEY_ID, user.getID().toString());
            userJSON.put(KEY_USERNAME, user.getName());
            userJSON.put(KEY_PASSWORD, user.getPassword());

            JSONArray charactersArray = new JSONArray();

            /*
             * Robust iteration for user.getCharacters():
             * - Accept Iterable<?> (List, ArrayList, etc)
             * - Accept array (Character[])
             * - Avoid compile-time mismatch if getCharacters() returns a raw type
             */
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
            // attach characters array (empty if none)
            userJSON.put(KEY_CHARACTERS, charactersArray);
            usersArray.add(userJSON);
        }
        try (FileWriter file = new FileWriter(USER_DATA_FILE))
        {
            file.write(usersArray.toJSONString());
            file.flush();
        } catch (IOException e) {
            System.err.println("Error saving users!" + e.getMessage());
        }
    }

// test to see if it works
public static void main (String[] args)
 {
    ArrayList<User> users = DataLoader.getUsers();

    for (User user : users)
    {
        System.out.println(user);
    }
 }

}//end DataLoader
