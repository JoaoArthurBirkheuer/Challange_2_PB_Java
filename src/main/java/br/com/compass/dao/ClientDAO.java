package br.com.compass.dao;

import br.com.compass.config.JpaConfig;
import br.com.compass.model.Client;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class ClientDAO {

	public Client findByCpf(String cpf) {
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

    public boolean existsByCpf(String cpf) {
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

    public boolean existsManagerWithSameCpf(String cpf) {
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

    public void update(Client client) {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(client);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Failed to update client: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void save(Client client) {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(client);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Failed to save client: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void delete(Client client) {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            Client managedClient = em.merge(client);
            em.remove(managedClient);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Failed to delete client: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
