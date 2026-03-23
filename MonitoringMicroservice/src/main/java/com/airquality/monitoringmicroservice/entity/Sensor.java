package com.airquality.monitoringmicroservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sensor {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String deviceId;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 128)
    private String location;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(nullable = false, length = 120)
    private String ingestKeyHash;
    @Transient
    private String ingestKey;
}
