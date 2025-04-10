package br.com.compass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;

import br.com.compass.dao.AccountDAO;
import br.com.compass.dao.AuditLogDAO;
import br.com.compass.dao.ClientDAO;
import br.com.compass.dao.ManagerDAO;
//import br.com.compass.dao.ReversalRequestDAO;
import br.com.compass.dao.TransactionDAO;
import br.com.compass.dao.UserDAO;
import br.com.compass.exceptions.BusinessException;
import br.com.compass.model.Account;
import br.com.compass.model.Client;
import br.com.compass.model.Manager;
import br.com.compass.model.Transaction;
import br.com.compass.model.User;
import br.com.compass.model.enums.AccountType;
import br.com.compass.services.AuditService;
import br.com.compass.services.AuthService;
import br.com.compass.services.ReversalService;
import br.com.compass.utils.CPFValidator;
import br.com.compass.utils.PasswordHasher;

public class App {
	
	/// TO GO TO MAIN MENU TYPE 000
	/// TO GO TO CLIENT MENU TYPE 111
	/// TO GO TO MANAGER MENU TYPE 222
	/// TO GO TO LOGIN MENU TYPE 333
	/// TO GO TO MANAGER REGISTRATION MENU TYPE 444
	/// TO GO TO CLIENT REGISTRATION MENU TYPE 555
	/// TO GO TO ACCOUNT MENU TYPE 666
	/// TO GO TO TRANSFER-MENU, TYPE 777
	/// TO GO TO REVERSAL REQUEST MENU, TYPE 888
	/// TO GO TO ACCOUNT INACTIVATION MENU, TYPE 999
    private static UserDAO userDAO = new UserDAO();
    private static final AccountDAO accountDAO = new AccountDAO();
    //private static final ReversalRequestDAO reversalRequestDAO = new ReversalRequestDAO();
    private static final TransactionDAO transactionDAO = new TransactionDAO();
    public static void main(String[] args) {
        br.com.compass.config.DataSeeder.seed();
        Scanner scanner = new Scanner(System.in);
        mainMenu(scanner);
        scanner.close();
        System.out.println("Application closed");
    }

    // 000
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
    
    /// 555
    private static void registerClient(Scanner scanner) {
        System.out.println("\n=== NEW CLIENT REGISTRATION === (Enter '0' at any time to cancel)");

        // Name validation
        String name;
        do {
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
            }
        } while (name.isEmpty() || name.length() < 3);

        // CPF validation
        String cpf;
        boolean cpfValid = false;
        do {
            System.out.print("\nCPF (format: XXX.XXX.XXX-XX): ");
            cpf = scanner.nextLine().trim();
            
            if (cpf.equals("0")) {
                System.out.println("Registration cancelled.");
                return;
            }
            
            if (!CPFValidator.isValidFormat(cpf)) {
                System.out.println("Error: Invalid CPF format! Please use XXX.XXX.XXX-XX pattern.");
                continue;
            }
            
            if (!CPFValidator.isValidCPF(cpf)) {
                System.out.println("Error: Invalid CPF number! Please check the digits.");
                continue;
            }
            
            if (userDAO.findClientByCpf(cpf) != null) {
                System.out.println("Error: This CPF is already registered!");
                continue;
            }
            
            cpfValid = true;
        } while (!cpfValid);

