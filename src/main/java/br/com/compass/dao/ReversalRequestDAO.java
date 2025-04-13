package br.com.compass.dao;

import java.util.List;

import br.com.compass.config.JpaConfig;
import br.com.compass.model.ReversalRequest;
import br.com.compass.model.Transaction;
import br.com.compass.model.enums.RequestStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class ReversalRequestDAO implements AutoCloseable {
    private final EntityManager em;
    
    public ReversalRequestDAO() {
        this.em = JpaConfig.getEntityManager();
    }
    
    public ReversalRequestDAO(EntityManager em) {
        this.em = em;
    }

    public List<ReversalRequest> findPendingRequests() {
        return em.createQuery(
            "SELECT r FROM ReversalRequest r WHERE r.status = :status", ReversalRequest.class)
            .setParameter("status", RequestStatus.PENDING)
            .getResultList();
    }

    public ReversalRequest findById(Long id) {
        return em.find(ReversalRequest.class, id);
    }

    public void save(ReversalRequest request) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(request);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to save reversal request", e);
        }
    }

    @Override
    public void close() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
    
    public boolean hasPendingReversalForTransaction(Transaction transaction) {
        Long count = em.createQuery(
            "SELECT COUNT(r) FROM ReversalRequest r " +
            "WHERE r.transaction = :transaction AND r.status = 'PENDING'", Long.class)
            .setParameter("transaction", transaction)
            .getSingleResult();
        return count > 0;
    }
}