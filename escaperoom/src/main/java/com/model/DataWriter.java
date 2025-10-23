package com.model;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

 
public class DataWriter extends DataConstants {
    public static final String FILENAME = "JSON/User.json";   //check this for the correct filepath

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
     * Writes user data to the JSON file provided in the FILENAME constant. This will write ALL of the 
     * user data. It would be called when adding a new user to the JSON file for the first time. It does not search
     * for a duplicate entry
     * @param user the user data to write to JSON
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
     * Updates data in the JSON file for this User to include progress made. Searches for the correct entry and 
     * updates the Progress data provided in the User Object
     * @param User the User data to update progress for
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

    //is this any different than SaveProgress?
    public void logOut(User user) {
        try {
            FileWriter file = new FileWriter(FILENAME);
            file.write(user.toString());
            System.out.println ("Successfully wrote user to JSON file ");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
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

    private static String safeString(String input) 
    {
        return input != null ? input : "";
    }

    public static void logQuit() {
        System.out.println("Logging out...");
        savePlayers();
        UserLoader.getInstance().clearUsers();
        System.out.println("Logged out.");
    }

    public static void main(String[] args) 
    {
        savePlayers();
        // logQuit(); // Uncomment to test logout saving and clearing
    }
}
