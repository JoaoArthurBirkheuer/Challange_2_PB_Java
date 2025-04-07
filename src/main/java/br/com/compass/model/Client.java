package br.com.compass.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Client extends User {
    private List<Account> accounts = new ArrayList<>();

    public Client() {}

    public Client(Long id, String cpf, String name, String passwordHash, LocalDateTime createdAt, String email) {
        super(id, cpf, name, passwordHash, createdAt, email);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void addAccount(Account account) {
        this.accounts.add(account);
    }
    
    public void requestAccountRemoval(Account account) {
    	
    }
}
