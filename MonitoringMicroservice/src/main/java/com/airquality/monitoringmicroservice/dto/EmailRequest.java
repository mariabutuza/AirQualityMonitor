package com.airquality.monitoringmicroservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    private String name;
    private String recipientEmail;
    private String subject;
    private String customMessage1;
    private String customMessage2;
}
