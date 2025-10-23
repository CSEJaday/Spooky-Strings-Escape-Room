package com.model;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This class loads data from the respository (i.e. JSON file) and returns it to
 * be used in the Escape Room classes.
 * @author 
 */
public class DataLoader extends DataConstants {

   /**
    *  Pulls user data from the JSON file and loads into User Objects that are 
     * added to an ArrayList
     * @return the list of users
    */
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

// test to see if it works
public static void main (String[] args)
 {
    ArrayList<User> users = DataLoader.getUsers();

    for (User user : users)
    {
        System.out.println(user);
    }
 }

}

/*
     * 
     * @param filePath
     * @return
     */
    //public T loadData(String filePath) 
    //{

    //}//end filePath()

    /**
     * Retrieves all of the Users stored in the User.json file, adds them to an ArrayList and return
     * the ArrayList.
     * @return The ArrayList of Users.
     */
    /* 
    public static ArrayList<User> getUsers() {
        try {
            FileReader file = new FileReader(User.json);
        } catch ( e) {
        }
    /*public static ArrayList<User> getUsers() {
        
        for (int i = 0; i < peopleJSON.size(); i++){
            JSONObject personJSON = (JSONObject)peopleJSON.get(i);
            UUID id = UUID.fromString((String)personJSON.get(USER_ID));
            String userName = (String)personJSON.get(USER_USER_NAME);
            String firstName = (String)personJSON.get(USER_FIRST_NAME);
            String lastName = (String)personJSON.get(USER_LAST_NAME);
            int age = ((Long)personJSON.get(USER_AGE)).intValue();
            String phoneNumber = (String)personJSON.get(USER_PHONE_NUMBER);

            user.add(new User(id, userName, firstName, lastName, age, phoneNumber));
        }
        return users;
    }//end getUsers()
    
}//end DataLoader()
*/