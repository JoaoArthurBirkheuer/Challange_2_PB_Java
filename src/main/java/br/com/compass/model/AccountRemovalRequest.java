package br.com.compass.model;

import java.time.LocalDateTime;

import br.com.compass.model.enums.RequestStatus;

public class AccountRemovalRequest {
    private Long id;
    private RequestStatus status = RequestStatus.PENDING;
    private String reason;
    private LocalDateTime requestDate = LocalDateTime.now();
    private LocalDateTime resolutionDate;
    private String resolutionNotes;

    private Account targetAccount;
    private Client requester;
    private Manager resolvedBy;

    public AccountRemovalRequest(Account account, Client requester, String reason) {
        this.targetAccount = account;
        this.requester = requester;
        this.reason = reason;
    }

    public void approve(Manager manager) {
        this.status = RequestStatus.APPROVED;
        this.resolvedBy = manager;
        this.resolutionDate = LocalDateTime.now();
        this.resolutionNotes = "Conta aprovada para deleção";
        this.targetAccount.setActive(false); // Ou alguma flag específica
    }

    public void reject(Manager manager, String notes) {
        this.status = RequestStatus.REJECTED;
        this.resolvedBy = manager;
        this.resolutionDate = LocalDateTime.now();
        this.resolutionNotes = notes;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RequestStatus getStatus() {
		return status;
	}

	public void setStatus(RequestStatus status) {
		this.status = status;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public LocalDateTime getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(LocalDateTime requestDate) {
		this.requestDate = requestDate;
	}

	public LocalDateTime getResolutionDate() {
		return resolutionDate;
	}

	public void setResolutionDate(LocalDateTime resolutionDate) {
		this.resolutionDate = resolutionDate;
	}

	public String getResolutionNotes() {
		return resolutionNotes;
	}

	public void setResolutionNotes(String resolutionNotes) {
		this.resolutionNotes = resolutionNotes;
	}

	public Account getTargetAccount() {
		return targetAccount;
	}

	public void setTargetAccount(Account targetAccount) {
		this.targetAccount = targetAccount;
	}

	public Client getRequester() {
		return requester;
	}

	public void setRequester(Client requester) {
		this.requester = requester;
	}

	public Manager getResolvedBy() {
		return resolvedBy;
	}

	public void setResolvedBy(Manager resolvedBy) {
		this.resolvedBy = resolvedBy;
	}

    
}
