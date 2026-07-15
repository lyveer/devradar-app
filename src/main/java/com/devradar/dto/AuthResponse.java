package com.devradar.dto;

public class AuthResponse {
    private String token;
    private String email;
    private String fullName;
    private boolean isVerified;

    public AuthResponse() {}

    public AuthResponse(String token, String email, String fullName, boolean isVerified) {
        this.token = token;
        this.email = email;
        this.fullName = fullName;
        this.isVerified = isVerified;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public boolean getIsVerified() { return isVerified; }
    public void setIsVerified(boolean isVerified) { this.isVerified = isVerified; }

    public static AuthResponseBuilder builder() {
        return new AuthResponseBuilder();
    }

    public static class AuthResponseBuilder {
        private String token;
        private String email;
        private String fullName;
        private boolean isVerified;

        public AuthResponseBuilder token(String token) { this.token = token; return this; }
        public AuthResponseBuilder email(String email) { this.email = email; return this; }
        public AuthResponseBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public AuthResponseBuilder isVerified(boolean isVerified) { this.isVerified = isVerified; return this; }

        public AuthResponse build() {
            return new AuthResponse(token, email, fullName, isVerified);
        }
    }
}
