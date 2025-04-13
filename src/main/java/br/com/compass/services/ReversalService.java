package br.com.compass.services;

import java.time.LocalDateTime;
import java.util.List;

import br.com.compass.config.JpaConfig;
import br.com.compass.dao.ReversalRequestDAO;
import br.com.compass.exceptions.BusinessException;
import br.com.compass.model.Client;
import br.com.compass.model.Manager;
import br.com.compass.model.ReversalRequest;
import br.com.compass.model.Transaction;
import br.com.compass.model.enums.RequestStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class ReversalService implements AutoCloseable {
    private final EntityManager em;
    private final ReversalRequestDAO reversalRequestDAO;

    public ReversalService() {
        this.em = JpaConfig.getEntityManager();
        this.reversalRequestDAO = new ReversalRequestDAO(em);
    }

    public ReversalService(EntityManager em) {
        this.em = em;
        this.reversalRequestDAO = new ReversalRequestDAO(em);
    }

    public void requestReversal(Client client, Transaction transaction, String reason) throws BusinessException {
        if (!em.getTransaction().isActive()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                processReversalRequest(client, transaction, reason);
                tx.commit();
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                throw new BusinessException("Failed to process reversal request: " + e.getMessage());
            }
        } else {
            processReversalRequest(client, transaction, reason);
        }
    }

    private void processReversalRequest(Client client, Transaction transaction, String reason) {
        if (hasPendingReversalForTransaction(transaction)) {
            throw new BusinessException("This transaction already has a pending reversal request");
        }

        ReversalRequest request = new ReversalRequest();
        request.setTransaction(transaction);
        request.setRequester(client);
        request.setReason(reason);
        request.setRequestDate(LocalDateTime.now());
        request.setStatus(RequestStatus.PENDING);
        
        em.persist(request);
    }

    public void approveRequest(Long requestId, Manager manager, String notes) throws BusinessException {
    	 try {
    	        processApproval(requestId, manager, notes);
    	    } catch (Exception e) {
    	        throw new BusinessException("Error approving request: " + e.getMessage());
    	    }
    }

    private void processApproval(Long requestId, Manager manager, String notes) {
        ReversalRequest request = em.find(ReversalRequest.class, requestId);
        if (request == null) {
            throw new BusinessException("Reversal request not found");
        }

        request.setStatus(RequestStatus.APPROVED);
        request.setResolver(manager);
        request.setResolutionNotes(notes);
        request.setResolutionDate(LocalDateTime.now());
        
        em.merge(request);
    }

    public List<ReversalRequest> findPendingRequests() {
        return reversalRequestDAO.findPendingRequests();
    }

    public void rejectRequest(Long requestId, Manager manager, String notes) throws BusinessException {
        if (!em.getTransaction().isActive()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                processRejection(requestId, manager, notes);
                tx.commit();
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                throw new BusinessException("Error rejecting request: " + e.getMessage());
            }
        } else {
            processRejection(requestId, manager, notes);
        }
    }

    private void processRejection(Long requestId, Manager manager, String notes) {
        // Verifica se a entidade está sendo gerenciada
        ReversalRequest request = em.find(ReversalRequest.class, requestId);
        if (request == null) {
            throw new BusinessException("Reversal request not found");
        }

        // Verifica se a requisição já foi processada
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BusinessException("Request has already been processed");
        }

        // Atualiza os campos
        request.setStatus(RequestStatus.REJECTED);
        request.setResolver(manager);
        request.setResolutionNotes(notes);
        request.setResolutionDate(LocalDateTime.now());
        
        // Se a entidade não está sendo gerenciada, faz o merge
        if (!em.contains(request)) {
            request = em.merge(request);
        }
        
        // Força o flush se necessário (dependendo da sua estratégia)
        em.flush();
    }

    @Override
    public void close() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
    
    private boolean hasPendingReversalForTransaction(Transaction transaction) {
        return reversalRequestDAO.hasPendingReversalForTransaction(transaction);
    }
    
    public EntityManager getEntityManager() {
        return this.em;
    }
}