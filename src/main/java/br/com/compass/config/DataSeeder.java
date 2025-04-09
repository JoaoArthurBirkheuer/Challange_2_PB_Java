package br.com.compass.config;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import br.com.compass.model.Client;
import br.com.compass.model.Manager;
import br.com.compass.utils.PasswordHasher;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

public class DataSeeder {
    
    public static final DateTimeFormatter BIRTHDATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void seed() {
        EntityManager em = JpaConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        
        try {
            System.out.println("Starting database seeding...");
            tx.begin();

            // Manager
            if (!managerExists(em, "343.209.320-99")) {
                Manager manager = createManager("Alice Manager", "343.209.320-99", "admin123",
                        "05/08/1980", "54997771267");
                em.persist(manager);
                System.out.println("Manager created: " + manager.getCpf());
            }

            // Client 1
            if (!clientExists(em, "733.006.260-05")) {
                Client client1 = createClient("Bob Client", "733.006.260-05", "client123", 0,
                        "12/04/2005", "54996661267");
                em.persist(client1);
                System.out.println("Client 1 created: " + client1.getCpf() + 
                                 " | Attempts: " + client1.getLoginAttempts() + 
                                 " | Blocked: " + client1.getBlocked());
            }

            // Client 2 (Bloqueado)
            if (!clientExists(em, "849.987.450-93")) {
                Client client2 = createClient("Charlie Blocked", "849.987.450-93", "wrongpass", 3,
                        "30/04/2004", "54996672666");
                em.persist(client2);
                
                // Verificação imediata
                System.out.println("Client 2 created: " + client2.getCpf() + 
                                 " | Attempts: " + client2.getLoginAttempts() + 
                                 " | Blocked: " + client2.getBlocked());
                
                // Força o flush para garantir persistência
                em.flush();
                System.out.println("Client 2 persisted successfully");
            }

            tx.commit();
            System.out.println("Database seeding completed successfully!");
        } catch (Exception e) {
            System.err.println("SEEDING ERROR: " + e.getMessage());
            if (tx.isActive()) {
                tx.rollback();
                System.err.println("Transaction rolled back");
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // ================= HELPER METHODS =================

    private static boolean managerExists(EntityManager em, String cpf) {
        try {
            em.createQuery("SELECT m FROM Manager m WHERE m.cpf = :cpf", Manager.class)
              .setParameter("cpf", cpf)
              .getSingleResult();
            return true;
        } catch (NoResultException e) {
            return false;
        }
    }

    private static boolean clientExists(EntityManager em, String cpf) {
        try {
            em.createQuery("SELECT c FROM Client c WHERE c.cpf = :cpf", Client.class)
              .setParameter("cpf", cpf)
              .getSingleResult();
            return true;
        } catch (NoResultException e) {
            return false;
        }
    }

    private static Client createClient(String name, String cpf, String plainPassword, int loginAttempts,
            String birthDate, String cellphoneNumber) {
        Client client = new Client();
        client.setName(name);
        client.setCpf(cpf);
        byte[] salt = PasswordHasher.generateSalt();
        client.setPasswordSalt(salt);
        client.setPasswordHash(PasswordHasher.hashPassword(plainPassword, salt));
        client.setLoginAttempts(loginAttempts);
        client.setBlocked(loginAttempts >= 3);
        client.setBirthDate(LocalDate.parse(birthDate, BIRTHDATE_FORMATTER)); 
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
        manager.setBirthDate(LocalDate.parse(birthDate, BIRTHDATE_FORMATTER));
        manager.setCellphoneNumber(cellphoneNumber);
        return manager;
    }
}