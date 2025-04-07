package br.com.compass.model;

import java.time.LocalDateTime;

import br.com.compass.model.enums.RequestStatus;

public class ReversalRequest {
    private Long id;
    private RequestStatus status = RequestStatus.PENDING;
    private String reason;
    private LocalDateTime requestDate = LocalDateTime.now();
    private LocalDateTime resolutionDate;
    private String resolutionNotes;
    private Transaction transaction;
    private Client requester;
    private Manager resolvedBy;

    public ReversalRequest(Transaction transaction, Account account, String reason) {
        this.transaction = transaction;
        this.reason = reason;
        
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

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
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

	@Override
	public String toString() {
		return "ReversalRequest [id=" + id + ", status=" + status + ", reason=" + reason + ", requestDate="
				+ requestDate + ", resolutionDate=" + resolutionDate + ", resolutionNotes=" + resolutionNotes
				+ ", transaction=" + transaction + ", requester=" + requester + ", resolvedBy=" + resolvedBy + "]";
	}

}
