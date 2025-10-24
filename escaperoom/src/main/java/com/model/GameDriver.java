package com.model;

public class GameDriver {

    private UserList userList;

    /**
     * Constructs a GameDriver instance then      
     * initializes the Userlist singleton.
     */
    public GameDriver()
    {
        userList = UserList.getInstance();
    }

    /**
     * Attempting to login a user with a specified username and password.
     * @param username The username to authenticate
     * @param password The password to authenticate 
     * @return {@code true} if the user exists and if the password matches.
     * @return {@code false} otherwise
     */
    public void login(String username, String password) 
    {
        if(Facade.getInstance().login(username, password)){
            System.out.println(Facade.getInstance().getCurrentUser());
        } else {
            System.out.println("login didn't work");
        }
    }
    

    /*
     * Main method to test the game
     */
    public static void main(String[] args) {
        EscapeRoomGameUI ui = new EscapeRoomGameUI();
        ui.run();
    }
}
   
