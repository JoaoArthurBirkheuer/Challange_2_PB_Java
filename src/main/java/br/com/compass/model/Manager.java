package br.com.compass.model;

// import java.util.ArrayList;
// import java.util.List;

import jakarta.persistence.Entity;
// import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_managers")
public class Manager extends User {

    /*@OneToMany(mappedBy = "resolver")
    private List<ReversalRequest> reversalRequests = new ArrayList<>();

    @OneToMany(mappedBy = "resolver")
    private List<AccountInactivationRequest> accountDeletionRequests = new ArrayList<>();

	public List<ReversalRequest> getReversalRequests() {
		return reversalRequests;
	}

	public List<AccountInactivationRequest> getAccountDeletionRequests() {
		return accountDeletionRequests;
	}*/
}
