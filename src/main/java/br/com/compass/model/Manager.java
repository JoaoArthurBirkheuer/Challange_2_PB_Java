package br.com.compass.model;

import java.time.LocalDateTime;
import java.util.List;

import br.com.compass.exceptions.AccountNotFoundException;
import br.com.compass.exceptions.RequestNotFoundException;
import br.com.compass.model.enums.RequestStatus;


public class Manager extends User {

    private List<ReversalRequest> reversalRequests;

    private List<AccountInactivationRequest> deletionRequests;

    public Manager() {}

    public Manager(Long id, String cpf, String name, String passwordHash, LocalDateTime createdAt, String email) {
        super(id, cpf, name, passwordHash, createdAt, email);
    }

    public void unlockAccount(Account account, boolean unlock) {
        if (account != null) {
            account.setActive(unlock);
        }
        else {
        	throw new AccountNotFoundException("");
        }
    }

    public void manageReversalRequest(ReversalRequest request, boolean approve) {
        if (request != null) {
            request.setStatus(approve ? RequestStatus.APPROVED : RequestStatus.REJECTED);
            request.setResolutionDate(LocalDateTime.now());
            request.setResolutionNotes(approve ? "Reversal approved" : "Reversal rejected");
        }
    }
    
    public void manageInactivationRequest(AccountInactivationRequest request, boolean approve, String rejectionNotes) {
        if (request == null) {
            throw new RequestNotFoundException("Request does not exist");
        }

        if (approve) {
            request.approve(this);
            Client c = request.getRequester();
           
        } else {
            if (rejectionNotes == null || rejectionNotes.isBlank()) {
                throw new IllegalArgumentException("Rejection notes must be provided when rejecting a request");
            }
            request.reject(this, rejectionNotes);
        }
    }


	public List<ReversalRequest> getReversalRequests() {
		return reversalRequests;
	}

	public List<AccountInactivationRequest> getDeletionRequests() {
		return deletionRequests;
	}
    
}
