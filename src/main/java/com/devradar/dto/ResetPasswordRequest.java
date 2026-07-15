package com.devradar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {

    @NotBlank(message = "E-posta gerekli")
    @Email(message = "Geçerli bir e-posta girin")
    private String email;

    @NotBlank(message = "Doğrulama kodu gerekli")
    private String code;

    @NotBlank(message = "Yeni şifre gerekli")
    @Size(min = 6, message = "Şifre en az 6 karakter olmalı")
    private String newPassword;

    public ResetPasswordRequest() {}

    public ResetPasswordRequest(String email, String code, String newPassword) {
        this.email = email;
        this.code = code;
        this.newPassword = newPassword;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
