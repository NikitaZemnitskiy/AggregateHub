package com.zemnitskiy.aggregatehub.service;

import com.zemnitskiy.aggregatehub.dao.MultiDatabaseUserDao;
import com.zemnitskiy.aggregatehub.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final MultiDatabaseUserDao userDao;

    @Autowired
    public UserService(MultiDatabaseUserDao userDao) {
        this.userDao = userDao;
    }

    public void saveUserToAllDatabases(User user) {
        userDao.saveUserToAllDatabases(user);
    }

    public List<User> getAllUsersFromAllDatabases() {
        return userDao.getAllUsersFromAllDatabases();
    }

}