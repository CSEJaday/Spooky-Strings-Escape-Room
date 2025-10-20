package com.model;
import java.io.*;
import java.util.ArrayList;

/**
 * The {@code UserLoader} class is responsible for managing user data stored in a simple text file.
 * It uses the Singleton design pattern to ensure that only one instance of the loader exists.
 * This class supports loading, adding, clearing, and saving users.
 */
public class UserLoader {
    /** The path to the file that stores user information. */
    private static final String FILE_PATH = "users.json";

    /** The single instance of UserLoader. */
    private static UserLoader instance;

    /** In-memory list of all loaded users. */
    private ArrayList<User> users;

    /**
     * Private constructor to prevent direct instantiation.
     * Loads users from the file into memory.
     */
    private UserLoader() {
        users = loadUsers();
    }

    /**
     * Returns the single instance of {@code UserLoader}.
     *
     * @return the instance of UserLoader
     */
    public static UserLoader getInstance() {
        if (instance == null) instance = new UserLoader();
        return instance;
    }

    /**
     * Returns the list of all users currently loaded in memory.
     *
     * @return an ArrayList} of User objects
     */
    public ArrayList<User> getUsers() {
        return users;
    }

    /**
     * Adds a user to the in-memory list and saves all users to the file.
     *
     * @param user the User object to add
     */
    public void addUser(User user) {
        users.add(user);
        saveUsers();
    }

    /**
     * Clears all users from memory and from the file.
     */
    public void clearUsers() {
        users.clear();
        saveUsers();
    }

    /**
     * Adds a single user directly to the file 
     * and also updates the in-memory list.
     *
     * @param user the User object to add
     */
    public void addUserToJSON(User user) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(user.toString());
            writer.newLine();
            System.out.println("User added successfully!");
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }

        users.add(user); // update in-memory list
    }

    /**
     * Loads all users from the file into an ArrayList.
     * If the file does not exist, an empty list is returned.
     *
     * @return a list of users read from the file
     */
    private ArrayList<User> loadUsers() {
        ArrayList<User> loadedUsers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                User user = User.fromString(line);
                if (user != null) loadedUsers.add(user);
            }
        } catch (FileNotFoundException e) {
            // File will be created later if it doesn't exist
        } catch (IOException e) {
            System.out.println("Error reading users: " + e.getMessage());
        }

        return loadedUsers;
    }

    /**
     * Saves all users currently in memory to the file,
     * overwriting any existing content.
     */
    private void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (User user : users) {
                writer.write(user.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }
}
