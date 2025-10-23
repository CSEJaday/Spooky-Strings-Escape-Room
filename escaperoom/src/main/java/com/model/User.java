
package com.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;

/**
 * Represents a user with login credentials, a unique ID, and a list of characters.
 * @param <Settings>
 */
public class User<Settings> {
    private String username;
    private String password;
    private UUID id;
    private ArrayList<Character> characterList;
    private HashMap<String, Settings> settings;
    private Progress progress;

    /**
     * Default Constructor that only requires the username, password and UUID. This would be used when a new user first 
     * creates an account. The characterList is empty, the Hashmap for setting is empty and the progress object is initialized
     * to default vaules.
     * @param username - the string representing the username of the user
     * @param password - a string password for this user (question - do we need to encrypt this?)
     * @param id - the UUID for this user.
     */
    public User(String username, String password, UUID id) {
        this.username = username;
        this.password = password;
        this.id = id;
        characterList = new ArrayList<Character>();
        settings = new HashMap();
        progress = new Progress();

        //do we write this to the JSON file when we create the user? I think we need to do this so that
        //we can update the user info as they play the game.

    }//end default constructor

    /**
     * This constructor is used by Leaderboard to retrieve existing users to display on the Leaderboard. Data is retrieved
     * from the JSON file and provided to the constructor
     * @param name = username for this User
     * @param pwd = password for the User
     * @param id = UUID for the User
     * @param characters = ArrayList of Characters for this User
     * @param settings = Hashmap of setting for this User
     * @param prg = Progress object for this User.
     */

    public User(String name, String pwd, UUID id, ArrayList<Character> characters, HashMap settings, Progress prg) {
        
        username = name;
        password = pwd;
        this.id = id;
        characterList = characters;
        this.settings = settings;
        progress = prg;
        
    }//end second constructor

    /**
     *  is there a  use case for another User constructor? Would we create a user AFTER they played a game and have a score?
     * or would we just create the user when they first log on to play the game and then update fields as they progress 
     * through the game?
     * 
     */

    /**
     * Adds a Character to the ArrayList of Characters for this User.
     * @param character
     */
    public void addCharacter(Character character) {
        characterList.add(character);
    }//end addCharacter()

    /**
     * Deletes the specified character from the characterList for this user.
     * @param character
     */
    public void deleteCharacter(Character character) {
        characterList.remove (character);    
    } //end deleteCharacter()

    /**
     * Updates the information in the Progress object for this User
     * @param progress - a Progress object containing the updated information for this User
     */
    public void setProgress (Progress progress){
        this.progress = progress;

    } // end setProgress

    /**
     * Returns the current Progress Object for this User
     * @return progress object for this User
     */
    public Progress getProgress (){
        return progress;
    } //end getProgress

    /**
     * Updates the settings for this User
     * @param settings - Hashmap containing settings for this user
     *
     */
    public void updateSettings (HashMap settings){
        this.settings  = settings;
    } // end updateSettings

    /**
     * Returns the Hashmap containing the settings for this User
     * @return settings - the Hashmap containing the settings for this User
     */
    public HashMap getSettings(){
        return settings;
    } // end getSettings

    public String getName(){
        return username;
    }
    public String getUsername(){
        return username;
    }
    

    public String getPassword(){
        return password;
    }

    public UUID getID(){
        return id;
    }

    /**
     * Returns a string representation of this User and all of their data
     * @return a string representation of this User and all of their data
     */
    @Override
    public String toString(){
        String userData = ("\"username:\" " + username + " \"password:\" " + password +
                " \"id:\" " + id + " \"characters:\" " + characterList + " \"Level:\" " + progress.getCurrentLevel() +
                " \"puzzles:\" " + progress.getCompletedPuzzles() + " \"time spent:\" " + progress.getTimeSpent() + 
                " \"score:\" " + progress.getScore());

        System.out.println("User Data = " + userData);

        return userData;
    }

    public Character[] getCharacters() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCharacters'");
    }

    public static User fromString(String line) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromString'");
    }
}//end User
/* 
        this.characters = new ArrayList<>();
    }

    // Add a character to the user
    public void addCharacter(Character character) {
        if (character != null) {
            characters.add(character);
        }
    }

    // Remove a character by name and avatar
    public boolean deleteCharacter(String name, String avatar) {
        Iterator<Character> iterator = characters.iterator();
        while (iterator.hasNext()) {
            Character character = iterator.next();
            if (character.getName().equals(name) && character.getAvatar().equals(avatar)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public UUID getId() {
        return id;
    }

    public ArrayList<Character> getCharacters() {
        return characters;
    }

    // Optional: Debugging output
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", id=" + id +
                ", characterCount=" + characters.size() +
                '}';
    }
<<<<<<< HEAD


    public static User fromString(String line) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromString'");
    }
}
=======
}
    */

>>>>>>> 0563f153d54f9fde8ab3b73dff3892622bbf4534
