package br.com.compass.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "tb_clients")
public class Client extends User {

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts;

	public List<Account> getAccounts() {
		return accounts;
	}
	
}
