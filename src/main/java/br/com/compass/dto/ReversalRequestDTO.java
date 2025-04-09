package br.com.compass.dto;

import java.time.LocalDateTime;

import br.com.compass.model.enums.RequestStatus;

public class ReversalRequestDTO {

    private Long id;
    private Long transactionId;
    private Long clientId;
    private Long resolvedByManagerId;
    private RequestStatus status;
    private String reason;
    private LocalDateTime requestDate;
    private LocalDateTime resolutionDate;
    private String resolutionNotes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getResolvedByManagerId() { return resolvedByManagerId; }
    public void setResolvedByManagerId(Long resolvedByManagerId) { this.resolvedByManagerId = resolvedByManagerId; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }

    public LocalDateTime getResolutionDate() { return resolutionDate; }
    public void setResolutionDate(LocalDateTime resolutionDate) { this.resolutionDate = resolutionDate; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    @Override
    public String toString() {
        return "ReversalRequestDTO {" +
                "id=" + id +
                ", transactionId=" + transactionId +
                ", clientId=" + clientId +
                ", resolvedByManagerId=" + resolvedByManagerId +
                ", status=" + status +
                ", reason='" + reason + '\'' +
                ", requestDate=" + requestDate +
                ", resolutionDate=" + resolutionDate +
                ", resolutionNotes='" + resolutionNotes + '\'' +
                '}';
    }
}
