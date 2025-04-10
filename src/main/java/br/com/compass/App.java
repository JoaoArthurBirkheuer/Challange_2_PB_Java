package br.com.compass;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import br.com.compass.dao.AccountDAO;
import br.com.compass.dao.ClientDAO;
import br.com.compass.dao.UserDAO;
import br.com.compass.model.Account;
import br.com.compass.model.Client;
import br.com.compass.model.Manager;
import br.com.compass.model.enums.AccountType;
import br.com.compass.services.AuditService;
import br.com.compass.utils.CPFValidator;
import br.com.compass.utils.PasswordHasher;

public class App {
    private static UserDAO userDAO = new UserDAO();

    public static void main(String[] args) {
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
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option! Please try again.");
            }
        }
    }
    
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
            // Create client
            Client newClient = new Client();
            newClient.setName(name);
            newClient.setCpf(cpf);
            newClient.setBirthDate(birthDate);
            newClient.setCellphoneNumber(phone);
            
            // Secure password setup
            byte[] salt = PasswordHasher.generateSalt();
            newClient.setPasswordSalt(salt);
            newClient.setPasswordHash(PasswordHasher.hashPassword(password, salt));
            
            // Create account
            Account newAccount = new Account();
            newAccount.setOwner(newClient);
            newAccount.setAccountNumber(generateAccountNumber());
            newAccount.setBalance(BigDecimal.ZERO);
            newAccount.setActive(true);
            newAccount.setType(accountType);
            
            // Persist data
            ClientDAO clientDAO = new ClientDAO();
            clientDAO.createClient(newClient);
            
            AccountDAO accountDAO = new AccountDAO();
            accountDAO.save(newAccount);
            
            // Audit log
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

    private static String generateAccountNumber() {
        return String.format("%08d", new Random().nextInt(100000000));
    }

    public static void loginMenu(Scanner scanner) {
        System.out.print("\n>> Enter CPF: ");
        String cpf = scanner.nextLine();

        // Check if CPF exists in either manager or client tables
        Manager manager = userDAO.findManagerByCpf(cpf);
        Client client = userDAO.findClientByCpf(cpf);

        if (manager == null && client == null) {
            System.out.println("CPF not registered in the system.");
            return;
        }

        // Case 1: User is only a manager
        if (manager != null && client == null) {
            handleManagerLogin(scanner, manager);
            return;
        }

        // Case 2: User is only a client
        if (client != null && manager == null) {
            handleClientLogin(scanner, client);
            return;
        }

        // Case 3: User is both manager and client
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
            return;
        }
        scanner.nextLine();

        switch (choice) {
            case 1:
                handleManagerLogin(scanner, manager);
                break;
            case 2:
                handleClientLogin(scanner, client);
                break;
            case 0:
                System.out.println("Login cancelled.");
                break;
            default:
                System.out.println("Invalid option.");
        }
    }
    
    private static void handleManagerLogin(Scanner scanner, Manager manager) {
        System.out.print(">> Enter manager password: ");
        String password = scanner.nextLine();
        
        if (manager.validateLogin(password, manager.getPasswordSalt())) {
            System.out.println("\nSuccessfully logged in as Manager.");
            managerMenu(scanner, manager);
        } else {
            System.out.println("Incorrect password for manager.");
        }
    }

    private static void handleClientLogin(Scanner scanner, Client client) {
        // Check if account is blocked
        if (client.getBlocked()) {
            System.out.println("\nACCOUNT BLOCKED: Too many failed attempts");
            System.out.println("Please contact a manager to unlock your account.");
            return;
        }

        System.out.print(">> Enter client password: ");
        String password = scanner.nextLine();

        if (client.validateLogin(password, client.getPasswordSalt()) && client.getLoginAttempts() < 3) {
            client.resetLoginAttempts();
            userDAO.update(client);
            System.out.println("\nSuccessfully logged in as Client.");
            clientMenu(scanner, client);
        } else {
            client.incrementLoginAttempts();
            userDAO.update(client);
            
            if (client.getLoginAttempts() >= 3) {
                System.out.println("\nACCOUNT BLOCKED: 3 failed attempts");
                System.out.println("Contact a manager to unlock your account.");
            } else {
                System.out.println("\nIncorrect password. Attempts: " + 
                    client.getLoginAttempts() + "/3");
            }
        }
    }

    public static void managerMenu(Scanner scanner, Manager manager) {
        boolean running = true;
        while (running) {
            System.out.println("\n======= MANAGER MENU =======");
            System.out.println("1. Register New Manager");
            System.out.println("2. Unlock Client Account");
            System.out.println("3. Review Reversal Requests");
            System.out.println("4. Review Account Closures");
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
                    // TODO
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
                System.out.printf("%d. Account #%d | Balance: $%.2f%n", 
                    (i + 1), acc.getId(), acc.getBalance());
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
                    // TODO: deposit logic
                    System.out.printf("$%.2f deposited successfully.%n", depositAmount);
                    break;
                case 3:
                    System.out.print("Enter withdrawal amount: $");
                    double withdrawAmount = scanner.nextDouble();
                    scanner.nextLine();
                    // TODO: withdraw logic
                    System.out.printf("$%.2f withdrawn successfully.%n", withdrawAmount);
                    break;
                case 4:
                    System.out.print("Recipient account number: ");
                    String destAccount = scanner.nextLine();
                    System.out.print("Transfer amount: $");
                    double transferAmount = scanner.nextDouble();
                    scanner.nextLine();
                    // TODO: transfer logic
                    System.out.printf("Transferred $%.2f to account %s%n", transferAmount, destAccount);
                    break;
                case 5:
                    reversalRequestMenu(scanner);
                    break;
                case 6:
                    System.out.print("Are you sure you want to close this account? (Y/N): ");
                    String confirm = scanner.nextLine();
                    if (confirm.equalsIgnoreCase("Y")) {
                        // TODO: closure logic
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
    }

    public static void reversalRequestMenu(Scanner scanner) {
        System.out.println("\nSelect transaction type to reverse:");
        System.out.println("1. Outgoing Transfers");
        System.out.println("2. Incoming Transfers");
        System.out.println("0. Cancel");
        System.out.print("Your selection: ");

        int choice;
        try {
            choice = scanner.nextInt();
        } catch (Exception e) {
            System.out.println("Invalid input.");
            scanner.nextLine();
            return;
        }
        scanner.nextLine();

        if (choice == 0) return;

        // TODO: Load and display transactions
        System.out.println("\nRecent transactions:");
        System.out.println("1. TXN#111 - $200.00");
        System.out.println("2. TXN#222 - $500.00");
        System.out.println("0. Back");

        System.out.print("Select transaction: ");
        int txChoice;
        try {
            txChoice = scanner.nextInt();
        } catch (Exception e) {
            System.out.println("Invalid input.");
            scanner.nextLine();
            return;
        }
        scanner.nextLine();

        if (txChoice == 0) return;

        System.out.print("Reason for reversal: ");
        // String reason = scanner.nextLine();

        // TODO: submit request
        System.out.println("Reversal request submitted. Reference: R" + System.currentTimeMillis());
    }
}