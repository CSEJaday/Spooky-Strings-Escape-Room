package com.model;

import java.util.UUID;
import java.util.ArrayList;
import java.util.HashMap;


public class User {
    private String username;
    private String password;
    private UUID id;
    private ArrayList<Character> character;
    private HashMap<String, Settings> settings;
    private Progress progress;

    public User(String username, String password, UUID id, Progress progress) {

    }//end constructor

    public void addCharacter(Character character) {

    }//end addCharacter()

    public void deleteCharacter(String name, String avatar) {

    }//end deleteCharacter()

}//end User
