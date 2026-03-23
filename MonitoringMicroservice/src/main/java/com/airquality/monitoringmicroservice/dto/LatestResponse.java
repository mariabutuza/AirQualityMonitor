package com.airquality.monitoringmicroservice.dto;

import com.airquality.monitoringmicroservice.entity.MetricType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data @Builder
public class LatestResponse {
    private Long sensorId;
    private MetricType metricType;
    private Double value;
    private Instant timestamp;
}