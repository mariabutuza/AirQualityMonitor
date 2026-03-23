package com.airquality.monitoringmicroservice.service;

import com.airquality.monitoringmicroservice.dto.LatestResponse;
import com.airquality.monitoringmicroservice.dto.TimeSeriesPoint;
import com.airquality.monitoringmicroservice.dto.TimeSeriesResponse;
import com.airquality.monitoringmicroservice.entity.MetricType;
import com.airquality.monitoringmicroservice.entity.SensorData;
import com.airquality.monitoringmicroservice.repository.SensorDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SensorDataQueryService {

    private final SensorDataRepository sensorDataRepository;

    public Optional<LatestResponse> latest(Long sensorId, MetricType metricType) {
        return sensorDataRepository.findFirstBySensor_IdAndMetricTypeOrderByTimestampDesc(sensorId, metricType)
                .map(d -> LatestResponse.builder()
                        .sensorId(sensorId)
                        .metricType(metricType)
                        .value(d.getValue())
                        .timestamp(d.getTimestamp())
                        .build());
    }

    public Page<SensorData> range(Long sensorId, MetricType metricType,
                                  Instant from, Instant to, Pageable pageable) {
        return sensorDataRepository.findBySensor_IdAndMetricTypeAndTimestampBetween(sensorId, metricType, from, to, pageable);
    }

    public TimeSeriesResponse aggregate(Long sensorId, MetricType metricType,
                                        Instant from, Instant to, Duration bucket) {

        List<SensorData> raw = sensorDataRepository
                .findBySensor_IdAndMetricTypeAndTimestampBetween(sensorId, metricType, from, to,
                        PageRequest.of(0, 100_000, Sort.by("timestamp").ascending()))
                .getContent();

        long step = Math.max(bucket.getSeconds(), 1);
        Map<Long, List<Double>> bins = new LinkedHashMap<>();
        for (SensorData d : raw) {
            long sec = d.getTimestamp().getEpochSecond();
            long k = sec - (sec % step);
            bins.computeIfAbsent(k, x -> new ArrayList<>()).add(d.getValue());
        }

        List<TimeSeriesPoint> points = bins.entrySet().stream()
                .map(e -> new TimeSeriesPoint(
                        Instant.ofEpochSecond(e.getKey()),
                        e.getValue().stream().mapToDouble(v -> v).average().orElse(Double.NaN)))
                .collect(Collectors.toList());

        return TimeSeriesResponse.builder()
                .sensorId(sensorId)
                .metricType(metricType)
                .from(from)
                .to(to)
                .bucket(bucket.toString())
                .points(points)
                .build();
    }
}
