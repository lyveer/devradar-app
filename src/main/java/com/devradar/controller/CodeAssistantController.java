package com.devradar.controller;

import com.devradar.dto.CodeAssistantRequest;
import com.devradar.dto.CodeAssistantResponse;
import com.devradar.model.User;
import com.devradar.repository.UserRepository;
import com.devradar.service.GeminiAIService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/code-assistant")
@CrossOrigin
public class CodeAssistantController {

    private final UserRepository userRepository;
    private final GeminiAIService aiService;

    public CodeAssistantController(UserRepository userRepository, GeminiAIService aiService) {
        this.userRepository = userRepository;
        this.aiService = aiService;
    }

    @PostMapping
    public ResponseEntity<CodeAssistantResponse> askAssistant(Authentication authentication,
                                                              @Valid @RequestBody CodeAssistantRequest request) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        String resolvedProvider;
        if (!Boolean.TRUE.equals(user.getIsPremium())) {
            // Free members are restricted to Groq
            resolvedProvider = "groq";

            // Credit validation
            if (user.getCredits() == null || user.getCredits() <= 0) {
                throw new RuntimeException("Yetersiz kredi! Kod Asistanını kullanabilmek için lütfen premium aboneliğe geçiş yapın.");
            }
        } else {
            // Premium members can choose gemini, claude, chatgpt, or groq
            String reqProvider = request.getAiProvider();
            if (reqProvider != null && (reqProvider.equalsIgnoreCase("gemini") 
                    || reqProvider.equalsIgnoreCase("claude") 
                    || reqProvider.equalsIgnoreCase("chatgpt") 
                    || reqProvider.equalsIgnoreCase("openai")
                    || reqProvider.equalsIgnoreCase("groq"))) {
                resolvedProvider = reqProvider.toLowerCase();
            } else {
                resolvedProvider = "gemini";
            }
        }

        // Call AI Service
        com.devradar.service.GeminiAIService.GeminiCallResult aiResult = aiService.generateCode(request.getPrompt(), resolvedProvider, request.getLanguage());
        String responseText = aiResult.success ? aiResult.text : aiResult.errorReason;

        // Deduct credit for free tier
        if (!Boolean.TRUE.equals(user.getIsPremium())) {
            user.setCredits(Math.max(0, user.getCredits() - 1));
            userRepository.save(user);
        }

        return ResponseEntity.ok(CodeAssistantResponse.builder()
                .response(responseText)
                .aiProviderUsed(aiResult.providerUsed != null ? aiResult.providerUsed : resolvedProvider)
                .modelUsed(aiResult.modelUsed != null ? aiResult.modelUsed : "unknown")
                .remainingCredits(user.getCredits())
                .build());
    }
}
