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
     * Fetches users from a specific database based on the given filters.
     *
     * @param dbName   the database name
     * @param id       optional user ID to filter by
     * @param name     optional username to filter by
     * @param surname  optional user surname to filter by
     * @param username optional username to filter by
     * @return a list of users matching the criteria
     */
    public List<User> fetchUsersFromDatabase(String dbName, String id, String name, String surname, String username) {
        EntityManagerFactory emf = entityManagerFactoryMap.get(dbName);
        if (emf == null) {
            logger.error("EntityManagerFactory not found for database '{}'", dbName);
            return new ArrayList<>();
        }

        try (EntityManager em = emf.createEntityManager()) {
            return fetchUsers(em, id, name, surname, username);
        } catch (PersistenceException ex) {
            logger.error("Error fetching users from database '{}': {}", dbName, ex.getMessage(), ex);
            return new ArrayList<>();
        }
    }

    private List<User> fetchUsers(EntityManager em, String id, String name, String surname, String username) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);

        List<Predicate> predicates = new ArrayList<>();
        if (id != null) predicates.add(cb.equal(root.get("id"), id));
        if (name != null) predicates.add(cb.equal(root.get("name"), name));
        if (surname != null) predicates.add(cb.equal(root.get("surname"), surname));
        if (username != null) predicates.add(cb.equal(root.get("username"), username));

        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        return em.createQuery(cq).getResultList();
    }

    public List<String> getDatabaseNames() {
        return new ArrayList<>(entityManagerFactoryMap.keySet());
    }
}