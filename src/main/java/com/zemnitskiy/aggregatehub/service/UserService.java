package com.zemnitskiy.aggregatehub.service;

import com.zemnitskiy.aggregatehub.dao.MultiDatabaseUserDao;
import com.zemnitskiy.aggregatehub.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final MultiDatabaseUserDao userDao;

    public UserService(MultiDatabaseUserDao userDao) {
        this.userDao = userDao;
    }

    public void saveUserToAllDatabases(User user) {
        if (user != null) {
            userDao.saveUserToAllDatabases(user);
        }
    }

    public List<User> getAllUsersFromAllDatabases(String id, String name, String surname, String username) {
        return userDao.getAllUsersFromAllDatabases(id, name, surname, username);
    }

}