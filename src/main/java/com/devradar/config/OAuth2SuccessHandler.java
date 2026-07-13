package com.devradar.config;

import com.devradar.model.User;
import com.devradar.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public OAuth2SuccessHandler(UserRepository userRepository,
                                @Lazy PasswordEncoder passwordEncoder,
                                JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        
        // Handle provider-specific login/email attributes
        String provider = "google";
        if (oAuth2User.getAttribute("login") != null) {
            provider = "github";
            if (name == null) {
                name = oAuth2User.getAttribute("login");
            }
            if (email == null) {
                email = oAuth2User.getAttribute("login") + "@github.com";
            }
        }

        if (email == null) {
            email = "oauth-user-" + UUID.randomUUID().toString() + "@devradar.com";
        }

        final String finalEmail = email;
        final String finalName = name != null ? name : "Geliştirici";

        User user = userRepository.findByEmail(finalEmail)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(finalEmail)
                            .fullName(finalName)
                            .password(passwordEncoder.encode("OAUTH_PASSWORD_" + UUID.randomUUID().toString()))
                            .credits(5)
                            .isPremium(false)
                            .build();
                    return userRepository.save(newUser);
                });

        String token = jwtUtil.generateToken(user.getEmail());

        String targetUrl = UriComponentsBuilder.fromUriString("/auth")
                .queryParam("token", token)
                .queryParam("email", user.getEmail())
                .queryParam("name", user.getFullName())
                .build().toUriString();

        response.sendRedirect(targetUrl);
    }
}
