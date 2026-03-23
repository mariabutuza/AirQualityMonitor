package com.airquality.monitoringmicroservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data @AllArgsConstructor
public class TimeSeriesPoint {
    private Instant ts;
    private Double value;
}