package com.model;

/**
 * 
 * @author 
 */
public class DataLoader extends DataConstants {

    /**
     * 
     * @param filePath
     * @return
     */
    public T loadData(String filePath) {

    }//end filePath()

    /**
     * 
     * @return
     */
    public static ArrayList<User> getUsers() {
        
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
