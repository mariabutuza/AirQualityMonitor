package com.airquality.monitoringmicroservice.controller;

import com.airquality.monitoringmicroservice.dto.IngestRequest;
import com.airquality.monitoringmicroservice.entity.Sensor;
import com.airquality.monitoringmicroservice.repository.SensorRepository;
import com.airquality.monitoringmicroservice.service.SensorDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingest")
@RequiredArgsConstructor
public class IngestController {

    private final SensorRepository sensorRepository;
    private final SensorDataService sensorDataService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/{deviceId}")
    public ResponseEntity<String> ingest(
            @PathVariable String deviceId,
            @RequestBody IngestRequest request,
            @RequestHeader("X-INGEST-KEY") String ingestKey
    ) {
        Sensor sensor = sensorRepository.findByDeviceId(deviceId)
                .orElse(null);

        if (sensor == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Sensor not found");
        }

        if (!passwordEncoder.matches(ingestKey, sensor.getIngestKeyHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid ingest key");
        }

        sensorDataService.ingest(deviceId, request, ingestKey);

        return ResponseEntity.ok("Data received");
    }
}
