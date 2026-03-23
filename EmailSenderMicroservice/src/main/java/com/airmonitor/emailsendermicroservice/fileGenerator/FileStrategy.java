package com.airmonitor.emailsendermicroservice.fileGenerator;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileStrategy {
    private final FileGeneration fileGeneration;

    public FileStrategy(FileGeneration fileGeneration) {
        this.fileGeneration = fileGeneration;
    }

    public void generateFile(String reportContent, String filePath) {
        fileGeneration.generateFile(reportContent, filePath);
    }
}