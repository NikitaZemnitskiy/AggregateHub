package com.zemnitskiy.aggregatehub.service;

import com.zemnitskiy.aggregatehub.dao.MultiDatabaseUserDao;
import com.zemnitskiy.aggregatehub.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing users and interacting with the MultiDatabaseUserDao.
 * Provides methods for saving and retrieving user data across multiple databases.
 */
@Service
public class UserService {

    private final MultiDatabaseUserDao userDao;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(MultiDatabaseUserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Saves a user to all available databases.
     * Ensures that the user object is not null before proceeding with the operation.
     *
     * @param user the user object to be saved
     */
    public void saveUserToAllDatabases(User user) {
        if (user != null) {
            try {
                logger.info("Saving user to all databases: {}", user);
                userDao.saveUserToAllDatabases(user);
            } catch (Exception e) {
                logger.error("Error occurred while saving user to all databases: {}", user, e);
            }
        } else {
            logger.warn("Attempted to save a null user to databases.");
        }
    }

    /**
     * Retrieves a list of users from all databases based on the provided filter criteria.
     * Allows filtering by user id, name, surname, and username.
     *
     * @param id       the user id (optional filter)
     * @param name     the username (optional filter)
     * @param surname  the user surname (optional filter)
     * @param username the user username (optional filter)
     * @return a list of matching users from all databases
     */
    public List<User> getAllUsersFromAllDatabases(String id, String name, String surname, String username) {
        return userDao.getAllUsersFromAllDatabases(id, name, surname, username);
    }
}