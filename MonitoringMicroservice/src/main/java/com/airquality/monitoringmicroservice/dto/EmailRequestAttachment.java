package com.airquality.monitoringmicroservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequestAttachment {
    private String name;
    private String recipientEmail;
    private String subject;
    private String fileType;
    private String reportContent;
    private String customMessage1;
    private String customMessage2;

    private String attachmentBase64;
    private String attachmentName;
    private String body;



}