        // Birth Date validation
        LocalDate birthDate = null;
        boolean dateValid = false;
        do {
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
                    System.out.println("Error: You must be at least 18 years old to open an account!");
                } else {
                    dateValid = true;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Error: Invalid date format! Please use DD/MM/YYYY.");
            }
        } while (!dateValid);

        // Phone validation
        String phone;
        boolean phoneValid = false;
        do {
            System.out.print("\nPhone Number (at least 8 digits): ");
            phone = scanner.nextLine().trim();
            
            if (phone.equals("0")) {
                System.out.println("Registration cancelled.");
                return;
            }
            
            String digitsOnly = phone.replaceAll("[^0-9]", "");
            if (digitsOnly.length() < 8) {
                System.out.println("Error: Phone number must contain at least 8 digits!");
            } else {
                phoneValid = true;
            }
        } while (!phoneValid);

        // Account Type selection
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
                switch(choice) {
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

        // Password validation
        String password;
        boolean passwordValid = false;
        do {
            System.out.print("\nCreate Password (cannot be empty or '0'): ");
            password = scanner.nextLine().trim();
            
            if (password.equals("0")) {
                System.out.println("Registration cancelled.");
                return;
            }
            
            if (password.isEmpty()) {
                System.out.println("Error: Password cannot be empty!");
            } else if (password.equals("0")) {
                System.out.println("Error: Password cannot be just '0'!");
            } else {
                // Confirm password
                System.out.print("Confirm Password: ");
                String confirmPassword = scanner.nextLine().trim();
                
                if (!password.equals(confirmPassword)) {
                    System.out.println("Error: Passwords don't match! Please try again.");
                } else {
                    passwordValid = true;
                }
            }
        } while (!passwordValid);

        // Final confirmation
        System.out.println("\n=== REGISTRATION SUMMARY ===");
        System.out.println("Name: " + name);
        System.out.println("CPF: " + cpf);
        System.out.println("Birth Date: " + birthDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        System.out.println("Phone: " + phone);
        System.out.println("Account Type: " + accountType);
        System.out.print("\nConfirm registration? (Y/N): ");
        
        String confirmation = scanner.nextLine().trim().toUpperCase();
        if (!confirmation.equals("Y")) {
            System.out.println("Registration cancelled.");
            return;
        }

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
            newAccount.setBalance(null);
            newAccount.setActive(true);
            newAccount.setType(accountType);
            
            ClientDAO clientDAO = new ClientDAO();
            clientDAO.createClient(newClient);
            
            AccountDAO accountDAO = new AccountDAO();
            accountDAO.save(newAccount);
            AuditService.logAction("ACCOUNT_CREATION", 
                "New " + accountType + " account created", 
                LocalDateTime.now(),
                newClient, 
                newAccount);
                
            System.out.println("\nRegistration successful!");
            System.out.println("Your account number: " + newAccount.getAccountNumber());
            
        } catch (Exception e) {
            AuditService.logAction("ACCOUNT_CREATION_FAILED", 
                "Error: " + e.getMessage(), 
                LocalDateTime.now(),
                null, 
                null);
            System.out.println("Registration failed: " + e.getMessage());
        }
    }
    
    /// 444
    private static void registerManager(Scanner scanner) {
        System.out.println("\n=== NEW MANAGER REGISTRATION === (Enter '0' at any time to cancel)");

        // Name validation
        String name;
        do {
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
            }
        } while (name.isEmpty() || name.length() < 3);

        // CPF validation
        String cpf;
        boolean cpfValid = false;
        do {
            System.out.print("\nCPF (format: XXX.XXX.XXX-XX): ");
            cpf = scanner.nextLine().trim();
            
            if (cpf.equals("0")) {
                System.out.println("Registration cancelled.");
                return;
            }
            
            if (!CPFValidator.isValidFormat(cpf)) {
                System.out.println("Error: Invalid CPF format! Please use XXX.XXX.XXX-XX pattern.");
                continue;
            }
            
            if (!CPFValidator.isValidCPF(cpf)) {
                System.out.println("Error: Invalid CPF number! Please check the digits.");
                continue;
            }
            
            if (userDAO.findManagerByCpf(cpf) != null) {
                System.out.println("Error: This CPF is already registered!");
                continue;
            }
            
            cpfValid = true;
        } while (!cpfValid);

        // Birth Date validation
        LocalDate birthDate = null;
        boolean dateValid = false;
        do {
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
                    System.out.println("Error: You must be at least 18 years old to have a manager access!");
                } else {
                    dateValid = true;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Error: Invalid date format! Please use DD/MM/YYYY.");
            }
        } while (!dateValid);

        // Phone validation
        String phone;
        boolean phoneValid = false;
        do {
            System.out.print("\nPhone Number (at least 8 digits): ");
            phone = scanner.nextLine().trim();
            
            if (phone.equals("0")) {
                System.out.println("Registration cancelled.");
                return;
            }
            
            String digitsOnly = phone.replaceAll("[^0-9]", "");
            if (digitsOnly.length() < 8) {
                System.out.println("Error: Phone number must contain at least 8 digits!");
            } else {
                phoneValid = true;
            }
        } while (!phoneValid);

        // Password validation
        String password;
        boolean passwordValid = false;
        do {
            System.out.print("\nCreate Password (cannot be empty or '0'): ");
            password = scanner.nextLine().trim();
            
            if (password.equals("0")) {
                System.out.println("Registration cancelled.");
                return;
            }
            
            if (password.isEmpty()) {
                System.out.println("Error: Password cannot be empty!");
            } else if (password.equals("0")) {
                System.out.println("Error: Password cannot be just '0'!");
            } else {
                // Confirm password
                System.out.print("Confirm Password: ");
                String confirmPassword = scanner.nextLine().trim();
                
                if (!password.equals(confirmPassword)) {
                    System.out.println("Error: Passwords don't match! Please try again.");
                } else {
                    passwordValid = true;
                }
            }
        } while (!passwordValid);

        // Final confirmation
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

        try {
            // Create client
            Manager newManager= new Manager();
            newManager.setName(name);
            newManager.setCpf(cpf);
            newManager.setBirthDate(birthDate);
            newManager.setCellphoneNumber(phone);
            
            // Secure password setup
            byte[] salt = PasswordHasher.generateSalt();
            newManager.setPasswordSalt(salt);
            newManager.setPasswordHash(PasswordHasher.hashPassword(password, salt));
            
            // Persist data
            ManagerDAO managerDAO = new ManagerDAO();
            managerDAO.createManager(newManager);
            
            // Audit log
            AuditService.logAction("ACCOUNT_CREATION", 
                "New Manager created", 
                LocalDateTime.now(),
                newManager, 
                null);
                
            System.out.println("\nRegistration successful!");
            
        } catch (Exception e) {
            AuditService.logAction("ACCOUNT_CREATION_FAILED", 
                "Error: " + e.getMessage(), 
                LocalDateTime.now(),
                null, 
                null);
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private static String generateAccountNumber() {
        return String.format("%08d", new Random().nextInt(100000000));
    }

    /// 333
   public static void loginMenu(Scanner scanner) {
        System.out.print("\n>> Enter CPF: ");
        String cpf = scanner.nextLine();

        AuthService authService = new AuthService(userDAO);
        AuthService.LoginType loginType = authService.checkLoginType(cpf);

        if (loginType == null) {
            System.out.println("CPF not registered in the system.");
            AuditService.logAction("LOGIN_ATTEMPT", "Failed login - CPF not found", 
                                LocalDateTime.now(), null, null);
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

   private static void handleManagerLogin(Scanner scanner, AuthService authService, String cpf) {
	    System.out.print(">> Enter manager password: ");
	    String password = scanner.nextLine();

	    User user = authService.login(cpf, password, AuthService.LoginType.MANAGER_ONLY);
	    
	    if (user != null) {
	        System.out.println("\nSuccessfully logged in as Manager.");
	        // Get a fresh managed instance of the user
	        User managedUser = userDAO.findById(user.getId());
	        AuditService.logAction("LOGIN_SUCCESS", "Manager login successful", 
	                            LocalDateTime.now(), managedUser, null);
	        managerMenu(scanner, (Manager) user);
	    } else {
	        System.out.println("Incorrect password for manager.");
	        AuditService.logAction("LOGIN_FAILURE", "Manager login failed - wrong password", 
	                            LocalDateTime.now(), null, null); // Now allowed to pass null
	    }
	}

    private static void handleClientLogin(Scanner scanner, AuthService authService, String cpf) {
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
                AuditService.logAction("ACCOUNT_BLOCKED", "Client account blocked after 3 failed attempts", 
                                    LocalDateTime.now(), client, null);
            } else {
                System.out.println("\nIncorrect password. Attempts: " + 
                    client.getLoginAttempts() + "/3");
                AuditService.logAction("LOGIN_FAILURE", "Client login failed - wrong password", 
                                    LocalDateTime.now(), client, null);
            }
        }
    }

    private static void handleDualRoleLogin(Scanner scanner, AuthService authService, String cpf) {
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
    }

    /// 222
    public static void managerMenu(Scanner scanner, Manager manager) {
        boolean running = true;
        while (running) {
            System.out.println("\n======= MANAGER MENU =======");
            System.out.println("1. Register New Manager");
            System.out.println("2. Unlock Client");
            System.out.println("3. Review Reversal Requests");
            System.out.println("4. Account Inactivation (Requests)");
            System.out.println("5. Account Reactivation");
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
                    registerManager(scanner);
                    break;
                case 2:
                    System.out.println("\n>> Unlocking Accounts");
                    // TODO
                    break;
                case 3:
                    System.out.println("\n>> Reversal Requests");
                    // TODO
                    break;
                case 4:
                    System.out.println("\n>> Account Closure Requests");
                    // TODO
                    break;
                case 0:
                    running = false;
                    System.out.println("Logging out...");
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    /// 111
    public static void clientMenu(Scanner scanner, Client client) {
        AccountDAO accountDAO = new AccountDAO();
        boolean choosing = true;

        while (choosing) {
            List<Account> accounts = accountDAO.findActiveAccountsByClient(client);

            System.out.println("\n>> Your Active Accounts:");
            if (accounts.isEmpty()) {
                System.out.println("You don't have any active accounts.");
                return;
            }

            for (int i = 0; i < accounts.size(); i++) {
                Account acc = accounts.get(i);
                System.out.printf("%d. Account #%s | Balance: $%.2f%n", 
                    (i + 1), acc.getAccountNumber(), acc.getBalance());
            }
            System.out.println("0. Back");
            System.out.print("Select account: ");

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
                clientAccountMenu(scanner, client, selectedAccount);
            } else {
                System.out.println("Invalid selection. Please try again.");
            }
        }
    }

    /// 666
    public static void clientAccountMenu(Scanner scanner, Client client, Account account) {
        boolean running = true;

        while (running) {
            System.out.println("\n======= ACCOUNT MENU =======");
            System.out.println("1. Check Balance");
            System.out.println("2. Make Deposit");
            System.out.println("3. Make Withdrawal");
            System.out.println("4. Transfer Funds");
            System.out.println("5. Request Transaction Reversal");
            System.out.println("6. Request Account Closure");
            System.out.println("7. Download CSV History of Account");
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

            switch (option) {
                case 1:
                    System.out.printf("\nCurrent balance: $%.2f%n", account.getBalance());
                    break;
                case 2:
                    System.out.print("Enter deposit amount: $");
                    double depositAmount = scanner.nextDouble();
                    scanner.nextLine();
                    account.setBalance(account.getBalance() + depositAmount);
                    accountDAO.update(account);
                    AuditService.logAction("DEPOSIT", "Deposited $" + depositAmount, LocalDateTime.now(), client, account);
                    System.out.printf("$%.2f deposited successfully.%n", depositAmount);
                    break;
                case 3:
                    System.out.print("Enter withdrawal amount: $");
                    double withdrawAmount = scanner.nextDouble();
                    scanner.nextLine();
                    if (account.getBalance() >= withdrawAmount) {
                        account.setBalance(account.getBalance() - withdrawAmount);
                        accountDAO.update(account);
                        AuditService.logAction("WITHDRAWAL", "Withdrew $" + withdrawAmount, LocalDateTime.now(), client, account);
                        System.out.printf("$%.2f withdrawn successfully.%n", withdrawAmount);
                    } else {
                        System.out.println("Insufficient funds.");
                    }
                    break;
                case 4:
                	System.out.print("Recipient account number: ");
                	String destAccountNumber = scanner.nextLine();

                	System.out.print("Transfer amount: $");
                	double transferAmount = scanner.nextDouble();
                	scanner.nextLine();

                	if (transferAmount <= 0) {
                	    System.out.println("Transfer amount must be greater than zero.");
                	    return;
                	}

                	Optional<Account> destination = accountDAO.findByAccountNumber(destAccountNumber);

                	if (destination.isEmpty()) {
                	    System.out.println("No account found with number: " + destAccountNumber);
                	    return;
                	}
                	else if (transferAmount <= 0) {
                	    System.out.println("Transfer amount must be greater than zero.");
                	    return;
                	}

                	else if (account.getBalance() >= transferAmount) {
                	    account.setBalance(account.getBalance() - transferAmount);
                	    destination.get().setBalance(destination.get().getBalance() + transferAmount);
                	    accountDAO.update(account);
                	    accountDAO.update(destination.get());
                	    AuditService.logAction("TRANSFER_OUT", "Transferred $" + transferAmount + " to " + destAccountNumber, LocalDateTime.now(), client, account);
                	    AuditService.logAction(
                	        "TRANSFER_IN",
                	        "Received $" + transferAmount + " from " + account.getAccountNumber(),
                	        LocalDateTime.now(),
                	        destination.get().getOwner(),
                	        destination.get()
                	    );
                	    System.out.printf("Transferred $%.2f to account %s%n", transferAmount, destAccountNumber);
                	} else {
                	    System.out.println("Invalid transfer: insufficient funds.");
                	}

                    break;
                case 5:
                    reversalRequestMenu(scanner, client, account);
                    break;
                case 6:
                    System.out.print("Are you sure you want to close this account? (Y/N): ");
                    String confirm = scanner.nextLine();
                    if (confirm.equalsIgnoreCase("Y")) {
                        account.setClosureRequested(true);
                        accountDAO.update(account);
                        AuditService.logAction("ACCOUNT_CLOSURE_REQUESTED", "Client requested account closure.", LocalDateTime.now(), client, account);
                        System.out.println("Account closure request submitted.");
                        return;
                    }
                    System.out.println("Operation cancelled.");
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }    //
    ///
    ///
    ///
    ///
    ///
    ///
    ///
    ///
    ///
    ///
    ///
    ///
    /////

    public static void reversalRequestMenu(Scanner scanner, Client client, Account account) {
    	List<Transaction> reversibleTransactions = transactionDAO.findByAccount(account).stream()
    		    .filter(Transaction::getIsReversible)
    		    .toList();

        if (reversibleTransactions.isEmpty()) {
            System.out.println("No reversible transactions found.");
            return;
        }

        System.out.println("\nSelect a transaction to request reversal:");
        for (int i = 0; i < reversibleTransactions.size(); i++) {
            Transaction txn = reversibleTransactions.get(i);
            System.out.printf("%d. TXN#%d - $%.2f - %s\n",
                i + 1, txn.getId(), txn.getAmount(), txn.getType());
        }
        System.out.println("0. Cancel");
        System.out.print("Your selection: ");

        int txChoice;
        try {
            txChoice = scanner.nextInt();
        } catch (Exception e) {
            System.out.println("Invalid input.");
            scanner.nextLine();
            return;
        }
        scanner.nextLine();

        if (txChoice == 0 || txChoice > reversibleTransactions.size()) return;

        Transaction selectedTransaction = reversibleTransactions.get(txChoice - 1);

        System.out.print("Reason for reversal: ");
        String reason = scanner.nextLine();

        ReversalService reversalService = new ReversalService();
        try {
            reversalService.requestReversal(client, selectedTransaction, reason);
            AuditService.logAction("REVERSAL_REQUESTED", 
                "Requested reversal for TXN#" + selectedTransaction.getId() + ": " + reason,
                LocalDateTime.now(), client, account);

            System.out.println("Reversal request submitted.");
        } catch (BusinessException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}