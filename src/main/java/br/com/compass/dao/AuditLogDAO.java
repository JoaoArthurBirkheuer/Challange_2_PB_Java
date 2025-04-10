package br.com.compass.dao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import br.com.compass.config.JpaConfig;
import br.com.compass.model.Account;
import br.com.compass.model.AuditLog;
import br.com.compass.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class AuditLogDAO {
    private EntityManager em;

    public AuditLogDAO() {
        this.em = JpaConfig.getEntityManager();
    }

    // Constantes para tipos de ação
    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILURE = "LOGIN_FAILURE";
    public static final String LOGOUT = "LOGOUT";
    public static final String ACCOUNT_ACCESS = "ACCOUNT_ACCESS";
    public static final String ACCOUNT_CREATION = "ACCOUNT_CREATION";
    public static final String ACCOUNT_CLOSURE = "ACCOUNT_CLOSURE";
    public static final String DEPOSIT = "DEPOSIT";
    public static final String WITHDRAWAL = "WITHDRAWAL";
    public static final String TRANSFER_IN = "TRANSFER_IN";
    public static final String TRANSFER_OUT = "TRANSFER_OUT";
    public static final String REVERSAL_REQUEST = "REVERSAL_REQUEST";
    public static final String ACCOUNT_BLOCKED = "ACCOUNT_BLOCKED";
    public static final String ACCOUNT_UNLOCKED = "ACCOUNT_UNLOCKED";
    public static final String CLIENT_REGISTRATION = "CLIENT_REGISTRATION";
    public static final String CLIENT_REGISTRATION_FAILED = "CLIENT_REGISTRATION_FAILED";

    public List<AuditLog> findLogs(List<String> actionTypes, User user, Account account, 
                                  LocalDateTime startDate, LocalDateTime endDate) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<AuditLog> cq = cb.createQuery(AuditLog.class);
        Root<AuditLog> log = cq.from(AuditLog.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Filtro por tipos de ação
        if (actionTypes != null && !actionTypes.isEmpty()) {
            predicates.add(log.get("actionType").in(actionTypes));
        }
        
        // Filtro por usuário
        if (user != null) {
            predicates.add(cb.equal(log.get("actor"), user));
        }
        
        // Filtro por conta
        if (account != null) {
            predicates.add(cb.equal(log.get("affectedAccount"), account));
        }
        
        // Filtro por intervalo de datas
        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(log.get("timestamp"), startDate));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(log.get("timestamp"), endDate));
        }
        
        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(log.get("timestamp"))); // Ordena por data mais recente
        
        TypedQuery<AuditLog> query = em.createQuery(cq);
        return query.getResultList();
    }

    public static void showAuditLogMenu(Scanner scanner) {
        AuditLogDAO auditLogDAO = new AuditLogDAO();
        
        while (true) {
            System.out.println("\n===== AUDIT LOG MENU =====");
            System.out.println("1. View All Logs");
            System.out.println("2. View Login/Logout Activities");
            System.out.println("3. View Account Operations");
            System.out.println("4. View Financial Transactions");
            System.out.println("5. View Client Registration Logs");
            System.out.println("6. View Account Management Logs");
            System.out.println("7. Custom Filter");
            System.out.println("0. Back to Main Menu");
            System.out.print("Select an option: ");
            
            int option;
            try {
                option = scanner.nextInt();
                scanner.nextLine(); // Consume newline
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
                continue;
            }
            
            List<AuditLog> logs;
            switch (option) {
                case 1:
                    logs = auditLogDAO.findLogs(null, null, null, null, null);
                    displayLogs(logs);
                    break;
                case 2:
                    logs = auditLogDAO.findLogs(
                        List.of(LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT), 
                        null, null, null, null);
                    displayLogs(logs);
                    break;
                case 3:
                    logs = auditLogDAO.findLogs(
                        List.of(ACCOUNT_ACCESS, ACCOUNT_CREATION, ACCOUNT_CLOSURE), 
                        null, null, null, null);
                    displayLogs(logs);
                    break;
                case 4:
                    logs = auditLogDAO.findLogs(
                        List.of(DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT), 
                        null, null, null, null);
                    displayLogs(logs);
                    break;
                case 5:
                    logs = auditLogDAO.findLogs(
                        List.of(CLIENT_REGISTRATION, CLIENT_REGISTRATION_FAILED), 
                        null, null, null, null);
                    displayLogs(logs);
                    break;
                case 6:
                    logs = auditLogDAO.findLogs(
                        List.of(ACCOUNT_BLOCKED, ACCOUNT_UNLOCKED, REVERSAL_REQUEST), 
                        null, null, null, null);
                    displayLogs(logs);
                    break;
                case 7:
                    customFilterMenu(scanner, auditLogDAO);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    private static void customFilterMenu(Scanner scanner, AuditLogDAO auditLogDAO) {
        System.out.println("\n===== CUSTOM FILTER =====");
        
        // Seleção de tipos de ação
        Set<String> actionTypes = new HashSet<>();
        System.out.println("Select action types (comma separated numbers, 0 to finish):");
        System.out.println("1. Login Success");
        System.out.println("2. Login Failure");
        System.out.println("3. Logout");
        System.out.println("4. Account Access");
        System.out.println("5. Account Creation");
        System.out.println("6. Account Closure");
        System.out.println("7. Deposit");
        System.out.println("8. Withdrawal");
        System.out.println("9. Transfer-in");
        System.out.println("10. Transfer-out");
        System.out.println("11. Reversal Request");
        System.out.println("12. Account Blocked");
        System.out.println("13. Account Unlocked");
        System.out.println("14. Client Registration");
        System.out.println("15. Client Registration Failed");
        System.out.println("0. Finish Selection");
        
        while (true) {
            System.out.print("Enter choices (e.g., 1,2,3): ");
            String input = scanner.nextLine().trim();
            
            if (input.equals("0")) break;
            
            try {
                String[] choices = input.split(",");
                for (String choice : choices) {
                    int num = Integer.parseInt(choice.trim());
                    switch (num) {
                        case 1: actionTypes.add(LOGIN_SUCCESS); break;
                        case 2: actionTypes.add(LOGIN_FAILURE); break;
                        case 3: actionTypes.add(LOGOUT); break;
                        case 4: actionTypes.add(ACCOUNT_ACCESS); break;
                        case 5: actionTypes.add(ACCOUNT_CREATION); break;
                        case 6: actionTypes.add(ACCOUNT_CLOSURE); break;
                        case 7: actionTypes.add(DEPOSIT); break;
                        case 8: actionTypes.add(WITHDRAWAL); break;
                        case 9: actionTypes.add(TRANSFER_IN); break;
                        case 10: actionTypes.add(TRANSFER_OUT); break;
                        case 11: actionTypes.add(REVERSAL_REQUEST); break;
                        case 12: actionTypes.add(ACCOUNT_BLOCKED); break;
                        case 13: actionTypes.add(ACCOUNT_UNLOCKED); break;
                        case 14: actionTypes.add(CLIENT_REGISTRATION); break;
                        case 15: actionTypes.add(CLIENT_REGISTRATION_FAILED); break;
                        default: System.out.println("Invalid choice: " + num);
                    }
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter numbers separated by commas.");
            }
        }
        
        // Outros filtros podem ser adicionados aqui (data, usuário, etc.)
        System.out.println("\nAdditional filters (press Enter to skip):");
        
        // Filtro por data
        LocalDateTime startDate = null, endDate = null;
        System.out.print("Start date (DD/MM/YYYY HH:MM): ");
        String startInput = scanner.nextLine().trim();
        if (!startInput.isEmpty()) {
            startDate = LocalDateTime.parse(startInput, 
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        
        System.out.print("End date (DD/MM/YYYY HH:MM): ");
        String endInput = scanner.nextLine().trim();
        if (!endInput.isEmpty()) {
            endDate = LocalDateTime.parse(endInput, 
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        
        List<String> uniqueActionTypes = new ArrayList<>(actionTypes);
        
        // Executa a consulta
        List<AuditLog> logs = auditLogDAO.findLogs(
            actionTypes.isEmpty() ? null : uniqueActionTypes, 
            null, null, startDate, endDate);
        
        displayLogs(logs);
    }

    private static void displayLogs(List<AuditLog> logs) {
        if (logs.isEmpty()) {
            System.out.println("\nNo logs found matching the criteria.");
            return;
        }
        
        System.out.println("\n===== AUDIT LOGS =====");
        System.out.printf("%-20s %-15s %-30s %-20s %s\n", 
            "Timestamp", "Action Type", "Details", "User", "Account");
        System.out.println("--------------------------------------------------------------------------------");
        
        for (AuditLog log : logs) {
            String user = log.getActor() != null ? log.getActor().getName() : "N/A";
            String account = log.getAffectedAccount() != null ? 
                log.getAffectedAccount().getAccountNumber() : "N/A";
            
            System.out.printf("%-20s %-15s %-30s %-20s %s\n",
                log.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                log.getActionType(),
                log.getDetails().length() > 30 ? 
                    log.getDetails().substring(0, 27) + "..." : log.getDetails(),
                user.length() > 20 ? user.substring(0, 17) + "..." : user,
                account);
        }
    }
}