package com.devradar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmailChangeRequest {

    @NotBlank(message = "Yeni e-posta adresi gerekli")
    @Email(message = "Geçerli bir e-posta girin")
    private String newEmail;

    private String currentPassword;
    private String code;

    public EmailChangeRequest() {}

    public EmailChangeRequest(String newEmail, String currentPassword, String code) {
        this.newEmail = newEmail;
        this.currentPassword = currentPassword;
        this.code = code;
    }

    public String getNewEmail() { return newEmail; }
    public void setNewEmail(String newEmail) { this.newEmail = newEmail; }

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
