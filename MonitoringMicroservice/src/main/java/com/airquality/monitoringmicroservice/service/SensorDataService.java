package com.airquality.monitoringmicroservice.service;

import com.airquality.monitoringmicroservice.dto.IngestRequest;
import com.airquality.monitoringmicroservice.entity.*;
import com.airquality.monitoringmicroservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SensorDataService {

    private final SensorRepository sensorRepository;
    private final SensorDataRepository sensorDataRepository;
    private final AlertThresholdService alertThresholdService;
    private final AlertEventRepository alertEventRepository;
    private final RestTemplate restTemplate;
    private final PasswordEncoder passwordEncoder;
    private final AlertService alertService;


    @Value("${email.service.url:/email-service}")
    private String emailServiceBaseUrl;

    @Value("${email.service.token:}")
    private String emailServiceToken;

    @Value("${ingest.vocIndex.k:2.0}")
    private double vocIndexK;

    public void ingest(String deviceId, IngestRequest request, String providedIngestKey) {
        Sensor sensor = sensorRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new NoSuchElementException("Unknown sensor: " + deviceId));

        if (!sensor.getActive()) {
            throw new IllegalStateException("Sensor is inactive");
        }

        if (!passwordEncoder.matches(providedIngestKey, sensor.getIngestKeyHash())) {
            throw new SecurityException("Invalid ingest key for device " + deviceId);
        }

        Double value = request.getValue();
        MetricType metric = request.getMetricType();
        Instant ts = request.getTimestamp() != null ? request.getTimestamp() : Instant.now();

        if (metric == MetricType.AIR_VOC_INDEX) {
            value = computeVocIndex(request.getValue(), vocIndexK);
            metric = MetricType.AIR_CO2_EQ_PPM; // convertim în ppm
        }

        SensorData data = SensorData.builder()
                .sensor(sensor)
                .metricType(metric)
                .value(value)
                .timestamp(ts)
                .build();

        sensorDataRepository.save(data);
        alertService.processAlert(sensor, metric, value, ts, null);
    }

    private Long saveReading(Sensor sensor, MetricType metricType, Double value, Instant ts) {
        SensorData d = SensorData.builder()
                .sensor(sensor)
                .metricType(metricType)
                .value(value)
                .timestamp(ts)
                .build();
        return sensorDataRepository.save(d).getId();
    }

    private double computeVocIndex(double ratio, double k) {
        final double RATIO_REF = 3.6;
        if (ratio <= 0) return 0;
        double x = Math.log10(RATIO_REF / ratio);
        double idx = 100.0 * x * k;
        return Math.max(0, Math.min(500, idx));
    }
}
