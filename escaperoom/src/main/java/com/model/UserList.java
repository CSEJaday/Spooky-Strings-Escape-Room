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
        users = new ArrayList<>();
        users.add(new User("asmith", "12345", UUID.randomUUID()));
        users.add(new User("bsmith", "12345", UUID.randomUUID()));

    }

    public ArrayList<User> getAllUsers()
    {
        return users;
    }

    public String getUserByName(String name)
    {
        return null;
    }
}
