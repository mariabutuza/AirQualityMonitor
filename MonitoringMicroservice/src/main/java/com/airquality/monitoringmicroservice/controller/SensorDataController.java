package com.airquality.monitoringmicroservice.controller;

import com.airquality.monitoringmicroservice.dto.EmailRequestAttachment;
import com.airquality.monitoringmicroservice.dto.LatestResponse;
import com.airquality.monitoringmicroservice.dto.TimeSeriesResponse;
import com.airquality.monitoringmicroservice.entity.MetricType;
import com.airquality.monitoringmicroservice.entity.SensorData;
import com.airquality.monitoringmicroservice.service.RaportService;
import com.airquality.monitoringmicroservice.service.SensorDataQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.airquality.monitoringmicroservice.security.JwtUtils;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorDataController {

    private final SensorDataQueryService queryService;
    private final JwtUtils jwtUtils;
    private final RestTemplate restTemplate;
    private final RaportService reportService;

    @Value("${email.service.url}")
    private String emailServiceUrl;

    @Value("${email.service.token}")
    private String emailServiceToken;

    @GetMapping("/{sensorId}/latest")
    public ResponseEntity<LatestResponse> latest(
            @PathVariable Long sensorId,
            @RequestParam MetricType metricType
    ) {
        Optional<LatestResponse> resp = queryService.latest(sensorId, metricType);
        return resp.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{sensorId}/range")
    public ResponseEntity<?> range(
            @PathVariable Long sensorId,
            @RequestParam MetricType metricType,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return ResponseEntity.ok(queryService.range(sensorId, metricType, from, to, PageRequest.of(page, size)));
    }

    @GetMapping("/{sensorId}/aggregate")
    public ResponseEntity<TimeSeriesResponse> aggregate(
            @PathVariable Long sensorId,
            @RequestParam MetricType metricType,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(defaultValue = "PT5M") Duration bucket
    ) {
        return ResponseEntity.ok(queryService.aggregate(sensorId, metricType, from, to, bucket));
    }

@PostMapping("/{sensorId}/send-report")
public ResponseEntity<String> sendReportByEmail(
        @PathVariable Long sensorId,
        @RequestParam MetricType metricType,
        @RequestParam Instant from,
        @RequestParam Instant to,
        @RequestParam(defaultValue = "csv") String fileType,
        @RequestHeader("Authorization") String authHeader,
        Principal principal
) {
    String userId = principal.getName(); // email-ul userului logat

    System.out.println("[SEND-REPORT] Pornit pentru sensorId=" + sensorId
            + " metric=" + metricType
            + " perioada=" + from + " → " + to);
    System.out.println("👤 User (email): " + userId);

    List<SensorData> data = queryService.range(
            sensorId, metricType, from, to, PageRequest.of(0, 1000)
    ).getContent();

    if (data.isEmpty()) {
        System.err.println("Nu există date pentru perioada selectată!");
        return ResponseEntity.badRequest().body("Nu există date pentru perioada selectată.");
    }

    String base64Content;
    String attachmentName;

    if ("pdf".equalsIgnoreCase(fileType)) {
        base64Content = Base64.getEncoder().encodeToString(
                reportService.buildPdfBytes(data, metricType, from, to)
        );
        attachmentName = "report.pdf";
        fileType = "pdf";
    } else {
        base64Content = Base64.getEncoder().encodeToString(
                reportService.buildCsvBytes(data)
        );
        attachmentName = "report.csv";
        fileType = "csv";
    }


    System.out.println("Raport generat: " + attachmentName
            + " (" + fileType + "), nr. de înregistrări: " + data.size());

    EmailRequestAttachment emailReq = new EmailRequestAttachment();
    emailReq.setRecipientEmail(userId);
    emailReq.setSubject("Raport calitate aer - " + metricType);
    emailReq.setCustomMessage1("Perioada: " + from + " → " + to);
    emailReq.setFileType(fileType);
    emailReq.setAttachmentName(attachmentName);
    emailReq.setAttachmentBase64(base64Content);
    emailReq.setBody("Raportul de calitate a aerului este atașat acestui email.");
    emailReq.setReportContent("Raport generat automat - " + data.size() + " înregistrări.");
    emailReq.setName("AirQuality Monitor");
    emailReq.setCustomMessage2("Mulțumim că folosești platforma AirQuality Monitor!");

    try {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (authHeader != null && !authHeader.isBlank()) {
            headers.set("Authorization", authHeader);
        }

        System.out.println("Trimitere către emailServiceUrl: " + emailServiceUrl + "/send-report");
        System.out.println("Header Authorization: " + headers.getFirst("Authorization"));

        ResponseEntity<Void> response = restTemplate.postForEntity(
                emailServiceUrl + "/send-report",
                new HttpEntity<>(emailReq, headers),
                Void.class
        );

        System.out.println("Răspuns primit de la email-service: " + response.getStatusCode());

        return ResponseEntity.ok("Raport trimis pe email");
    } catch (Exception e) {
        e.printStackTrace();
        System.err.println("Eroare la trimiterea raportului: " + e.getMessage());
        return ResponseEntity.internalServerError().body("Eroare la trimiterea raportului: " + e.getMessage());
    }
}

@GetMapping("/{sensorId}/report")
public ResponseEntity<byte[]> downloadReport(
        @PathVariable Long sensorId,
        @RequestParam MetricType metricType,
        @RequestParam Instant from,
        @RequestParam Instant to,
        @RequestParam String fileType,
        Principal principal
) {
    String userId = principal.getName();

    List<SensorData> data = queryService.range(
            sensorId, metricType, from, to, PageRequest.of(0, 1000)
    ).getContent();

    if (data.isEmpty()) {
        return ResponseEntity.badRequest().body(null);
    }

    byte[] fileBytes;
    String fileName;
    MediaType mediaType;

    if ("pdf".equalsIgnoreCase(fileType)) {
        fileBytes = reportService.buildPdfBytes(data, metricType, from, to);
        fileName = "report.pdf";
        mediaType = MediaType.APPLICATION_PDF;
    } else {
        fileBytes = reportService.buildCsvBytes(data);
        fileName = "report.csv";
        mediaType = MediaType.valueOf("text/csv");
    }

    return ResponseEntity.ok()
            .contentType(mediaType)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
            .body(fileBytes);
}
    private byte[] buildCsvBytes(List<SensorData> data) {
        StringBuilder sb = new StringBuilder("timestamp,value,metricType,location\n");
        for (SensorData d : data) {
            sb.append(d.getTimestamp()).append(",")
                    .append(d.getValue()).append(",")
                    .append(d.getMetricType()).append(",")
                    .append(d.getSensor().getLocation()).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] buildPdfBytes(List<SensorData> data, MetricType metricType, Instant from, Instant to) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            com.lowagie.text.Document doc = new com.lowagie.text.Document();
            com.lowagie.text.pdf.PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new com.lowagie.text.Paragraph("Raport calitate aer - " + metricType));
            doc.add(new com.lowagie.text.Paragraph("Perioada: " + from + " → " + to));
            doc.add(new com.lowagie.text.Paragraph("\nDate:\n"));

            for (SensorData d : data) {
                doc.add(new com.lowagie.text.Paragraph(
                        d.getTimestamp() + " | " + d.getValue() + " | " + d.getMetricType() + " | " + d.getSensor().getLocation()
                ));
            }
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Eroare generare PDF", e);
        }
    }

}
