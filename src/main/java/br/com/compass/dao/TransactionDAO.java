package br.com.compass.dao;
import java.util.List;

import br.com.compass.config.JpaConfig;
import br.com.compass.model.Account;
import br.com.compass.model.Transaction;
import jakarta.persistence.EntityManager;

public class TransactionDAO {
    public void save(Transaction transaction) {
        EntityManager em = JpaConfig.getEntityManager();
        em.getTransaction().begin();
        em.persist(transaction);
        em.getTransaction().commit();
        em.close();
    }

    public List<Transaction> findByAccount(Account account) {
        EntityManager em = JpaConfig.getEntityManager();
        List<Transaction> list = em.createQuery(
            "FROM Transaction t WHERE t.sourceAccount = :account OR t.targetAccount = :account ORDER BY t.timestamp DESC",
            Transaction.class)
            .setParameter("account", account)
            .getResultList();
        em.close();
        return list;
    }
    
    
}
