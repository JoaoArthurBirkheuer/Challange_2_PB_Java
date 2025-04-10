package br.com.compass.services;

import java.time.LocalDateTime;

import br.com.compass.dao.ReversalRequestDAO;
import br.com.compass.exceptions.BusinessException;
import br.com.compass.model.Client;
import br.com.compass.model.Manager;
import br.com.compass.model.ReversalRequest;
import br.com.compass.model.Transaction;
import br.com.compass.model.enums.RequestStatus;

public class ReversalService {

    private final ReversalRequestDAO reversalRequestDAO = new ReversalRequestDAO();

    public void requestReversal(Client client, Transaction transaction, String reason) {
        if (!transaction.getIsReversible()) {
            throw new BusinessException("This transaction is not reversible.");
        }

        // Criar nova solicitação
        ReversalRequest request = new ReversalRequest();
        request.setTransaction(transaction);
        request.setRequester(client);
        request.setReason(reason);
        request.setRequestDate(LocalDateTime.now());
        request.setStatus(RequestStatus.PENDING);

        reversalRequestDAO.save(request);
    }

    public void approveRequest(Long requestId, Manager manager, String notes) {
        ReversalRequest request = reversalRequestDAO.findById(requestId);
        if (request == null) {
            throw new BusinessException("Reversal request not found.");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BusinessException("Request has already been processed.");
        }

        // Atualizar status
        request.setStatus(RequestStatus.APPROVED);
        request.setResolutionNotes(notes);
        request.setResolutionDate(LocalDateTime.now());
        request.setResolver(manager);

        // Marcar transação como irreversível (evita reversão duplicada)
        request.getTransaction().markAsIrreversible();

        reversalRequestDAO.update(request);

        // TODO: aqui você pode chamar um serviço para efetivamente desfazer a transação, se desejar.
    }

    public void rejectRequest(Long requestId, Manager manager, String notes) {
        ReversalRequest request = reversalRequestDAO.findById(requestId);
        if (request == null) {
            throw new BusinessException("Reversal request not found.");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BusinessException("Request has already been processed.");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setResolutionNotes(notes);
        request.setResolutionDate(LocalDateTime.now());
        request.setResolver(manager);

        reversalRequestDAO.update(request);
    }
}
