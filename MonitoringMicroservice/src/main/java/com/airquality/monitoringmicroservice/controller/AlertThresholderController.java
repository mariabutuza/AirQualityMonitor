package com.airquality.monitoringmicroservice.controller;

import com.airquality.monitoringmicroservice.entity.AlertThreshold;
import com.airquality.monitoringmicroservice.entity.ComparatorOp;
import com.airquality.monitoringmicroservice.service.AlertThresholdService;
import com.airquality.monitoringmicroservice.security.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.airquality.monitoringmicroservice.dto.AlertThresholdRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/thresholds")
@RequiredArgsConstructor
public class AlertThresholderController {

    private final AlertThresholdService service;
    private final JwtUtils jwtUtils;


    @GetMapping
    public ResponseEntity<List<AlertThreshold>> getUserThresholds(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtils.extractUserId(token);
        return ResponseEntity.ok(service.getUserThresholds(userId));
    }


    @PostMapping
    public ResponseEntity<String> setThreshold(@RequestBody @Valid AlertThresholdRequest request,
                                           @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtils.extractUserId(token);

        AlertThreshold entity = mapToEntity(request, userId);

        service.saveOrUpdateThreshold(userId, entity);
        return ResponseEntity.ok("Threshold saved/updated");
    }

    private AlertThreshold mapToEntity(AlertThresholdRequest dto, Long userId) {
        return AlertThreshold.builder()
                .userId(userId)
                .location(dto.getLocation())
                .metricType(dto.getMetricType())
                .thresholdValue(dto.getThresholdValue())
                .comparator(dto.getComparator() != null ? dto.getComparator() : ComparatorOp.GREATER_THAN)
                .enabled(dto.getEnabled() != null ? dto.getEnabled() : true)
                .isDefault(false)
                .build();
    }

}
