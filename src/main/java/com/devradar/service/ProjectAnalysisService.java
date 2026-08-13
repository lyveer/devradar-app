package com.devradar.service;

import com.devradar.dto.*;
import com.devradar.model.ProjectAnalysis;
import com.devradar.model.User;
import com.devradar.repository.ProjectAnalysisRepository;
import com.devradar.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectAnalysisService {

    private final ProjectAnalysisRepository analysisRepository;
    private final UserRepository userRepository;
    private final GeminiAIService aiService;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Value("${devradar.free.daily.limit:5}")
    private int freeDailyLimit;

    public ProjectAnalysisService(ProjectAnalysisRepository analysisRepository,
                                  UserRepository userRepository,
                                  GeminiAIService aiService,
                                  ObjectMapper objectMapper) {
        this.analysisRepository = analysisRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
        this.objectMapper = objectMapper;
    }

    public ProjectAnalysisResponse analyzeProject(String email, ProjectAnalysisRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        String resolvedProvider;

        if (!Boolean.TRUE.equals(user.getIsPremium())) {
            // Daily limit check for free tier
            java.time.LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
            long todayCount = analysisRepository.countByUserIdAndCreatedAtGreaterThanEqual(user.getId(), startOfDay);
            if (todayCount >= freeDailyLimit) {
                throw new RuntimeException("Günlük analiz limitinize ulaştınız (" + freeDailyLimit + "). Daha fazla analiz için lütfen Premium üyeliğe yükseltin.");
            }

            // Kredi kontrolü
            if (user.getCredits() == null || user.getCredits() <= 0) {
                throw new RuntimeException("Yetersiz kredi! Analiz yapabilmek için lütfen premium aboneliğe geçiş yapın.");
            }

            // Free tier users are restricted to Groq
            resolvedProvider = "groq";
        } else {
            // Premium tier users can select gemini or claude, default to gemini
            String requestedProvider = request.getAiProvider();
            if (requestedProvider != null && (requestedProvider.equalsIgnoreCase("gemini") || requestedProvider.equalsIgnoreCase("claude"))) {
                resolvedProvider = requestedProvider.toLowerCase();
            } else {
                resolvedProvider = "gemini";
            }
        }

        ProjectAnalysisResponse response = aiService.analyzeProject(
                request.getProjectName(),
                request.getProjectDescription(),
                request.getTargetLanguage(),
                request.getLanguage(),
                resolvedProvider
        );

        // Krediyi azalt (Only for free tier users)
        if (!Boolean.TRUE.equals(user.getIsPremium())) {
            user.setCredits(Math.max(0, user.getCredits() - 1));
            userRepository.save(user);
        }

        // Save analysis to DB
        ProjectAnalysis analysis = ProjectAnalysis.builder()
                .userId(user.getId())
                .projectName(request.getProjectName())
                .projectDescription(request.getProjectDescription())
                .targetLanguage(request.getTargetLanguage())
                .completedSteps("")
                .build();

        try {
            analysis.setAiAnalysisResult(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            analysis.setAiAnalysisResult("{}");
        }

        analysis = analysisRepository.save(analysis);
        response.setId(analysis.getId());
        response.setProjectName(analysis.getProjectName());
        response.setTargetLanguage(analysis.getTargetLanguage());
        response.setCreatedAt(analysis.getCreatedAt().toString());
        response.setCompletedSteps(analysis.getCompletedSteps());

        return response;
    }

    public List<ProjectAnalysisResponse> getHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        return analysisRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(analysis -> {
                    try {
                        ProjectAnalysisResponse response = objectMapper.readValue(
                                analysis.getAiAnalysisResult(), ProjectAnalysisResponse.class);
                        response.setId(analysis.getId());
                        response.setProjectName(analysis.getProjectName());
                        response.setTargetLanguage(analysis.getTargetLanguage());
                        response.setCreatedAt(analysis.getCreatedAt().toString());
                        response.setCompletedSteps(analysis.getCompletedSteps());
                        return response;
                    } catch (Exception e) {
                        return ProjectAnalysisResponse.builder()
                                .id(analysis.getId())
                                .projectName(analysis.getProjectName())
                                .targetLanguage(analysis.getTargetLanguage())
                                .createdAt(analysis.getCreatedAt().toString())
                                .completedSteps(analysis.getCompletedSteps())
                                .build();
                    }
                })
                .collect(Collectors.toList());
    }

    public void updateCompletedSteps(String email, Long analysisId, List<Integer> steps) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        ProjectAnalysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("Analiz bulunamadı"));

        if (!analysis.getUserId().equals(user.getId())) {
            throw new RuntimeException("Yetkisiz işlem");
        }

        String stepsStr = steps.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        analysis.setCompletedSteps(stepsStr);
        analysisRepository.save(analysis);
    }
}
