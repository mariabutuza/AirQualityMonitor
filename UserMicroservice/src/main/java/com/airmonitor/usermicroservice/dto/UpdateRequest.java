package com.airmonitor.usermicroservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UpdateRequest {

    @NotBlank(message = "Full name must not be blank")
    private String fullName;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email must not be blank")
    private String email;

    public UpdateRequest() {
    }

    public UpdateRequest(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
