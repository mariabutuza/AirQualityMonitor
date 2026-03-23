package com.airquality.monitoringmicroservice.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DeviceTokenClient {

    private final RestTemplate restTemplate;
    private final AuthClient authClient;
    private final UserAuthProperties props;

    @Data
    public static class DeviceTokenDTO {
        private Long userId;
        private String token;
        private String platform;
        private Boolean expo;
    }

    public List<DeviceTokenDTO> getTokensForUser(Long userId) {
        String url = props.getUrl() + "/push/" + userId + "/tokens";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authClient.getToken());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<DeviceTokenDTO[]> resp =
                restTemplate.exchange(url, HttpMethod.GET, entity, DeviceTokenDTO[].class);
        return resp.getBody() != null ? Arrays.asList(resp.getBody()) : List.of();
    }
}
