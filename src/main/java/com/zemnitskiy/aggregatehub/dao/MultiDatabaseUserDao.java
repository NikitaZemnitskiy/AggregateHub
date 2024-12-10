package com.zemnitskiy.aggregatehub.dao;

import com.zemnitskiy.aggregatehub.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MultiDatabaseUserDao {

    private final Map<String, EntityManagerFactory> entityManagerFactoryMap;

    @Autowired
    public MultiDatabaseUserDao(Map<String, EntityManagerFactory> entityManagerFactoryMap) {
        this.entityManagerFactoryMap = entityManagerFactoryMap;
    }

    public void saveUserToAllDatabases(User user) {
        for (String dbName : entityManagerFactoryMap.keySet()) {
            EntityManager em = entityManagerFactoryMap.get(dbName).createEntityManager();
            em.getTransaction().begin();
            try {
                em.persist(user);
                em.getTransaction().commit();
                System.out.println("User saved to " + dbName);
            } catch (PersistenceException ex) {
                em.getTransaction().rollback();
                System.err.println("Error saving user to " + dbName + ": " + ex.getMessage());
            } finally {
                em.close();
            }
        }
    }

    public List<User> getAllUsersFromAllDatabases() {
        List<User> allUsers = new ArrayList<>();
        for (String dbName : entityManagerFactoryMap.keySet()) {
            EntityManager em = entityManagerFactoryMap.get(dbName).createEntityManager();
            try {
                List<User> users = em.createQuery("SELECT u FROM User u", User.class).getResultList();
                allUsers.addAll(users);
                System.out.println("Fetched users from " + dbName);
            } catch (PersistenceException ex) {
                System.err.println("Error fetching users from " + dbName + ": " + ex.getMessage());
            } finally {
                em.close();
            }
        }
        return allUsers;
    }
}