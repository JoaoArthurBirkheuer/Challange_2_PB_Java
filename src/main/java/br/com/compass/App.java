package br.com.compass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import br.com.compass.dao.AccountDAO;
import br.com.compass.dao.AuditLogDAO;
import br.com.compass.dao.ClientDAO;
import br.com.compass.dao.ManagerDAO;
import br.com.compass.dao.ReversalRequestDAO;
import br.com.compass.dao.TransactionDAO;
import br.com.compass.dao.UserDAO;
import br.com.compass.exceptions.BusinessException;
import br.com.compass.model.Account;
import br.com.compass.model.AccountInactivationRequest;
import br.com.compass.model.AuditLog;
import br.com.compass.model.Client;
import br.com.compass.model.Manager;
import br.com.compass.model.ReversalRequest;
import br.com.compass.model.Transaction;
import br.com.compass.model.User;
import br.com.compass.model.enums.AccountType;
import br.com.compass.model.enums.RequestStatus;
import br.com.compass.model.enums.TransactionType;
import br.com.compass.services.AccountService;
import br.com.compass.services.AuditService;
import br.com.compass.services.AuthService;
import br.com.compass.services.InactivationRequestService;
import br.com.compass.services.ReversalService;
import br.com.compass.utils.CPFValidator;
import br.com.compass.utils.PasswordHasher;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class App {

	private static UserDAO userDAO = new UserDAO();
	private static final AccountDAO accountDAO = new AccountDAO();
	private static final AccountService as = new AccountService(accountDAO);
	private static final ReversalService reversalService = new ReversalService();

	public static void main(String[] args) {
		Locale.setDefault(Locale.US);
		br.com.compass.config.DataSeeder.seed();
		Scanner scanner = new Scanner(System.in);
		mainMenu(scanner);
		scanner.close();
		System.out.println("Application closed");
	}

	public static void mainMenu(Scanner scanner) {
		boolean running = true;

		while (running) {
			System.out.println("\n========= MAIN MENU =========");
			System.out.println("1. Login");
			System.out.println("2. Open New Account");
			System.out.println("3. Get System Operations");
			System.out.println("0. Exit");
			System.out.println("============================");
			System.out.print("Select an option: ");

			int option;
			try {
				option = scanner.nextInt();
			} catch (Exception e) {
				System.out.println("Invalid input. Please enter a number.");
				scanner.nextLine();
				continue;
			}
			scanner.nextLine();

			switch (option) {
			case 1:
				loginMenu(scanner);
				break;
			case 2:
				System.out.println("\n>> Account Opening");
				registerClient(scanner);
				break;
			case 3:
				AuditLogDAO.showAuditLogMenu(scanner);
				break;
			case 0:
				running = false;
				break;
			default:
				System.out.println("Invalid option! Please try again.");
			}
		}
	}

	private static void registerClient(Scanner scanner) {
	    String name = null;
	    String cpf = null;
	    LocalDate birthDate = null;
	    String phone = null;
	    AccountType accountType = null;
	    String password = null;

	    System.out.println("\n=== NEW CLIENT REGISTRATION === (Enter '0' at any time to cancel)");
	    boolean nameValid = false;
	    while (!nameValid) {
	        System.out.print("\nFull Name (minimum 3 characters): ");
	        name = scanner.nextLine().trim();

	        if (name.equals("0")) {
	            System.out.println("Registration cancelled.");
	            return;
	        }

	        if (name.isEmpty()) {
	            System.out.println("Error: Name cannot be empty!");
	        } else if (name.length() < 3) {
	            System.out.println("Error: Name must have at least 3 characters!");
	        } else {
	            nameValid = true;
	        }
	    }

	    boolean cpfValid = false;
	    while (!cpfValid) {
	        System.out.print("\nCPF (format: XXX.XXX.XXX-XX): ");
	        cpf = scanner.nextLine().trim();

	        if (cpf.equals("0")) {
	            System.out.println("Registration cancelled.");
	            return;
	        }

	        if (!cpf.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}")) {
	            System.out.println("Error: Invalid CPF format! Please use XXX.XXX.XXX-XX pattern.");
	            continue;
	        }

	        if (!CPFValidator.isValidCPF(cpf)) {
	            System.out.println("Error: Invalid CPF number! Please check the digits.");
	            continue;
	        }

	        try (ClientDAO tempClientDAO = new ClientDAO()) {
	            if (tempClientDAO.findByCpf(cpf) != null) {
	                System.out.println("Error: This CPF is already registered!");
	                continue;
	            }
	            cpfValid = true;
	        } catch (Exception e) {
	            System.out.println("Error verifying CPF: " + e.getMessage());
	        }
	    }

	    boolean dateValid = false;
	    while (!dateValid) {
	        System.out.print("\nBirth Date (DD/MM/YYYY): ");
	        String dateInput = scanner.nextLine().trim();

	        if (dateInput.equals("0")) {
	            System.out.println("Registration cancelled.");
	            return;
	        }

	        try {
	            birthDate = LocalDate.parse(dateInput, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	            LocalDate minimumDate = LocalDate.now().minusYears(18);

	            if (birthDate.isAfter(minimumDate)) {
	                System.out.println("Error: You must be at least 18 years old!");
	            } else {
	                dateValid = true;
	            }
	        } catch (DateTimeParseException e) {
	            System.out.println("Error: Invalid date format! Use DD/MM/YYYY.");
	        }
	    }

	    boolean phoneValid = false;
	    while (!phoneValid) {
	        System.out.print("\nPhone Number (with area code): ");
	        phone = scanner.nextLine().trim();

	        if (phone.equals("0")) {
	            System.out.println("Registration cancelled.");
	            return;
	        }

	        String digitsOnly = phone.replaceAll("[^0-9]", "");
	        if (digitsOnly.length() < 11) {
	            System.out.println("Error: Phone must have at least 11 digits (including area code)!");
	        } else {
	            phoneValid = true;
	        }
	    }

	    boolean typeValid = false;
	    while (!typeValid) {
	        System.out.println("\nSelect Account Type:");
	        System.out.println("1. Checking Account");
	        System.out.println("2. Salary Account");
	        System.out.println("3. Savings Account");
	        System.out.println("4. Investments Account");
	        System.out.println("0. Cancel Registration");
	        System.out.print("Choose [1-4] or 0 to cancel: ");

	        String typeInput = scanner.nextLine().trim();
	        if (typeInput.equals("0")) {
	            System.out.println("Registration cancelled.");
	            return;
	        }

	        try {
	            int choice = Integer.parseInt(typeInput);
	            accountType = switch (choice) {
	                case 1 -> AccountType.CHECKING;
	                case 2 -> AccountType.SALARY;
	                case 3 -> AccountType.SAVINGS;
	                case 4 -> AccountType.INVESTMENT;
	                default -> {
	                    System.out.println("Error: Invalid choice!");
	                    yield null;
	                }
	            };
	            if (accountType != null) typeValid = true;
	        } catch (NumberFormatException e) {
	            System.out.println("Error: Please enter a number!");
	        }
	    }

	    boolean passwordValid = false;
	    while (!passwordValid) {
	        System.out.print("\nCreate Password (min 8 chars, 1 number, 1 special char): ");
	        password = scanner.nextLine().trim();

	        if (password.equals("0")) {
	            System.out.println("Registration cancelled.");
	            return;
	        }

	        if (password.length() < 8) {
	            System.out.println("Error: Password must be at least 8 characters!");
	            continue;
	        }

	        if (!password.matches(".*\\d.*")) {
	            System.out.println("Error: Password must contain at least 1 number!");
	            continue;
	        }

	        if (!password.matches(".*[!@#$%^&*()].*")) {
	            System.out.println("Error: Password must contain at least 1 special character!");
	            continue;
	        }

	        System.out.print("Confirm Password: ");
	        String confirmPassword = scanner.nextLine().trim();
	        if (!password.equals(confirmPassword)) {
	            System.out.println("Error: Passwords don't match!");
	        } else {
	            passwordValid = true;
	        }
	    }

	    System.out.println("\n=== REGISTRATION SUMMARY ===");
	    System.out.println("Name: " + name);
	    System.out.println("CPF: " + cpf);
	    System.out.println("Birth Date: " + birthDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
	    System.out.println("Phone: " + phone);
	    System.out.println("Account Type: " + accountType);
	    System.out.print("\nConfirm registration? (Y/N): ");

	    String confirmation = scanner.nextLine().trim().toUpperCase();
	    if (!confirmation.equalsIgnoreCase("Y")) {
	        System.out.println("Registration cancelled.");
	        return;
	    }

	    // 9. Database Operations
	    try {
	       
	        Client newClient = new Client();
	        newClient.setName(name);
	        newClient.setCpf(cpf);
	        newClient.setBirthDate(birthDate);
	        newClient.setCellphoneNumber(phone);
	        
	        byte[] salt = PasswordHasher.generateSalt();
	        newClient.setPasswordSalt(salt);
	        newClient.setPasswordHash(PasswordHasher.hashPassword(password, salt));

	        Account newAccount = new Account();
	        newAccount.setOwner(newClient);
	        newAccount.setAccountNumber(generateAccountNumber());
	        newAccount.setBalance(0.0);
	        newAccount.setActive(true);
	        newAccount.setType(accountType);

	        try (ClientDAO clientDAO = new ClientDAO()) {
	            EntityManager em = clientDAO.getEntityManager();
	            
	            if (em.getTransaction().isActive()) {
	                em.getTransaction().rollback();
	            }

	            EntityTransaction tx = em.getTransaction();
	            try {
	                tx.begin();
	                em.persist(newClient);
	                em.flush(); 
	                
	                newAccount.setOwner(newClient);
	                em.persist(newAccount);
	                
	                tx.commit();

	                AuditService.logAction(
	                    "ACCOUNT_CREATION",
	                    String.format("New %s account created for %s", accountType, name),
	                    LocalDateTime.now(),
	                    newClient,
	                    newAccount
	                );

	                System.out.println("\nRegistration successful!");
	                System.out.println("Account Number: " + newAccount.getAccountNumber());
	                System.out.println("Initial Balance: $0.00");

	            } catch (Exception e) {
	                if (tx != null && tx.isActive()) {
	                    tx.rollback();
	                }
	                AuditService.logAction(
	                    "REGISTRATION_FAILED",
	                    "Error: " + e.getMessage(),
	                    LocalDateTime.now(),
	                    null,
	                    null
	                );
	                System.out.println("Registration failed: " + e.getMessage());
	            }
	        }
	    } catch (Exception e) {
	        System.out.println("System error: " + e.getMessage());
	    }
	}

	private static void registerManager(Scanner scanner, Manager manager) {
	    if (manager.getId() != 1) {
	        System.out.println("Only Super Manager can register new managers");
	        return;
	    }

	    System.out.println("\n=== NEW MANAGER REGISTRATION === (Enter '0' at any time to cancel)");
	    String name = null;
	    String cpf = null;
	    LocalDate birthDate = null;
	    String phone = null;
	    String password = null;

	    boolean nameValid = false;
	    while (!nameValid) {
	        System.out.print("\nFull Name (minimum 3 characters): ");
	        name = scanner.nextLine().trim();

	        if (name.equals("0")) {
	            System.out.println("Registration cancelled.");
	            return;
	        }

	        if (name.isEmpty()) {
	            System.out.println("Error: Name cannot be empty!");
	        } else if (name.length() < 3) {
	            System.out.println("Error: Name must have at least 3 characters!");
	        } else {
	            nameValid = true;
	        }
	    }

	    boolean cpfValid = false;
	    while (!cpfValid) {
	        System.out.print("\nCPF (format: XXX.XXX.XXX-XX): ");
	        cpf = scanner.nextLine().trim();

	        if (cpf.equals("0")) {
	            System.out.println("Registration cancelled.");
	            return;
	        }

	        if (!cpf.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}")) {
	            System.out.println("Error: Invalid CPF format! Please use XXX.XXX.XXX-XX pattern.");
	            continue;
	        }
	       
	        if (!CPFValidator.isValidCPF(cpf)) {
	            System.out.println("Error: Invalid CPF number! Please check the digits.");
	            continue;
	        }

	        try (ManagerDAO tempManagerDAO = new ManagerDAO()) {
	            if (tempManagerDAO.findByCpf(cpf) != null) {
	                System.out.println("Error: This CPF is already registered!");
	                continue;
	            }
	            cpfValid = true;
	        } catch (Exception e) {
	            System.out.println("Error verifying CPF: " + e.getMessage());
	        }
	    }

	    boolean dateValid = false;
	    while (!dateValid) {
	        System.out.print("\nBirth Date (DD/MM/YYYY): ");
	        String dateInput = scanner.nextLine().trim();

	        if (dateInput.equals("0")) {
	            System.out.println("Registration cancelled.");
	            return;
	        }

	        try {
	            birthDate = LocalDate.parse(dateInput, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	            LocalDate minimumDate = LocalDate.now().minusYears(18);

	            if (birthDate.isAfter(minimumDate)) {
	                System.out.println("Error: You must be at least 18 years old!");
	            } else {
	                dateValid = true;
	            }
	        } catch (DateTimeParseException e) {
	            System.out.println("Error: Invalid date format! Please use DD/MM/YYYY.");
	        }
	    }

	    boolean phoneValid = false;
	    while (!phoneValid) {
	        System.out.print("\nPhone Number (with area code): ");
	        phone = scanner.nextLine().trim();

	        if (phone.equals("0")) {
	            System.out.println("Registration cancelled.");
	            return;
	        }

	        String digitsOnly = phone.replaceAll("[^0-9]", "");
	        if (digitsOnly.length() < 11) {  
	            System.out.println("Error: Phone must have at least 11 digits (including area code)!");
	        } else {
	            phoneValid = true;
	        }
	    }

	    boolean passwordValid = false;
	    while (!passwordValid) {
	        System.out.print("\nCreate Password (min 8 chars, 1 number, 1 special char): ");
	        password = scanner.nextLine().trim();

	        if (password.equals("0")) {
	            System.out.println("Registration cancelled.");
	            return;
	        }

	        if (password.length() < 8) {
	            System.out.println("Error: Password must be at least 8 characters!");
	            continue;
	        }

	        if (!password.matches(".*\\d.*")) {
	            System.out.println("Error: Password must contain at least 1 number!");
	            continue;
	        }

	        if (!password.matches(".*[!@#$%^&*()].*")) {
	            System.out.println("Error: Password must contain at least 1 special character!");
	            continue;
	        }

	        System.out.print("Confirm Password: ");
	        String confirmPassword = scanner.nextLine().trim();
	        if (!password.equals(confirmPassword)) {
	            System.out.println("Error: Passwords don't match!");
	        } else {
	            passwordValid = true;
	        }
	    }

	    System.out.println("\n=== REGISTRATION SUMMARY ===");
	    System.out.println("Name: " + name);
	    System.out.println("CPF: " + cpf);
	    System.out.println("Birth Date: " + birthDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
	    System.out.println("Phone: " + phone);
	    System.out.print("\nConfirm registration? (Y/N): ");

	    String confirmation = scanner.nextLine().trim().toUpperCase();
	    if (!confirmation.equals("Y")) {
	        System.out.println("Registration cancelled.");
	        return;
	    }

	    // Database Operations
	    try (ManagerDAO managerDAO = new ManagerDAO()) {
	        EntityTransaction tx = managerDAO.beginTransaction();
	        try {
	            // Create Manager
	            Manager newManager = new Manager();
	            newManager.setName(name);
	            newManager.setCpf(cpf);
	            newManager.setBirthDate(birthDate);
	            newManager.setCellphoneNumber(phone);
	            
	            // Password Hashing
	            byte[] salt = PasswordHasher.generateSalt();
	            newManager.setPasswordSalt(salt);
	            newManager.setPasswordHash(PasswordHasher.hashPassword(password, salt));
	            
	            managerDAO.save(newManager);
	            tx.commit();

	            // Audit Log
	            AuditService.logAction(
	                "MANAGER_CREATION",
	                "New manager account created",
	                LocalDateTime.now(),
	                newManager,
	                null
	            );

	            System.out.println("\nRegistration successful!");

	        } catch (Exception e) {
	            if (tx.isActive()) tx.rollback();
	            AuditService.logAction(
	                "MANAGER_CREATION_FAILED",
	                "Error: " + e.getMessage(),
	                LocalDateTime.now(),
	                null,
	                null
	            );
	            System.out.println("Registration failed: " + e.getMessage());
	        }
	    } catch (Exception e) {
	        System.out.println("System error: " + e.getMessage());
	    }
	}

	private static String generateAccountNumber() {
		return String.format("%08d", new Random().nextInt(100000000));
	}
	
	
	public static void loginMenu(Scanner scanner) {
	    System.out.print("\n>> Enter CPF: ");
	    String cpf = scanner.nextLine();

	    try (AuthService authService = new AuthService(userDAO)) {
	        AuthService.LoginType loginType = authService.checkLoginType(cpf);

	        if (loginType == null) {
	            System.out.println("CPF not registered in the system.");
	            AuditService.logAction("LOGIN_ATTEMPT", "Failed login - CPF not found", LocalDateTime.now(), null, null);
	            return;
	        }

	        switch (loginType) {
	            case MANAGER_ONLY:
	                handleManagerLogin(scanner, authService, cpf);
	                break;
	            case CLIENT_ONLY:
	                handleClientLogin(scanner, authService, cpf);
	                break;
	            case BOTH:
	                handleDualRoleLogin(scanner, authService, cpf);
	                break;
	        }
	    }
	}

	/// STEP 1.1
	private static void handleManagerLogin(Scanner scanner, AuthService authService, String cpf) {
	    try (UserDAO userDAO = new UserDAO()) {
	        System.out.print(">> Enter manager password: ");
	        String password = scanner.nextLine();

	        User user = authService.login(cpf, password, AuthService.LoginType.MANAGER_ONLY);

	        if (user != null) {
	            System.out.println("\nSuccessfully logged in as Manager.");
	            User managedUser = userDAO.findById(user.getId());
	            AuditService.logAction("LOGIN_SUCCESS", "Manager login successful", 
	                LocalDateTime.now(), managedUser, null);
	            managerMenu(scanner, (Manager) user);
	        } else {
	            System.out.println("Incorrect password for manager.");
	            AuditService.logAction("LOGIN_FAILURE", "Manager login failed - wrong password", 
	                LocalDateTime.now(), null, null);
	        }
	    } catch (Exception e) {
	        System.err.println("Login error: " + e.getMessage());
	        AuditService.logAction("LOGIN_ERROR", "Manager login error: " + e.getMessage(), 
	            LocalDateTime.now(), null, null);
	    }
	}

	// STEP 1.2
	private static void handleClientLogin(Scanner scanner, AuthService authService, String cpf) {
	    try (UserDAO userDAO = new UserDAO()) {
	        Client client = (Client) userDAO.findClientByCpf(cpf);

	        if (client.getBlocked()) {
	            System.out.println("\nACCOUNT BLOCKED: Too many failed attempts");
	            System.out.println("Please contact a manager to unlock your account.");
	            AuditService.logAction("LOGIN_ATTEMPT", "Blocked client login attempt", 
	                LocalDateTime.now(), client, null);
	            return;
	        }

	        System.out.print(">> Enter client password: ");
	        String password = scanner.nextLine();

	        User user = authService.login(cpf, password, AuthService.LoginType.CLIENT_ONLY);

	        if (user != null) {
	            client.resetLoginAttempts();
	            userDAO.update(client);
	            System.out.println("\nSuccessfully logged in as Client.");
	            AuditService.logAction("LOGIN_SUCCESS", "Client login successful", 
	                LocalDateTime.now(), user, null);
	            clientMenu(scanner, (Client) user);
	        } else {
	            client.incrementLoginAttempts();
	            userDAO.update(client);

	            if (client.getLoginAttempts() >= 3) {
	                System.out.println("\nACCOUNT BLOCKED: 3 failed attempts");
	                System.out.println("Contact a manager to unlock your account.");
	                AuditService.logAction("ACCOUNT_BLOCKED", 
	                    "Client account blocked after 3 failed attempts",
	                    LocalDateTime.now(), client, null);
	            } else {
	                System.out.println("\nIncorrect password. Attempts: " + client.getLoginAttempts() + "/3");
	                AuditService.logAction("LOGIN_FAILURE", "Client login failed - wrong password", 
	                    LocalDateTime.now(), client, null);
	            }
	        }
	    } catch (Exception e) {
	        System.err.println("Login error: " + e.getMessage());
	        AuditService.logAction("LOGIN_ERROR", "Client login error: " + e.getMessage(), 
	            LocalDateTime.now(), null, null);
	    }
	}

	/// STEP 1.3
	private static void handleDualRoleLogin(Scanner scanner, AuthService authService, String cpf) {
	    try {
	        System.out.println("\nThis CPF is registered as both Manager and Client.");
	        System.out.println("1. Login as Manager");
	        System.out.println("2. Login as Client");
	        System.out.println("0. Cancel");
	        System.out.print("Select login type: ");

	        int choice;
	        try {
	            choice = scanner.nextInt();
	        } catch (Exception e) {
	            System.out.println("Invalid input.");
	            scanner.nextLine();
	            AuditService.logAction("LOGIN_ATTEMPT", "Invalid input in dual role selection", 
	                LocalDateTime.now(), null, null);
	            return;
	        }
	        scanner.nextLine();

	        switch (choice) {
	            case 1:
	                handleManagerLogin(scanner, authService, cpf);
	                break;
	            case 2:
	                handleClientLogin(scanner, authService, cpf);
	                break;
	            case 0:
	                System.out.println("Login cancelled.");
	                AuditService.logAction("LOGIN_CANCELLED", "User cancelled dual role login", 
	                    LocalDateTime.now(), null, null);
	                break;
	            default:
	                System.out.println("Invalid option.");
	                AuditService.logAction("LOGIN_ATTEMPT", "Invalid option in dual role selection", 
	                    LocalDateTime.now(), null, null);
	        }
	    } catch (Exception e) {
	        System.err.println("Dual role login error: " + e.getMessage());
	        AuditService.logAction("LOGIN_ERROR", "Dual role login error: " + e.getMessage(), 
	            LocalDateTime.now(), null, null);
	    }
	}

	public static void managerMenu(Scanner scanner, Manager manager) {
		boolean running = true;
		while (running) {
			System.out.println("\n======= MANAGER MENU =======");
			System.out.println("1. Register New Manager");
			System.out.println("2. Unlock Client");
			System.out.println("3. Review Reversal Requests");
			System.out.println("4. Account Inactivation Requests");
			System.out.println("5. Account Reactivation"); // implement
			System.out.println("0. Logout");
			System.out.println("============================");
			System.out.print("Select an option: ");

			int option;
			try {
				option = scanner.nextInt();
			} catch (Exception e) {
				System.out.println("Invalid input. Please enter a number.");
				scanner.nextLine();
				continue;
			}
			scanner.nextLine();

			switch (option) {
			case 1:
				System.out.println("\n>> Manager Registration");
				registerManager(scanner, manager);
				break;
			case 2:
				System.out.println("\n>> Locked Clients: ");
				unlockClient(scanner, manager);
				break;
			case 3:
				System.out.println("\n>> Reversal Requests");
				reviewReversalRequests(scanner, manager, reversalService);
				break;
			case 4:
				System.out.println("\n>> Account Closure Requests");
				handleInactivationRequests(scanner, manager);
				break;
			case 5:
				System.out.println("\n>> View Closed Accounts");
				openClosedAccount(scanner, manager);
			case 0:
				running = false;
				System.out.println("Logging out...");
				break;
			default:
				System.out.println("Invalid option.");
			}
		}
	}

	private static void openClosedAccount(Scanner scanner, Manager manager) {
	    try (AccountDAO accountDAO = new AccountDAO();
	         ClientDAO clientDAO = new ClientDAO()) {
	        
	        List<Account> closedAccounts = accountDAO.findAllInactiveAccounts();
	        
	        if (closedAccounts.isEmpty()) {
	            System.out.println("No closed accounts found.");
	            return;
	        }

	        System.out.println("\nClosed Accounts:");
	        for (int i = 0; i < closedAccounts.size(); i++) {
	            Client c = clientDAO.findById(closedAccounts.get(i).getOwner().getId());
	            System.out.printf("%d. CPF: %s | Name: %s | Account: %s | Attempts: %d\n", 
	                (i + 1), c.getCpf(), c.getName(), 
	                closedAccounts.get(i).getAccountNumber(), 
	                c.getLoginAttempts());
	        }

	        System.out.print("Enter the account number to reactivate (or 'cancel' to return): ");
	        String input = scanner.nextLine().trim();

	        if (input.equalsIgnoreCase("cancel")) {
	            System.out.println("Operation cancelled.");
	            return;
	        }

	        Optional<Account> selectedAccountOpt = closedAccounts.stream()
	            .filter(acc -> acc.getAccountNumber().equals(input))
	            .findFirst();

	        if (selectedAccountOpt.isEmpty()) {
	            System.out.println("Account number not found among closed accounts.");
	            return;
	        }

	        Account selectedAccount = selectedAccountOpt.get();
	        selectedAccount.setActive(true);
	        selectedAccount.setClosureRequested(false);
	        accountDAO.update(selectedAccount);

	        String details = String.format("Reactivated account %s for client %s (CPF: %s)", 
	            selectedAccount.getAccountNumber(),
	            selectedAccount.getOwner().getName(),
	            selectedAccount.getOwner().getCpf());

	        AuditService.logAction("ACCOUNT_REACTIVATED", details, LocalDateTime.now(),manager, selectedAccount);

	        System.out.println("Account " + selectedAccount.getAccountNumber() + " successfully reactivated.");

	    } catch (Exception e) {
	        System.err.println("Error reactivating account: " + e.getMessage());
	        AuditService.logAction("ACCOUNT_REACTIVATION_FAILED","Failed to reactivate account: " + e.getMessage(),
	            LocalDateTime.now(),manager,null);
	    }
	}

	public static void unlockClient(Scanner scanner, Manager currentManager) {
	    try (ClientDAO clientDAO = new ClientDAO()) {
	        List<Client> blockedClients = clientDAO.findBlockedClients();

	        if (blockedClients.isEmpty()) {
	            System.out.println("No blocked clients found.");
	            return;
	        }

	        System.out.println("\nBlocked Clients:");
	        for (int i = 0; i < blockedClients.size(); i++) {
	            Client c = blockedClients.get(i);
	            System.out.printf("%d. CPF: %s | Name: %s | ID: %d | Attempts: %d\n", 
	                (i + 1), c.getCpf(), c.getName(), 
	                c.getId(), c.getLoginAttempts());
	        }

	        System.out.print("Select a client to unlock (1-" + blockedClients.size() + " or 0 to cancel): ");
	        
	        int accountChoice;
	        try {
	            accountChoice = Integer.parseInt(scanner.nextLine());
	        } catch (NumberFormatException e) {
	            System.out.println("Invalid input. Operation cancelled.");
	            AuditService.logAction("CLIENT_UNLOCK_ATTEMPT","Invalid input format for client selection",
	                LocalDateTime.now(),currentManager,null);
	            return;
	        }

	        if (accountChoice == 0) {
	            System.out.println("Operation cancelled.");
	            return;
	        }

	        if (accountChoice < 1 || accountChoice > blockedClients.size()) {
	            System.out.println("Invalid selection. Operation cancelled.");
	            return;
	        }

	        Client selectedClient = blockedClients.get(accountChoice - 1);
	        selectedClient.resetLoginAttempts();
	        selectedClient.setBlocked(false);
	        clientDAO.update(selectedClient);

	        String details = String.format("Unlocked client %s (CPF: %s)", 
	            selectedClient.getName(), selectedClient.getCpf());
	        
	        AuditService.logAction("CLIENT_UNBLOCKED",details,LocalDateTime.now(),currentManager,null);

	        System.out.printf("\nClient %s (CPF: %s) has been successfully unlocked.\n", 
	            selectedClient.getName(), selectedClient.getCpf());

	    } catch (Exception e) {
	        System.err.println("Error unlocking client: " + e.getMessage());
	        AuditService.logAction("CLIENT_UNLOCK_FAILED","Failed to unlock client: " + e.getMessage(),LocalDateTime.now(),
	            currentManager,null);
	    }
	}
	public static void reviewReversalRequests(Scanner scanner, Manager manager, ReversalService reversalService) {
	    try {
	        List<ReversalRequest> pendingRequests = reversalService.findPendingRequests();

	        if (pendingRequests.isEmpty()) {
	            System.out.println("No pending reversal requests.");
	            return;
	        }

	        System.out.println("\nPending Reversal Requests:");
	        for (int i = 0; i < pendingRequests.size(); i++) {
	            ReversalRequest r = pendingRequests.get(i);
	            Transaction t = r.getTransaction();
	            System.out.printf("%d. TXN#%d | $%.2f | From: %s | To: %s | Reason: %s%n", 
	                i + 1, 
	                t.getId(),
	                t.getAmount(),
	                t.getSourceAccount().getAccountNumber(),
	                t.getTargetAccount().getAccountNumber(),
	                r.getReason());
	        }

	        System.out.print("\nSelect a request to review (0 to cancel): ");
	        int choice;
	        try {
	            choice = Integer.parseInt(scanner.nextLine());
	            if (choice == 0) {
	                System.out.println("Operation cancelled.");
	                return;
	            }
	            if (choice < 1 || choice > pendingRequests.size()) {
	                System.out.println("Invalid selection.");
	                return;
	            }
	        } catch (NumberFormatException e) {
	            System.out.println("Invalid input. Please enter a number.");
	            return;
	        }

	        ReversalRequest selected = pendingRequests.get(choice - 1);
	        Transaction transaction = selected.getTransaction();

	        System.out.println("\n1. Approve");
	        System.out.println("2. Reject");
	        System.out.print("Choose an option: ");
	        int decision;
	        try {
	            decision = Integer.parseInt(scanner.nextLine());
	            if (decision != 1 && decision != 2) {
	                System.out.println("Invalid option selected.");
	                return;
	            }
	        } catch (NumberFormatException e) {
	            System.out.println("Invalid input. Please enter 1 or 2.");
	            return;
	        }

	        System.out.print("Resolution notes: ");
	        String notes = scanner.nextLine().trim();

	        if (decision == 1) {
	            
	            EntityTransaction tx = null;
	            try {
	                tx = reversalService.getEntityManager().getTransaction();
	                tx.begin();

	                Account sender = reversalService.getEntityManager().find(
	                    Account.class, transaction.getSourceAccount().getId());
	                Account receiver = reversalService.getEntityManager().find(
	                    Account.class, transaction.getTargetAccount().getId());

	                if (receiver.getBalance() < transaction.getAmount()) {
	                    System.out.println("Reversal failed: insufficient funds in target account.");
	                    tx.rollback();
	                    return;
	                }

	                receiver.setBalance(receiver.getBalance() - transaction.getAmount());
	                sender.setBalance(sender.getBalance() + transaction.getAmount());
	                transaction.setIsReversible(false);

	                reversalService.getEntityManager().merge(receiver);
	                reversalService.getEntityManager().merge(sender);
	                reversalService.getEntityManager().merge(transaction);

	                reversalService.approveRequest(selected.getId(), manager, notes);

	                String auditMessage = String.format(
	                    "Approved reversal of $%.2f from %s to %s. Notes: %s",
	                    transaction.getAmount(),
	                    receiver.getAccountNumber(),
	                    sender.getAccountNumber(),
	                    notes
	                );
	                AuditService.logAction("REVERSAL_APPROVED",auditMessage,LocalDateTime.now(),manager,sender);
	                
	                tx.commit();
	                System.out.println("Reversal approved and processed successfully!");
	            } catch (Exception e) {
	                if (tx != null && tx.isActive()) {
	                    tx.rollback();
	                }
	                System.err.println("Error processing reversal: " + e.getMessage());
	                AuditService.logAction("REVERSAL_PROCESSING_ERROR","Failed to process reversal: " + e.getMessage(),
	                    LocalDateTime.now(),manager,null);
	            }
	        } else {
	           
	            try {
	                EntityTransaction tx = reversalService.getEntityManager().getTransaction();
	                try {
	                    tx.begin();
	                    reversalService.rejectRequest(selected.getId(), manager, notes);
	                    tx.commit();
	                    
	                    System.out.println("Reversal request rejected.");
	                    
	                   
	                    AuditService.logAction("REVERSAL_REJECTED",String.format("Rejected reversal of TXN#%d ($%.2f). Reason: %s",
	                            transaction.getId(),transaction.getAmount(),notes),LocalDateTime.now(),manager,transaction.getSourceAccount());
	                } catch (Exception e) {
	                    if (tx.isActive()) {
	                        tx.rollback();
	                    }
	                    System.err.println("Error rejecting reversal: " + e.getMessage());
	                    AuditService.logAction("REVERSAL_REJECTION_ERROR","Failed to reject reversal: " + e.getMessage(),
	                        LocalDateTime.now(),manager,null
	                    );
	                }
	            } catch (Exception e) {
	                System.err.println("System error: " + e.getMessage());
	                AuditService.logAction("REVERSAL_REJECTION_ERROR","System error during rejection: " + e.getMessage(),
	                    LocalDateTime.now(),manager,null);
	            }
	        }
	    } catch (Exception e) {
	        System.err.println("System error: " + e.getMessage());
	        AuditService.logAction("REVERSAL_REVIEW_ERROR","Error reviewing reversals: " + e.getMessage(),LocalDateTime.now(),
	            manager,null);
	    }
	}
	
	public static void handleInactivationRequests(Scanner scanner, User manager) {
	    try (InactivationRequestService requestService = new InactivationRequestService()) {
	        // Get pending requests
	        List<AccountInactivationRequest> pendingRequests = requestService.findPendingRequests();

	        if (pendingRequests.isEmpty()) {
	            System.out.println("\nThere are no pending inactivation requests.");
	            return;
	        }

	        System.out.println("\nPending Inactivation Requests:");
	        for (int i = 0; i < pendingRequests.size(); i++) {
	            AccountInactivationRequest req = pendingRequests.get(i);
	            System.out.printf("%d. Account: %s | Client: %s (CPF: %s)%n", 
	                i + 1,
	                req.getAccount().getAccountNumber(),
	                req.getAccount().getOwner().getName(),
	                req.getAccount().getOwner().getCpf());
	        }

	        System.out.print("\nSelect a request to handle (1-" + pendingRequests.size() + " or 0 to cancel): ");
	        int choice;
	        try {
	            choice = scanner.nextInt();
	            scanner.nextLine();
	            
	            if (choice == 0) {
	                System.out.println("Operation cancelled.");
	                return;
	            }
	            
	            if (choice < 1 || choice > pendingRequests.size()) {
	                System.out.println("Invalid selection.");
	                return;
	            }
	        } catch (InputMismatchException e) {
	            System.out.println("Invalid input. Please enter a number.");
	            scanner.nextLine();
	            return;
	        }
	        AccountInactivationRequest selectedRequest = pendingRequests.get(choice - 1);

	        System.out.println("\n1. Approve");
	        System.out.println("2. Reject");
	        System.out.print("Choose an action: ");
	        
	        int action;
	        try {
	            action = scanner.nextInt();
	            scanner.nextLine();
	            
	            if (action != 1 && action != 2) {
	                System.out.println("Invalid option.");
	                return;
	            }
	        } catch (InputMismatchException e) {
	            System.out.println("Invalid input. Please enter 1 or 2.");
	            scanner.nextLine();
	            return;
	        }

	        try {
	        	if (action == 1) {
	        	    System.out.print("Enter approval notes: ");
	        	    String notes = scanner.nextLine();
	        	    requestService.approveRequest(selectedRequest, manager, notes);
	        	    System.out.println("\nRequest approved and account deactivated.");
	        	    // Here, AuditService is on Service
	        	} else {
	        	    System.out.print("Enter rejection reason: ");
	        	    String reason = scanner.nextLine();
	        	    requestService.rejectRequest(selectedRequest, manager, reason);
	        	    System.out.println("\nRequest rejected.");
	        	 // Here, AuditService is on Service
	        	}
	        } catch (Exception e) {
	            System.err.println("\nError processing request: " + e.getMessage());
	            AuditService.logAction("INACTIVATION_REQUEST_ERROR", "Failed to process inactivation request: " + e.getMessage(),
	                LocalDateTime.now(),manager,selectedRequest.getAccount()
	            );
	        }
	    } catch (Exception e) {
	        System.err.println("\nSystem error: " + e.getMessage());
	        AuditService.logAction("INACTIVATION_REVIEW_ERROR","Error reviewing inactivation requests: " + e.getMessage(),
	            LocalDateTime.now(),manager,null
	        );
	    }
	}
	/// AUDIT LOGS HERE

	/// 111
	public static void clientMenu(Scanner scanner, Client client) {
		try (AccountDAO accountDAO = new AccountDAO()) {
			boolean choosing = true;

			while (choosing) {
				accountDAO.clearCache();
				List<Account> accounts = accountDAO.findActiveAccountsByClient(client);
				System.out.println("\n>> Your Active Accounts:");
				if (accounts.isEmpty()) {
					System.out.println("You don't have any active accounts.");
					return;
				}

				for (int i = 0; i < accounts.size(); i++) {
				    Account acc = accounts.get(i);
				    if (acc.getActive()) {
				        System.out.printf("%d. Account #%s | Balance: $%.2f", (i + 1), acc.getAccountNumber(), acc.getBalance());
				        if (acc.getClosureRequested()) {
				            System.out.print(" | CLOSURE REQUESTED");
				        }
				        System.out.println(); // Finaliza a linha
				    }
				}
				System.out.println("\n" + (accounts.size() + 1) + ". Create new account");
				System.out.println("0. Back");
				System.out.print("Select active or closure requested account: ");

				int accountChoice;
				try {
					accountChoice = scanner.nextInt();
				} catch (Exception e) {
					System.out.println("Invalid input.");
					scanner.nextLine();
					continue;
				}
				scanner.nextLine();

				if (accountChoice == 0) {
					choosing = false;
				} else if (accountChoice > 0 && accountChoice <= accounts.size()) {
					Account selectedAccount = accounts.get(accountChoice - 1);
					clientAccountMenu(scanner, client, selectedAccount, as);
					AuditService.logAction("ACCOUNT_ACCESS", "Logged in on account" + selectedAccount.getAccountNumber(),
							LocalDateTime.now(), client, selectedAccount);
				} else if (accountChoice == accounts.size() + 1) {
					registerAccount(scanner, client);
				} else {
					System.out.println("Invalid selection. Please try again.");
				}
			}
		}
	}

	/// 666
	public static void clientAccountMenu(Scanner scanner, Client client, Account account,
	        AccountService accountService) {
	    boolean running = true;

	    while (running) {
	        System.out.println("\n======= ACCOUNT MENU =======");
	        System.out.println("1. Check Balance");
	        System.out.println("2. Make Deposit");
	        System.out.println("3. Make Withdrawal");
	        System.out.println("4. Transfer Funds");
	        System.out.println("5. Request Transaction Reversal");
	        System.out.println("6. Request Account Closure");
	        System.out.println("7. View Extract of Account");
	        System.out.println("0. Back to Accounts");
	        System.out.println("============================");
	        System.out.print("Select an option: ");

	        int option;
	        try {
	            option = scanner.nextInt();
	        } catch (Exception e) {
	            System.out.println("Invalid input.");
	            scanner.nextLine();
	            continue;
	        }
	        scanner.nextLine();

	        try {
	            switch (option) {
	                case 1:
	                    System.out.printf("\nCurrent balance: $%.2f%n", account.getBalance());
	                    break;
	                case 2:
	                    if (!account.getClosureRequested()) {
	                        System.out.print("Enter deposit amount: $");
	                        double depositAmount = scanner.nextDouble();
	                        scanner.nextLine();
	                        try (AccountDAO accountDAO = new AccountDAO()) {
	                            account.setBalance(account.getBalance() + depositAmount);
	                            accountDAO.update(account);
	                            AuditService.logAction("DEPOSIT", "Deposited $" + depositAmount, LocalDateTime.now(), client,account);
	                            System.out.printf("$%.2f deposited successfully.%n", depositAmount);
	                        }
	                    } else {
	                        System.out.println("Account has pending closure request\n");
	                    }
	                    break;
	                case 3:
	                    if (!account.getClosureRequested()) {
	                        System.out.print("Enter withdrawal amount: $");
	                        double withdrawAmount = scanner.nextDouble();
	                        scanner.nextLine();
	                        if (account.getBalance() >= withdrawAmount) {
	                            try (AccountDAO accountDAO = new AccountDAO()) {
	                                account.setBalance(account.getBalance() - withdrawAmount);
	                                accountDAO.update(account);
	                                AuditService.logAction("WITHDRAWAL", "Withdrew $" + withdrawAmount, LocalDateTime.now(), client,
	                                        account);
	                                System.out.printf("$%.2f withdrawn successfully.%n", withdrawAmount);
	                            }
	                        } else {
	                            System.out.println("Insufficient funds.");
	                        }
	                    } else {
	                        System.out.println("Account has pending closure request\n");
	                    }
	                    break;
	                case 4:
	                    if (!account.getClosureRequested()) {
	                        System.out.print("Recipient account number: ");
	                        String destAccountNumber = scanner.nextLine();

	                        System.out.print("Transfer amount: $");
	                        double transferAmount = scanner.nextDouble();
	                        scanner.nextLine();

	                        if (transferAmount <= 0) {
	                            System.out.println("Transfer amount must be greater than zero.");
	                            break;
	                        }

	                        try (AccountDAO accountDAO = new AccountDAO();
	                             TransactionDAO transactionDAO = new TransactionDAO()) {
	                        	EntityTransaction tx = accountDAO.beginTransaction();
	                        	if (tx == null) {
	                                System.err.println("Failed to start database transaction");
	                                return;
	                            }
	                            
	                            Account destination = accountDAO.findByAccountNumber(destAccountNumber);

	                            if (destination == null || !destination.getActive()) {
	                                System.out.println("No account found with number: " + destAccountNumber);
	                                break;
	                            }
	                            
	                            if(destination.getClosureRequested()) {
	                            	System.out.println("Destination account has pending closure request.");
	                            	break;
	                            }
	                            
	                            if(destination.getOwner().getBlocked()) {
	                            	System.out.println("Client with the account destination is blocked");
	                            	break;
	                            }

	                            if (account.getBalance() >= transferAmount) {
	                                account.setBalance(account.getBalance() - transferAmount);
	                                destination.setBalance(destination.getBalance() + transferAmount);

	                                accountDAO.update(account);
	                                accountDAO.update(destination);

	                                LocalDateTime now = LocalDateTime.now();

	                                Transaction transferOut = new Transaction();
	                                transferOut.setSourceAccount(account);
	                                transferOut.setTargetAccount(destination);
	                                transferOut.setAmount(transferAmount);
	                                transferOut.setTimestamp(now);
	                                transferOut.setType(TransactionType.TRANSFER_OUT);
	                                transactionDAO.save(transferOut);

	                                Transaction transferIn = new Transaction();
	                                transferIn.setSourceAccount(account);
	                                transferIn.setTargetAccount(destination);
	                                transferIn.setAmount(transferAmount);
	                                transferIn.setTimestamp(now);
	                                transferIn.setType(TransactionType.TRANSFER_IN);
	                                transactionDAO.save(transferIn);

	                                AuditService.logAction("TRANSFER_OUT",
	                                        "Transferred $" + transferAmount + " to " + destAccountNumber, now, client, account);
	                                AuditService.logAction("TRANSFER_IN",
	                                        "Received $" + transferAmount + " from " + account.getAccountNumber(), now,
	                                        destination.getOwner(), destination);
	                                System.out.println("Transfer performed from account " + account.getAccountNumber()
	                                        + " to account " + destination.getAccountNumber());
	                            } else {
	                                System.out.println("Invalid transfer: insufficient funds.");
	                            }
	                        }
	                    } else {
	                        System.out.println("Account has pending closure request\n");
	                    }
	                    break;
	                case 5:
	                    reversalRequestMenu(scanner, client, account);
	                    break;
	                case 6:
	                    System.out.print("\nAre you sure you want to close this account? (Y/N): ");
	                    String confirm = scanner.nextLine().trim();
	                    
	                    if (confirm.equalsIgnoreCase("Y")) {
	                        System.out.print("Enter the reason for closing the account: ");
	                        String reason = scanner.nextLine().trim();
	                        
	                        try {
	                            
	                            AccountInactivationRequest request = new AccountInactivationRequest();
	                            request.setAccount(account);
	                            request.setRequester(client);
	                            request.setReason(reason);
	                            request.setStatus(RequestStatus.PENDING);
	                            request.setRequestDate(LocalDateTime.now());
	                            
	                            try (InactivationRequestService service = new InactivationRequestService()) {
	                                service.createRequest(request);
	                                account.setClosureRequested(true);
	                                System.out.println("Account closure request submitted successfully!");
	                            }
	                            
	                           
	                            Thread.sleep(500);
	                            break; 
	                        } catch (Exception e) {
	                            System.out.println("❌ Error submitting request: " + e.getMessage());
	                      
	                            scanner.nextLine();
	                        }
	                    } else {
	                        System.out.println("Operation cancelled.");
	                    }
	                    break;
	                case 7:
	                    System.out.print("\nChoose what operation(s) to include in the Extract (0 to quit): \n");
	                    System.out.println("1 - ALL");
	                    System.out.println("2 - DEPOSITS ONLY");
	                    System.out.println("3 - WITHDRAWALS ONLY");
	                    System.out.println("4 - TRANSFERS (IN & OUT) ONLY");
	                    System.out.println("5 - Custom Filter\n");

	                    System.out.print("Choose option: ");
	                    int opt = scanner.nextInt();
	                    scanner.nextLine(); 
	                    List<AuditLog> logs = new ArrayList<>();

	                    try (AuditLogDAO auditLogDAO = new AuditLogDAO()) {
	                        switch (opt) {
	                            case 1:
	                                logs = AuditLogDAO.findAllAccountOperations(account);
	                                break;
	                            case 2:
	                                logs = AuditLogDAO.findDepositsByAccount(account);
	                                break;
	                            case 3:
	                                logs = AuditLogDAO.findWithdrawalsByAccount(account);
	                                break;
	                            case 4:
	                                logs.addAll(AuditLogDAO.findTransfersSentByAccount(account));
	                                logs.addAll(AuditLogDAO.findTransfersReceivedByAccount(account));
	                                logs.sort(Comparator.comparing(AuditLog::getTimestamp));
	                                break;
	                            case 5:
	                                Set<String> actionTypes = new HashSet<>();
	                                System.out.println("Select action types (comma separated numbers, 0 to finish):");
	                                System.out.println("1. Deposit");
	                                System.out.println("2. Withdrawal");
	                                System.out.println("3. Transfer-in");
	                                System.out.println("4. Transfer-out");
	                                System.out.println("0. Finish Selection");

	                                while (true) {
	                                    System.out.print("Enter choices (e.g., 1,2,3): ");
	                                    String input = scanner.nextLine().trim();

	                                    if (input.equals("0"))
	                                        break;

	                                    try {
	                                        String[] choices = input.split(",");
	                                        for (String choice : choices) {
	                                            int num = Integer.parseInt(choice.trim());
	                                            switch (num) {
	                                                case 1:
	                                                    actionTypes.add(AuditLogDAO.DEPOSIT);
	                                                    break;
	                                                case 2:
	                                                    actionTypes.add(AuditLogDAO.WITHDRAWAL);
	                                                    break;
	                                                case 3:
	                                                    actionTypes.add(AuditLogDAO.TRANSFER_IN);
	                                                    break;
	                                                case 4:
	                                                    actionTypes.add(AuditLogDAO.TRANSFER_OUT);
	                                                    break;
	                                                default:
	                                                    System.out.println("Invalid choice: " + num);
	                                            }
	                                        }

	                                        logs = AuditLogDAO.findByAccountAndActions(account, actionTypes);
	                                        break;
	                                    } catch (NumberFormatException e) {
	                                        System.out.println("Invalid input. Please enter numbers separated by commas.");
	                                    }
	                                }
	                                break;
	                            
	                            case 0:
	                                System.out.println("Leaving extract visualization option...\n");
	                                break;
	                            default:
	                                System.out.println("Invalid option.");
	                                break;
	                        }

	                        if (!logs.isEmpty()) {
	                            System.out.println("\n--- Extract for Account: " + account.getAccountNumber() + " ---");
	                            logs.sort(Comparator.comparing(AuditLog::getTimestamp));
	                            for (AuditLog log : logs) {
	                                System.out.println(log.getTimestamp() + " | " + log.getActionType() + " | " + log.getDetails());
	                            }
	                            System.out.println("\nWould you like to export this extract as CSV? (y/n)");
	                            String csvOption = scanner.nextLine().trim().toLowerCase();

	                            if (csvOption.equals("y")) {
	                                String userHome = System.getProperty("user.home");
	                                String downloadsPath = userHome + "\\Downloads\\";
	                                accountService.exportAccountStatementToCSV(account, logs, downloadsPath);
	                            }
	                        }
	                    }
	                    break;
	                case 0:
	                    running = false;
	                    break;
	                default:
	                    System.out.println("Invalid option.");
	            }
	        } catch (Exception e) {
	            System.out.println("An error occurred: " + e.getMessage());
	            e.printStackTrace();
	        }
	    }
	} 

	public static void reversalRequestMenu(Scanner scanner, Client client, Account account) {
	    try {
	        if (account.getClosureRequested()) {
	            System.out.println("\nAccount has pending closure request");
	            return;
	        }

	        List<Transaction> reversibleTransactions;
	        try (TransactionDAO transactionDAO = new TransactionDAO();
	        	     ReversalRequestDAO reversalRequestDAO = new ReversalRequestDAO()) {

	        	    reversibleTransactions = transactionDAO.findReversibleTransactions(account)
	        	        .stream()
	        	        .filter(txn ->
	        	            !reversalRequestDAO.hasPendingReversalForTransaction(txn) &&
	        	            !reversalRequestDAO.hasApprovedReversalForTransaction(txn)
	        	        )
	        	        .toList();
	        	}


	        if (reversibleTransactions.isEmpty()) {
	            System.out.println("\nNo reversible transactions available");
	            return;
	        }

	        System.out.println("\nSelect a transaction to request reversal:");
	        for (int i = 0; i < reversibleTransactions.size(); i++) {
	            Transaction txn = reversibleTransactions.get(i);
	            String description = switch(txn.getType()) {
	                case TRANSFER_OUT -> String.format("Transfer to account %s", 
	                                     txn.getTargetAccount().getAccountNumber());
	                case TRANSFER_IN -> String.format("Received from account %s", 
	                                    txn.getSourceAccount().getAccountNumber());
	                default -> txn.getType().toString();
	            };
	            
	            System.out.printf("%d. TXN#%d - $%.2f - %s (%s)%n",
	                i + 1, txn.getId(), txn.getAmount(),
	                description,
	                txn.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
	        }
	        
	        System.out.println("0. Cancel");
	        System.out.print("Your selection: ");

	        try {
	            int txChoice = scanner.nextInt();
	            scanner.nextLine(); // Limpa buffer
	            
	            if (txChoice == 0 || txChoice > reversibleTransactions.size()) {
	                return;
	            }

	            Transaction selectedTransaction = reversibleTransactions.get(txChoice - 1);

	            System.out.print("Reason for reversal: ");
	            String reason = scanner.nextLine().trim();

	            if (reason.isEmpty()) {
	                System.out.println("Reason cannot be empty");
	                return;
	            }

	          
	            try (ReversalService reversalService = new ReversalService()) {
	                reversalService.requestReversal(client, selectedTransaction, reason);
	                
	                AuditService.logAction("REVERSAL_REQUESTED",
	                    String.format("Requested reversal for TXN#%d (Amount: $%.2f). Reason: %s", 
	                        selectedTransaction.getId(),
	                        selectedTransaction.getAmount(),
	                        reason),
	                    LocalDateTime.now(), 
	                    client, 
	                    account);
	                    
	                System.out.println("\nReversal request submitted successfully!");
	            }
	            
	        } catch (InputMismatchException e) {
	            System.out.println("Invalid input. Please enter a number.");
	            scanner.nextLine();
	        } catch (BusinessException e) {
	            System.out.println("Error: " + e.getMessage());
	        }
	    } catch (Exception e) {
	        System.err.println("System error: " + e.getMessage());
	        e.printStackTrace();
	    }
	}

	private static void registerAccount(Scanner scanner, Client client) {
	    System.out.println("\n=== NEW ACCOUNT REGISTRATION === (Enter '0' at any time to cancel)");

	    AccountType accountType = null;
	    boolean typeValid = false;
	    do {
	        System.out.println("\nSelect Account Type:");
	        System.out.println("1. Checking Account");
	        System.out.println("2. Salary Account");
	        System.out.println("3. Savings Account");
	        System.out.println("4. Investments Account");
	        System.out.println("0. Cancel Registration");
	        System.out.print("Choose [1-4] or 0 to cancel: ");

	        String typeInput = scanner.nextLine().trim();

	        if (typeInput.equals("0")) {
	            System.out.println("Registration cancelled.");
	            return;
	        }

	        try {
	            int choice = Integer.parseInt(typeInput);
	            switch (choice) {
	                case 1:
	                    accountType = AccountType.CHECKING;
	                    typeValid = true;
	                    break;
	                case 2:
	                    accountType = AccountType.SALARY;
	                    typeValid = true;
	                    break;
	                case 3:
	                    accountType = AccountType.SAVINGS;
	                    typeValid = true;
	                    break;
	                case 4:
	                    accountType = AccountType.INVESTMENT;
	                    typeValid = true;
	                    break;
	                default:
	                    System.out.println("Error: Please select a valid option (1-4)!");
	            }
	        } catch (NumberFormatException e) {
	            System.out.println("Error: Please enter a number!");
	        }
	    } while (!typeValid);

	    System.out.println("\n=== REGISTRATION SUMMARY ===");
	    System.out.println("Account Type: " + accountType);
	    System.out.print("\nConfirm registration? (Y/N): ");

	    String confirmation = scanner.nextLine().trim().toUpperCase();
	    if (!confirmation.equals("Y")) {
	        System.out.println("Registration cancelled.");
	        return;
	    }

	    Account newAccount = null;
	    try {
	        newAccount = new Account();
	        newAccount.setOwner(client);
	        newAccount.setAccountNumber(generateAccountNumber());
	        newAccount.setBalance(0.0);
	        newAccount.setActive(true);
	        newAccount.setType(accountType);

	        try (AccountDAO accountDAO = new AccountDAO()) {
	            accountDAO.save(newAccount);
	            
	            AuditService.logAction("ACCOUNT_CREATION", "New " + accountType + " account created",LocalDateTime.now(),
	                client, newAccount);

	            System.out.println("\nRegistration successful!");
	            System.out.println("Your account number: " + newAccount.getAccountNumber());
	        }
	    } catch (Exception e) {
	        String accountNumber = (newAccount != null) ? newAccount.getAccountNumber() : "N/A";
	        AuditService.logAction("ACCOUNT_CREATION_FAILED", "Failed to create " + accountType + " account. Error: " + e.getMessage(), 
	            LocalDateTime.now(),client, null);

	        System.out.println("\nRegistration failed: " + e.getMessage());
	        
	        if (newAccount != null) {
	            System.out.println("Account number " + accountNumber + " may not be available.");
	        }
	    }
	}
}