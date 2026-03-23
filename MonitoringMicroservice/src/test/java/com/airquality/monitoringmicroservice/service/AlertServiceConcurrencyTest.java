package com.airquality.monitoringmicroservice.service;

import com.airquality.monitoringmicroservice.client.DeviceTokenClient;
import com.airquality.monitoringmicroservice.client.EmailClient;
import com.airquality.monitoringmicroservice.client.PushClient;
import com.airquality.monitoringmicroservice.client.UserClient;
import com.airquality.monitoringmicroservice.dto.UserDTO;
import com.airquality.monitoringmicroservice.entity.AlertThreshold;
import com.airquality.monitoringmicroservice.entity.ComparatorOp;
import com.airquality.monitoringmicroservice.entity.MetricType;
import com.airquality.monitoringmicroservice.entity.Sensor;
import com.airquality.monitoringmicroservice.repository.AlertEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AlertServiceConcurrencyTest {

    @Mock
    private AlertThresholdService alertThresholdService;
    @Mock
    private AlertEventRepository alertEventRepository;
    @Mock
    private UserClient userClient;
    @Mock
    private EmailClient emailClient;
    @Mock
    private DeviceTokenClient deviceTokenClient;
    @Mock
    private PushClient pushClient;

    @InjectMocks
    private AlertService alertService;

    private Sensor sensor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sensor = new Sensor();
        sensor.setId(1L);
        sensor.setName("Sensor 1");
        sensor.setLocation("TestLocation");

        UserDTO user = new UserDTO();
        user.setId(10L);
        user.setEmail("test@test.com");
        user.setFullName("Test User");

        when(userClient.getAllUsers()).thenReturn(List.of(user));

        AlertThreshold threshold = new AlertThreshold();
        threshold.setDefault(true);
        threshold.setThresholdValue(1000.0);
        threshold.setComparator(ComparatorOp.GREATER_THAN);
        threshold.setLocation("TestLocation");
        threshold.setMetricType(MetricType.AIR_CO2_EQ_PPM);

        when(alertThresholdService.getAllThresholdsForLocationAndMetric("TestLocation", MetricType.AIR_CO2_EQ_PPM))
                .thenReturn(List.of(threshold));

        when(alertEventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testConcurrentDefaultAlertOnlyOneSaved() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        Runnable task = () -> {
            try {
                alertService.processAlert(sensor, MetricType.AIR_CO2_EQ_PPM, 2000.0, Instant.now(), "auth");
            } finally {
                latch.countDown();
            }
        };

        executor.submit(task);
        executor.submit(task);

        latch.await();

        verify(alertEventRepository, atMost(1)).save(any());
    }
}
