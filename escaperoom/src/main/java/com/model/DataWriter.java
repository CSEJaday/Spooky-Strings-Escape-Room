package com.model;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DataWriter extends DataConstants {

    public static void savePlayers() 
    {
        Users users = Users.getInstance();
        ArrayList<User> userList = users.getUsers();

        JSONArray jsonUsers = new JSONArray();

        for (User user : userList) 
        {
            jsonUsers.add(getUserJSON(user));
        }

        try (FileWriter file = new FileWriter(USER_DATA_FILE)) 
        {
            file.write(jsonUsers.toJSONString());
            file.flush();
            System.out.println("Users saved successfully to " + USER_DATA_FILE);
        } 
        catch (IOException e) 
        {
            System.err.println(ERROR_SAVING_DATA + " " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getUserJSON(User user) 
    {
        JSONObject userDetails = new JSONObject();

        userDetails.put(KEY_USERNAME, safeString(user.getUserName()));
        userDetails.put(KEY_PASSWORD, safeString(user.getPassWord()));
        userDetails.put(KEY_ID, user.getId() != null ? user.getId().toString() : "null");

        JSONArray charactersArray = new JSONArray();

        if (user.getCharacters() != null) 
        {
            for (Character character : user.getCharacters()) 
            {
                if (character == null) continue;
                JSONObject charObj = new JSONObject();
                charObj.put("name", safeString(character.getName()));
                charObj.put("level", character.getLevel()); // assuming level is int
                // Add more fields if needed
                charactersArray.add(charObj);
            }
        }

        userDetails.put(KEY_CHARACTERS, charactersArray);
        // Optionally: add settings, etc. here if needed
        return userDetails;
    }

    private static String safeString(String input) 
    {
        return input != null ? input : "";
    }

    public static void logQuit() {
        System.out.println("Logging out...");
        savePlayers();
        Users.getInstance().clearUsers();
        System.out.println("Logged out.");
    }

    public static void main(String[] args) 
    {
        savePlayers();
        // logQuit(); // Uncomment to test logout saving and clearing
    }
}
