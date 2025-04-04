package br.com.compass.model;

import java.time.LocalDateTime;
import java.util.Objects;

public abstract class User {
	private Long id;
	private String cpf;
	private String name;
	private String passwordHash;
	private LocalDateTime createdAt; // ÚTIL PARA ENVIAR AO LOG
									 // CASO CONTRÁRIO, SERIA DESNECESSÁRIO
	
	public User(){}

	public User(Long id, String cpf, String name, String passwordHash, LocalDateTime createdAt) {
		super();
		this.id = id;
		this.cpf = cpf;
		this.name = name;
		this.passwordHash = passwordHash;
		this.createdAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public int hashCode() {
		return Objects.hash(cpf, createdAt, id, name, passwordHash);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		return Objects.equals(cpf, other.cpf) && Objects.equals(createdAt, other.createdAt)
				&& Objects.equals(id, other.id) && Objects.equals(name, other.name)
				&& Objects.equals(passwordHash, other.passwordHash);
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", cpf=" + cpf + ", name=" + name + ", passwordHash=" + passwordHash + ", createdAt="
				+ createdAt + "]";
	}
	
}
