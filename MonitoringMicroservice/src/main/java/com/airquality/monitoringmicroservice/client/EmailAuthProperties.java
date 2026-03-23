package com.airquality.monitoringmicroservice.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "email.service")
@Data
public class EmailAuthProperties {
    private String url;
    private String email;
    private String password;
}
