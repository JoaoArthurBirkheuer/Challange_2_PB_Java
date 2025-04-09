package br.com.compass.dto;

import java.math.BigDecimal;

import br.com.compass.model.enums.AccountType;

public class AccountDTO {

    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private AccountType type;
    private boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public AccountType getType() { return type; }
    public void setType(AccountType type) { this.type = type; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return "AccountDTO {" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", balance=" + balance +
                ", type=" + type +
                ", active=" + active +
                '}';
    }
}
