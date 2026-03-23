package com.airquality.monitoringmicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableAsync   // 🔑 permite rularea metodelor async
public class MonitoringMicroserviceApplication {

    public static void main(String[] args) {
        String raw = "XktZmNYliw2glr-33qFQMxUUCAVrMUqs";

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String generatedHash = encoder.encode(raw);

        System.out.println("🔑 Cheia în clar: " + raw);
        System.out.println("🔒 Hash generat: " + generatedHash);
        System.out.println("Verificare .matches(): " + encoder.matches(raw, generatedHash));

        SpringApplication.run(MonitoringMicroserviceApplication.class, args);
    }
}
