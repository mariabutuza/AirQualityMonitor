package com.airmonitor.emailsendermicroservice;

import com.airmonitor.emailsendermicroservice.model.EmailRequest;
import com.airmonitor.emailsendermicroservice.model.EmailRequestAttachment;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(EmailRequest emailRequest) throws IOException, MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        Address to = new InternetAddress(emailRequest.getRecipientEmail());
        message.setFrom(new InternetAddress("airquality@monitoring-system.com"));
        message.setRecipient(MimeMessage.RecipientType.TO, to);
        message.setSubject(emailRequest.getSubject());

        String htmlTemplate = readFile("templates/email-template.html");
        String htmlContent = htmlTemplate
                .replace("${name}", emailRequest.getName())
                .replace("${message}", "Calitatea aerului este importantă pentru sănătatea ta.")
                .replace("${message2}", "Ai primit această alertă ca urmare a abonării la notificările sistemului.");

        message.setContent(htmlContent, "text/html; charset=utf-8");
        mailSender.send(message);
    }

    public void sendEmail(EmailRequestAttachment emailRequest) throws IOException, MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        Address to = new InternetAddress(emailRequest.getRecipientEmail());
        message.setFrom(new InternetAddress("airquality@monitoring-system.com"));
        message.setRecipient(MimeMessage.RecipientType.TO, to);
        message.setSubject(emailRequest.getSubject());

        String htmlTemplate = readFile("templates/email-template.html");
        String htmlContent = htmlTemplate
                .replace("${name}", emailRequest.getName())
                .replace("${message}", emailRequest.getCustomMessage1())
                .replace("${message2}", emailRequest.getCustomMessage2());

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(htmlContent, "text/html; charset=utf-8");

        byte[] fileBytes = Base64.getDecoder().decode(emailRequest.getAttachmentBase64());
        String mimeType = "pdf".equalsIgnoreCase(emailRequest.getFileType()) ? "application/pdf" : "text/csv";
        DataSource dataSource = new ByteArrayDataSource(fileBytes, mimeType);

        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.setDataHandler(new DataHandler(dataSource));
        attachmentPart.setFileName(emailRequest.getAttachmentName());

        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(attachmentPart);

        message.setContent(multipart);
        mailSender.send(message);

    }

    public void sendConfirmationEmail(EmailRequest emailRequest) throws IOException, MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        Address to = new InternetAddress(emailRequest.getRecipientEmail());
        message.setFrom(new InternetAddress("airquality@monitoring-system.com"));
        message.setRecipient(MimeMessage.RecipientType.TO, to);
        message.setSubject(emailRequest.getSubject());

        String htmlTemplate = readFile("templates/email-template.html");
        String htmlContent = htmlTemplate
                .replace("${name}", emailRequest.getName())
                .replace("${message}", emailRequest.getCustomMessage1())
                .replace("${message2}", emailRequest.getCustomMessage2());

        message.setContent(htmlContent, "text/html; charset=utf-8");
        mailSender.send(message);

    }

    private String readFile(String path) throws IOException {
        var resource = new ClassPathResource(path);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
