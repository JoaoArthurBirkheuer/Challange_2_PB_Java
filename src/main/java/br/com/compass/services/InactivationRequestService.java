package br.com.compass.services;

import java.time.LocalDateTime;
import java.util.List;

import br.com.compass.config.JpaConfig;
import br.com.compass.dao.AuditLogDAO;
import br.com.compass.dao.InactivationRequestDAO;
import br.com.compass.model.AccountInactivationRequest;
import br.com.compass.model.User;
import br.com.compass.model.enums.RequestStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class InactivationRequestService implements AutoCloseable {

    private final EntityManager em;
    private final InactivationRequestDAO inactivationRequestDAO;

    public InactivationRequestService() {
        this.em = JpaConfig.getEntityManager();
        this.inactivationRequestDAO = new InactivationRequestDAO(em);
    }

    public List<AccountInactivationRequest> findPendingRequests() {
        return inactivationRequestDAO.findByStatus(RequestStatus.PENDING);
    }

    public void approveRequest(AccountInactivationRequest request, User manager) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            AccountInactivationRequest managedRequest = em.merge(request);
            managedRequest.setStatus(RequestStatus.APPROVED);
            managedRequest.getAccount().setActive(false);
            
            inactivationRequestDAO.update(managedRequest);
            
            AuditService.logAction(
                AuditLogDAO.ACCOUNT_CLOSURE_APPROVED,
                String.format("Account %s inactivation approved", 
                    managedRequest.getAccount().getAccountNumber()),
                LocalDateTime.now(),
                manager,
                managedRequest.getAccount()
            );
            
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to approve request: " + e.getMessage(), e);
        }
    }

    public void rejectRequest(AccountInactivationRequest request, User manager) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            AccountInactivationRequest managedRequest = em.merge(request);
            managedRequest.setStatus(RequestStatus.REJECTED);
            
            inactivationRequestDAO.update(managedRequest);
            
            AuditService.logAction(
                AuditLogDAO.ACCOUNT_CLOSURE_REJECTED,
                String.format("Account %s inactivation rejected", 
                    managedRequest.getAccount().getAccountNumber()),
                LocalDateTime.now(),
                manager,
                managedRequest.getAccount()
            );
            
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to reject request: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}