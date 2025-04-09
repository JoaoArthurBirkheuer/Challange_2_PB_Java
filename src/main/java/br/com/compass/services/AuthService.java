package br.com.compass.services;

import br.com.compass.dao.ClientDAO;
import br.com.compass.dao.ManagerDAO;
import br.com.compass.model.Client;
import br.com.compass.model.Manager;
import br.com.compass.utils.PasswordHasher;

public class AuthService {

    public static Object login(String cpf, String password) {
        ManagerDAO managerDAO = new ManagerDAO();
        Manager manager = managerDAO.findByCpf(cpf);
        if (manager != null) {

            if (PasswordHasher.verifyPassword(password, manager.getPasswordHash(),manager.getPasswordSalt())) {
                return manager;
            } else {
                System.out.println("Incorrect password for manager.");
                return null;
            }
        }

        ClientDAO clientDAO = new ClientDAO();
        Client client = clientDAO.findByCpf(cpf);
        if (client != null) {
            
            if (client.getLoginAttempts() >= 3) {
                System.out.println("Client is blocked due to too many failed login attempts.");
                return null;
            }
            if (PasswordHasher.verifyPassword(password, client.getPasswordHash(),client.getPasswordSalt())) {
                client.setLoginAttempts(0);
                clientDAO.update(client);
                return client;
            } else {
                int attempts = client.getLoginAttempts() + 1;
                client.setLoginAttempts(attempts);
                clientDAO.update(client);
                if (attempts >= 3) {
                    System.out.println("Too many failed attempts. Client is now blocked.");
                } else {
                    System.out.println("Incorrect password. Attempts: " + attempts);
                }
                return null;
            }
        }
        else
        System.out.println("User not found.");
        return null;
    }
}
