package com.model;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

 
/**
 * Handles writing user data to JSON files, including saving users,
 * updating progress, and handling logout data persistence.
 *
 * Extends {@link DataConstants} to reuse key names, file paths,
 * and error message constants.
 */
public class DataWriter extends DataConstants {
    /** Path to the main user JSON file. */
    public static final String FILENAME = "JSON/User.json";   //check this for the correct filepath

    /**
     * Saves all current users from the {@link UserList} singleton
     * to the user data JSON file. Each user is converted to JSON format.
     */
    public static void savePlayers() 
    {
        UserList users = UserList.getInstance();
        ArrayList<User> userList = users.getAllUsers();

        JSONArray jsonUsers = new JSONArray();

        for (User user : userList) 
        {
            jsonUsers.add(getUserJSON(user));
        }

        try (FileWriter file = new FileWriter(USER_DATA_FILE_TEST)) 
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

    /**
     * Writes the provided user data to the JSON file.
     * Used primarily when adding a new user for the first time.
     * This method does not check for duplicates.
     *
     * @param user the {@link User} object to write to the JSON file
     */
    public void writeUserData(User user){
    
        try {
            FileWriter file = new FileWriter(FILENAME);
            file.write(user.toString());
            System.out.println ("Successfully wrote user to JSON file ");
            
        } catch (IOException e) {
        }
    }

    /**
     * Updates an existing user's JSON entry to include new progress data.
     * Overwrites the corresponding user's entry with updated progress values.
     *
     * @param user the {@link User} whose progress should be updated
     */
    public void saveProgress(User user) {

        //need to find the correct JSON entry and either completely overwrite it will all of the user data
        //or just update the Progress data
        try {
            FileWriter file = new FileWriter(FILENAME);
            file.write(user.toString());
            System.out.println ("Successfully wrote user to JSON file ");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs out the given user and writes their latest data to the JSON file.
     * Functionally similar to {@link #saveProgress(User)}.
     *
     * @param user the {@link User} to log out
     */
    public void logOut(User user) {
        try {
            FileWriter file = new FileWriter(FILENAME);
            file.write(user.toString());
            System.out.println ("Successfully wrote user to JSON file ");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Converts a {@link User} object into a JSON representation.
     *
     * @param user the {@link User} to convert
     * @return a {@link JSONObject} containing user data
     */
    @SuppressWarnings("unchecked")
    public static JSONObject getUserJSON(User user) 
    {
        JSONObject userDetails = new JSONObject();

        userDetails.put(KEY_USERNAME, safeString(user.getName()));
        userDetails.put(KEY_PASSWORD, safeString(user.getPassword()));
        userDetails.put(KEY_ID, user.getID() != null ? user.getID().toString() : "null");
 /* 
        JSONArray charactersArray = new JSONArray();

        if (user.getCharacters() != null) 
        {
            for (Character character : user.getCharacters()) 
            {
                if (character == null) continue;
                JSONObject charObj = new JSONObject();
                charObj.put("avatar", safeString(character.getAvatar()));
                charObj.put("name", safeString(character.getName()));
                charObj.put("level", character.getLevel()); // assuming level is int
                // Add more fields if needed
                charactersArray.add(charObj);
            }
        }

        userDetails.put(KEY_CHARACTERS, charactersArray);
        */
        // Optionally: add settings, etc. here if needed
        return userDetails;
    }

    /**
     * Ensures a non-null string is returned.
     *
     * @param input the string to check
     * @return the original string if not null, otherwise an empty string
     */
    private static String safeString(String input) 
    {
        return input != null ? input : "";
    }

    /**
     * Logs out all users by saving their data and clearing user memory.
     * Called when the application is shutting down or logging out globally.
     */
    public static void logQuit() {
        System.out.println("Logging out...");
        savePlayers();
        UserLoader.getInstance().clearUsers();
        System.out.println("Logged out.");
    }

    /** Basic test method for manual verification. */
    public static void main(String[] args) 
    {
        savePlayers();
        // logQuit(); // Uncomment to test logout saving and clearing
    }
}
