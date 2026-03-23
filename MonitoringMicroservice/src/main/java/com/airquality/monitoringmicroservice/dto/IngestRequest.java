package com.airquality.monitoringmicroservice.dto;

import com.airquality.monitoringmicroservice.entity.MetricType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class IngestRequest {
    @NotNull
    private MetricType metricType;
    @NotNull
    private Double value;
    private Instant timestamp;

}