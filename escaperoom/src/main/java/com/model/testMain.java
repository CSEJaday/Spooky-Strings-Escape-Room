package com.model;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Explain how code works: So I created two scenarios one for loging in and one for creating an account.
 * 
 * For loging in I am allowing new users to put in their information (username and password)
 * and once I have those two pieces of information i can take that and save it to the json file.
 * 
 * {@code UserList} is a singleton that holds all of the users data in memory, handles looking up the user
 * and deals with account creation. 
 * 
 * {@code DataLoader} loads and saves the user data from and to the JSON file.
 * {@code User} represents the attributes of each user. So the username, password, ID,
 * and a list of characters. 
 */
public class testMain {

    public static void main(String[] args) 
{
    Scanner key = new Scanner(System.in);
    //intializes UserList
    //uses singleton, only one shared list of users
    UserList userList = UserList.getInstance();

    System.out.println("Choose option: \n 1. Sign in \n 2. Create an account");
    String option = key.nextLine();

    if (option.equals("1")) 
    {
        // Signing in
        System.out.println("Enter username:");
        String username = key.nextLine();

        System.out.println("Enter password:");
        String password = key.nextLine();

        //looks up the username in userList.
        //if the username exists and the password is a match, the user is logged in.
        User user = userList.getUserByName(username);
        if (user != null && user.getPassword().equals(password)) 
        {
            System.out.println("Log in successful! Welcome " + user.getUsername() + "!");
        } 
        else 
        {
            System.out.println("Invalid username or password!");
        }

    } 
    else if (option.equals("2")) 
    {
        // Creating an account
        System.out.println("Enter new username:");
        String newUsername = key.nextLine();

        System.out.println("Enter new password:");
        String newPassword = key.nextLine();

        //create account checks if username has been taken
        //if not it will create a new user object with a UUID, add it to the internal list,
        //then calls DataLoader.saveUsers() to write the updated list back to the json file.
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
    // Load users from data source
    //userList.loadUsers(); 
    
    //reads users directly from json file and returns a new list of user objects
    ArrayList<User> users = DataLoader.getUsers();
    /* 
    ArrayList<User> users = DataLoader.getUsers();  // Or userList.getAllUsers();
    for (User u : users) 
    {
        System.out.println(u);
    }
    */
    key.close();

    }
}