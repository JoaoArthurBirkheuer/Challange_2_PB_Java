package br.com.compass.dao;

import br.com.compass.config.JpaConfig;
import br.com.compass.model.Manager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class ManagerDAO implements AutoCloseable {

    private final EntityManager em;

    public ManagerDAO() {
        this.em = JpaConfig.getEntityManager();
    }

    public EntityTransaction beginTransaction() {
        EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        return tx;
    }

    public Manager findByCpf(String cpf) {
        try {
            TypedQuery<Manager> query = em.createQuery(
                "SELECT m FROM Manager m WHERE m.cpf = :cpf", Manager.class);
            query.setParameter("cpf", cpf);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public boolean existsByCpf(String cpf) {
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(m) FROM Manager m WHERE m.cpf = :cpf", Long.class);
        query.setParameter("cpf", cpf);
        return query.getSingleResult() > 0;
    }

    public boolean existsClientWithSameCpf(String cpf) {
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(c) FROM Client c WHERE c.cpf = :cpf", Long.class);
        query.setParameter("cpf", cpf);
        return query.getSingleResult() > 0;
    }

    // Métodos ajustados para não controlar transação:
    
    public void update(Manager manager) {
        em.merge(manager);
    }

    public void save(Manager manager) {
        em.persist(manager);
    }

    public void delete(Manager manager) {
        Manager managedManager = em.merge(manager);
        em.remove(managedManager);
    }

    public void createManager(Manager newManager) {
        em.persist(newManager); // mesma função do save()
    }

    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void close() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}
