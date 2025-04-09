package br.com.compass.dao;

import java.util.List;

import br.com.compass.config.JpaConfig;
import br.com.compass.model.Account;
import br.com.compass.model.Client;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.EntityTransaction;

public class AccountDAO {

    private EntityManager em;

    public AccountDAO() {
        this.em = JpaConfig.getEntityManager();
    }

    public List<Account> findActiveAccountsByClient(Client client) {
        String jpql = "SELECT a FROM Account a WHERE a.owner = :owner AND a.active = true";
        TypedQuery<Account> query = em.createQuery(jpql, Account.class);
        query.setParameter("owner", client);
        return query.getResultList();
    }

    public List<Account> findAllActiveAccounts() {
        String jpql = "SELECT a FROM Account a WHERE a.active = true";
        TypedQuery<Account> query = em.createQuery(jpql, Account.class);
        return query.getResultList();
    }

    public Account findById(Long id) {
        return em.find(Account.class, id);
    }

    public void update(Account account) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(account);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
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
            throw e;
        }
    }
}
