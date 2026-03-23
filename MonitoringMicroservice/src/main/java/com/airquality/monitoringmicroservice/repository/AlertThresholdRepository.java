package com.airquality.monitoringmicroservice.repository;

import com.airquality.monitoringmicroservice.entity.AlertThreshold;
import com.airquality.monitoringmicroservice.entity.MetricType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface AlertThresholdRepository extends JpaRepository<AlertThreshold, Long> {

    List<AlertThreshold> findByUserId(Long userId);

    Optional<AlertThreshold> findByUserIdAndMetricTypeAndLocation(Long userId, MetricType metricType, String location);

    Optional<AlertThreshold> findByIsDefaultTrueAndMetricTypeAndLocation(MetricType metricType, String location);
    List<AlertThreshold> findAllByMetricTypeAndLocationAndEnabledTrue(MetricType metricType, String location);

    List<AlertThreshold> findByIsDefaultTrueAndEnabledTrue();

}