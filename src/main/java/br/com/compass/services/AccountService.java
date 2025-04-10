package br.com.compass.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import br.com.compass.dao.AccountDAO;
import br.com.compass.exceptions.AccountNotFoundException;
import br.com.compass.exceptions.BusinessRuleException;
import br.com.compass.model.Account;
import br.com.compass.model.Client;

public class AccountService {

    private final AccountDAO accountDAO;

    public AccountService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    public void deposit(Client client, String accountNumber, Double amount) {
        Account account = getAccountFromClient(client, accountNumber);
        if (!account.getActive()) {
            throw new BusinessRuleException("Cannot deposit to an inactive account.");
        }

        account.setBalance(account.getBalance() + amount);
        accountDAO.update(account);

        AuditService.logAction(
            "DEPOSIT",
            "Deposit of " + amount + " to account " + accountNumber,
            LocalDateTime.now(),
            client,
            account
        );
    }

    public void withdraw(Client client, String accountNumber, Double amount) {
        Account account = getAccountFromClient(client, accountNumber);
        if (!account.getActive()) {
            throw new BusinessRuleException("Cannot withdraw from an inactive account.");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessRuleException("Insufficient balance.");
        }

        account.setBalance(account.getBalance() - amount);
        accountDAO.update(account);

        AuditService.logAction(
            "WITHDRAW",
            "Withdrawal of " + amount + " from account " + accountNumber,
            LocalDateTime.now(),
            client,
            account
        );
    }

    public void transfer(Client client, String sourceAccountNumber, String destinationAccountNumber, Double amount) {
        Account source = getAccountFromClient(client, sourceAccountNumber);

        if (!source.getActive()) {
            throw new BusinessRuleException("Cannot transfer from an inactive account.");
        }

        if (source.getBalance().compareTo(amount) < 0) {
            throw new BusinessRuleException("Insufficient balance for transfer.");
        }

        Optional<Account> optionalDestination = accountDAO.findByAccountNumber(destinationAccountNumber);
        if (optionalDestination.isEmpty()) {
            throw new AccountNotFoundException("Destination account not found.");
        }

        Account destination = optionalDestination.get();
        if (!destination.getActive()) {
            throw new BusinessRuleException("Destination account is inactive.");
        }

        // Realiza transferência
        source.setBalance(source.getBalance() - amount);
        destination.setBalance(destination.getBalance() + amount);
        accountDAO.update(source);
        accountDAO.update(destination);

        AuditService.logAction(
            "TRANSFER",
            "Transfer of " + amount + " from account " + sourceAccountNumber + " to " + destinationAccountNumber,
            LocalDateTime.now(),
            client,
            source
        );
    }

    public void requestReversal(Client client, String accountNumber, String operationId) {
        Account account = getAccountFromClient(client, accountNumber);
        AuditService.logAction(
            "REVERSAL_REQUEST",
            "Reversal requested for operation ID " + operationId,
            LocalDateTime.now(),
            client,
            account
        );
    }

    public void requestInactivation(Client client, String accountNumber) {
        Account account = getAccountFromClient(client, accountNumber);
        if (!account.getActive()) {
            throw new BusinessRuleException("Account is already inactive.");
        }

        account.setActive(false);
        accountDAO.update(account);

        AuditService.logAction(
            "INACTIVATION_REQUEST",
            "Account " + accountNumber + " inactivation requested",
            LocalDateTime.now(),
            client,
            account
        );
    }

    private Account getAccountFromClient(Client client, String accountNumber) {
        return client.getAccounts().stream()
            .filter(acc -> acc.getAccountNumber().equals(accountNumber))
            .findFirst()
            .orElseThrow(() -> new AccountNotFoundException("Account not found for this client."));
    }
}
