package br.com.compass.dao;

import java.util.List;

import br.com.compass.config.JpaConfig;
import br.com.compass.model.Client;
import br.com.compass.model.Manager;
import br.com.compass.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class UserDAO {

    public List<User> findAllByCpf(String cpf) {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.cpf = :cpf", User.class);
            query.setParameter("cpf", cpf);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public int countUsersByCpf(String cpf) {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.cpf = :cpf", Long.class);
            query.setParameter("cpf", cpf);
            return query.getSingleResult().intValue();
        } finally {
            em.close();
        }
    }
    
    public Client findClientByCpf(String cpf) {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            TypedQuery<Client> query = em.createQuery(
                "SELECT c FROM Client c WHERE c.cpf = :cpf", Client.class);
            query.setParameter("cpf", cpf);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public Manager findManagerByCpf(String cpf) {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            TypedQuery<Manager> query = em.createQuery(
                "SELECT m FROM Manager m WHERE m.cpf = :cpf", Manager.class);
            query.setParameter("cpf", cpf);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
    
    public void update(User user) {
        EntityManager em = JpaConfig.getEntityManager();
        EntityTransaction tx = null;
        
        try {
            tx = em.getTransaction();
            tx.begin();
            
            if (user instanceof Client) {
                em.merge((Client) user); 
            } else if (user instanceof Manager) {
                em.merge((Manager) user);
            } else {
                throw new IllegalArgumentException("Unknown user type");
            }
            
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to update user: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public User findById(Long id) {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            return em.find(User.class, id);
        } finally {
            em.close();
        }
    }
}