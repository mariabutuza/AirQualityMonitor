package com.airquality.monitoringmicroservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthClient {

    private final RestTemplate restTemplate;
    private final UserAuthProperties props;

    private String jwtToken;
    private long expiryEpoch = 0;

    public String getToken() {
        if (jwtToken == null || isExpired()) {
            authenticate();
        }
        return jwtToken;
    }

    private void authenticate() {
        String url = props.getUrl() + "/login";

        Map<String, String> loginRequest = Map.of(
                "email", props.getEmail(),
                "password", props.getPassword()
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(url, loginRequest, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            jwtToken = (String) response.getBody().get("token");

            expiryEpoch = decodeExpiry(jwtToken);
        } else {
            throw new IllegalStateException("Nu am putut obține token pentru Monitoring");
        }
    }

    private boolean isExpired() {
        return System.currentTimeMillis() / 1000 > expiryEpoch - 60;
    }

    private long decodeExpiry(String token) {
        try {
            String[] parts = token.split("\\.");
            String payload = new String(Base64.getDecoder().decode(parts[1]));
            Map<String, Object> claims = new ObjectMapper().readValue(payload, Map.class);
            return ((Number) claims.get("exp")).longValue();
        } catch (Exception e) {
            return 0;
        }
    }
}
