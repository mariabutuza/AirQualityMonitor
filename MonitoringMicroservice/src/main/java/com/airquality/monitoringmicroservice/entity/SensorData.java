package com.airquality.monitoringmicroservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SensorData {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MetricType metricType;

    @Column(nullable = false)
    private Double value;

    @Column(nullable = false)
    private Instant timestamp;
}
