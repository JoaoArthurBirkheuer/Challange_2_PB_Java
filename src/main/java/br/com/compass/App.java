package br.com.compass;

import java.util.List;
import java.util.Scanner;

import br.com.compass.dao.AccountDAO;
import br.com.compass.dao.UserDAO;
import br.com.compass.model.Account;
import br.com.compass.model.Client;
import br.com.compass.model.Manager;
// import br.com.compass.services.AuthService;

public class App {
    private static UserDAO userDAO = new UserDAO();
    // private static AuthService authService = new AuthService(userDAO);

    public static void main(String[] args) {
        // br.com.compass.config.DataSeeder.seed();
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
                    // TODO: account creation logic
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option! Please try again.");
            }
        }
    }

    public static void loginMenu(Scanner scanner) {
        System.out.print("\n>> Enter CPF: ");
        String cpf = scanner.nextLine();

        // Verifica primeiro se é manager (não aplica bloqueio)
        Manager manager = userDAO.findManagerByCpf(cpf);
        if (manager != null) {
            System.out.print(">> Enter password: ");
            String password = scanner.nextLine();
            
            if (manager.validateLogin(password, manager.getPasswordSalt())) {
                System.out.println("\nSuccessfully logged in as Manager.");
                managerMenu(scanner, manager);
            } else {
                System.out.println("Incorrect password for manager.");
            }
            return;
        }

        // Se não for manager, verifica como client
        Client client = userDAO.findClientByCpf(cpf);
        if (client == null) {
            System.out.println("CPF not registered in the system.");
            return;
        }

        // Verifica se conta está bloqueada
        if (client.getBlocked()) {
            System.out.println("\nACCOUNT BLOCKED: Too many failed attempts");
            System.out.println("Please contact a manager to unlock your account.");
            return;
        }
        System.out.println(client.getLoginAttempts());
        System.out.println(client.getBlocked());
        System.out.print(">> Enter password: ");
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
        System.out.println("1. Deposits");
        System.out.println("2. Withdrawals");
        System.out.println("3. Outgoing Transfers");
        System.out.println("4. Incoming Transfers");
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