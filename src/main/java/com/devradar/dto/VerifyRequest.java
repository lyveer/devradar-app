package com.devradar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class VerifyRequest {

    @NotBlank(message = "E-posta gerekli")
    @Email(message = "Geçerli bir e-posta girin")
    private String email;

    @NotBlank(message = "Doğrulama kodu gerekli")
    private String code;

    public VerifyRequest() {}

    public VerifyRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
