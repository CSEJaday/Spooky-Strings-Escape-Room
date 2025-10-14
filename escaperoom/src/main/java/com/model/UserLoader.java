package com.model;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class UserLoader 
{
    private static UserLoader userLoader;

    private UserLoader()
    {
        return;
    }

    public UserLoader getInstance()
    {
        return null;
    }

    public ArrayList<User> loadData(String filepath) {
        
}