package br.com.compass.dao;

import br.com.compass.config.JpaConfig;
import br.com.compass.model.Account;
import br.com.compass.model.Client;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class AccountDAO implements AutoCloseable {
    private final EntityManager em;

    public AccountDAO() {
        this.em = JpaConfig.getEntityManager();
    }

    public AccountDAO(EntityManager em) {
        this.em = em;
    }

    public EntityTransaction beginTransaction() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        return em.getTransaction();
    }

    public Account findById(Long id) {
        return em.find(Account.class, id);
    }

    public Account findByAccountNumber(String accountNumber) {
        return em.createQuery("SELECT a FROM Account a WHERE a.accountNumber = :number", Account.class)
               .setParameter("number", accountNumber)
               .getResultStream()
               .findFirst()
               .orElse(null);
    }

    public void update(Account account) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(account);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to update account", e);
        }
    }

    @Override
    public void close() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
    
    public void save(Account account) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(account);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to save account", e);
        }
    }

    public List<Account> findActiveAccountsByClient(Client client) {
        return em.createQuery(
            "SELECT a FROM Account a WHERE a.owner = :owner AND a.active = true", Account.class)
            .setParameter("owner", client)
            .getResultList();
    }

    public List<Account> findAllInactiveAccounts() {
        return em.createQuery(
            "SELECT a FROM Account a WHERE a.active = false", Account.class)
            .getResultList();
    }

    public void clearCache() {
        if (em.isOpen()) {
            em.clear();
        }
    }
}