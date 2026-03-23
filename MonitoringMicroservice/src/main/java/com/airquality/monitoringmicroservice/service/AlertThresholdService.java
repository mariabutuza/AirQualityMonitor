package com.airquality.monitoringmicroservice.service;

import com.airquality.monitoringmicroservice.entity.AlertThreshold;
import com.airquality.monitoringmicroservice.entity.MetricType;
import com.airquality.monitoringmicroservice.repository.AlertThresholdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlertThresholdService {

    private final AlertThresholdRepository repository;

    public List<AlertThreshold> getUserThresholds(Long userId) {
        var defaults = repository.findByIsDefaultTrueAndEnabledTrue();
        var customs = repository.findByUserId(userId);

        return new java.util.ArrayList<>() {{
            addAll(defaults);
            addAll(customs);
        }};
    }

    public void saveOrUpdateThreshold(Long userId, AlertThreshold incoming) {
        incoming.setUserId(userId);
        incoming.setDefault(false);

        Optional<AlertThreshold> existing =
                repository.findByUserIdAndMetricTypeAndLocation(userId, incoming.getMetricType(), incoming.getLocation());

        if (existing.isPresent()) {
            AlertThreshold th = existing.get();
            th.setThresholdValue(incoming.getThresholdValue());
            repository.save(th);
        } else {
            repository.save(incoming);
        }
    }


    public AlertThreshold getEffectiveThreshold(Long userId, MetricType metricType, String location) {
        return repository.findByUserIdAndMetricTypeAndLocation(userId, metricType, location)
                .orElseGet(() ->
                        repository.findByIsDefaultTrueAndMetricTypeAndLocation(metricType, location)
                                .orElse(null));
    }

    public List<AlertThreshold> getAllThresholdsForLocationAndMetric(String location, MetricType metricType) {
        return repository.findAllByMetricTypeAndLocationAndEnabledTrue(metricType, location);
    }



}
