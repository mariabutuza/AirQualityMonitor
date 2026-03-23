package com.airquality.monitoringmicroservice.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private Long userId;

    @Column(nullable = false, length = 128)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MetricType metricType;

    @Column(nullable = false)
    private Double measuredValue;

    @Column(nullable = false)
    private Double thresholdValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private ComparatorOp comparator;

    @Column(nullable = false)
    private Instant triggeredAt;

    @Builder.Default
    private Boolean emailSent = false;

    @Builder.Default
    private Boolean pushSent = false;

    @Builder.Default
    private Boolean resolved = false;

    private Instant resolvedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
}