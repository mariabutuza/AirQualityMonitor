package com.airquality.monitoringmicroservice.repository;

import com.airquality.monitoringmicroservice.entity.AlertEvent;
import com.airquality.monitoringmicroservice.entity.MetricType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AlertEventRepository extends JpaRepository<AlertEvent, Long> {

    Page<AlertEvent> findByUserIdOrderByTriggeredAtDesc(Long userId, Pageable pageable);
    Optional<AlertEvent> findTop1ByUserIdAndLocationAndMetricTypeOrderByTriggeredAtDesc(
            Long userId, String location, MetricType metricType);

    Page<AlertEvent> findByUserIdAndLocationAndMetricTypeAndTriggeredAtAfter(
            Long userId, String location, MetricType metricType, Instant after, Pageable pageable);

    Optional<AlertEvent> findTopByUserIdAndLocationAndMetricTypeAndIsDefaultAndResolvedFalseOrderByTriggeredAtDesc(
            Long userId, String location, MetricType metricType, Boolean isDefault);

    List<AlertEvent> findByLocationAndMetricTypeAndIsDefaultAndResolvedFalse(
            String location, MetricType metricType, Boolean isDefault);


}