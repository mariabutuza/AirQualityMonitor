package com.airquality.monitoringmicroservice.service;

import com.airquality.monitoringmicroservice.client.DeviceTokenClient;
import com.airquality.monitoringmicroservice.client.EmailClient;
import com.airquality.monitoringmicroservice.client.PushClient;
import com.airquality.monitoringmicroservice.client.UserClient;
import com.airquality.monitoringmicroservice.dto.EmailRequest;
import com.airquality.monitoringmicroservice.dto.UserDTO;
import com.airquality.monitoringmicroservice.entity.AlertEvent;
import com.airquality.monitoringmicroservice.entity.AlertThreshold;
import com.airquality.monitoringmicroservice.entity.ComparatorOp;
import com.airquality.monitoringmicroservice.entity.MetricType;
import com.airquality.monitoringmicroservice.entity.Sensor;
import com.airquality.monitoringmicroservice.repository.AlertEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertThresholdService alertThresholdService;
    private final AlertEventRepository alertEventRepository;
    private final UserClient userClient;
    private final EmailClient emailClient;
    private final DeviceTokenClient deviceTokenClient;
    private final PushClient pushClient;

    @Async
    @Transactional
    public void processAlert(Sensor sensor,
                             MetricType metricType,
                             Double value,
                             Instant ts,
                             String authHeader) {
        try {
            if (metricType != MetricType.AIR_CO2_EQ_PPM) {
                log.debug("Ignor alerta pentru metricType={} (nu e CO₂).", metricType);
                return;
            }

            log.info("Pornesc verificarea pragurilor pentru location={}, metricType={}, valoare={}",
                    sensor.getLocation(), metricType, value);

            var thresholds = alertThresholdService.getAllThresholdsForLocationAndMetric(
                    sensor.getLocation(), metricType
            );

            if (thresholds.isEmpty()) {
                log.warn("Nu există praguri configurate pentru location={}, metric={}",
                        sensor.getLocation(), metricType);
                return;
            }

            for (AlertThreshold th : thresholds) {
                log.info("Prag {} pentru userId={}, value={}, threshold={}, comparator={}",
                        th.isDefault() ? "DEFAULT" : "CUSTOM",
                        th.getUserId(), value, th.getThresholdValue(), th.getComparator());

                boolean crossed = compare(value, th.getThresholdValue(), th.getComparator());
                log.debug("Verific prag pentru userId={}, metric={}, value={} vs prag={} → crossed={}",
                        th.getUserId(), metricType, value, th.getThresholdValue(), crossed);

                if (th.isDefault()) {
                    handleDefaultThreshold(th, sensor, metricType, value, ts, crossed);
                } else {
                    handleCustomThreshold(th, sensor, metricType, value, ts, crossed);
                }
            }
        } catch (Exception ex) {
            log.error("Eroare în processAlert: {}", ex.getMessage(), ex);
        }
    }
    private void handleDefaultThreshold(AlertThreshold th,
                                        Sensor sensor,
                                        MetricType metricType,
                                        double value,
                                        Instant ts,
                                        boolean crossed) {
        if (crossed) {
            List<UserDTO> users;
            try {
                users = userClient.getAllUsers();
            } catch (Exception ex) {
                log.error("Nu am putut obține lista userilor: {}", ex.getMessage());
                return;
            }

            for (var user : users) {
                if (user.getEmail() == null || user.getEmail().isBlank()) {
                    log.warn("Userul {} nu are email setat, skip.", user.getId());
                    continue;
                }

                String lockKey = (user.getId() + "|" + sensor.getLocation() + "|" + metricType + "|DEFAULT").intern();

                synchronized (lockKey) {
                    var activeOpt = alertEventRepository
                            .findTopByUserIdAndLocationAndMetricTypeAndIsDefaultAndResolvedFalseOrderByTriggeredAtDesc(
                                    user.getId(), sensor.getLocation(), metricType, true);

                    if (activeOpt.isPresent()) {
                        log.debug("Alertă DEFAULT deja activă pentru userId={}, location={}",
                                user.getId(), sensor.getLocation());
                        continue;
                    }

                    AlertEvent event = AlertEvent.builder()
                            .userId(user.getId())
                            .location(sensor.getLocation())
                            .sensor(sensor)
                            .metricType(metricType)
                            .measuredValue(value)
                            .thresholdValue(th.getThresholdValue())
                            .comparator(th.getComparator())
                            .triggeredAt(ts)
                            .emailSent(false)
                            .pushSent(false)
                            .resolved(false)
                            .isDefault(true)
                            .build();

                    try {
                        AlertEvent saved = alertEventRepository.save(event);

                        boolean sent = sendEmail(user, sensor, metricType, value, th.getThresholdValue(), true);
                        saved.setEmailSent(sent);
                        alertEventRepository.save(saved);

                        boolean push = sendPush(
                                user.getId(),
                                "Calitate aer " + sensor.getLocation(),
                                metricType + " = " + round(value) + " a depășit pragul " + round(th.getThresholdValue()),
                                java.util.Map.of(
                                        "eventType", "ALERT_TRIGGERED",
                                        "metric", metricType.name(),
                                        "value", value,
                                        "threshold", th.getThresholdValue(),
                                        "location", sensor.getLocation(),
                                        "isDefault", true
                                )
                        );
                        saved.setPushSent(push);
                        alertEventRepository.save(saved);

                    } catch (DataIntegrityViolationException ex) {
                        log.warn("Alertă DEFAULT deja activă (concurență cross-node) pentru userId={}, location={}, metric={}",
                                user.getId(), sensor.getLocation(), metricType);
                    }
                }
            }
        } else {
            var activeEvents = alertEventRepository
                    .findByLocationAndMetricTypeAndIsDefaultAndResolvedFalse(
                            sensor.getLocation(), metricType, true);

            for (var active : activeEvents) {
                active.setResolved(true);
                sendPush(
                        active.getUserId(),
                        "Revenit sub prag - " + active.getLocation(),
                        active.getMetricType() + " a revenit sub " + round(active.getThresholdValue()),
                        java.util.Map.of(
                                "eventType", "ALERT_RESOLVED",
                                "metric", active.getMetricType().name(),
                                "threshold", active.getThresholdValue(),
                                "location", active.getLocation(),
                                "isDefault", true
                        )
                );
                active.setResolvedAt(ts);
                alertEventRepository.save(active);
                log.info("Prag DEFAULT revenit la normal pentru userId={}, location={}, metric={}",
                        active.getUserId(), sensor.getLocation(), metricType);
            }
        }
    }

    private void handleCustomThreshold(AlertThreshold th,
                                       Sensor sensor,
                                       MetricType metricType,
                                       double value,
                                       Instant ts,
                                       boolean crossed) {
        if (crossed) {
            if (th.getUserId() == null) {
                log.warn("Prag CUSTOM fără userId -> ignor.");
                return;
            }

            UserDTO user;
            try {
                user = userClient.getUserById(th.getUserId());
            } catch (Exception ex) {
                log.error("Nu am putut obține userul {}: {}", th.getUserId(), ex.getMessage());
                return;
            }

            if (user.getEmail() == null || user.getEmail().isBlank()) {
                log.warn("Userul {} nu are email setat, skip.", th.getUserId());
                return;
            }

            String lockKey = (th.getUserId() + "|" + sensor.getLocation() + "|" + metricType + "|CUSTOM").intern();

            synchronized (lockKey) {
                var activeOpt = alertEventRepository
                        .findTopByUserIdAndLocationAndMetricTypeAndIsDefaultAndResolvedFalseOrderByTriggeredAtDesc(
                                th.getUserId(), sensor.getLocation(), metricType, false);

                if (activeOpt.isPresent()) {
                    log.debug("Alertă CUSTOM deja activă pentru userId={}, location={}",
                            th.getUserId(), sensor.getLocation());
                    return;
                }

                AlertEvent event = AlertEvent.builder()
                        .userId(th.getUserId())
                        .location(sensor.getLocation())
                        .sensor(sensor)
                        .metricType(metricType)
                        .measuredValue(value)
                        .thresholdValue(th.getThresholdValue())
                        .comparator(th.getComparator())
                        .triggeredAt(ts)
                        .emailSent(false)
                        .pushSent(false)
                        .resolved(false)
                        .isDefault(false)
                        .build();

                try {
                    AlertEvent saved = alertEventRepository.save(event);

                    boolean sent = sendEmail(user, sensor, metricType, value, th.getThresholdValue(), false);
                    saved.setEmailSent(sent);
                    alertEventRepository.save(saved);

                    boolean push = sendPush(
                            user.getId(),
                            "Calitate aer " + sensor.getLocation(),
                            metricType + " = " + round(value) + " a depășit pragul " + round(th.getThresholdValue()),
                            java.util.Map.of(
                                    "eventType", "ALERT_TRIGGERED",
                                    "metric", metricType.name(),
                                    "value", value,
                                    "threshold", th.getThresholdValue(),
                                    "location", sensor.getLocation(),
                                    "isDefault", false
                            )
                    );
                    saved.setPushSent(push);
                    alertEventRepository.save(saved);

                } catch (DataIntegrityViolationException ex) {
                    log.warn("Alertă CUSTOM deja activă (concurență cross-node) pentru userId={}, location={}, metric={}",
                            th.getUserId(), sensor.getLocation(), metricType);
                }
            }
        } else {
            var activeOpt = alertEventRepository
                    .findTopByUserIdAndLocationAndMetricTypeAndIsDefaultAndResolvedFalseOrderByTriggeredAtDesc(
                            th.getUserId(), sensor.getLocation(), metricType, false);

            if (activeOpt.isPresent()) {
                var active = activeOpt.get();
                active.setResolved(true);
                active.setResolvedAt(ts);

                sendPush(
                        active.getUserId(),
                        "Revenit sub prag - " + active.getLocation(),
                        active.getMetricType() + " a revenit sub " + round(active.getThresholdValue()),
                        java.util.Map.of(
                                "eventType", "ALERT_RESOLVED",
                                "metric", active.getMetricType().name(),
                                "threshold", active.getThresholdValue(),
                                "location", active.getLocation(),
                                "isDefault", false
                        )
                );

                alertEventRepository.save(active);
                log.info("Prag CUSTOM revenit la normal pentru userId={}, metric={}, location={}",
                        th.getUserId(), metricType, sensor.getLocation());
            }
        }
    }
    private static boolean compare(double value, double threshold, ComparatorOp op) {
        return switch (op) {
            case GREATER_THAN -> value > threshold;
            case LESS_THAN -> value < threshold;
        };
    }

    private boolean sendEmail(UserDTO user,
                              Sensor sensor,
                              MetricType metricType,
                              double value,
                              double threshold,
                              boolean isDefault) {
        try {
            EmailRequest emailReq = new EmailRequest();
            emailReq.setName(user.getFullName());
            emailReq.setRecipientEmail(user.getEmail());
            emailReq.setSubject("ALERTĂ calitate aer – " + sensor.getLocation());
            emailReq.setCustomMessage1("Metrică: " + metricType + " | Valoare: " + round(value));
            emailReq.setCustomMessage2("Prag: " + round(threshold) + " | Senzor: " + sensor.getName());

            emailClient.sendSystemAlert(emailReq);

            log.info("Email {} trimis către userId={}, email={}",
                    isDefault ? "DEFAULT" : "CUSTOM",
                    user.getId(),
                    user.getEmail());

            return true;
        } catch (Exception e) {
            log.error("Eroare la trimiterea emailului către {} (userId={}): {}",
                    user.getEmail(), user.getId(), e.getMessage());
            return false;
        }
    }

    private boolean sendPush(Long userId, String title, String body, Object data) {
        try {
            var tokens = deviceTokenClient.getTokensForUser(userId);
            var expoMsgs = tokens.stream()
                    .filter(t -> Boolean.TRUE.equals(t.getExpo()))
                    .map(t -> {
                        var m = new PushClient.ExpoMessage();
                        m.setTo(t.getToken());
                        m.setTitle(title);
                        m.setBody(body);
                        m.setData(data);
                        m.setTtl(24 * 3600);
                        m.setPriority("high");
                        return m;
                    }).toList();

            pushClient.sendExpoBatch(expoMsgs);
            return !expoMsgs.isEmpty();
        } catch (Exception e) {
            log.error("Eroare trimitere push către userId={}: {}", userId, e.getMessage());
            return false;
        }
    }

    private static String round(double v) {
        return String.format(Locale.US, "%.2f", v);
    }
}