package br.com.compass.services;

import java.time.LocalDateTime;

import br.com.compass.config.JpaConfig;
import br.com.compass.model.Account;
import br.com.compass.model.AuditLog;
import br.com.compass.model.User;
import jakarta.persistence.EntityManager;

public class AuditService {

    public static void logAction(String actionType, String details, LocalDateTime timestamp, User actor, Account affectedAccount) {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            AuditLog log = new AuditLog(actionType, details, timestamp, actor, affectedAccount);
            em.persist(log);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            System.err.println("Error while logging audit action: " + e.getMessage());
        } finally {
            em.close();
        }
    }
}
