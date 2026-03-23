package com.airmonitor.emailsendermicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EmailSenderMicroserviceApplication {
    public static void main(String[] args) {
        System.out.println("EmailSenderMicroservice a pornit!");
        SpringApplication.run(EmailSenderMicroserviceApplication.class, args);
    }

}
