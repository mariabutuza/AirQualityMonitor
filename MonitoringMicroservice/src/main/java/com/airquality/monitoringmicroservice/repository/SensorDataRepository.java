package com.airquality.monitoringmicroservice.repository;

import com.airquality.monitoringmicroservice.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.time.Instant;
import java.util.Optional;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    Optional<SensorData> findFirstBySensor_IdAndMetricTypeOrderByTimestampDesc(
            Long sensorId, MetricType metricType);

    Page<SensorData> findBySensor_IdAndMetricTypeAndTimestampBetween(
            Long sensorId, MetricType metricType, Instant from, Instant to, Pageable pageable);

    Optional<SensorData> findFirstBySensorAndMetricTypeOrderByTimestampDesc(
            Sensor sensor, MetricType metricType);

    Page<SensorData> findBySensorAndMetricTypeAndTimestampBetween(
            Sensor sensor, MetricType metricType, Instant from, Instant to, Pageable pageable);
}