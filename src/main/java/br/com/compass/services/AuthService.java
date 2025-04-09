package br.com.compass.services;

import br.com.compass.dao.UserDAO;
import br.com.compass.model.Client;
// import br.com.compass.model.Manager;
import br.com.compass.model.User;
// import br.com.compass.utils.PasswordHasher;

public class AuthService {
    private final UserDAO userDAO;
    
    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public enum LoginType {
        CLIENT_ONLY, MANAGER_ONLY, BOTH
    }

    public LoginType checkLoginType(String cpf) {
        boolean isClient = userDAO.findClientByCpf(cpf) != null;
        boolean isManager = userDAO.findManagerByCpf(cpf) != null;
        
        if (isClient && isManager) return LoginType.BOTH;
        if (isClient) return LoginType.CLIENT_ONLY;
        if (isManager) return LoginType.MANAGER_ONLY;
        return null;
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
                    return null; // Cliente bloqueado
                }
                return user;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Login failed", e);
        }
    }
}