package com.model;

import java.util.ArrayList;
import java.util.UUID;

public class UserList {
    // List to store user objects
    private ArrayList<User> users;
    private static UserList userList;

    public static UserList getInstance()
    {
        if (userList == null)
        {
            userList = new UserList();
        }
        return userList;
    }

    // constructor
    private UserList()
    {
        //users = new ArrayList<>();
        //users.add(new User("asmith", "12345", UUID.randomUUID()));
        //users.add(new User("bsmith", "12345", UUID.randomUUID()));
        users = DataLoader.getUsers();
    }

    public ArrayList<User> getAllUsers()
    {
        return users;
    }

    public User getUserByName(String name) 
    {
        for (User user : users) 
        {
            if (user.getUsername().equalsIgnoreCase(name)) 
            {
                return user;
            }
        }
        return null;
    }

    // added this method so that i can add new users to the list
    // in my create account logic in the facade
    public void addUser(User user)
    {
        if (user != null)
        {
            users.add(user);
        }
    }
    
}

