package com.model;

public class Facade {
    private User currentUser;
    private static Facade facade;

    private Facade() {

    }

    public static Facade getInstance() {
        if(facade == null) {
            facade = new Facade();
        } 

        return facade;
    }

    public boolean login(String username, String password){
        User temp = UserList.getInstance().getUserByName(username);

        if(temp == null) {
            return false;
        }

        currentUser = temp;
        return true;
    }

    public User getCurrentUser() 
    {
        return currentUser;
    }

    public boolean createAccount(String username, String password)
    {
        UserList userList = UserList.getInstance();
        // ask professor if this is ok
        // if user already exists, this account cannot be created
        User existingUser = userList.getUserByName(username); 
        if(existingUser != null)
        {
            return false;
        }

        // Creates new user and adds them to the list
        User newUser = new User(username, password);
        userList.addUser(newUser);

        currentUser = newUser;
        return true;
    }
}
