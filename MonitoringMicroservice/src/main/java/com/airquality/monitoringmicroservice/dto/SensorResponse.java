package com.airquality.monitoringmicroservice.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class SensorResponse {
    private Long id;
    private String deviceId;
    private String name;
    private String location;
    private Boolean active;
}
