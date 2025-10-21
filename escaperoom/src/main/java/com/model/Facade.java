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
        
        User user = userList.createAccount(username, password);

        if(user == null) {
            return false;
        }

        currentUser = user;
        return true;
    }
}
