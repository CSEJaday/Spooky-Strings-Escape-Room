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
    public HashMap<Integer, String[]> loadData(String filepath) 
    {
        HashMap<Integer, String[]> hintsMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader("hints.txt"))) 
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                // assuming each line has the proper formatting
                String[] parts = line.split("\\|");
                if (parts.length != 2)
                {
                    continue;
                }

                int key;
                try 
                {
                    key = Integer.parseInt(parts[0].trim());
                } catch (NumberFormatException e) {
                    continue;
                }

                String[] hints = parts[1].split(",");

                for (int i = 0; i < hints.length; i++)
                {
                    hints[i] = hints[i].trim();
                }

                hintsMap.put(key, hints);

            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return hintsMap;
    }
        
    
}
