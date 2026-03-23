package com.airmonitor.usermicroservice.dto;

public class EmailRequest {

    private String name;
    private String recipientEmail;
    private String subject;
    private String customMessage1;
    private String customMessage2;

    public EmailRequest() {
    }

    public EmailRequest(String name, String recipientEmail, String subject, String customMessage1, String customMessage2) {
        this.name = name;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
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
}
