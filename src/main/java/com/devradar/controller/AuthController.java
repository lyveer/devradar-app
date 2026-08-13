package com.devradar.controller;

import com.devradar.dto.AuthResponse;
import com.devradar.dto.LoginRequest;
import com.devradar.dto.RegisterRequest;
import com.devradar.model.User;
import com.devradar.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        
        org.springframework.http.ResponseCookie jwtCookie = org.springframework.http.ResponseCookie.from("jwt_token", authResponse.getToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .sameSite("Strict")
                .build();
                
        org.springframework.http.ResponseCookie prefCookie = org.springframework.http.ResponseCookie.from("user_preferences", "theme=dark;lang=tr")
                .httpOnly(false)
                .secure(true)
                .path("/")
                .maxAge(365 * 24 * 60 * 60) // 1 year
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, prefCookie.toString())
                .body(authResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMe(Authentication authentication) {
        return ResponseEntity.ok(authService.getMe(authentication.getName()));
    }

    @PostMapping("/verify")
    public ResponseEntity<AuthResponse> verify(@Valid @RequestBody com.devradar.dto.VerifyRequest request) {
        AuthResponse authResponse = authService.verify(request.getEmail(), request.getCode());
        
        if (authResponse.getToken() != null) {
            org.springframework.http.ResponseCookie jwtCookie = org.springframework.http.ResponseCookie.from("jwt_token", authResponse.getToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .sameSite("Strict")
                    .build();
                    
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(authResponse);
        }
        
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/resend")
    public ResponseEntity<Void> resendCode(@RequestParam String email) {
        authService.resendCode(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody com.devradar.dto.ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody com.devradar.dto.ResetPasswordRequest request) {
        authService.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/profile/request-email-change")
    public ResponseEntity<Void> requestEmailChange(Authentication authentication,
                                                   @Valid @RequestBody com.devradar.dto.EmailChangeRequest request) {
        authService.requestEmailChange(authentication.getName(), request.getNewEmail(), request.getCurrentPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/profile/confirm-email-change")
    public ResponseEntity<AuthResponse> confirmEmailChange(Authentication authentication,
                                                           @Valid @RequestBody com.devradar.dto.EmailChangeRequest request) {
        return ResponseEntity.ok(authService.confirmEmailChange(authentication.getName(), request.getNewEmail(), request.getCode()));
    }

    @PostMapping("/profile/request-password-change")
    public ResponseEntity<Void> requestPasswordChange(Authentication authentication) {
        authService.requestPasswordChange(authentication.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/profile/confirm-password-change")
    public ResponseEntity<Void> confirmPasswordChange(Authentication authentication,
                                                      @Valid @RequestBody com.devradar.dto.PasswordChangeRequest request) {
        authService.confirmPasswordChange(authentication.getName(), request.getNewPassword(), request.getCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/subscribe")
    public ResponseEntity<User> subscribe(Authentication authentication) {
        return ResponseEntity.ok(authService.subscribe(authentication.getName()));
    }

}
