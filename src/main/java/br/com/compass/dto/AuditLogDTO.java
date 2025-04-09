package br.com.compass.dto;

import java.time.LocalDateTime;

public class AuditLogDTO {

    private Long id;
    private String actionType;
    private String details;
    private LocalDateTime timestamp;
    private Long userId;
    private Long affectedAccountId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getAffectedAccountId() { return affectedAccountId; }
    public void setAffectedAccountId(Long affectedAccountId) { this.affectedAccountId = affectedAccountId; }

    @Override
    public String toString() {
        return "AuditLogDTO {" +
                "id=" + id +
                ", actionType='" + actionType + '\'' +
                ", details='" + details + '\'' +
                ", timestamp=" + timestamp +
                ", userId=" + userId +
                ", affectedAccountId=" + affectedAccountId +
                '}';
    }
}
