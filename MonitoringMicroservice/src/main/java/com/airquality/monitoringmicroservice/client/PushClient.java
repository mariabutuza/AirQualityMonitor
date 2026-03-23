package com.airquality.monitoringmicroservice.client;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PushClient {

    private static final String EXPO_URL = "https://exp.host/--/api/v2/push/send";
    private final RestTemplate restTemplate = new RestTemplate();

    public void sendExpoBatch(List<ExpoMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            log.warn("Niciun mesaj push de trimis.");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<List<ExpoMessage>> entity = new HttpEntity<>(messages, headers);

            log.info("Trimit batch push cu {} mesaje către Expo...", messages.size());
            for (ExpoMessage msg : messages) {
                log.info("Push către token={}, title='{}', body='{}'",
                        msg.getTo(), msg.getTitle(), msg.getBody());
            }

            ResponseEntity<Map> resp = restTemplate.exchange(EXPO_URL, HttpMethod.POST, entity, Map.class);

            log.info("Răspuns Expo status={} body={}",
                    resp.getStatusCode(), resp.getBody());

        } catch (Exception e) {
            log.error("Eroare la trimiterea push către Expo: {}", e.getMessage(), e);
        }
    }

    @Data
    public static class ExpoMessage {
        private String to;
        private String title;
        private String body;
        private Object data;
        private Integer ttl;
        private String priority;
    }
}
