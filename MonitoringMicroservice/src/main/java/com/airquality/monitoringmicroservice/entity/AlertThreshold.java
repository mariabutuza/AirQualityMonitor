package com.airquality.monitoringmicroservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertThreshold {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private Long userId;

    @Column(nullable = false, length = 128)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MetricType metricType;

    @Column(nullable = false)
    private Double thresholdValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    @Builder.Default
    private ComparatorOp comparator = ComparatorOp.GREATER_THAN;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    private boolean isDefault;

}