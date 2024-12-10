package com.zemnitskiy.aggregatehub.service;

import com.zemnitskiy.aggregatehub.dao.MultiDatabaseUserDao;
import com.zemnitskiy.aggregatehub.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service class for managing users across multiple databases.
 */
@Service
public class UserService {

    private final MultiDatabaseUserDao userDao;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(MultiDatabaseUserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Saves a user to all available databases.
     *
     * @param user the user object to be saved
     */
    public void saveUserToAllDatabases(User user) {
        if (user != null) {
            try {
                logger.info("Saving user to all databases: {}", user);
                userDao.saveUserToAllDatabases(user);
            } catch (Exception e) {
                logger.error("Error saving user to all databases: {}", e.getMessage(), e);
            }
        } else {
            logger.warn("Attempted to save a null user to databases.");
        }
    }

    /**
     * Retrieves users asynchronously from all databases based on filter criteria.
     *
     * @param id       the user ID (optional filter)
     * @param name     the username (optional filter)
     * @param surname  the user surname (optional filter)
     * @param username the user username (optional filter)
     * @return a CompletableFuture with a list of users matching the criteria from all databases
     */
    public List<User> getAllUsersFromAllDatabases(String id, String name, String surname, String username) {
        List<CompletableFuture<List<User>>> futures = userDao.getDatabaseNames().stream()
                .map(dbName -> CompletableFuture.supplyAsync(() ->
                        userDao.fetchUsersFromDatabase(dbName, id, name, surname, username)))
                .toList();

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allFutures.get();
            return futures.stream()
                    .flatMap(future -> {
                        try {
                            return future.get().stream();
                        } catch (InterruptedException | ExecutionException e) {
                            logger.error("Error retrieving users from a database: {}", e.getMessage(), e);
                            Thread.currentThread().interrupt();
                            return Stream.empty();
                        }
                    })
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error while waiting for all database queries to complete: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            return List.of();
        }
    }
}