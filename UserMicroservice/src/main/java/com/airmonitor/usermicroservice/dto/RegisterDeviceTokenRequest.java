package com.airmonitor.usermicroservice.dto;

import jakarta.validation.constraints.NotBlank;

public class RegisterDeviceTokenRequest {
    @NotBlank private String token;
    @NotBlank private String platform;
    private Boolean expo = true;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public Boolean getExpo() { return expo; }
    public void setExpo(Boolean expo) { this.expo = expo; }
}
