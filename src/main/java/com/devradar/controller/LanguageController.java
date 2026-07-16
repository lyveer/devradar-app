package com.devradar.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Public endpoint that advertises the supported UI/output languages.
 * The frontend can call GET /api/languages on load to populate a language switcher.
 */
@RestController
@RequestMapping("/api/languages")
@CrossOrigin
public class LanguageController {

    private static final List<Map<String, String>> SUPPORTED = List.of(
            Map.of("code", "tr", "name", "Türkçe", "flag", "🇹🇷"),
            Map.of("code", "en", "name", "English", "flag", "🇬🇧"),
            Map.of("code", "de", "name", "Deutsch", "flag", "🇩🇪")
    );

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSupportedLanguages() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("default", "tr");
        response.put("languages", SUPPORTED);
        return ResponseEntity.ok(response);
    }
}
