package com.model;

import java.util.UUID;

public class UserMaker {
    public User createUser(String username, String password, UUID id) {
        return new User(username, password, id);
    }
}

