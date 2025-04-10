package br.com.compass.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import br.com.compass.model.Account;
import br.com.compass.model.Client;
import br.com.compass.model.Manager;
import br.com.compass.model.enums.AccountType;
import br.com.compass.utils.PasswordHasher;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

public class DataSeeder {
    
    public static final DateTimeFormatter BIRTHDATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Random random = new Random();

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

            // Client 1 with accounts
            if (!clientExists(em, "733.006.260-05")) {
                Client client1 = createClient("Bob Client", "733.006.260-05", "client123", 0,
                        "12/04/2005", "54996661267");
                em.persist(client1);
                
                // Create accounts for client1
                Account checkingAccount = createAccount(client1, AccountType.CHECKING, "1000.00");
                Account savingsAccount = createAccount(client1, AccountType.SAVINGS, "5000.00");
                
                em.persist(checkingAccount);
                em.persist(savingsAccount);
                
                System.out.println("Client 1 created with accounts: " + client1.getCpf());
                System.out.println(" - Checking Account: " + checkingAccount.getAccountNumber() + 
                                 " | Balance: " + checkingAccount.getBalance());
                System.out.println(" - Savings Account: " + savingsAccount.getAccountNumber() + 
                                 " | Balance: " + savingsAccount.getBalance());
            }

            // Client 2 (Blocked) with accounts
            if (!clientExists(em, "849.987.450-93")) {
                Client client2 = createClient("Charlie Blocked", "849.987.450-93", "wrongpass", 3,
                        "30/04/2004", "54996672666");
                em.persist(client2);
                
                // Create single account for blocked client
                Account salaryAccount = createAccount(client2, AccountType.SALARY, "2500.00");
                em.persist(salaryAccount);
                
                System.out.println("Client 2 (Blocked) created with account: " + client2.getCpf());
                System.out.println(" - Salary Account: " + salaryAccount.getAccountNumber() + 
                                 " | Balance: " + salaryAccount.getBalance());
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

    private static Account createAccount(Client owner, AccountType type, String initialBalance) {
        Account account = new Account();
        account.setOwner(owner);
        account.setType(type);
        account.setBalance(new BigDecimal(initialBalance));
        account.setActive(true);
        account.setAccountNumber(generateAccountNumber());
        return account;
    }

    private static String generateAccountNumber() {
        // Generate an 8-digit account number with leading zeros
        return String.format("%08d", random.nextInt(100000000));
    }
}