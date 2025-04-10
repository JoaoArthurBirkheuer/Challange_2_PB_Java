package br.com.compass.services;

import java.time.LocalDateTime;

import br.com.compass.config.JpaConfig;
import br.com.compass.model.Account;
import br.com.compass.model.AuditLog;
import br.com.compass.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class AuditService {

    public static void logAction(String actionType, String details, LocalDateTime timestamp, User actor, Account affectedAccount) {
        EntityManager em = JpaConfig.getEntityManager();
        EntityTransaction tx = null;
        
        try {
            tx = em.getTransaction();
            tx.begin();
            
            // Ensure actor is managed if it exists
            User managedActor = null;
            if (actor != null) {
                managedActor = em.merge(actor); // Re-attach if detached
            }
            
            // Ensure account is managed if it exists
            Account managedAccount = null;
            if (affectedAccount != null) {
                managedAccount = em.merge(affectedAccount);
            }
            
            AuditLog log = new AuditLog(actionType, details, timestamp, managedActor, managedAccount);
            em.persist(log);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            System.err.println("Error while logging audit action: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
    }}

/*public static AuditLog logAction(String actionType, String details, LocalDateTime timestamp, User actor, Account affectedAccount) {
    EntityManager em = JpaConfig.getEntityManager();
    EntityTransaction tx = null;
    AuditLog log = null;

    try {
        tx = em.getTransaction();
        tx.begin();

        User managedActor = (actor != null) ? em.merge(actor) : null;
        Account managedAccount = (affectedAccount != null) ? em.merge(affectedAccount) : null;

        log = new AuditLog(actionType, details, timestamp, managedActor, managedAccount);
        em.persist(log);
        tx.commit();
    } catch (Exception e) {
        if (tx != null && tx.isActive()) tx.rollback();
        System.err.println("Error while logging audit action: " + e.getMessage());
        e.printStackTrace();
    } finally {
        em.close();
    }

    return log;
}*/