package br.com.compass;

import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        mainMenu(scanner);
        scanner.close();
        System.out.println("Application closed");
    }

    public static void mainMenu(Scanner scanner) {
        boolean running = true;

        while (running) {
            System.out.println("========= Main Menu =========");
            System.out.println("|| 1. Login                ||");
            System.out.println("|| 2. Account Opening      ||");
            System.out.println("|| 0. Exit                 ||");
            System.out.println("=============================");
            System.out.print("Choose an option: ");

            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1:
                    loginMenu(scanner);
                    break;
                case 2:
                    System.out.println(">> Account Opening.");
                    // TODO: call account creation logic here
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
        System.out.println(">> CPF: ");
        String cpf = scanner.nextLine();

        System.out.println(">> Password: ");
        String password = scanner.nextLine();

        // TODO: Validate CPF and password with AuthService

        boolean isManager = false;  // Replace with actual check
        boolean isClient = true;    // Replace with actual check

        if (!isManager && !isClient) {
            System.out.println("No user found with given CPF.");
            return;
        }

        if (isManager && isClient) {
            System.out.println("CPF belongs to both roles. Enter 'M' for Manager or 'C' for Client:");
            String choice = scanner.nextLine().toUpperCase();
            if (choice.equals("M")) {
                managerMenu(scanner);
            } else if (choice.equals("C")) {
                clientMenu(scanner);
            } else {
                System.out.println("Invalid role choice.");
            }
        } else if (isManager) {
            managerMenu(scanner);
        } else if (isClient) {
            clientMenu(scanner);
        }
    }

    public static void managerMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("======= Manager Menu =======");
            System.out.println("|| 1. Create Manager       ||");
            System.out.println("|| 2. Review Reversal Req ||");
            System.out.println("|| 3. Review Inactivation ||");
            System.out.println("|| 0. Exit                ||");
            System.out.println("============================");
            System.out.print("Choose an option: ");

            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1:
                    System.out.println(">> Creating new manager...");
                    // TODO: logic to create a manager
                    break;
                case 2:
                    System.out.println(">> Listing Reversal Requests...");
                    // TODO: list reversal requests and approve/reject
                    break;
                case 3:
                    System.out.println(">> Listing Account Inactivation Requests...");
                    // TODO: list inactivation requests and approve/reject
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    public static void clientMenu(Scanner scanner) {
        boolean choosing = true;

        while (choosing) {
            System.out.println(">> List of your accounts:");
            // TODO: Load user's accounts
            System.out.println("1. Account 1234");
            System.out.println("2. Account 5678");
            System.out.println("0. Exit");
            System.out.print("Choose account: ");
            int accountChoice = scanner.nextInt();
            scanner.nextLine();

            if (accountChoice == 0) {
                choosing = false;
            } else {
                // TODO: Validate chosen account
                clientAccountMenu(scanner, accountChoice);
            }
        }
    }

    public static void clientAccountMenu(Scanner scanner, int accountId) {
        boolean running = true;

        while (running) {
            System.out.println("====== Account Menu ======");
            System.out.println("|| 1. View Balance       ||");
            System.out.println("|| 2. Deposit            ||");
            System.out.println("|| 3. Withdraw           ||");
            System.out.println("|| 4. Transfer           ||");
            System.out.println("|| 5. Request Reversal   ||");
            System.out.println("|| 6. Request Inactivation ||");
            System.out.println("|| 0. Back               ||");
            System.out.println("==========================");
            System.out.print("Choose an option: ");

            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1:
                    System.out.println(">> Your current balance is: ...");
                    // TODO: print balance using service
                    break;
                case 2:
                    System.out.print(">> Amount to deposit: ");
                    double depositAmount = scanner.nextDouble();
                    scanner.nextLine();
                    // TODO: perform deposit
                    System.out.println(">> Deposited " + depositAmount);
                    break;
                case 3:
                    System.out.print(">> Amount to withdraw: ");
                    double withdrawAmount = scanner.nextDouble();
                    scanner.nextLine();
                    // TODO: perform withdrawal
                    System.out.println(">> Withdrawn " + withdrawAmount);
                    break;
                case 4:
                    System.out.print(">> Enter destination account number: ");
                    String destAccount = scanner.nextLine();
                    System.out.print(">> Enter amount: ");
                    double transferAmount = scanner.nextDouble();
                    scanner.nextLine();
                    // TODO: perform transfer
                    System.out.println(">> Transferred " + transferAmount + " to " + destAccount);
                    break;
                case 5:
                    reversalRequestMenu(scanner);
                    break;
                case 6:
                    System.out.println(">> Are you sure you want to request account inactivation? (Y/N)");
                    String confirm = scanner.nextLine();
                    if (confirm.equalsIgnoreCase("Y")) {
                        // TODO: request inactivation
                        System.out.println(">> Inactivation request submitted.");
                        return;
                    } else {
                        System.out.println(">> Canceled.");
                    }
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
        System.out.println("Select the type of operation to reverse:");
        System.out.println("1. Deposits");
        System.out.println("2. Withdrawals");
        System.out.println("3. Transfers Sent");
        System.out.println("4. Transfers Received");
        System.out.println("0. Cancel");
        System.out.print("Your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 0) {
            return;
        }

        // TODO: Load and display corresponding transactions
        System.out.println(">> Displaying transactions...");
        System.out.println("1. Transaction #111 - R$200");
        System.out.println("2. Transaction #222 - R$500");
        System.out.println("0. Cancel");

        System.out.print("Select transaction to reverse: ");
        int txChoice = scanner.nextInt();
        scanner.nextLine();

        if (txChoice == 0) return;

        System.out.print("Enter reason for reversal: ");
        String reason = scanner.nextLine();

        // TODO: submit reversal request
        System.out.println(">> Reversal request submitted with reason: " + reason);
    }
}
