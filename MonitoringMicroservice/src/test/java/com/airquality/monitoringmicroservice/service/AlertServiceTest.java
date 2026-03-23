package com.airquality.monitoringmicroservice.service;

import com.airquality.monitoringmicroservice.client.UserClient;
import com.airquality.monitoringmicroservice.dto.UserDTO;
import com.airquality.monitoringmicroservice.entity.*;
import com.airquality.monitoringmicroservice.repository.AlertEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AlertServiceTest {

    @Mock
    private AlertThresholdService thresholdService;

    @Mock
    private AlertEventRepository eventRepository;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private AlertService alertService;

    private Sensor sensor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        sensor = new Sensor();
        sensor.setLocation("Marasti");
    }

    @Test
    void test_DefaultThreshold_SentToUserWithoutCustom() {
        AlertThreshold defaultTh = AlertThreshold.builder()
                .isDefault(true)
                .thresholdValue(3000.0)
                .comparator(ComparatorOp.GREATER_THAN)
                .build();

        AlertThreshold customTh = AlertThreshold.builder()
                .isDefault(false)
                .userId(2L)
                .thresholdValue(3500.0)
                .comparator(ComparatorOp.GREATER_THAN)
                .build();

        when(thresholdService.getAllThresholdsForLocationAndMetric("Marasti", MetricType.AIR_CO2_EQ_PPM))
                .thenReturn(List.of(defaultTh, customTh));

        UserDTO user1 = new UserDTO(1L, "user1@test.com", "User One", "USER");
        UserDTO user2 = new UserDTO(2L, "user2@test.com", "User Two", "USER");

        when(userClient.getAllUsers()).thenReturn(List.of(user1, user2));
        when(userClient.getUserById(2L)).thenReturn(user2);

        alertService.processAlert(sensor, MetricType.AIR_CO2_EQ_PPM, 3600.0, Instant.now(), "Bearer test");

        verify(eventRepository, atLeastOnce()).save(any(AlertEvent.class));
    }

    @Test
    void test_CustomThreshold_OnlyForThatUser() {
        AlertThreshold customTh = AlertThreshold.builder()
                .isDefault(false)
                .userId(2L)
                .thresholdValue(3500.0)
                .comparator(ComparatorOp.GREATER_THAN)
                .build();

        when(thresholdService.getAllThresholdsForLocationAndMetric("Marasti", MetricType.AIR_CO2_EQ_PPM))
                .thenReturn(List.of(customTh));

        UserDTO user2 = new UserDTO(2L, "user2@test.com", "User Two", "USER");
        when(userClient.getAllUsers()).thenReturn(List.of(user2));
        when(userClient.getUserById(2L)).thenReturn(user2);

        alertService.processAlert(sensor, MetricType.AIR_CO2_EQ_PPM, 3600.0, Instant.now(), "Bearer test");

        verify(eventRepository, atLeastOnce()).save(any(AlertEvent.class));
    }

}
