package com.devradar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @NotBlank(message = "E-posta gerekli")
    @Email(message = "Geçerli bir e-posta girin")
    private String email;

    @NotBlank(message = "Ad soyad gerekli")
    private String fullName;

    @Size(min = 6, message = "Şifre en az 6 karakter olmalı")
    private String password;

    private String code;

    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String email, String fullName, String password, String code) {
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.code = code;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
