package br.com.compass.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import br.com.compass.model.Account;
import br.com.compass.model.AuditLog;

public class CsvReportExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss");
    private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public void exportAccountStatement(Account account, List<AuditLog> logs, String downloadDir) throws IOException {
        if (account == null || logs == null || logs.isEmpty()) return;

        String headerDate = "Date";
        String headerAction = "Action";
        String headerDetails = "Details";

        int maxDateLength = Math.max(headerDate.length(), 20);  // 20 como tamanho mínimo
        int maxActionLength = Math.max(headerAction.length(), 15);  // 15 como tamanho mínimo
        int maxDetailsLength = Math.max(headerDetails.length(), 30);  // 30 como tamanho mínimo

        for (AuditLog log : logs) {
            String formattedDate = log.getTimestamp().format(DATE_FORMATTER);
            maxDateLength = Math.max(maxDateLength, formattedDate.length());
            maxActionLength = Math.max(maxActionLength, log.getActionType().length());
            maxDetailsLength = Math.max(maxDetailsLength, log.getDetails().length());
        }

        String timestamp = LocalDateTime.now().format(FILE_NAME_FORMATTER);
        String fileName = "account_statement_" + account.getAccountNumber() + "_" + timestamp + ".csv";
        String fullPath = Paths.get(downloadDir, fileName).toString();

        File directory = new File(downloadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try (FileWriter writer = new FileWriter(fullPath)) {
            String separator = "+-" + "-".repeat(maxDateLength) + "-+-" + 
                              "-".repeat(maxActionLength) + "-+-" + 
                              "-".repeat(maxDetailsLength) + "-+\n";
            writer.write(separator);

            String header = String.format("| %-" + maxDateLength + "s | %-" + maxActionLength + "s | %-" + maxDetailsLength + "s |\n",
                    headerDate, headerAction, headerDetails);
            writer.write(header);

            writer.write(separator);

            for (AuditLog log : logs) {
                String formattedDate = log.getTimestamp().format(DATE_FORMATTER);
                String action = log.getActionType();
                String details = log.getDetails().replace(",", " ");

                String line = String.format("| %-" + maxDateLength + "s | %-" + maxActionLength + "s | %-" + maxDetailsLength + "s |\n",
                        formattedDate, action, details);
                writer.write(line);
            }

            writer.write(separator);
        }
    }
}
