package com.airquality.monitoringmicroservice.service;

import com.airquality.monitoringmicroservice.entity.SensorData;
import com.airquality.monitoringmicroservice.entity.MetricType;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@Service
public class RaportService {

    public byte[] buildCsvBytes(List<SensorData> data) {
        StringBuilder sb = new StringBuilder("timestamp,value,metricType,location\n");
        for (SensorData d : data) {
            sb.append(d.getTimestamp()).append(",")
                    .append(d.getValue()).append(",")
                    .append(d.getMetricType()).append(",")
                    .append(d.getSensor().getLocation()).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] buildPdfBytes(List<SensorData> data, MetricType metricType, Instant from, Instant to) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document();
            PdfWriter.getInstance(doc, out);
            doc.open();

            BaseFont baseFont = BaseFont.createFont(
                    getClass().getResource("/DejaVuSans-Oblique.ttf").toExternalForm(),
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED
            );


            Font titleFont = new Font(baseFont, 18, Font.BOLD, Color.BLACK);
            Font subFont   = new Font(baseFont, 12, Font.NORMAL, Color.DARK_GRAY);
            Font headFont  = new Font(baseFont, 11, Font.BOLD, Color.BLACK);
            Font cellFont  = new Font(baseFont, 10, Font.NORMAL, Color.BLACK);


            Paragraph title = new Paragraph("Raport Calitate Aer ", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
            Paragraph subtitle = new Paragraph(
                    "Perioada: " + fmt.format(from) + " → " + fmt.format(to),
                    subFont
            );
            subtitle.setAlignment(Element.ALIGN_CENTER);
            doc.add(subtitle);
            doc.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            Stream.of("Timestamp", "Valoare (ppm)", "Metrică", "Locație").forEach(col -> {
                PdfPCell hcell = new PdfPCell(new Phrase(col, headFont));
                hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                hcell.setBackgroundColor(new Color(220, 230, 241));
                hcell.setPadding(5);
                table.addCell(hcell);
            });

            boolean alternate = false;
            for (SensorData d : data) {
                Color bg = alternate ? new Color(245, 245, 245) : Color.WHITE;
                alternate = !alternate;

                PdfPCell cell1 = new PdfPCell(new Phrase(fmt.format(d.getTimestamp()), cellFont));
                PdfPCell cell2 = new PdfPCell(new Phrase(String.format("%.2f", d.getValue()), cellFont));
                PdfPCell cell3 = new PdfPCell(new Phrase(d.getMetricType().toString(), cellFont));
                PdfPCell cell4 = new PdfPCell(new Phrase(d.getSensor().getLocation(), cellFont));

                for (PdfPCell c : List.of(cell1, cell2, cell3, cell4)) {
                    c.setBackgroundColor(bg);
                    c.setPadding(4);
                }

                table.addCell(cell1);
                table.addCell(cell2);
                table.addCell(cell3);
                table.addCell(cell4);
            }

            doc.add(table);

            double avg = data.stream().mapToDouble(SensorData::getValue).average().orElse(0);
            double min = data.stream().mapToDouble(SensorData::getValue).min().orElse(0);
            double max = data.stream().mapToDouble(SensorData::getValue).max().orElse(0);

            doc.add(new Paragraph("\nRezumat:", headFont));
            doc.add(new Paragraph(
                    String.format("Media: %.2f ppm   |   Minim: %.2f   |   Maxim: %.2f", avg, min, max),
                    cellFont
            ));

            doc.add(new Paragraph("\n\nGenerat automat de AirQuality Monitor – " + fmt.format(Instant.now()), subFont));

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Eroare generare PDF", e);
        }
    }


}
