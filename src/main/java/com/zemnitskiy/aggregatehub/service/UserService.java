package com.zemnitskiy.aggregatehub.service;

import com.zemnitskiy.aggregatehub.dao.MultiDatabaseUserDao;
import com.zemnitskiy.aggregatehub.exception.AggregateHubServiceException;
import com.zemnitskiy.aggregatehub.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Service class for managing users across multiple databases.
 */
@Service
public class UserService {

    private final MultiDatabaseUserDao userDao;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    public UserService(MultiDatabaseUserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Retrieves users asynchronously from all databases based on filter criteria with timeout.
     *
     * @param id       the user ID (optional filter)
     * @param name     the username (optional filter)
     * @param surname  the user surname (optional filter)
     * @param username the user username (optional filter)
     * @return a list of users matching the criteria from all databases
     * @throws AggregateHubServiceException if an error occurs while retrieving users
     */
    public List<User> getAllUsersFromAllDatabases(String id, String name, String surname, String username) {
        List<CompletableFuture<List<User>>> futures = userDao.getDatabaseNames().stream()
                .map(dbName -> userDao.fetchUsersFromDatabaseAsync(dbName, id, name, surname, username))
                .toList();

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allFutures.get(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            return futures.stream()
                    .map(listCompletableFuture -> {
                        try {
                            return listCompletableFuture.get();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // Restore interrupted status
                            throw new AggregateHubServiceException("Thread was interrupted while fetching users", e);
                        } catch (ExecutionException e) {
                            throw new AggregateHubServiceException("Failed to fetch users from database", e.getCause());
                        }
                    })
                    .flatMap(List::stream)
                    .toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            logger.error("Thread was interrupted: {}", e.getMessage(), e);
            throw new AggregateHubServiceException("Thread was interrupted while fetching users", e);
        } catch (ExecutionException | TimeoutException e) {
            logger.error("An error occurred while fetching users: {}", e.getMessage(), e);
            futures.forEach(f -> f.cancel(true)); // Cancel all futures on error
            throw new AggregateHubServiceException("Error fetching users from databases", e);
        } finally {
            // Ensure all futures are cancelled if they haven't completed
            futures.forEach(f -> {
                if (!f.isDone()) {
                    f.cancel(true);
                }
            });
        }
    }
}