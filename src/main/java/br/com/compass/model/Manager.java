package br.com.compass.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "tb_managers")
public class Manager extends User {

    @OneToMany(mappedBy = "resolver")
    private List<ReversalRequest> reversalRequests;

    @OneToMany(mappedBy = "resolver")
    private List<AccountInactivationRequest> accountDeletionRequests;

	public List<ReversalRequest> getReversalRequests() {
		return reversalRequests;
	}

	public List<AccountInactivationRequest> getAccountDeletionRequests() {
		return accountDeletionRequests;
	}

}
