package com.airmonitor.emailsendermicroservice.fileGenerator;

public interface FileGeneration {
    void generateFile(String reportContent, String filePath);
}