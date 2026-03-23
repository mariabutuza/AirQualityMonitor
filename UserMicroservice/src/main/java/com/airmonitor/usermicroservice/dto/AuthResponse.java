package com.airmonitor.usermicroservice.dto;

public class AuthResponse {

    private String token;
    private String role;
    private String fullName;

    public AuthResponse() {
    }

    public AuthResponse(String token) {
        this.token = token;
    }

    public AuthResponse(String token, String role, String fullName) {
        this.token = token;
        this.role = role;
        this.fullName = fullName;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }
    public String getFullName() {
        return fullName;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public void setRole(String role) {
        this.role = role;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String token;
        private String role;
        private String fullName;

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public AuthResponse build() {
            return new AuthResponse(token, role, fullName);
        }
    }
}
