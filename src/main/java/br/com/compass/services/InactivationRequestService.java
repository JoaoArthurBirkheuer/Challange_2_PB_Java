package br.com.compass.services;

import java.time.LocalDateTime;
import java.util.List;

import br.com.compass.config.JpaConfig;
import br.com.compass.dao.InactivationRequestDAO;
import br.com.compass.model.Account;
import br.com.compass.model.AccountInactivationRequest;
import br.com.compass.model.Manager;
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

    public void approveRequest(AccountInactivationRequest request, User manager, String notes) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            AccountInactivationRequest managedRequest = em.find(AccountInactivationRequest.class, request.getId());
            if (managedRequest == null) {
                throw new RuntimeException("Request not found in database");
            }
           
            Account account = em.find(Account.class, managedRequest.getAccount().getId());
            if (account == null) {
                throw new RuntimeException("Associated account not found");
            }

            managedRequest.setStatus(RequestStatus.APPROVED);
            managedRequest.setResolutionDate(LocalDateTime.now());
            managedRequest.setResolver((Manager) manager);
            managedRequest.setResolutionNotes(notes);
            
            account.setActive(false);
            em.merge(managedRequest);
            em.merge(account);
           
            AuditService.logAction(
                "ACCOUNT_CLOSURE_APPROVED",
                String.format("Account %s closed by manager", account.getAccountNumber()),
                LocalDateTime.now(),
                manager,
                account
            );
            
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to approve request: " + e.getMessage(), e);
        }
    }

    public void rejectRequest(AccountInactivationRequest request, User manager, String notes) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            AccountInactivationRequest managedRequest = em.find(AccountInactivationRequest.class, request.getId());
            if (managedRequest == null) {
                throw new RuntimeException("Request not found in database");
            }
            
            Account account = em.find(Account.class, managedRequest.getAccount().getId());
            if (account == null) {
                throw new RuntimeException("Associated account not found");
            }
            
            managedRequest.setStatus(RequestStatus.REJECTED);
            managedRequest.setResolutionDate(LocalDateTime.now());
            managedRequest.setResolver((Manager) manager);
            managedRequest.setResolutionNotes(notes);
            
            account.setClosureRequested(false);
            
            em.merge(managedRequest);
            em.merge(account);
            
            AuditService.logAction(
                "ACCOUNT_CLOSURE_REJECTED",
                String.format("Account %s closure rejected. Reason: %s", 
                    managedRequest.getAccount().getAccountNumber(),
                    notes),
                LocalDateTime.now(),
                manager,
                managedRequest.getAccount()
            );
            
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to reject request: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
    
    public void createRequest(AccountInactivationRequest request) {
        if (request.getAccount() == null || request.getRequester() == null) {
            throw new IllegalArgumentException("Account and requester must be specified");
        }
        

        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            
            Account account = em.find(Account.class, request.getAccount().getId());
            if (account == null) {
                throw new RuntimeException("Account not found");
            }
            
            if (!account.getActive()) {
                throw new RuntimeException("Account is already inactive");
            }

            boolean exists = em.createQuery(
                    "SELECT COUNT(r) FROM AccountInactivationRequest r " +
                    "WHERE r.account = :account AND r.status = 'PENDING'", 
                    Long.class)
                    .setParameter("account", account)
                    .getSingleResult() > 0;
                    
            if (exists) {
                throw new RuntimeException("This account already has a pending closure request");
            }
            
            request.setRequestDate(LocalDateTime.now());
            request.setStatus(RequestStatus.PENDING);
            
            em.persist(request);
            
            account.setClosureRequested(true);
            em.merge(account);
            
            tx.commit();
            AuditService.logAction(
                "ACCOUNT_CLOSURE_REQUESTED",
                String.format("Requested closure for account %s. Reason: %s",
                    account.getAccountNumber(),
                    request.getReason()),
                LocalDateTime.now(),
                request.getRequester(),
                account
            );
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
              tx.rollback();
            }
            throw new RuntimeException("Failed to create closure request: " + e.getMessage(), e);
        }
    }
}