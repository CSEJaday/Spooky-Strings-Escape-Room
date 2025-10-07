package com.model;

public class DataConstants {
    protected static final String USER_DATA_FILE = "escaperoom/src/main/java/com/data/users.json";
    protected static final String ROOM_DATA_FILE = "escaperoom/src/main/java/com/data/rooms.json";

    //User JSON Keys
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_ID = "id";
    public static final String KEY_CHARACTERS = "characters";
    public static final String KEY_SETTINGS = "settings";


    //Escape Room JSON Keys
    protected static final String KEY_NAME = "roomName";
    protected static final String KEY_DESCRIPTION = "description";
    protected static final String KEY_IS_SOLVED = "isSolved";
    protected static final String KEY_LEVEL = "level";

    protected static final String ERROR_LOADING_DATA = "Error loading data from file.";
    protected static final String ERROR_SAVING_DATA = "Error saving data to file.";

    private DataConstants() {

    }
}


