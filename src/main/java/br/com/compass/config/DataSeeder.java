package br.com.compass.config;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import br.com.compass.model.Client;
import br.com.compass.model.Manager;
import br.com.compass.utils.PasswordHasher;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class DataSeeder {
	
	public static final DateTimeFormatter BIRTHDATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void seed() {
        EntityManager em = JpaConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // Criando um gerente (senha: admin123)
            Manager manager = createManager("Alice Manager", "343.209.320-99", "admin123",
            		"05/08/1980","54997771267");
            em.persist(manager);

            // Criando um cliente desbloqueado (senha: client123)
            Client client1 = createClient("Bob Client", "733.006.260-05", "client123", 0,
            		"12/04/2005", "54996661267"); // senha: client123
            em.persist(client1);

            // Criando um cliente bloqueado (senha: wrongpass)
            Client client2 = createClient("Charlie Blocked", "849.987.450-93", "wrongpass", 3,
            		"30/04/2004","54996672666"); // senha: wrongpass
            em.persist(client2);

            tx.commit();
            System.out.println("Seed data created successfully!");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // ================= MÉTODOS AUXILIARES =================

    private static Client createClient(String name, String cpf, String plainPassword, int loginAttempts,
    		String birthDate, String cellphoneNumber) {
        Client client = new Client();
        client.setName(name);
        client.setCpf(cpf);
        byte[] salt = PasswordHasher.generateSalt();
        client.setPasswordSalt(salt);
        client.setPasswordHash(PasswordHasher.hashPassword(plainPassword, salt));
        client.setLoginAttempts(loginAttempts);
        client.setBirthDate(LocalDate.parse(birthDate,BIRTHDATE_FORMATTER)); 
        client.setCellphoneNumber(cellphoneNumber);

        return client;
    }

    private static Manager createManager(String name, String cpf, String plainPassword,
    		String birthDate, String cellphoneNumber) {
        Manager manager = new Manager();
        manager.setName(name);
        manager.setCpf(cpf);
        byte[] salt = PasswordHasher.generateSalt();
        manager.setPasswordSalt(salt);
        manager.setPasswordHash(PasswordHasher.hashPassword(plainPassword, salt));
        manager.setBirthDate(LocalDate.parse(birthDate,BIRTHDATE_FORMATTER));
        manager.setCellphoneNumber(cellphoneNumber);
        return manager;
    }
}
