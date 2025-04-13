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

public class UserDAO implements AutoCloseable {
    
    private EntityManager em;
    private boolean isClosed = false;
    
    public UserDAO() {
        this.em = JpaConfig.getEntityManager();
    }

    public boolean isOpen() {
        return em != null && em.isOpen() && !isClosed;
    }

    public List<User> findAllByCpf(String cpf) {
        checkOpen();
        try {
            TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.cpf = :cpf", User.class);
            query.setParameter("cpf", cpf);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to find users by CPF: " + cpf, e);
        }
    }

    public int countUsersByCpf(String cpf) {
        checkOpen();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.cpf = :cpf", Long.class);
            query.setParameter("cpf", cpf);
            return query.getSingleResult().intValue();
        } catch (Exception e) {
            throw new RuntimeException("Failed to count users by CPF: " + cpf, e);
        }
    }
    
    public Client findClientByCpf(String cpf) {
        checkOpen();
        try {
            TypedQuery<Client> query = em.createQuery(
                "SELECT c FROM Client c WHERE c.cpf = :cpf", Client.class);
            query.setParameter("cpf", cpf);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to find client by CPF: " + cpf, e);
        }
    }

    public Manager findManagerByCpf(String cpf) {
        checkOpen();
        try {
            TypedQuery<Manager> query = em.createQuery(
                "SELECT m FROM Manager m WHERE m.cpf = :cpf", Manager.class);
            query.setParameter("cpf", cpf);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to find manager by CPF: " + cpf, e);
        }
    }
    
    public void update(User user) {
        checkOpen();
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
        }
    }
    
    public User findById(Long id) {
        checkOpen();
        try {
            return em.find(User.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find user by ID: " + id, e);
        }
    }

    @Override
    public void close() {
        if (isOpen()) {
            try {
                em.close();
            } finally {
                isClosed = true;
            }
        }
    }

    private void checkOpen() {
        if (!isOpen()) {
            throw new IllegalStateException("EntityManager is closed");
        }
    }
}