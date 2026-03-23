package com.airquality.monitoringmicroservice.client;

import com.airquality.monitoringmicroservice.dto.EmailRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class EmailClient {

    private final RestTemplate restTemplate;
    private final AuthClient authClient;
    private final EmailAuthProperties props;

    public void sendSystemAlert(EmailRequest emailRequest) {
        String url = props.getUrl() + "/system/send-alert";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authClient.getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<EmailRequest> entity = new HttpEntity<>(emailRequest, headers);

        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }
}
