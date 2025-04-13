package br.com.compass.dao;
import java.time.LocalDateTime;
import java.util.List;

import br.com.compass.config.JpaConfig;
import br.com.compass.model.Account;
import br.com.compass.model.Transaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class TransactionDAO implements AutoCloseable{
    private final EntityManager em;

    public TransactionDAO() {
        this.em = JpaConfig.getEntityManager();
    }

    public void save(Transaction transaction) {
        try {
            em.getTransaction().begin();
            em.persist(transaction);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    public List<Transaction> findReversibleTransactions(Account account) {
        return em.createQuery("""
            SELECT t FROM Transaction t 
            WHERE ((t.sourceAccount = :account AND t.type = 'TRANSFER_OUT')
                   OR (t.targetAccount = :account AND t.type = 'TRANSFER_IN'))
               AND t.isReversible = true
               AND t.timestamp >= :dataLimite
               AND NOT EXISTS (
                   SELECT 1 FROM ReversalRequest r 
                   WHERE r.transaction = t 
                   AND r.status = 'PENDING'
               )
            ORDER BY t.timestamp DESC""", Transaction.class)
            .setParameter("account", account)
            .setParameter("dataLimite", LocalDateTime.now().minusDays(5))
            .getResultList();
    }
    
    public void close() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
    
    public void update(Transaction transaction) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(transaction);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
}