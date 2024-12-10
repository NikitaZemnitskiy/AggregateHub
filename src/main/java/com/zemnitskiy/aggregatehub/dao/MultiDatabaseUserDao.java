package com.zemnitskiy.aggregatehub.dao;

import com.zemnitskiy.aggregatehub.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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

    public List<User> getAllUsersFromAllDatabases(String id, String name, String surname, String username) {
        List<User> allUsers = new ArrayList<>();
        for (String dbName : entityManagerFactoryMap.keySet()) {
            EntityManager em = entityManagerFactoryMap.get(dbName).createEntityManager();
            try {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<User> cq = cb.createQuery(User.class);
                Root<User> root = cq.from(User.class);
                List<Predicate> predicates = new ArrayList<>();

                if (id != null && !id.isEmpty()) {
                    predicates.add(cb.equal(root.get("id"), id));
                }
                if (name != null && !name.isEmpty()) {
                    predicates.add(cb.equal(root.get("name"), name));
                }
                if (surname != null && !surname.isEmpty()) {
                    predicates.add(cb.equal(root.get("surname"), surname));
                }
                if (username != null && !username.isEmpty()) {
                    predicates.add(cb.equal(root.get("username"), username));
                }

                if (!predicates.isEmpty()) {
                    cq.where(cb.and(predicates.toArray(new Predicate[0])));
                }

                List<User> users = em.createQuery(cq).getResultList();
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