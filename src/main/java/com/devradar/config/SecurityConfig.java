package com.devradar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @org.springframework.beans.factory.annotation.Value("${security.require-ssl:false}")
    private boolean requireSsl;

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, OAuth2SuccessHandler oAuth2SuccessHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (requireSsl) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
        }

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/announcements", "/api/stats").permitAll()
                .requestMatchers("/api/languages/**").permitAll()
                .requestMatchers("/", "/*.html", "/css/**", "/js/**", "/images/**", "/fonts/**").permitAll()
                .requestMatchers("/dashboard", "/auth", "/giris", "/panel").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/login/oauth2/**", "/oauth2/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/auth")
                .successHandler(oAuth2SuccessHandler)
            )
            .headers(headers -> {
                headers.xssProtection(xss -> xss.headerValue(org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK));
                headers.contentTypeOptions(ct -> {}); // Explicitly enable X-Content-Type-Options
                headers.frameOptions(fo -> fo.sameOrigin());
                headers.contentSecurityPolicy(csp -> csp.policyDirectives("upgrade-insecure-requests; frame-ancestors 'self'; script-src 'self' 'unsafe-eval' https://cdn.tailwindcss.com https://pagead2.googlesyndication.com; script-src-elem 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.tailwindcss.com https://pagead2.googlesyndication.com; script-src-attr 'unsafe-inline'; object-src 'none';"));
                headers.httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                    .preload(true)
                    .requestMatcher(request -> true) // Force HSTS even on HTTP / behind proxies without proper forwarding
                );
            })
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);



        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(4);
    }
}
