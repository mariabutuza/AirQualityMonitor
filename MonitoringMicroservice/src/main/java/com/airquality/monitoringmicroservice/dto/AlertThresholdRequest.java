package com.airquality.monitoringmicroservice.dto;

import com.airquality.monitoringmicroservice.entity.ComparatorOp;
import com.airquality.monitoringmicroservice.entity.MetricType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlertThresholdRequest {
    @NotBlank private String location;
    @NotNull  private MetricType metricType;
    @NotNull  private Double thresholdValue;
    private ComparatorOp comparator = ComparatorOp.GREATER_THAN;
    private Boolean enabled = true;
}
