package br.com.compass.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import br.com.compass.model.enums.AccountType;
import br.com.compass.model.enums.TransactionType;

public class Account {
    private Long id;
    private String accountNumber;
    private BigDecimal balance = BigDecimal.ZERO;
    private AccountType type;
    private boolean active;

    private List<Transaction> deposits = new ArrayList<>();
    private List<Transaction> withdrawals = new ArrayList<>();
    private List<Transaction> transactionsSent = new ArrayList<>();
    private List<Transaction> transactionsReceived = new ArrayList<>();

    public Transaction deposit(BigDecimal amount) {
        Transaction t = new Transaction(amount, TransactionType.DEPOSIT, this, null);
        deposits.add(t);
        balance = balance.add(amount);
        return t;
    }

    public Transaction withdraw(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) throw new IllegalArgumentException("Saldo insuficiente");
        Transaction t = new Transaction(amount, TransactionType.WITHDRAWAL, this, null);
        withdrawals.add(t);
        balance = balance.subtract(amount);
        return t;
    }

    public Transaction transfer(Account target, BigDecimal amount) {
        if (balance.compareTo(amount) < 0) throw new IllegalArgumentException("Saldo insuficiente");
        Transaction t = new Transaction(amount, TransactionType.TRANSFER_OUT, this, target);
        transactionsSent.add(t);
        target.transactionsReceived.add(t);
        this.balance = this.balance.subtract(amount);
        target.balance = target.balance.add(amount);
        return t;
    }

    public void requestReversal(Transaction t, String reason) {
        ReversalRequest request = new ReversalRequest(t, this, reason);
        // Este método apenas cria. O Manager deve aprovar/rejeitar.
    }

    public List<Transaction> getFullHistory() {
        List<Transaction> history = new ArrayList<>();
        history.addAll(deposits);
        history.addAll(withdrawals);
        history.addAll(transactionsSent);
        history.addAll(transactionsReceived);
        return history;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public AccountType getType() {
		return type;
	}

	public void setType(AccountType type) {
		this.type = type;
	}

	public boolean isActive() {
		return active;
	}

	public List<Transaction> getDeposits() {
		return deposits;
	}

	public void setDeposits(List<Transaction> deposits) {
		this.deposits = deposits;
	}

	public List<Transaction> getWithdrawals() {
		return withdrawals;
	}

	public void setWithdrawals(List<Transaction> withdrawals) {
		this.withdrawals = withdrawals;
	}

	public List<Transaction> getTransactionsSent() {
		return transactionsSent;
	}

	public void setTransactionsSent(List<Transaction> transactionsSent) {
		this.transactionsSent = transactionsSent;
	}

	public List<Transaction> getTransactionsReceived() {
		return transactionsReceived;
	}

	public void setTransactionsReceived(List<Transaction> transactionsReceived) {
		this.transactionsReceived = transactionsReceived;
	}

	public void setActive(boolean unlock) {
		// TODO Auto-generated method stub
		
	}

    
}
