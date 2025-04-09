package br.com.compass.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.compass.model.enums.TransactionType;

public class TransactionDTO {

    private Long id;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private TransactionType type;
    private Long sourceAccountId;
    private Long targetAccountId;
    private boolean isReversible;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public Long getSourceAccountId() { return sourceAccountId; }
    public void setSourceAccountId(Long sourceAccountId) { this.sourceAccountId = sourceAccountId; }

    public Long getTargetAccountId() { return targetAccountId; }
    public void setTargetAccountId(Long targetAccountId) { this.targetAccountId = targetAccountId; }

    public boolean isReversible() { return isReversible; }
    public void setReversible(boolean reversible) { isReversible = reversible; }

    @Override
    public String toString() {
        return "TransactionDTO {" +
                "id=" + id +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                ", type=" + type +
                ", sourceAccountId=" + sourceAccountId +
                ", targetAccountId=" + targetAccountId +
                ", isReversible=" + isReversible +
                '}';
    }
}
