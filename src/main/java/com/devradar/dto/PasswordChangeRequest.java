package com.devradar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordChangeRequest {

    @NotBlank(message = "Yeni şifre gerekli")
    @Size(min = 6, message = "Şifre en az 6 karakter olmalı")
    private String newPassword;

    private String code;

    public PasswordChangeRequest() {}

    public PasswordChangeRequest(String newPassword, String code) {
        this.newPassword = newPassword;
        this.code = code;
    }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
