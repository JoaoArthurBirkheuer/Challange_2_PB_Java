package br.com.compass.model;

import java.time.LocalDateTime;

public class AuditLog {
    private Long id;
    private String actionType;
    private String details;
    private LocalDateTime timestamp = LocalDateTime.now();
    private Account affectedAccount;

    public AuditLog(String actionType, String details, Account account) {
        this.actionType = actionType;
        this.details = details;
        this.affectedAccount = account;
    }

    // Getters e Setters omitidos por brevidade
}
