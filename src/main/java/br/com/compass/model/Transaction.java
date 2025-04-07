package br.com.compass.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.compass.model.enums.TransactionType;

public class Transaction {
    private Long id;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private TransactionType type;
    private Account sourceAccount;
    private Account targetAccount;
    private boolean isReversible = true;

    public Transaction(BigDecimal amount, TransactionType type, Account source, Account target) {
        this.amount = amount;
        this.type = type;
        this.sourceAccount = source;
        this.targetAccount = target;
        this.timestamp = LocalDateTime.now();
    }

    public void markAsIrreversible() {
        this.isReversible = false;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public TransactionType getType() {
		return type;
	}

	public void setType(TransactionType type) {
		this.type = type;
	}

	public Account getSourceAccount() {
		return sourceAccount;
	}

	public void setSourceAccount(Account sourceAccount) {
		this.sourceAccount = sourceAccount;
	}

	public Account getTargetAccount() {
		return targetAccount;
	}

	public void setTargetAccount(Account targetAccount) {
		this.targetAccount = targetAccount;
	}

	public boolean isReversible() {
		return isReversible;
	}

	public void setReversible(boolean isReversible) {
		this.isReversible = isReversible;
	}

	@Override
	public String toString() {
		return "Transaction [id=" + id + ", amount=" + amount + ", timestamp=" + timestamp + ", type=" + type
				+ ", sourceAccount=" + sourceAccount + ", targetAccount=" + targetAccount + ", isReversible="
				+ isReversible + "]";
	}

    
}
