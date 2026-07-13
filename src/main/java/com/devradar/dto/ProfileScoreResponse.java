package com.devradar.dto;

import java.util.List;

public class ProfileScoreResponse {
    private Integer score;
    private String summary;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> recommendations;

    // True when this result actually came from Gemini; false when it's fallback/mock data
    // (no API key configured, or the AI call failed). Lets the frontend show an honest
    // "this is example data" notice instead of silently pretending it's a real AI result.
    private Boolean aiPowered;

    public ProfileScoreResponse() {}

    public ProfileScoreResponse(Integer score, String summary, List<String> strengths,
                                List<String> weaknesses, List<String> recommendations, Boolean aiPowered) {
        this.score = score;
        this.summary = summary;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.recommendations = recommendations;
        this.aiPowered = aiPowered;
    }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<String> getStrengths() { return strengths; }
    public void setStrengths(List<String> strengths) { this.strengths = strengths; }

    public List<String> getWeaknesses() { return weaknesses; }
    public void setWeaknesses(List<String> weaknesses) { this.weaknesses = weaknesses; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

    public Boolean getAiPowered() { return aiPowered; }
    public void setAiPowered(Boolean aiPowered) { this.aiPowered = aiPowered; }

    public static ProfileScoreResponseBuilder builder() {
        return new ProfileScoreResponseBuilder();
    }

    public static class ProfileScoreResponseBuilder {
        private Integer score;
        private String summary;
        private List<String> strengths;
        private List<String> weaknesses;
        private List<String> recommendations;
        private Boolean aiPowered;

        public ProfileScoreResponseBuilder score(Integer score) { this.score = score; return this; }
        public ProfileScoreResponseBuilder summary(String summary) { this.summary = summary; return this; }
        public ProfileScoreResponseBuilder strengths(List<String> strengths) { this.strengths = strengths; return this; }
        public ProfileScoreResponseBuilder weaknesses(List<String> weaknesses) { this.weaknesses = weaknesses; return this; }
        public ProfileScoreResponseBuilder recommendations(List<String> recommendations) { this.recommendations = recommendations; return this; }
        public ProfileScoreResponseBuilder aiPowered(Boolean aiPowered) { this.aiPowered = aiPowered; return this; }

        public ProfileScoreResponse build() {
            return new ProfileScoreResponse(score, summary, strengths, weaknesses, recommendations, aiPowered);
        }
    }
}
