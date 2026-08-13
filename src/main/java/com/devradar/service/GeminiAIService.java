package com.devradar.service;

import com.devradar.dto.ProfileScoreResponse;
import com.devradar.dto.ProjectAnalysisResponse;
import com.devradar.model.Profile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GeminiAIService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAIService.class);

    // ---- Per-provider API keys ----
    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${claude.api.key:}")
    private String claudeApiKey;

    @Value("${groq.api.key:}")
    private String groqApiKey;

    @Value("${perplexity.api.key:}")
    private String perplexityApiKey;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    // ---- Active provider selection ----
    // Allowed values: gemini | claude | groq | perplexity | chatgpt
    @Value("${ai.provider:gemini}")
    private String aiProvider;

    // ---- Model names (per provider) ----
    // NOTE: "gemini-2.0-flash" was shut down by Google on 2026-06-01 and now returns HTTP 404
    // for every request. "gemini-flash-latest" is a self-updating alias maintained by Google
    // that always points at the current recommended Flash model.
    @Value("${gemini.model:gemini-flash-latest}")
    private String geminiModel;

    @Value("${claude.model:claude-3-haiku-20240307}")
    private String claudeModel;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String groqModel;

    @Value("${perplexity.model:sonar}")
    private String perplexityModel;

    @Value("${openai.model:gpt-4o-mini}")
    private String openaiModel;

    @Value("${gemini.timeout.connect-ms:5000}")
    private int connectTimeoutMs;

    @Value("${gemini.timeout.read-ms:20000}")
    private int readTimeoutMs;

    private final ObjectMapper objectMapper;
    private RestTemplate restTemplate;

    public GeminiAIService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    private void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        this.restTemplate = new RestTemplate(factory);

        // Startup diagnostics — clearly show which provider is active.
        String activeKey = getActiveApiKey();
        if (activeKey == null || activeKey.isBlank()) {
            log.warn("=================================================================");
            log.warn(" NO API KEY CONFIGURED for provider '{}'.", aiProvider);
            log.warn(" DevRadar will run in MOCK MODE.");
            log.warn(" Set the matching key in application.properties:");
            log.warn("   gemini.api.key   -> provider=gemini");
            log.warn("   claude.api.key   -> provider=claude");
            log.warn("   groq.api.key     -> provider=groq");
            log.warn("   perplexity.api.key -> provider=perplexity");
            log.warn("=================================================================");
        } else {
            log.info("=================================================================");
            log.info(" AI provider: '{}', key length={}", aiProvider, activeKey.length());
            log.info("=================================================================");
        }
    }

    /** Returns the API key for the currently active provider, or null/blank if not set. */
    private String getActiveApiKey() {
        return switch (aiProvider.toLowerCase().trim()) {
            case "claude"     -> claudeApiKey;
            case "groq"       -> groqApiKey;
            case "perplexity" -> perplexityApiKey;
            case "chatgpt"    -> openaiApiKey;
            case "openai"     -> openaiApiKey;
            default           -> apiKey; // gemini
        };
    }

    private boolean isAnyProviderConfigured() {
        String key = getActiveApiKey();
        return key != null && !key.isBlank();
    }

    // Kept for internal Gemini-specific checks
    private boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    private String normalizeLanguage(String language) {
        if (language == null) return "tr";
        String l = language.trim().toLowerCase();
        if (l.startsWith("en")) return "en";
        if (l.startsWith("de")) return "de";
        return "tr";
    }

    // ========== PROFILE SCORING ==========

    public ProfileScoreResponse scoreProfile(Profile profile, String language) {
        String lang = normalizeLanguage(language);

        if (!isAnyProviderConfigured()) {
            return getMockProfileScore(profile, lang);
        }

        String prompt = buildProfileScoringPrompt(profile, lang);
        GeminiCallResult result = callGemini(prompt);

        if (!result.success) {
            log.warn("Falling back to mock profile score because the Gemini call failed: {}", result.errorReason);
            return getMockProfileScore(profile, lang);
        }

        try {
            String json = extractJson(result.text);
            JsonNode node = objectMapper.readTree(json);

            return ProfileScoreResponse.builder()
                    .score(node.path("score").asInt(50))
                    .summary(node.path("summary").asText(""))
                    .strengths(jsonArrayToList(node.path("strengths")))
                    .weaknesses(jsonArrayToList(node.path("weaknesses")))
                    .recommendations(jsonArrayToList(node.path("recommendations")))
                    .aiPowered(true)
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse AI profile score response. Raw text was: {}", result.text, e);
            return getMockProfileScore(profile, lang);
        }
    }

    // Backward-compatible overload (defaults to Turkish) so existing callers keep compiling.
    public ProfileScoreResponse scoreProfile(Profile profile) {
        return scoreProfile(profile, "tr");
    }

    // ========== PROJECT ANALYSIS ==========

    public ProjectAnalysisResponse analyzeProject(String projectName, String description, String targetLanguage, String responseLanguage, String resolvedProvider) {
        String lang = normalizeLanguage(responseLanguage);

        if (!isAnyProviderConfigured()) {
            return getMockProjectAnalysis(projectName, description, targetLanguage, lang);
        }

        String prompt = buildProjectAnalysisPrompt(projectName, description, targetLanguage, lang);
        GeminiCallResult result = callProvider(resolvedProvider, prompt);

        if (!result.success) {
            log.warn("Falling back to mock project analysis because the dynamic provider call failed: {}", result.errorReason);
            return getMockProjectAnalysis(projectName, description, targetLanguage, lang);
        }

        try {
            String json = extractJson(result.text);
            JsonNode node = objectMapper.readTree(json);
            if (node.isArray() && node.size() > 0) {
                node = node.get(0);
            }

            // Parse market price range
            JsonNode priceNode = node.path("marketPriceRange");
            ProjectAnalysisResponse.PriceRange marketPrice = ProjectAnalysisResponse.PriceRange.builder()
                    .min(priceNode.path("min").asInt(5000))
                    .max(priceNode.path("max").asInt(25000))
                    .currency(priceNode.path("currency").asText("USD"))
                    .build();

            // Parse freelancer income
            JsonNode incomeNode = node.path("freelancerIncome");
            JsonNode hourlyNode = incomeNode.path("hourlyRate");
            JsonNode projectNode = incomeNode.path("projectBased");

            ProjectAnalysisResponse.FreelancerIncome income = ProjectAnalysisResponse.FreelancerIncome.builder()
                    .hourlyRate(ProjectAnalysisResponse.PriceRange.builder()
                            .min(hourlyNode.path("min").asInt(25))
                            .max(hourlyNode.path("max").asInt(75))
                            .currency("USD").build())
                    .projectBased(ProjectAnalysisResponse.PriceRange.builder()
                            .min(projectNode.path("min").asInt(3000))
                            .max(projectNode.path("max").asInt(15000))
                            .currency("USD").build())
                    .build();

            // Parse development time
            JsonNode timeNode = node.path("estimatedDevelopmentTime");
            ProjectAnalysisResponse.DevelopmentTime devTime = ProjectAnalysisResponse.DevelopmentTime.builder()
                    .minWeeks(timeNode.path("minWeeks").asInt(4))
                    .maxWeeks(timeNode.path("maxWeeks").asInt(12))
                    .description(timeNode.path("description").asText(""))
                    .build();

            // Parse tech stack
            List<ProjectAnalysisResponse.TechRecommendation> techStack = new ArrayList<>();
            if (node.has("recommendedTechStack")) {
                for (JsonNode tech : node.get("recommendedTechStack")) {
                    techStack.add(ProjectAnalysisResponse.TechRecommendation.builder()
                            .name(tech.path("name").asText(""))
                            .purpose(tech.path("purpose").asText(""))
                            .build());
                }
            }

            // Parse enhancements
            List<ProjectAnalysisResponse.Enhancement> enhancements = new ArrayList<>();
            if (node.has("enhancements")) {
                for (JsonNode enh : node.get("enhancements")) {
                    enhancements.add(ProjectAnalysisResponse.Enhancement.builder()
                            .title(enh.path("title").asText(""))
                            .description(enh.path("description").asText(""))
                            .build());
                }
            }

            // Parse tips
            List<ProjectAnalysisResponse.Tip> tips = new ArrayList<>();
            if (node.has("tips")) {
                for (JsonNode tip : node.get("tips")) {
                    tips.add(ProjectAnalysisResponse.Tip.builder()
                            .title(tip.path("title").asText(""))
                            .description(tip.path("description").asText(""))
                            .build());
                }
            }

            // Parse competitors
            List<ProjectAnalysisResponse.CompetitorExample> competitors = new ArrayList<>();
            if (node.has("competitors")) {
                for (JsonNode comp : node.get("competitors")) {
                    competitors.add(ProjectAnalysisResponse.CompetitorExample.builder()
                            .name(comp.path("name").asText(""))
                            .url(comp.path("url").asText(""))
                            .build());
                }
            }

            // Parse freelancer platforms
            List<ProjectAnalysisResponse.FreelancerPlatform> freelancerPlatforms = new ArrayList<>();
            if (node.has("freelancerPlatforms")) {
                for (JsonNode plat : node.get("freelancerPlatforms")) {
                    freelancerPlatforms.add(ProjectAnalysisResponse.FreelancerPlatform.builder()
                            .name(plat.path("name").asText(""))
                            .estimatedPrice(plat.path("estimatedPrice").asText(""))
                            .url(plat.path("url").asText(""))
                            .build());
                }
            }

            // Parse project resources
            List<ProjectAnalysisResponse.ProjectResource> projectResources = new ArrayList<>();
            if (node.has("projectResources")) {
                for (JsonNode res : node.get("projectResources")) {
                    projectResources.add(ProjectAnalysisResponse.ProjectResource.builder()
                            .title(res.path("title").asText(""))
                            .url(res.path("url").asText(""))
                            .build());
                }
            }

            // Parse code snippets
            List<ProjectAnalysisResponse.CodeSnippet> codeSnippets = new ArrayList<>();
            if (node.has("codeSnippets")) {
                for (JsonNode snip : node.get("codeSnippets")) {
                    codeSnippets.add(ProjectAnalysisResponse.CodeSnippet.builder()
                            .title(snip.path("title").asText(""))
                            .code(snip.path("code").asText(""))
                            .language(snip.path("language").asText(""))
                            .build());
                }
            }

            String codeRecommendation = getCodeRecommendation(projectName, description, targetLanguage, lang, resolvedProvider);

            return ProjectAnalysisResponse.builder()
                    .projectName(projectName)
                    .targetLanguage(targetLanguage)
                    .marketPriceRange(marketPrice)
                    .freelancerIncome(income)
                    .demandLevel(node.path("demandLevel").asText("HIGH"))
                    .demandDescription(node.path("demandDescription").asText(""))
                    .estimatedDevelopmentTime(devTime)
                    .recommendedTechStack(techStack)
                    .enhancements(enhancements)
                    .tips(tips)
                    .competitorInsight(node.path("competitorInsight").asText(""))
                    .competitors(competitors)
                    .freelancerPlatforms(freelancerPlatforms)
                    .projectResources(projectResources)
                    .codeSnippets(codeSnippets)
                    .aiPowered(true)
                    .codeRecommendation(codeRecommendation)
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse AI project analysis response. Raw text was: {}", result.text, e);
            return getMockProjectAnalysis(projectName, description, targetLanguage, lang);
        }
    }

    public ProjectAnalysisResponse analyzeProject(String projectName, String description, String targetLanguage, String responseLanguage) {
        return analyzeProject(projectName, description, targetLanguage, responseLanguage, aiProvider);
    }

    // Backward-compatible overload (defaults to Turkish) so existing callers keep compiling.
    public ProjectAnalysisResponse analyzeProject(String projectName, String description, String targetLanguage) {
        return analyzeProject(projectName, description, targetLanguage, "tr");
    }

    public GeminiCallResult generateCode(String userPrompt, String provider, String responseLanguage) {
        String lang = normalizeLanguage(responseLanguage);
        boolean en = "en".equals(lang);
        String systemInstruction = en
                ? "You are DevRadar AI, a skilled and friendly software development assistant. Help developers build projects, write code, debug issues, explain concepts, and suggest best practices. Be natural and conversational — not just a code generator. When writing code, use markdown code blocks with the language specified (```python, ```javascript etc). Answer questions thoroughly and suggest alternatives when helpful. IMPORTANT: NEVER return your response in JSON format. Always reply in raw, plain text using markdown formatting. Before providing your final answer, ALWAYS wrap your step-by-step reasoning and plan inside <thinking> ... </thinking> XML tags."
                : "Sen DevRadar AI'sın — deneyimli ve samimi bir yazılım geliştirme asistanısın. Geliştiricilere proje geliştirme, kod yazma, hata ayıklama, kavram açıklama ve mimari konularda yardım ediyorsun. Doğal ve akıcı bir şekilde sohbet et; sadece kod üretmekle sınırlı değilsin. Kod yazarken dil belirtilmiş markdown kod blokları kullan (```python, ```javascript vb.). Sorulara detaylı yanıt ver, gerektiğinde alternatif yaklaşımlar öner. ÖNEMLİ: YANITINI ASLA JSON FORMATINDA DÖNDÜRME! Her zaman doğrudan düz metin ve markdown kullanarak cevap ver. Nihai cevabını yazmadan önce HER ZAMAN adım adım düşünce sürecini ve planını <thinking> ... </thinking> XML etiketleri içerisine yaz.";

        String fullPrompt = systemInstruction + "\n\n" + userPrompt;

        if (!isAnyProviderConfigured()) {
            return GeminiCallResult.ok(en 
                ? "Mock Code Assistant: Here is a sample code for your request. Add an API key to enable live generation.\n\n```javascript\n// Sample placeholder code\nconsole.log(\"Hello from DevRadar AI!\");\n```"
                : "Mock Kod Asistanı: İsteğiniz için örnek kod aşağıdadır. Gerçek yanıtlar almak için lütfen API anahtarlarını ayarlayın.\n\n```javascript\n// Örnek kod şablonu\nconsole.log(\"DevRadar AI'dan Merhaba!\");\n```", "MOCK", "mock-model");
        }

        GeminiCallResult result = callProvider(provider, fullPrompt);
        if (!result.success) {
            String error = en
                ? "Error: AI assistant failed to respond: " + result.errorReason
                : "Hata: Yapay zeka yanıt üretemedi: " + result.errorReason;
            return GeminiCallResult.fail(error, result.providerUsed != null ? result.providerUsed : provider, result.modelUsed);
        }

        return result;
    }

    private String getCodeRecommendation(String projectName, String description, String targetLanguage, String lang, String resolvedProvider) {
        String prompt = buildCodeRecommendationPrompt(projectName, description, targetLanguage, lang);

        if (isAnyProviderConfigured()) {
            GeminiCallResult result = callProvider(resolvedProvider, prompt);
            if (result.success) {
                return result.text;
            }
            log.warn("Code recommendation call failed (provider={}): {}", resolvedProvider, result.errorReason);
        }

        return getMockCodeRecommendation(targetLanguage, lang);
    }

    private String getCodeRecommendation(String projectName, String description, String targetLanguage, String lang) {
        return getCodeRecommendation(projectName, description, targetLanguage, lang, aiProvider);
    }

    // ========== GEMINI API CALL ==========

    /** Small result wrapper so callers can tell "AI succeeded" apart from "AI failed, this is a placeholder". */
    public static class GeminiCallResult {
        public final boolean success;
        public final String text;
        public final String errorReason;
        public final String providerUsed;
        public final String modelUsed;

        private GeminiCallResult(boolean success, String text, String errorReason, String providerUsed, String modelUsed) {
            this.success = success;
            this.text = text;
            this.errorReason = errorReason;
            this.providerUsed = providerUsed;
            this.modelUsed = modelUsed;
        }

        static GeminiCallResult ok(String text, String providerUsed, String modelUsed) {
            return new GeminiCallResult(true, text, null, providerUsed, modelUsed);
        }

        static GeminiCallResult fail(String reason, String providerUsed, String modelUsed) {
            return new GeminiCallResult(false, null, reason, providerUsed, modelUsed);
        }
    }

    /**
     * Main dispatcher — routes to the correct AI backend based on dynamic provider selection.
     * Providers: gemini | claude | groq | perplexity
     */
    private GeminiCallResult callProvider(String provider, String prompt) {
        String resolvedProvider = provider != null ? provider.toLowerCase().trim() : aiProvider.toLowerCase().trim();
        
        GeminiCallResult result;
        switch (resolvedProvider) {
            case "claude": result = callClaude(prompt); break;
            case "groq": result = callGroq(prompt); break;
            case "perplexity": result = callPerplexity(prompt); break;
            case "chatgpt":
            case "openai": result = callOpenAI(prompt); break;
            default: result = callGeminiInternal(prompt, geminiModel, true); break; // gemini
        }

        // Automatic Fallback System: If primary model fails, fallback to Groq if key is available
        if (!result.success && !resolvedProvider.equals("groq") && groqApiKey != null && !groqApiKey.isBlank()) {
            log.warn("Primary AI Provider ({}) failed. Error: {}. Falling back to GROQ.", resolvedProvider, result.errorReason);
            GeminiCallResult fallbackResult = callGroq(prompt);
            if (fallbackResult.success) {
                return fallbackResult;
            }
        }
        
        return result;
    }

    private GeminiCallResult callGemini(String prompt) {
        return callProvider(aiProvider, prompt);
    }

    private GeminiCallResult callGeminiInternal(String prompt, String resolvedModel, boolean useSearch) {
        String url = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                resolvedModel, apiKey);

        Map<String, Object> body;
        if (useSearch) {
            body = Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", prompt))
                    )),
                    "tools", List.of(Map.of("google_search", Map.of()))
            );
        } else {
            body = Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", prompt))
                    ))
            );
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, JsonNode.class);
            JsonNode responseBody = response.getBody();

            if (responseBody != null && responseBody.has("candidates")
                    && responseBody.get("candidates").size() > 0) {
                JsonNode candidate = responseBody.get("candidates").get(0);

                String finishReason = candidate.path("finishReason").asText("");
                if (!candidate.has("content") || !candidate.get("content").has("parts")) {
                    return GeminiCallResult.fail("Gemini returned no content (finishReason=" + finishReason + ")", "GEMINI", resolvedModel);
                }

                String text = candidate.get("content").get("parts").get(0).path("text").asText("");
                if (text.isBlank()) {
                    return GeminiCallResult.fail("Gemini returned an empty text part (finishReason=" + finishReason + ")", "GEMINI", resolvedModel);
                }
                return GeminiCallResult.ok(text, "GEMINI", resolvedModel);
            }

            if (responseBody != null && responseBody.has("promptFeedback")) {
                return GeminiCallResult.fail("Prompt was blocked: " + responseBody.get("promptFeedback"), "GEMINI", resolvedModel);
            }

            return GeminiCallResult.fail("Gemini response had no candidates. Full body: " + responseBody, "GEMINI", resolvedModel);

        } catch (RestClientResponseException e) {
            log.error("Gemini API call failed with HTTP {} — body: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            if (useSearch) {
                log.warn("Retrying Gemini API call without search grounding tool...");
                return callGeminiInternal(prompt, resolvedModel, false);
            }
            return GeminiCallResult.fail("HTTP " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString(), "GEMINI", resolvedModel);
        } catch (Exception e) {
            log.error("Gemini API call failed unexpectedly", e);
            if (useSearch) {
                log.warn("Retrying Gemini API call without search grounding tool...");
                return callGeminiInternal(prompt, resolvedModel, false);
            }
            return GeminiCallResult.fail(e.getClass().getSimpleName() + ": " + e.getMessage(), "GEMINI", resolvedModel);
        }
    }

    private GeminiCallResult callGroq(String prompt) {
        String url = "https://api.groq.com/openai/v1/chat/completions";

        Map<String, Object> message = Map.of("role", "user", "content", prompt);
        Map<String, Object> body = Map.of(
                "model", groqModel,
                "messages", List.of(message)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + groqApiKey.trim());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, JsonNode.class);
            JsonNode responseBody = response.getBody();

            if (responseBody != null && responseBody.has("choices")
                    && responseBody.get("choices").size() > 0) {
                JsonNode choice = responseBody.get("choices").get(0);
                String text = choice.path("message").path("content").asText("");
                if (text.isBlank()) {
                    return GeminiCallResult.fail("Groq returned an empty text part", "GROQ", groqModel);
                }
                return GeminiCallResult.ok(text, "GROQ", groqModel);
            }
            return GeminiCallResult.fail("Groq response had no choices. Full body: " + responseBody, "GROQ", groqModel);
        } catch (RestClientResponseException e) {
            log.error("Groq API call failed with HTTP {} — body: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            return GeminiCallResult.fail("HTTP " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString(), "GROQ", groqModel);
        } catch (Exception e) {
            log.error("Groq API call failed unexpectedly", e);
            return GeminiCallResult.fail(e.getClass().getSimpleName() + ": " + e.getMessage(), "GROQ", groqModel);
        }
    }

    private GeminiCallResult callOpenAI(String prompt) {
        if (openaiApiKey == null || openaiApiKey.isBlank()) {
            log.warn("OpenAI API key is missing, using mock/placeholder coding assistance response.");
            return GeminiCallResult.ok("Mock OpenAI Response: OpenAI API key is missing. Please set 'openai.api.key' in application.properties to enable ChatGPT.", "OPENAI", openaiModel);
        }
        String url = "https://api.openai.com/v1/chat/completions";

        Map<String, Object> message = Map.of("role", "user", "content", prompt);
        Map<String, Object> body = Map.of(
                "model", openaiModel,
                "messages", List.of(message)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + openaiApiKey.trim());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, JsonNode.class);
            JsonNode responseBody = response.getBody();

            if (responseBody != null && responseBody.has("choices")
                    && responseBody.get("choices").size() > 0) {
                JsonNode choice = responseBody.get("choices").get(0);
                String text = choice.path("message").path("content").asText("");
                if (text.isBlank()) {
                    return GeminiCallResult.fail("OpenAI returned an empty text part", "OPENAI", openaiModel);
                }
                return GeminiCallResult.ok(text, "OPENAI", openaiModel);
            }
            return GeminiCallResult.fail("OpenAI response had no choices. Full body: " + responseBody, "OPENAI", openaiModel);
        } catch (RestClientResponseException e) {
            log.error("OpenAI API call failed with HTTP {} — body: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            return GeminiCallResult.fail("HTTP " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString(), "OPENAI", openaiModel);
        } catch (Exception e) {
            log.error("OpenAI API call failed unexpectedly", e);
            return GeminiCallResult.fail(e.getClass().getSimpleName() + ": " + e.getMessage(), "OPENAI", openaiModel);
        }
    }

    private GeminiCallResult callPerplexity(String prompt) {
        String url = "https://api.perplexity.ai/chat/completions";

        Map<String, Object> message = Map.of("role", "user", "content", prompt);
        Map<String, Object> body = Map.of(
                "model", perplexityModel,
                "messages", List.of(message)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + perplexityApiKey.trim());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, JsonNode.class);
            JsonNode responseBody = response.getBody();

            if (responseBody != null && responseBody.has("choices")
                    && responseBody.get("choices").size() > 0) {
                JsonNode choice = responseBody.get("choices").get(0);
                String text = choice.path("message").path("content").asText("");
                if (text.isBlank()) {
                    return GeminiCallResult.fail("Perplexity returned an empty text part", "PERPLEXITY", perplexityModel);
                }
                return GeminiCallResult.ok(text, "PERPLEXITY", perplexityModel);
            }
            return GeminiCallResult.fail("Perplexity response had no choices. Full body: " + responseBody, "PERPLEXITY", perplexityModel);
        } catch (RestClientResponseException e) {
            log.error("Perplexity API call failed with HTTP {} — body: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            return GeminiCallResult.fail("HTTP " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString(), "PERPLEXITY", perplexityModel);
        } catch (Exception e) {
            log.error("Perplexity API call failed unexpectedly", e);
            return GeminiCallResult.fail(e.getClass().getSimpleName() + ": " + e.getMessage(), "PERPLEXITY", perplexityModel);
        }
    }

    private GeminiCallResult callClaude(String prompt) {
        String url = "https://api.anthropic.com/v1/messages";

        Map<String, Object> message = Map.of("role", "user", "content", prompt);
        Map<String, Object> body = Map.of(
                "model", claudeModel,
                "max_tokens", 4000,
                "messages", List.of(message)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", claudeApiKey.trim());
        headers.set("anthropic-version", "2023-06-01");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, JsonNode.class);
            JsonNode responseBody = response.getBody();

            if (responseBody != null && responseBody.has("content") && responseBody.get("content").size() > 0) {
                JsonNode contentNode = responseBody.get("content").get(0);
                String text = contentNode.path("text").asText("");
                if (text.isBlank()) {
                    return GeminiCallResult.fail("Claude returned an empty text part", "CLAUDE", claudeModel);
                }
                return GeminiCallResult.ok(text, "CLAUDE", claudeModel);
            }
            return GeminiCallResult.fail("Claude response had no content. Full body: " + responseBody, "CLAUDE", claudeModel);
        } catch (RestClientResponseException e) {
            log.error("Claude API call failed with HTTP {} — body: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            return GeminiCallResult.fail("HTTP " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString(), "CLAUDE", claudeModel);
        } catch (Exception e) {
            log.error("Claude API call failed unexpectedly", e);
            return GeminiCallResult.fail(e.getClass().getSimpleName() + ": " + e.getMessage(), "CLAUDE", claudeModel);
        }
    }

    // ========== HELPERS ==========

    private String extractJson(String text) {
        text = text.trim();
        if (text.startsWith("```json")) {
            text = text.substring(7);
        } else if (text.startsWith("```")) {
            text = text.substring(3);
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }
        return text.trim();
    }

    private List<String> jsonArrayToList(JsonNode arrayNode) {
        List<String> list = new ArrayList<>();
        if (arrayNode != null && arrayNode.isArray()) {
            for (JsonNode item : arrayNode) {
                list.add(item.asText());
            }
        }
        return list;
    }

    // ========== PROMPTS ==========

    private String buildProfileScoringPrompt(Profile profile, String lang) {
        boolean en = "en".equals(lang);
        String outputLangInstruction = en
                ? "Respond ONLY in English."
                : "SADECE Türkçe yanıt ver.";

        return String.format("""
                You are an expert software developer profile evaluator. Analyze the developer profile below and give a score from 0-100.
                %s

                Developer Profile:
                - Specialization: %s
                - Programming Languages: %s
                - Years of Experience: %d
                - Previous Projects: %s
                - GitHub: %s

                Respond with ONLY a valid JSON object, nothing else:
                {
                  "score": <number 0-100>,
                  "summary": "<2-3 sentence overall assessment>",
                  "strengths": ["<strength 1>", "<strength 2>", "<strength 3>"],
                  "weaknesses": ["<weakness 1>", "<weakness 2>"],
                  "recommendations": ["<recommendation 1>", "<recommendation 2>", "<recommendation 3>"]
                }
                """,
                outputLangInstruction,
                profile.getSpecialization(),
                profile.getLanguages(),
                profile.getExperienceYears() != null ? profile.getExperienceYears() : 0,
                profile.getPreviousProjects() != null ? profile.getPreviousProjects() : (en ? "Not specified" : "Belirtilmemiş"),
                profile.getGithubUrl() != null ? profile.getGithubUrl() : (en ? "Not specified" : "Belirtilmemiş"));
    }

    private String buildProjectAnalysisPrompt(String projectName, String description, String targetLanguage, String lang) {
        boolean en = "en".equals(lang);
        String outputLangInstruction = en
                ? "Respond ONLY in English."
                : "SADECE Türkçe yanıt ver.";

        return String.format("""
                You are an expert software project analyst and market researcher. Analyze the project idea below and provide comprehensive market insights. Prices should be in USD.
                %s

                Project: %s
                Description: %s
                Target Technology/Language: %s

                Respond with ONLY a single valid JSON object, NOT wrapped in a JSON array:
                {
                  "marketPriceRange": {"min": <number>, "max": <number>, "currency": "USD"},
                  "freelancerIncome": {
                    "hourlyRate": {"min": <number>, "max": <number>},
                    "projectBased": {"min": <number>, "max": <number>}
                  },
                  "demandLevel": "<LOW|MEDIUM|HIGH|VERY_HIGH>",
                  "demandDescription": "<current market demand description>",
                  "estimatedDevelopmentTime": {
                    "minWeeks": <number>,
                    "maxWeeks": <number>,
                    "description": "<detailed time breakdown>"
                  },
                  "recommendedTechStack": [
                    {"name": "<technology name>", "purpose": "<why this technology>"}
                  ],
                  "enhancements": [
                    {"title": "<enhancement idea>", "description": "<how to implement it>"}
                  ],
                  "tips": [
                    {"title": "<tip title>", "description": "<detailed tip>"}
                  ],
                  "competitorInsight": "<brief competitive analysis>",
                  "competitors": [
                    {"name": "<competitor/example product name>"}
                  ],
                  "projectResources": [
                    {"title": "<resource title, e.g. Official Documentation or Tutorial>", "url": "<valid url related to target technology>"}
                  ],
                  "codeSnippets": [
                    {"title": "<e.g. App setup skeleton or Config template>", "code": "<clean code snippet>", "language": "<e.g. java, python, javascript>"}
                  ]
                }
                Do not include any URLs or web links in the text fields except inside the projectResources section.
                """,
                outputLangInstruction, projectName, description, targetLanguage);
    }

    // ========== MOCK DATA (used when API key is missing or the Gemini call fails) ==========

    private ProfileScoreResponse getMockProfileScore(Profile profile, String lang) {
        boolean en = "en".equals(lang);

        int baseScore = 50;
        if (profile.getExperienceYears() != null) {
            baseScore += Math.min(profile.getExperienceYears() * 5, 25);
        }
        if (profile.getLanguages() != null && profile.getLanguages().length() > 5) {
            baseScore += 10;
        }
        if (profile.getGithubUrl() != null && !profile.getGithubUrl().isBlank()) {
            baseScore += 5;
        }
        if (profile.getPreviousProjects() != null && !profile.getPreviousProjects().isBlank()) {
            baseScore += 10;
        }
        baseScore = Math.min(baseScore, 100);

        String spec = profile.getSpecialization() != null ? profile.getSpecialization() : (en ? "Software" : "Yazılım");
        int years = profile.getExperienceYears() != null ? profile.getExperienceYears() : 0;

        if (en) {
            return ProfileScoreResponse.builder()
                    .score(baseScore)
                    .summary(String.format(
                            "A developer with %d years of experience in %s. " +
                            "Based on technical skills and project portfolio, this is a strong profile.",
                            years, spec))
                    .strengths(List.of(
                            "Knowledge of multiple programming languages",
                            "Expertise in " + spec,
                            "Active project development experience"))
                    .weaknesses(List.of(
                            "Portfolio diversity could be improved",
                            "Open-source contributions could be expanded"))
                    .recommendations(List.of(
                            "Contribute to open-source projects on GitHub",
                            "Share knowledge by writing a technical blog",
                            "Track new technologies and integrate them into your projects"))
                    .aiPowered(false)
                    .build();
        }

        return ProfileScoreResponse.builder()
                .score(baseScore)
                .summary(String.format(
                        "%s alanında %d yıllık deneyime sahip bir geliştirici. " +
                        "Teknik beceriler ve proje portföyü değerlendirildiğinde güçlü bir profile sahip.",
                        spec, years))
                .strengths(List.of(
                        "Çoklu programlama dili bilgisi",
                        spec + " alanında uzmanlık",
                        "Aktif proje geliştirme deneyimi"))
                .weaknesses(List.of(
                        "Portföy çeşitliliği artırılabilir",
                        "Açık kaynak katkıları geliştirilebilir"))
                .recommendations(List.of(
                        "GitHub'da açık kaynak projelere katkıda bulunun",
                        "Teknik blog yazarak bilgi paylaşın",
                        "Yeni teknolojileri takip edip projelerinize entegre edin"))
                .aiPowered(false)
                .build();
    }

    private ProjectAnalysisResponse getMockProjectAnalysis(String name, String desc, String lang, String outputLang) {
        boolean en = "en".equals(outputLang);
        String lowerName = name.toLowerCase();
        String lowerDesc = desc.toLowerCase();

        List<ProjectAnalysisResponse.CompetitorExample> competitors = new ArrayList<>();
        List<ProjectAnalysisResponse.FreelancerPlatform> freelancerPlatforms = new ArrayList<>();

        if (lowerName.contains("ai") || lowerDesc.contains("ai") ||
            lowerName.contains("yapay zeka") || lowerDesc.contains("yapay zeka") ||
            lowerName.contains("gpt") || lowerDesc.contains("gpt") ||
            lowerName.contains("chat") || lowerDesc.contains("chat") ||
            lowerName.contains("bot") || lowerDesc.contains("bot") ||
            lowerName.contains("claude") || lowerDesc.contains("claude")) {

            competitors.add(ProjectAnalysisResponse.CompetitorExample.builder()
                    .name("ChatGPT").build());
            competitors.add(ProjectAnalysisResponse.CompetitorExample.builder()
                    .name("Claude AI").build());
            competitors.add(ProjectAnalysisResponse.CompetitorExample.builder()
                    .name("Gemini").build());

            freelancerPlatforms.add(ProjectAnalysisResponse.FreelancerPlatform.builder()
                    .name("Upwork").estimatedPrice("$2,500 - $10,000").build());
            freelancerPlatforms.add(ProjectAnalysisResponse.FreelancerPlatform.builder()
                    .name("Fiverr").estimatedPrice("$500 - $3,000").build());
            freelancerPlatforms.add(ProjectAnalysisResponse.FreelancerPlatform.builder()
                    .name("Bionluk").estimatedPrice("15,000 TL - 50,000 TL").build());
        } else if (lowerName.contains("stok") || lowerDesc.contains("stok") ||
                   lowerName.contains("envanter") || lowerDesc.contains("envanter") ||
                   lowerName.contains("depo") || lowerDesc.contains("depo") ||
                   lowerName.contains("inventory") || lowerDesc.contains("inventory") ||
                   lowerName.contains("stock") || lowerDesc.contains("stock")) {

            competitors.add(ProjectAnalysisResponse.CompetitorExample.builder()
                    .name("Zoho Inventory").build());
            competitors.add(ProjectAnalysisResponse.CompetitorExample.builder()
                    .name("Katana MRP").build());
            competitors.add(ProjectAnalysisResponse.CompetitorExample.builder()
                    .name("Shopify Stocky").build());

            freelancerPlatforms.add(ProjectAnalysisResponse.FreelancerPlatform.builder()
                    .name("Upwork").estimatedPrice("$1,500 - $6,000").build());
            freelancerPlatforms.add(ProjectAnalysisResponse.FreelancerPlatform.builder()
                    .name("Fiverr").estimatedPrice("$300 - $1,500").build());
            freelancerPlatforms.add(ProjectAnalysisResponse.FreelancerPlatform.builder()
                    .name("Bionluk").estimatedPrice("8,000 TL - 25,000 TL").build());
        } else {
            competitors.add(ProjectAnalysisResponse.CompetitorExample.builder()
                    .name("GitHub").build());
            competitors.add(ProjectAnalysisResponse.CompetitorExample.builder()
                    .name("Trello").build());
            competitors.add(ProjectAnalysisResponse.CompetitorExample.builder()
                    .name("Jira").build());

            freelancerPlatforms.add(ProjectAnalysisResponse.FreelancerPlatform.builder()
                    .name("Upwork").estimatedPrice("$1,000 - $5,000").build());
            freelancerPlatforms.add(ProjectAnalysisResponse.FreelancerPlatform.builder()
                    .name("Fiverr").estimatedPrice("$200 - $1,200").build());
            freelancerPlatforms.add(ProjectAnalysisResponse.FreelancerPlatform.builder()
                    .name("Bionluk").estimatedPrice("5,000 TL - 20,000 TL").build());
        }

        if (en) {
            return ProjectAnalysisResponse.builder()
                    .projectName(name)
                    .targetLanguage(lang)
                    .marketPriceRange(ProjectAnalysisResponse.PriceRange.builder()
                            .min(5000).max(25000).currency("USD").build())
                    .freelancerIncome(ProjectAnalysisResponse.FreelancerIncome.builder()
                            .hourlyRate(ProjectAnalysisResponse.PriceRange.builder()
                                    .min(25).max(75).currency("USD").build())
                            .projectBased(ProjectAnalysisResponse.PriceRange.builder()
                                    .min(3000).max(15000).currency("USD").build())
                            .build())
                    .demandLevel("HIGH")
                    .demandDescription("This type of project sees high demand in the market. " +
                            "Interest in " + lang + " technology has grown significantly in recent years.")
                    .estimatedDevelopmentTime(ProjectAnalysisResponse.DevelopmentTime.builder()
                            .minWeeks(4).maxWeeks(12)
                            .description("Basic MVP: 4-6 weeks, Full-featured version: 8-12 weeks. " +
                            "Includes backend development, frontend design, testing and deployment.")
                            .build())
                    .recommendedTechStack(List.of(
                            ProjectAnalysisResponse.TechRecommendation.builder()
                                    .name(lang).purpose("As the main development language").build(),
                            ProjectAnalysisResponse.TechRecommendation.builder()
                                    .name("Docker").purpose("For containerization and easy deployment").build(),
                            ProjectAnalysisResponse.TechRecommendation.builder()
                                    .name("PostgreSQL").purpose("Reliable and scalable database").build(),
                            ProjectAnalysisResponse.TechRecommendation.builder()
                                    .name("Redis").purpose("Caching and performance optimization").build(),
                            ProjectAnalysisResponse.TechRecommendation.builder()
                                    .name("GitHub Actions").purpose("CI/CD pipeline automation").build()))
                    .enhancements(List.of(
                            ProjectAnalysisResponse.Enhancement.builder()
                                    .title("API Integration")
                                    .description("Add integrations with third-party services to increase functionality").build(),
                            ProjectAnalysisResponse.Enhancement.builder()
                                    .title("Real-Time Notifications")
                                    .description("Add an instant notification system using WebSockets").build(),
                            ProjectAnalysisResponse.Enhancement.builder()
                                    .title("Analytics Dashboard")
                                    .description("Add a user behavior analytics and reporting module").build()))
                    .tips(List.of(
                            ProjectAnalysisResponse.Tip.builder()
                                    .title("MVP First")
                                    .description("Ship a minimum viable product first, then iterate").build(),
                            ProjectAnalysisResponse.Tip.builder()
                                    .title("Security")
                                    .description("Guard against OWASP Top 10 vulnerabilities from the start").build(),
                            ProjectAnalysisResponse.Tip.builder()
                                    .title("Test Coverage")
                                    .description("Aim for at least 80% test coverage, especially for critical business logic").build(),
                            ProjectAnalysisResponse.Tip.builder()
                                    .title("Documentation")
                                    .description("Use Swagger/OpenAPI for API documentation").build()))
                    .competitorInsight("Similar solutions already exist in the market, but custom solutions built with " +
                            lang + " offer a niche opportunity. " +
                            "Focus on user experience and performance for a competitive edge.")
                    .competitors(competitors)
                    .freelancerPlatforms(freelancerPlatforms)
                    .projectResources(List.of(
                            ProjectAnalysisResponse.ProjectResource.builder()
                                    .title(lang + " Official Documentation")
                                    .url("https://docs.oracle.com/en/" + lang.toLowerCase()).build(),
                            ProjectAnalysisResponse.ProjectResource.builder()
                                    .title("DevRadar AI Developer Hub")
                                    .url("https://github.com/lyveer/devradar-app").build()
                    ))
                    .codeSnippets(List.of(
                            ProjectAnalysisResponse.CodeSnippet.builder()
                                    .title("Main Application Setup")
                                    .code("// Skeleton setup for " + lang + "\npublic class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Starting " + name + "...\");\n    }\n}")
                                    .language(lang.toLowerCase()).build()
                    ))
                    .aiPowered(false)
                    .codeRecommendation(getMockCodeRecommendation(lang, outputLang))
                    .build();
        }

        return ProjectAnalysisResponse.builder()
                .projectName(name)
                .targetLanguage(lang)
                .marketPriceRange(ProjectAnalysisResponse.PriceRange.builder()
                        .min(5000).max(25000).currency("USD").build())
                .freelancerIncome(ProjectAnalysisResponse.FreelancerIncome.builder()
                        .hourlyRate(ProjectAnalysisResponse.PriceRange.builder()
                                .min(25).max(75).currency("USD").build())
                        .projectBased(ProjectAnalysisResponse.PriceRange.builder()
                                .min(3000).max(15000).currency("USD").build())
                        .build())
                .demandLevel("YÜKSEK")
                .demandDescription("Bu tür projeler piyasada yüksek talep görmektedir. " +
                        lang + " teknolojisine olan ilgi son yıllarda artış göstermiştir.")
                .estimatedDevelopmentTime(ProjectAnalysisResponse.DevelopmentTime.builder()
                        .minWeeks(4).maxWeeks(12)
                        .description("Temel MVP: 4-6 hafta, Tam özellikli versiyon: 8-12 hafta. " +
                        "Backend geliştirme, frontend tasarım, test ve deployment süreçleri dahil.")
                        .build())
                .recommendedTechStack(List.of(
                        ProjectAnalysisResponse.TechRecommendation.builder()
                                .name(lang).purpose("Ana geliştirme dili olarak").build(),
                        ProjectAnalysisResponse.TechRecommendation.builder()
                                .name("Docker").purpose("Konteynerizasyon ve kolay deployment için").build(),
                        ProjectAnalysisResponse.TechRecommendation.builder()
                                .name("PostgreSQL").purpose("Güvenilir ve ölçeklenebilir veritabanı").build(),
                        ProjectAnalysisResponse.TechRecommendation.builder()
                                .name("Redis").purpose("Önbellekleme ve performans optimizasyonu").build(),
                        ProjectAnalysisResponse.TechRecommendation.builder()
                                .name("GitHub Actions").purpose("CI/CD pipeline otomasyonu").build()))
                .enhancements(List.of(
                        ProjectAnalysisResponse.Enhancement.builder()
                                .title("API Entegrasyonu")
                                .description("Üçüncü parti servislerle entegrasyon ekleyerek işlevselliği artırın").build(),
                        ProjectAnalysisResponse.Enhancement.builder()
                                .title("Gerçek Zamanlı Bildirimler")
                                .description("WebSocket kullanarak anlık bildirim sistemi ekleyin").build(),
                        ProjectAnalysisResponse.Enhancement.builder()
                                .title("Analitik Dashboard")
                                .description("Kullanıcı davranış analitiği ve raporlama modülü ekleyin").build()))
                .tips(List.of(
                        ProjectAnalysisResponse.Tip.builder()
                                .title("MVP Önceliği")
                                .description("Önce minimum uygulanabilir ürünü çıkarın, sonra iteratif olarak geliştirin").build(),
                        ProjectAnalysisResponse.Tip.builder()
                                .title("Güvenlik")
                                .description("OWASP Top 10 güvenlik açıklarına karşı baştan önlem alın").build(),
                        ProjectAnalysisResponse.Tip.builder()
                                .title("Test Kapsamı")
                                .description("En az %80 test kapsamı hedefleyin, özellikle kritik iş mantığında").build(),
                        ProjectAnalysisResponse.Tip.builder()
                                .title("Dokümantasyon")
                                .description("API dokümantasyonu için Swagger/OpenAPI kullanın").build()))
                .competitorInsight("Piyasada benzer çözümler mevcut ancak " + lang +
                        " ile geliştirilmiş özelleştirilmiş çözümler niş bir pazar sunuyor. " +
                        "Rekabet avantajı için kullanıcı deneyimi ve performansa odaklanın.")
                .competitors(competitors)
                .freelancerPlatforms(freelancerPlatforms)
                .projectResources(List.of(
                        ProjectAnalysisResponse.ProjectResource.builder()
                                .title(lang + " Resmi Dokümantasyonu")
                                .url("https://docs.oracle.com/en/" + lang.toLowerCase()).build(),
                        ProjectAnalysisResponse.ProjectResource.builder()
                                .title("DevRadar AI Geliştirici Merkezi")
                                .url("https://github.com/lyveer/devradar-app").build()
                ))
                .codeSnippets(List.of(
                        ProjectAnalysisResponse.CodeSnippet.builder()
                                .title("Ana Uygulama Kurulumu")
                                .code("// " + lang + " için iskelet kurulum\npublic class Main {\n    public static void main(String[] args) {\n        System.out.println(\"" + name + " başlatılıyor...\");\n    }\n}")
                                .language(lang.toLowerCase()).build()
                ))
                .aiPowered(false)
                .codeRecommendation(getMockCodeRecommendation(lang, outputLang))
                .build();
    }



    private String buildCodeRecommendationPrompt(String projectName, String description, String targetLanguage, String lang) {
        if ("en".equals(lang)) {
            return "You are an expert AI software architect. Generate a clean, production-ready code template, " +
                    "architecture structure, or skeleton code for the project: '" + projectName + "'.\n" +
                    "Target Technology/Language: " + targetLanguage + "\n" +
                    "Project Description: " + description + "\n\n" +
                    "Please structure your response with markdown code blocks and clear architecture explanations. " +
                    "Include folders, key files, and a sample working code block. Do NOT use any introductory or conversational text, " +
                    "start directly with the architectural overview or the code.";
        } else if ("de".equals(lang)) {
            return "Sie sind ein erfahrener Softwarearchitekt. Generieren Sie eine saubere, produktionsbereite Codevorlage, " +
                    "Architekturstruktur oder Skelettcode für das Projekt: '" + projectName + "'.\n" +
                    "Zieltechnologie/-sprache: " + targetLanguage + "\n" +
                    "Projektbeschreibung: " + description + "\n\n" +
                    "Bitte strukturieren Sie Ihre Antwort mit Markdown-Codeblöcken und klaren Architekturerklärungen. " +
                    "Geben Sie Ordner, Schlüsseldateien und einen funktionierenden Beispielcodeblock an. Verwenden Sie keine Einleitung, " +
                    "beginnen Sie direkt mit der Architekturübersicht oder dem Code.";
        } else {
            return "Sen uzman bir yazılım mimarısın. Şu proje için temiz, üretime hazır bir kod şablonu, " +
                    "mimari yapı tasarımı veya iskelet kod oluştur: '" + projectName + "'.\n" +
                    "Hedef Teknoloji/Dil: " + targetLanguage + "\n" +
                    "Proje Açıklaması: " + description + "\n\n" +
                    "Lütfen yanıtını markdown kod blokları ve net mimari açıklamalarla yapılandır. " +
                    "Klasör yapısını, anahtar dosyaları ve örnek çalışan bir kod bloğunu dahil et. Giriş veya sohbet cümleleri kullanma, " +
                    "doğrudan mimari genel bakış veya kodla başla.";
        }
    }



    private String getMockCodeRecommendation(String targetLanguage, String lang) {
        if ("en".equals(lang)) {
            return "### Recommended Architecture (" + targetLanguage + ")\n\n" +
                    "```\n" +
                    "src/\n" +
                    "├── config/\n" +
                    "├── controllers/\n" +
                    "├── models/\n" +
                    "└── services/\n" +
                    "```\n\n" +
                    "*(Note: Configure a Claude, Gemini, or Groq API key to generate tailored, real-time code recommendations)*";
        } else if ("de".equals(lang)) {
            return "### Empfohlene Architektur (" + targetLanguage + ")\n\n" +
                    "```\n" +
                    "src/\n" +
                    "├── config/\n" +
                    "├── controllers/\n" +
                    "├── models/\n" +
                    "└── services/\n" +
                    "```\n\n" +
                    "*(Hinweis: Konfigurieren Sie einen Claude-, Gemini- oder Groq-API-Schlüssel, um maßgeschneiderte Echtzeit-Codeempfehlungen zu generieren)*";
        } else {
            return "### Önerilen Proje Mimarisi (" + targetLanguage + ")\n\n" +
                    "```\n" +
                    "src/\n" +
                    "├── config/\n" +
                    "├── controllers/\n" +
                    "├── models/\n" +
                    "└── services/\n" +
                    "```\n\n" +
                    "*(Not: Claude, Gemini veya Groq API anahtarınızı yapılandırarak projeye özel canlı kod taslakları oluşturabilirsiniz)*";
        }
    }
}
