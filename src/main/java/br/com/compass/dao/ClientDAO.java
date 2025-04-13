package br.com.compass.dao;

import java.util.List;

import br.com.compass.config.JpaConfig;
import br.com.compass.model.Client;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class ClientDAO implements AutoCloseable {
    
    private final EntityManager em;
    
    public ClientDAO() {
        this.em = JpaConfig.getEntityManager();
    }

    // Add this method to begin transactions
    public EntityTransaction beginTransaction() {
        return em.getTransaction();
    }

    public Client findByCpf(String cpf) {
        try {
            TypedQuery<Client> query = em.createQuery(
                "SELECT c FROM Client c WHERE c.cpf = :cpf", Client.class);
            query.setParameter("cpf", cpf);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public Client findById(Long id) {
        try {
            TypedQuery<Client> query = em.createQuery(
                "SELECT c FROM Client c WHERE c.id = :id", Client.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public List<Client> findBlockedClients() {
        TypedQuery<Client> query = em.createQuery(
            "SELECT c FROM Client c WHERE c.blocked = true", Client.class);
        return query.getResultList();
    }

    public boolean existsByCpf(String cpf) {
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(c) FROM Client c WHERE c.cpf = :cpf", Long.class);
        query.setParameter("cpf", cpf);
        return query.getSingleResult() > 0;
    }

    public boolean existsManagerWithSameCpf(String cpf) {
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(m) FROM Manager m WHERE m.cpf = :cpf", Long.class);
        query.setParameter("cpf", cpf);
        return query.getSingleResult() > 0;
    }

    public void update(Client client) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(client);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to update client: " + e.getMessage(), e);
        }
    }

    public void save(Client client) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(client);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to save client: " + e.getMessage(), e);
        }
    }

    public void delete(Client client) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Client managedClient = em.merge(client);
            em.remove(managedClient);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to delete client: " + e.getMessage(), e);
        }
    }
    
    public void createClient(Client client) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(client);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to register client: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        if (em != null && em.isOpen()) {
            em.close();
       }
    }
   

	public EntityManager getEntityManager() {
		// TODO Auto-generated method stub
		return this.em;
	}
}