package com.model;

public class DataConstants {

    public static final String DATA_DIRECTORY = "JSON/";

    public static final String USER_DATA_FILE = DATA_DIRECTORY + "users.json";
    public static final String ROOM_DATA_FILE = DATA_DIRECTORY + "rooms.json";

    public static final String ERROR_LOADING_DATA = "Error loading data from file.";
    public static final String ERROR_SAVING_DATA = "Error saving data to file.";

    private DataConstants() {
        
    }
}
