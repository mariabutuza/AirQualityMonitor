package com.airmonitor.emailsendermicroservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EmailRequestAttachment {

    @NotBlank(message = "Name can not be empty!")
    private String name;

    @NotNull(message = "Email can not be null!")
    private String recipientEmail;

    @NotNull(message = "Subject can not be null!")
    private String subject;

    @NotNull(message = "File type can not be null!")
    private String fileType;

    @NotNull(message = "Report content can not be null!")
    private String reportContent;

    @NotNull(message = "Custom message 1 can not be null!")
    private String customMessage1;

    @NotNull(message = "Custom message 2 can not be null!")
    private String customMessage2;
    private String attachmentName;

    // conținutul fișierului codificat Base64
    private String attachmentBase64;

    public EmailRequestAttachment() {
    }

    public EmailRequestAttachment(String attachmentName, String attachmentBase64) {
        this.attachmentName = attachmentName;
        this.attachmentBase64 = attachmentBase64;
    }

    public EmailRequestAttachment(String name, String recipientEmail, String subject, String fileType, String reportContent, String customMessage1, String customMessage2) {
        this.name = name;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.fileType = fileType;
        this.reportContent = reportContent;
        this.customMessage1 = customMessage1;
        this.customMessage2 = customMessage2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getReportContent() {
        return reportContent;
    }

    public void setReportContent(String reportContent) {
        this.reportContent = reportContent;
    }

    public String getCustomMessage1() {
        return customMessage1;
    }

    public void setCustomMessage1(String customMessage1) {
        this.customMessage1 = customMessage1;
    }

    public String getCustomMessage2() {
        return customMessage2;
    }

    public void setCustomMessage2(String customMessage2) {
        this.customMessage2 = customMessage2;
    }
    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getAttachmentBase64() {
        return attachmentBase64;
    }

    public void setAttachmentBase64(String attachmentBase64) {
        this.attachmentBase64 = attachmentBase64;
    }
}

