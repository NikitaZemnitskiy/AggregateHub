package com.zemnitskiy.aggregatehub.dao;

import com.zemnitskiy.aggregatehub.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for User entities across multiple databases.
 * Provides methods to save and retrieve users from all configured databases.
 */
@Repository
public class MultiDatabaseUserDao {

    private static final Logger logger = LoggerFactory.getLogger(MultiDatabaseUserDao.class);

    private final Map<String, EntityManagerFactory> entityManagerFactoryMap;

    public MultiDatabaseUserDao(Map<String, EntityManagerFactory> entityManagerFactoryMap) {
        this.entityManagerFactoryMap = entityManagerFactoryMap;
    }

    /**
     * Saves a user to all configured databases.
     *
     * @param user the user to be saved
     */
    public void saveUserToAllDatabases(User user) {
        for (var entry : entityManagerFactoryMap.entrySet()) {
            var dbName = entry.getKey();
            var emf = entry.getValue();
            var em = emf.createEntityManager();
            em.getTransaction().begin();
            try {
                em.persist(user);
                em.getTransaction().commit();
                logger.info("User saved to database: {}", dbName);
            } catch (PersistenceException ex) {
                em.getTransaction().rollback();
                logger.error("Error saving user to database '{}': {}", dbName, ex.getMessage(), ex);
            } finally {
                em.close();
            }
        }
    }

    /**
     * Retrieves all users from all configured databases with optional filtering.
     *
     * @param id       optional user ID to filter by
     * @param name     optional username to filter by
     * @param surname  optional user surname to filter by
     * @param username optional username to filter by
     * @return a list of users matching the provided criteria
     */
    public List<User> getAllUsersFromAllDatabases(String id, String name, String surname, String username) {
        List<User> allUsers = new ArrayList<>();

        for (var entry : entityManagerFactoryMap.entrySet()) {
            var dbName = entry.getKey();
            var emf = entry.getValue();
            try (var em = emf.createEntityManager()) {
                var users = fetchUsers(em, id, name, surname, username);
                allUsers.addAll(users);
                logger.info("Fetched {} users from database: {}", users.size(), dbName);
            } catch (PersistenceException ex) {
                logger.error("Error fetching users from database '{}': {}", dbName, ex.getMessage(), ex);
            }
        }

        return allUsers;
    }

    /**
     * Fetches users from a specific database based on the given filters.
     *
     * @param em       the EntityManager to use
     * @param id       optional user ID to filter by
     * @param name     optional username to filter by
     * @param surname  optional user surname to filter by
     * @param username optional username to filter by
     * @return a list of users matching the provided criteria from the specified database
     */
    private List<User> fetchUsers(EntityManager em, String id, String name, String surname, String username) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(User.class);
        var root = cq.from(User.class);

        var predicates = buildPredicates(cb, root, id, name, surname, username);
        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        return em.createQuery(cq).getResultList();
    }

    /**
     * Builds a list of JPA Criteria Predicates based on the provided filter parameters.
     *
     * @param cb       the CriteriaBuilder
     * @param root     the Root<User> for the query
     * @param id       optional user ID to filter by
     * @param name     optional username to filter by
     * @param surname  optional user surname to filter by
     * @param username optional username to filter by
     * @return a list of Predicates to apply to the query
     */
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<User> root, String id, String name, String surname, String username) {
        List<Predicate> predicates = new ArrayList<>();

        if (isNotBlank(id)) {
            predicates.add(cb.equal(root.get("id"), id));
        }
        if (isNotBlank(name)) {
            predicates.add(cb.equal(root.get("name"), name));
        }
        if (isNotBlank(surname)) {
            predicates.add(cb.equal(root.get("surname"), surname));
        }
        if (isNotBlank(username)) {
            predicates.add(cb.equal(root.get("username"), username));
        }

        return predicates;
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.isBlank();
    }
}