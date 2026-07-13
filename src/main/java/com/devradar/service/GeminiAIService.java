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

    @Value("${gemini.api.key:}")
    private String apiKey;

    // NOTE: "gemini-2.0-flash" was shut down by Google on 2026-06-01 and now returns HTTP 404
    // for every request. "gemini-flash-latest" is a self-updating alias maintained by Google
    // that always points at the current recommended Flash model, so this class of failure
    // (silently falling back to mock data because the model name went stale) can't recur.
    @Value("${gemini.model:gemini-flash-latest}")
    private String model;

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

        // Loud, unmissable startup diagnostics — this is exactly the kind of thing that was
        // being swallowed before, making it look like "the AI barely works" with no clue why.
        if (!isApiKeyConfigured()) {
            log.warn("=================================================================");
            log.warn(" GEMINI API KEY IS NOT CONFIGURED (gemini.api.key is empty).");
            log.warn(" DevRadar will run in MOCK MODE: every response is template data,");
            log.warn(" not real AI output. Set gemini.api.key (env var GEMINI_API_KEY)");
            log.warn(" to enable real Gemini responses.");
            log.warn("=================================================================");
        } else {
            log.info("Gemini AI service initialized. model='{}', key length={}", model, apiKey.length());
        }
    }

    private boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    private String normalizeLanguage(String language) {
        if (language == null) return "tr";
        String l = language.trim().toLowerCase();
        if (l.startsWith("en")) return "en";
        return "tr";
    }

    // ========== PROFILE SCORING ==========

    public ProfileScoreResponse scoreProfile(Profile profile, String language) {
        String lang = normalizeLanguage(language);

        if (!isApiKeyConfigured()) {
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

    public ProjectAnalysisResponse analyzeProject(String projectName, String description, String targetLanguage, String responseLanguage) {
        String lang = normalizeLanguage(responseLanguage);

        if (!isApiKeyConfigured()) {
            return getMockProjectAnalysis(projectName, description, targetLanguage, lang);
        }

        String prompt = buildProjectAnalysisPrompt(projectName, description, targetLanguage, lang);
        GeminiCallResult result = callGemini(prompt);

        if (!result.success) {
            log.warn("Falling back to mock project analysis because the Gemini call failed: {}", result.errorReason);
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
                    .aiPowered(true)
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse AI project analysis response. Raw text was: {}", result.text, e);
            return getMockProjectAnalysis(projectName, description, targetLanguage, lang);
        }
    }

    // Backward-compatible overload (defaults to Turkish) so existing callers keep compiling.
    public ProjectAnalysisResponse analyzeProject(String projectName, String description, String targetLanguage) {
        return analyzeProject(projectName, description, targetLanguage, "tr");
    }

    // ========== GEMINI API CALL ==========

    /** Small result wrapper so callers can tell "AI succeeded" apart from "AI failed, this is a placeholder". */
    private static class GeminiCallResult {
        final boolean success;
        final String text;
        final String errorReason;

        private GeminiCallResult(boolean success, String text, String errorReason) {
            this.success = success;
            this.text = text;
            this.errorReason = errorReason;
        }

        static GeminiCallResult ok(String text) {
            return new GeminiCallResult(true, text, null);
        }

        static GeminiCallResult fail(String reason) {
            return new GeminiCallResult(false, null, reason);
        }
    }

    private GeminiCallResult callGemini(String prompt) {
        if (apiKey != null && apiKey.trim().startsWith("gsk_")) {
            return callGroq(prompt);
        }

        String url = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                model, apiKey);

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                )),
                "tools", List.of(Map.of("google_search", Map.of()))
        );

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

                // A finishReason like SAFETY or RECITATION means there's no usable text part.
                String finishReason = candidate.path("finishReason").asText("");
                if (!candidate.has("content") || !candidate.get("content").has("parts")) {
                    return GeminiCallResult.fail("Gemini returned no content (finishReason=" + finishReason + ")");
                }

                String text = candidate.get("content").get("parts").get(0).path("text").asText("");
                if (text.isBlank()) {
                    return GeminiCallResult.fail("Gemini returned an empty text part (finishReason=" + finishReason + ")");
                }
                return GeminiCallResult.ok(text);
            }

            if (responseBody != null && responseBody.has("promptFeedback")) {
                return GeminiCallResult.fail("Prompt was blocked: " + responseBody.get("promptFeedback"));
            }

            return GeminiCallResult.fail("Gemini response had no candidates. Full body: " + responseBody);

        } catch (RestClientResponseException e) {
            // This is the important one: it surfaces WHY the call failed (bad model name -> 404,
            // bad/expired API key -> 400/403, quota exceeded -> 429) instead of silently hiding it.
            log.error("Gemini API call failed with HTTP {} — body: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            return GeminiCallResult.fail("HTTP " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Gemini API call failed unexpectedly", e);
            return GeminiCallResult.fail(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private GeminiCallResult callGroq(String prompt) {
        String url = "https://api.groq.com/openai/v1/chat/completions";
        
        String groqModel = "llama-3.3-70b-versatile";
        if (model != null && (model.contains("llama") || model.contains("gemma") || model.contains("mixtral") || model.contains("groq"))) {
            groqModel = model;
        }

        Map<String, Object> message = Map.of("role", "user", "content", prompt);
        Map<String, Object> body = Map.of(
                "model", groqModel,
                "messages", List.of(message),
                "response_format", Map.of("type", "json_object")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey.trim());
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
                    return GeminiCallResult.fail("Groq returned an empty text part");
                }
                return GeminiCallResult.ok(text);
            }
            return GeminiCallResult.fail("Groq response had no choices. Full body: " + responseBody);
        } catch (RestClientResponseException e) {
            log.error("Groq API call failed with HTTP {} — body: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            return GeminiCallResult.fail("HTTP " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Groq API call failed unexpectedly", e);
            return GeminiCallResult.fail(e.getClass().getSimpleName() + ": " + e.getMessage());
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
                  ]
                }
                Do not include any URLs or web links in the response.
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
                    .aiPowered(false)
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
                .aiPowered(false)
                .build();
    }
}
