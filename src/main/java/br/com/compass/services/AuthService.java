package br.com.compass.services;

import br.com.compass.dao.UserDAO;
import br.com.compass.model.Client;
import br.com.compass.model.User;

public class AuthService implements AutoCloseable {
    private final UserDAO userDAO;
    private final boolean ownsUserDAO;
    
    // Constructor for external DAO management
    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.ownsUserDAO = false;
    }
    
    // Constructor for internal DAO management
    public AuthService() {
        this.userDAO = new UserDAO();
        this.ownsUserDAO = true;
    }

    public enum LoginType {
        CLIENT_ONLY, MANAGER_ONLY, BOTH
    }

    public LoginType checkLoginType(String cpf) {
        try {
            boolean isClient = userDAO.findClientByCpf(cpf) != null;
            boolean isManager = userDAO.findManagerByCpf(cpf) != null;
            
            if (isClient && isManager) return LoginType.BOTH;
            if (isClient) return LoginType.CLIENT_ONLY;
            if (isManager) return LoginType.MANAGER_ONLY;
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to check login type", e);
        }
    }

    public User login(String cpf, String password, LoginType loginType) {
        try {
            User user = null;
            
            switch (loginType) {
                case CLIENT_ONLY:
                    user = userDAO.findClientByCpf(cpf);
                    break;
                case MANAGER_ONLY:
                    user = userDAO.findManagerByCpf(cpf);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid login type");
            }
            
            if (user != null && user.validateLogin(password, user.getPasswordSalt())) {
                if (user instanceof Client && ((Client) user).isBlocked()) {
                    return null; // Blocked client
                }
                return user;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Login failed", e);
        }
    }

    @Override
    public void close() {
        if (ownsUserDAO && userDAO != null) {
            try {
                userDAO.close();
            } catch (Exception e) {
                System.err.println("Error while closing UserDAO in AuthService: " + e.getMessage());
            }
        }
    }
}