package com.airquality.monitoringmicroservice.controller;

import com.airquality.monitoringmicroservice.dto.SensorCreateResponse;
import com.airquality.monitoringmicroservice.dto.SensorResponse;
import com.airquality.monitoringmicroservice.entity.Sensor;
import com.airquality.monitoringmicroservice.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/sensors")
@RequiredArgsConstructor
public class SensorAdminController {

    private final SensorRepository sensorRepository;
    private final PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<SensorCreateResponse> createSensor(@RequestBody Sensor sensor) {
        if (sensorRepository.findByDeviceId(sensor.getDeviceId()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        String rawKey = generateIngestKey();
        sensor.setIngestKeyHash(passwordEncoder.encode(rawKey));

        Sensor saved = sensorRepository.save(sensor);

        SensorCreateResponse response = SensorCreateResponse.builder()
                .id(saved.getId())
                .deviceId(saved.getDeviceId())
                .name(saved.getName())
                .location(saved.getLocation())
                .active(saved.getActive())
                .ingestKey(rawKey)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<SensorResponse>> listSensors() {
        List<SensorResponse> sensors = sensorRepository.findAll().stream()
                .map(s -> SensorResponse.builder()
                        .id(s.getId())
                        .deviceId(s.getDeviceId())
                        .name(s.getName())
                        .location(s.getLocation())
                        .active(s.getActive())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(sensors);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSensor(@PathVariable Long id) {
        if (!sensorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        sensorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private String generateIngestKey() {
        byte[] randomBytes = new byte[24];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/distinct-locations/count")
    public ResponseEntity<Long> countDistinctLocations() {
        long count = sensorRepository.findAll().stream()
                .map(s -> s.getLocation().trim().toLowerCase())
                .distinct()
                .count();
        return ResponseEntity.ok(count);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/by-device/{deviceId}")
    public ResponseEntity<SensorResponse> getByDeviceId(@PathVariable String deviceId) {
        return sensorRepository.findByDeviceId(deviceId)
                .map(s -> ResponseEntity.ok(SensorResponse.builder()
                        .id(s.getId())
                        .deviceId(s.getDeviceId())
                        .name(s.getName())
                        .location(s.getLocation())
                        .active(s.getActive())
                        .build()))
                .orElse(ResponseEntity.notFound().build());
    }


}
