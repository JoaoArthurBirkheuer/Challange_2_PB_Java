package br.com.compass.utils;

public class CPFValidator {

    private static final String CPF_REGEX = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}";

    // Verifica se o CPF está no formato XXX.XXX.XXX-XX
    public static boolean isValidFormat(String cpf) {
        return cpf != null && cpf.matches(CPF_REGEX);
    }

    // Valida o CPF com base no formato e nos dígitos verificadores
    public static boolean isValidCPF(String cpf) {
        if (!isValidFormat(cpf)) return false;

        String numericCpf = cpf.replaceAll("\\D", "");

        // Verifica se possui 11 dígitos e se todos os dígitos são iguais (CPF inválido)
        if (numericCpf.length() != 11 || numericCpf.chars().distinct().count() == 1) {
            return false;
        }

        try {
            int sum1 = 0, sum2 = 0;
            for (int i = 0; i < 9; i++) {
                int digit = Character.getNumericValue(numericCpf.charAt(i));
                sum1 += digit * (10 - i);
                sum2 += digit * (11 - i);
            }

            int check1 = 11 - (sum1 % 11);
            check1 = (check1 >= 10) ? 0 : check1;

            sum2 += check1 * 2;
            int check2 = 11 - (sum2 % 11);
            check2 = (check2 >= 10) ? 0 : check2;

            return check1 == Character.getNumericValue(numericCpf.charAt(9)) &&
                   check2 == Character.getNumericValue(numericCpf.charAt(10));

        } catch (NumberFormatException e) {
            return false;
        }
    }
}
