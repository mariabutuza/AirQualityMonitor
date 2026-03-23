package com.airquality.monitoringmicroservice.dto;

import com.airquality.monitoringmicroservice.entity.MetricType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data @Builder
public class TimeSeriesResponse {
    private Long sensorId;
    private MetricType metricType;
    private Instant from;
    private Instant to;
    private String bucket;
    private List<TimeSeriesPoint> points;
}