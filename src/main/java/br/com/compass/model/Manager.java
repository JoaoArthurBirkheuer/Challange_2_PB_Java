package br.com.compass.model;

import java.time.LocalDateTime;

import br.com.compass.model.enums.RequestStatus;

public class Manager extends User {

    public Manager() {}

    public Manager(Long id, String cpf, String name, String passwordHash, LocalDateTime createdAt, String email) {
        super(id, cpf, name, passwordHash, createdAt, email);
    }

    public void unlockAccount(Account account, boolean unlock) {
        if (account != null) {
            account.setActive(unlock);
        }
    }

    public void manageReversalRequest(ReversalRequest request, boolean approve) {
        if (request != null) {
            request.setStatus(approve ? RequestStatus.APPROVED : RequestStatus.REJECTED);
            request.setResolutionDate(LocalDateTime.now());
            request.setResolutionNotes(approve ? "Reversão aprovada" : "Reversão rejeitada");
        }
    }
}
