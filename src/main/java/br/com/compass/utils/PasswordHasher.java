package br.com.compass.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public final class PasswordHasher {

    private static final int ITERATIONS = 1000; // NÚMERO DE ITERAÇÕES DO ALGORITMO PBKDF2
    private static final int KEY_LENGTH = 32; // TAMANHO DA CHAVE GERADA (EM BYTES)
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256"; // ALGORITMO DE HASH USADO
    private static final int SALT_LENGTH = 16; // TAMANHO DO SALT (EM BYTES)
    
    // SALT É UM CONJUNTO DE BYTES ALEATÓRIOS ADICIONADO À SENHA ANTES DE PASSAR PELO ALGORITMO

    public static String hashPassword(String password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
            );
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Falha ao gerar hash da senha", e);
        }
    }

    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    public static boolean verifyPassword(
        String inputPassword,
        String storedHash,
        byte[] storedSalt
    ) {
        String inputHash = hashPassword(inputPassword, storedSalt);
        return inputHash.equals(storedHash);
    }
}
