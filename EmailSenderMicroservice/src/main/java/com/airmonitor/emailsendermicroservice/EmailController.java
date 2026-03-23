package com.airmonitor.emailsendermicroservice;

import com.airmonitor.emailsendermicroservice.model.EmailRequest;
import com.airmonitor.emailsendermicroservice.model.EmailRequestAttachment;
import com.airmonitor.emailsendermicroservice.model.ResponseDTO;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
        System.out.println("EmailController încărcat cu ruta: /api/email");
    }

    @PostMapping("/send-alert")
    public ResponseEntity<ResponseDTO> sendAlertEmail(
            @Valid @RequestBody EmailRequest emailRequest,
            BindingResult bindingResult,
            Principal principal
    ) throws MessagingException, IOException {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO(bindingResult.getFieldError().getDefaultMessage()));
        }

        String userEmail = principal.getName();
        emailRequest.setRecipientEmail(userEmail);

        emailService.sendEmail(emailRequest);
        return ResponseEntity.ok(new ResponseDTO("Alert email sent successfully!"));
    }

    @PostMapping("/send-report")
    public ResponseEntity<ResponseDTO> sendReportEmail(
            @Valid @RequestBody EmailRequestAttachment emailRequest,
            BindingResult bindingResult,
            Principal principal
    ) throws MessagingException, IOException {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO(bindingResult.getFieldError().getDefaultMessage()));
        }

        String userEmail = principal.getName();
        emailRequest.setRecipientEmail(userEmail);

        emailService.sendEmail(emailRequest);
        return ResponseEntity.ok(new ResponseDTO("Detailed report email sent successfully!"));
    }

    @PostMapping("/sendConfirmationEmail")
    public ResponseEntity<ResponseDTO> sendConfirmationEmail(
            @Valid @RequestBody EmailRequest emailRequest,
            BindingResult bindingResult
    ) throws MessagingException, IOException {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO(bindingResult.getFieldError().getDefaultMessage()));
        }

        emailService.sendConfirmationEmail(emailRequest);
        System.out.println("Email de confirmare trimis cu succes către: " + emailRequest.getRecipientEmail());

        return ResponseEntity.ok(new ResponseDTO("Confirmation email sent successfully!"));
    }

    @PostMapping("/system/send-alert")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO> sendAlertEmailFromSystem(
            @Valid @RequestBody EmailRequest emailRequest,
            BindingResult bindingResult
    ) throws MessagingException, IOException {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO(bindingResult.getFieldError().getDefaultMessage()));
        }

        emailService.sendEmail(emailRequest);
        return ResponseEntity.ok(new ResponseDTO("System alert email sent successfully!"));
    }

}
