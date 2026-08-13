package com.devradar.dto;

import jakarta.validation.constraints.*;

public class RegisterRequest {

    @NotBlank(message = "E-posta gerekli")
    @Email(message = "Geçerli bir e-posta girin")
    @Size(max = 100, message = "E-posta çok uzun")
    private String email;

    @NotBlank(message = "Şifre gerekli")
    @Size(min = 6, max = 50, message = "Şifre 6 ile 50 karakter arasında olmalı")
    private String password;

    @NotBlank(message = "Ad soyad gerekli")
    @Size(max = 100, message = "Ad soyad çok uzun")
    private String fullName;

    public RegisterRequest() {}

    public RegisterRequest(String email, String password, String fullName) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
