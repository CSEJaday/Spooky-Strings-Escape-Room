package com.model;

/**
 * This class loads data from the respository (i.e. JSON file) and returns it to
 * be used in the Escape Room classes.
 * @author 
 */
public class DataLoader extends DataConstants {

    /**
     * Pulls user data from the JSON file and loads into User Objects that are 
     * added to an ArrayList
     * @param filePath
     * @return
     */
    public T loadData(String filePath) {
        

    }//end loadData()

    /**
     * Retrieves all of the Users stored in the User.json file, adds them to an ArrayList and return
     * the ArrayList.
     * @return The ArrayList of Users.
     */
    public static ArrayList<User> getUsers() {
        try {
            FileReader file = new FileReader(User.json);
        } catch ( e) {
        }
        for (int i = 0; i < peopleJSON.size(); i++){
            JSONObject personJSON = (JSONObject)peopleJSON.get(i);
            UUID id = UUID.fromString((String)personJSON.get(USER_ID));
            String userName = (String)personJSON.get(USER_USER_NAME);
            String firstName = (String)personJSON.get(USER_FIRST_NAME);
            String lastName = (String)personJSON.get(USER_LAST_NAME);
            int age = ((Long)personJSON.get(USER_AGE)).intValue();
            String phoneNumber = (String)personJSON.get(USER_PHONE_NUMBER);

            user.add(new User(id, userName, firstName, lastName, age, phoneNumber));
        }
        return users;
    }//end getUsers()
    
}//end DataLoader()
