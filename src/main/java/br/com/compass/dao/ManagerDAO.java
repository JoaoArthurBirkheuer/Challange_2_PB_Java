package br.com.compass.dao;

import br.com.compass.config.JpaConfig;
import br.com.compass.model.Manager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class ManagerDAO {

	public Manager findByCpf(String cpf) {
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

    public boolean existsByCpf(String cpf) {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(m) FROM Manager m WHERE m.cpf = :cpf", Long.class);
            query.setParameter("cpf", cpf);
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    public boolean existsClientWithSameCpf(String cpf) {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM Client c WHERE c.cpf = :cpf", Long.class);
            query.setParameter("cpf", cpf);
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    public void update(Manager manager) {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(manager);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Failed to update manager: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void save(Manager manager) {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(manager);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Failed to save manager: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void delete(Manager manager) {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            Manager managedManager = em.merge(manager);
            em.remove(managedManager);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Failed to delete manager: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
