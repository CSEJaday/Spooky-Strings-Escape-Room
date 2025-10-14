package com.model;

import java.io.FileWriter;
import java.io.IOException;

public class DataWriter {
    public static final String FILENAME = "JSON/User.json";   //check this for the correct filepath

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
            e.printStackTrace();
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
}
