package com.devradar.service;

import com.devradar.dto.*;
import com.devradar.model.Profile;
import com.devradar.model.User;
import com.devradar.repository.ProfileRepository;
import com.devradar.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final GeminiAIService aiService;
    private final ObjectMapper objectMapper;

    public ProfileService(ProfileRepository profileRepository,
                          UserRepository userRepository,
                          GeminiAIService aiService,
                          ObjectMapper objectMapper) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
        this.objectMapper = objectMapper;
    }

    public Profile getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return profileRepository.findByUserId(user.getId()).orElse(null);
    }

    public Profile saveProfile(String email, ProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElse(Profile.builder().userId(user.getId()).build());

        profile.setSpecialization(request.getSpecialization());
        try {
            profile.setLanguages(objectMapper.writeValueAsString(request.getLanguages()));
        } catch (JsonProcessingException e) {
            profile.setLanguages("[]");
        }
        profile.setExperienceYears(request.getExperienceYears());
        profile.setPreviousProjects(request.getPreviousProjects());
        profile.setGithubUrl(request.getGithubUrl());
        profile.setPreferredLanguage(request.getLanguage() != null ? request.getLanguage() : "tr");

        profile = profileRepository.save(profile);

        try {
            scoreProfile(email, profile.getPreferredLanguage());
            profile = profileRepository.findByUserId(user.getId()).orElse(profile);
        } catch (Exception e) {
            // AI scoring failed or was rate-limited - we ignore to ensure profile save is successful
        }

        return profile;
    }

    public ProfileScoreResponse scoreProfile(String email) {
        return scoreProfile(email, null);
    }

    public ProfileScoreResponse scoreProfile(String email, String language) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Önce profilinizi doldurun"));

        String lang = language != null ? language : profile.getPreferredLanguage();

        // Kredi kontrolü
        if (!Boolean.TRUE.equals(user.getIsPremium())) {
            if (user.getCredits() == null || user.getCredits() <= 0) {
                throw new RuntimeException("Yetersiz kredi! Profilinizi puanlamak için lütfen premium aboneliğe geçiş yapın.");
            }
        }

        ProfileScoreResponse scoreResponse = aiService.scoreProfile(profile, lang);

        // Krediyi azalt
        if (!Boolean.TRUE.equals(user.getIsPremium())) {
            user.setCredits(Math.max(0, user.getCredits() - 1));
            userRepository.save(user);
        }

        // Save AI results to profile
        profile.setAiScore(scoreResponse.getScore());
        profile.setAiSummary(scoreResponse.getSummary());
        try {
            profile.setAiStrengths(objectMapper.writeValueAsString(scoreResponse.getStrengths()));
            profile.setAiWeaknesses(objectMapper.writeValueAsString(scoreResponse.getWeaknesses()));
            profile.setAiRecommendations(objectMapper.writeValueAsString(scoreResponse.getRecommendations()));
        } catch (JsonProcessingException e) {
            // Silently handle — data still returned via response
        }
        profileRepository.save(profile);

        return scoreResponse;
    }
}
