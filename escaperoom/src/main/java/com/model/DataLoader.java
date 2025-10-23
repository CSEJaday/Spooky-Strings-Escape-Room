package com.model;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
//import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Scanner;
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
            userJSON.put(KEY_ID, user.getId().toString());
            userJSON.put(KEY_USERNAME, user.getUsername());
            userJSON.put(KEY_PASSWORD, user.getPassword());

            JSONArray charactersArray = new JSONArray();
            for (Character character : user.getCharacters())
            {
                JSONObject charJSON = new JSONObject();
                charJSON.put("name", character.getName());
                charJSON.put("level", character.getLevel());
                charJSON.put("avatar", character.getAvatar());
                charactersArray.add(charJSON);
            }
            userJSON.put(KEY_CHARACTERS, charactersArray);
            usersArray.add(userJSON);
        }
        try (FileWriter file = new FileWriter(USER_DATA_FILE))
        {
            file.write(usersArray.toJSONString());
            file.flush();
        } catch (IOException e) {
            System.err.println("Error saving users!" + e.getMessage());
            System.out.println("Choose option: \n 1 to sign in, \n 2 to create account");
        }
    }

/* 
// test to see if it works
public static void main(String[] args) 
{
    Scanner key = new Scanner(System.in);
    UserList userList = UserList.getInstance();

    // Load users from data source
    //userList.loadUsers(); 

    System.out.println("Choose option 1 to sign in, 2 to create account");
    String option = key.nextLine();

    if (option.equals("1")) 
    {
        // Signing in
        System.out.println("Enter username:");
        String username = key.nextLine();

        System.out.println("Enter password:");
        String password = key.nextLine();

        User user = userList.getUserByName(username);

        if (user != null && user.getPassword().equals(password)) 
        {
            System.out.println("Sign-in successful! Welcome, " + user.getUsername() + "!");
        } 
        else 
        {
            System.out.println("Invalid username or password!");
        }

    } else if (option.equals("2")) 
    {
        // Creating an account
        System.out.println("Enter new username:");
        String newUsername = key.nextLine();

        System.out.println("Enter new password:");
        String newPassword = key.nextLine();

        boolean successful = userList.createAccount(newUsername, newPassword);
        if (successful) 
        {
            System.out.println("Account has been created!");
        } 
        else 
        {
            System.out.println("This username is already taken.");
        }

    } 
    else 
    {
        System.out.println("Invalid option.");
    }

    // Display all users for testing
    userList.loadUsers();
    ArrayList<User> users = DataLoader.getUsers();  // Or userList.getAllUsers();
    for (User u : users) {
        System.out.println(u);
    }

    key.close();
*/    }