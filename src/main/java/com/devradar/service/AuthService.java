package com.devradar.service;

import com.devradar.config.JwtUtil;
import com.devradar.dto.*;
import com.devradar.model.User;
import com.devradar.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final com.devradar.repository.ProfileRepository profileRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       com.devradar.repository.ProfileRepository profileRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.profileRepository = profileRepository;
        this.emailService = emailService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu e-posta adresi zaten kayıtlı");
        }

        String verificationCode = String.format("%06d", new java.util.Random().nextInt(1000000));

        User user = User.builder()
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .credits(5)
                .isPremium(false)
                .isVerified(false)
                .verificationCode(verificationCode)
                .verificationCodeExpiresAt(java.time.LocalDateTime.now().plusMinutes(10))
                .build();

        userRepository.save(user);

        emailService.sendVerificationCode(user.getEmail(), verificationCode);

        return AuthResponse.builder()
                .token(null)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .isVerified(false)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Geçersiz e-posta veya şifre"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Geçersiz e-posta veya şifre");
        }

        if (!user.getIsVerified()) {
            throw new RuntimeException("EMAIL_NOT_VERIFIED: Lütfen e-posta adresinizi doğrulayın.");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .isVerified(true)
                .build();
    }

    public AuthResponse verify(String email, String code) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        if (user.getIsVerified()) {
            throw new RuntimeException("Bu hesap zaten doğrulanmış.");
        }

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(code)) {
            throw new RuntimeException("Geçersiz doğrulama kodu.");
        }

        if (user.getVerificationCodeExpiresAt() != null && user.getVerificationCodeExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Doğrulama kodunun süresi dolmuş. Lütfen yeni kod isteyin.");
        }

        user.setIsVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .isVerified(true)
                .build();
    }

    public void resendCode(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        if (user.getIsVerified()) {
            throw new RuntimeException("Bu hesap zaten doğrulanmış.");
        }

        String verificationCode = String.format("%06d", new java.util.Random().nextInt(1000000));
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiresAt(java.time.LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        emailService.sendVerificationCode(user.getEmail(), verificationCode);
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        String resetCode = String.format("%06d", new java.util.Random().nextInt(1000000));
        user.setVerificationCode(resetCode);
        user.setVerificationCodeExpiresAt(java.time.LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        emailService.sendPasswordResetCode(user.getEmail(), resetCode);
    }

    public void resetPassword(String email, String code, String newPassword) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(code)) {
            throw new RuntimeException("Geçersiz doğrulama kodu.");
        }

        if (user.getVerificationCodeExpiresAt() != null && user.getVerificationCodeExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Doğrulama kodunun süresi dolmuş. Lütfen tekrar deneyin.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setIsVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);
    }

    public AuthResponse updateProfile(String currentEmail, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // If trying to change password
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (request.getCode() == null || request.getCode().isBlank()) {
                String verificationCode = String.format("%06d", new java.util.Random().nextInt(1000000));
                user.setVerificationCode(verificationCode);
                user.setVerificationCodeExpiresAt(java.time.LocalDateTime.now().plusMinutes(10));
                userRepository.save(user);

                emailService.sendPasswordResetCode(user.getEmail(), verificationCode);
                throw new RuntimeException("PASSWORD_CHANGE_CODE_SENT: Şifre değişikliği için e-postanıza gönderilen doğrulama kodunu girin.");
            } else {
                if (user.getVerificationCode() == null || !user.getVerificationCode().equals(request.getCode())) {
                    throw new RuntimeException("Geçersiz doğrulama kodu.");
                }
                if (user.getVerificationCodeExpiresAt() != null && user.getVerificationCodeExpiresAt().isBefore(java.time.LocalDateTime.now())) {
                    throw new RuntimeException("Doğrulama kodunun süresi dolmuş. Lütfen tekrar deneyin.");
                }
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                user.setVerificationCode(null);
                user.setVerificationCodeExpiresAt(null);
            }
        }

        String newEmail = request.getEmail().trim().toLowerCase();
        if (!user.getEmail().equalsIgnoreCase(newEmail)) {
            if (userRepository.existsByEmail(newEmail)) {
                throw new RuntimeException("Bu e-posta adresi zaten başka bir kullanıcı tarafından kullanılıyor");
            }
            user.setEmail(newEmail);
        }

        user.setFullName(request.getFullName().trim());

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .isVerified(user.getIsVerified())
                .build();
    }

    public User getMe(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
    }

    public User subscribe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        user.setIsPremium(true);
        user.setCredits(99999);
        return userRepository.save(user);
    }

}
