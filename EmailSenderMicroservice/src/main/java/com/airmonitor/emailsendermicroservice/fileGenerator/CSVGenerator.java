package com.airmonitor.emailsendermicroservice.fileGenerator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CSVGenerator implements FileGeneration {

    @Override
    public void generateFile(String reportContent, String filePath) {
        try {
            Files.writeString(Paths.get(filePath), reportContent, StandardCharsets.UTF_8);
            System.out.println("CSV generat: " + Path.of(filePath).toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error generating CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

}