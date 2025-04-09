package br.com.compass.dto;

import java.time.LocalDateTime;

import br.com.compass.model.enums.RequestStatus;

public class AccountInactivationRequestDTO {

    private Long id;
    private Long accountId;
    private String reason;
    private RequestStatus status;
    private LocalDateTime requestDate;
    private LocalDateTime resolutionDate;
    private String resolutionNotes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }

    public LocalDateTime getResolutionDate() { return resolutionDate; }
    public void setResolutionDate(LocalDateTime resolutionDate) { this.resolutionDate = resolutionDate; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    @Override
    public String toString() {
        return "AccountInactivationRequestDTO {" +
                "id=" + id +
                ", accountId=" + accountId +
                ", reason='" + reason + '\'' +
                ", status=" + status +
                ", requestDate=" + requestDate +
                ", resolutionDate=" + resolutionDate +
                ", resolutionNotes='" + resolutionNotes + '\'' +
                '}';
    }
}
