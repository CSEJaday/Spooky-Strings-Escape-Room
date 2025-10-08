package com.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class HintsLoader {
    // singleton
    private static HintsLoader instance;

    // explain
    private HintsLoader() {}

    // public method to get the singleton instance
    public static HintsLoader getInstance()
    {
       if (instance == null) 
       {
          instance = new HintsLoader();
       }
       return instance;
       
    }

    // method to load data from a file
    public HashMap<Integer, String[]> loadData(String filepath) {
        HashMap<Integer, String[]> hintsMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null)
            {
                // assuming each line has the proper formatting
                String[] parts = line.split

            }
        }
    }
        
    
}
