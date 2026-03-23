package com.airquality.monitoringmicroservice.client;

import com.airquality.monitoringmicroservice.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserClient {

    private final RestTemplate restTemplate;
    private final AuthClient authClient;
    private final UserAuthProperties userProps;

    public UserDTO getUserById(Long userId) {
        String url = userProps.getUrl() + "/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authClient.getToken());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(url, HttpMethod.GET, entity, UserDTO.class).getBody();
    }

    public String getUserEmail(Long userId) {
        String url = userProps.getUrl() + "/" + userId + "/email";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authClient.getToken());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
    }

    public List<UserDTO> getAllUsers() {
        String url = userProps.getUrl() + "/all";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authClient.getToken());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey("content")) {
            return List.of();
        }

        List<Map<String, Object>> content = (List<Map<String, Object>>) body.get("content");

        return content.stream()
                .map(item -> new UserDTO(
                        ((Number) item.get("id")).longValue(),
                        (String) item.get("email"),
                        (String) item.get("fullName"),
                        (String) item.get("role")
                ))
                .collect(Collectors.toList());
    }
}
