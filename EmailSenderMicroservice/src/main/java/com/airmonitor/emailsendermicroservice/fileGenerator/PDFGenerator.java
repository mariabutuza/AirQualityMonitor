package com.airmonitor.emailsendermicroservice.fileGenerator;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class PDFGenerator implements FileGeneration {

    @Override
    public void generateFile(String reportContent, String filePath) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (InputStream fontStream = getClass().getResourceAsStream("/fonts/DejaVuSans.ttf")) {
                if (fontStream == null) {
                    throw new IOException("Fontul DejaVuSans.ttf nu a fost găsit în resources/fonts/");
                }
                var font = PDType0Font.load(document, fontStream);

                reportContent = reportContent.replace("\n", " ");

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.beginText();
                    contentStream.setFont(font, 12);
                    contentStream.newLineAtOffset(50, 700);
                    contentStream.setLeading(14.5f);
                    contentStream.showText("Raport Calitate Aer: " + reportContent);
                    contentStream.endText();
                }
            }

            document.save(filePath);
            System.out.println("PDF generat: " + Path.of(filePath).toAbsolutePath());

        } catch (IOException e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
