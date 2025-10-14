package com.model;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class UserLoader {
    private static UserLoader instance;
    private ArrayList<User> users;

    private UserLoader()
    {
        users = DataLoader.getUsers(); //load users from json
    }

    public static UserLoader getInstance()
    {
        if (instance == null)
        {
            instance = new UserLoader();
        }
        return instance;
    }

    public ArrayList<User> getUsers()
    {
        return users;
    }

    public void addUser(User user)
    {
        users.add(user);
    }

    public void clearUsers()
    {
        users.clear();
    }
}
