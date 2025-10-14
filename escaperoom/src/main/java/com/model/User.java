package com.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class User {
    private String username;
    private String password;
    private UUID id;
    private ArrayList<Character> character;
    private HashMap<String, Settings> settings;

    public User(String username, String password, UUID id) 
    {
        this.username = username;
        this.password = password;
        this.id = id;
        this.character = new ArrayList<>();
        this.settings = new HashMap<>();
    }

    public void addCharacter(Character character)
    {
    }

    public void deleteCharacter(String name, String avatar) 
    {

    }
}

