package br.com.compass.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_clients")
public class Client extends User {

	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts =  new ArrayList<>();

	public List<Account> getAccounts() {
		return accounts;
	}

	public boolean isBlocked() {
		return getBlocked() != null && getBlocked();
	}
	
}
