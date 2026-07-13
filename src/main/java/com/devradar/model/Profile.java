package com.devradar.model;

import jakarta.persistence.*;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    private String specialization;

    @Column(columnDefinition = "TEXT")
    private String languages;

    private Integer experienceYears;

    @Column(columnDefinition = "TEXT")
    private String previousProjects;

    private String githubUrl;

    private Integer aiScore;

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    @Column(columnDefinition = "TEXT")
    private String aiStrengths;

    @Column(columnDefinition = "TEXT")
    private String aiWeaknesses;

    @Column(columnDefinition = "TEXT")
    private String aiRecommendations;

    // Preferred output language for AI results: "tr" or "en". Defaults to "tr" for existing rows.
    private String preferredLanguage = "tr";

    public Profile() {}

    public Profile(Long id, Long userId, String specialization, String languages, Integer experienceYears,
                   String previousProjects, String githubUrl, Integer aiScore, String aiSummary,
                   String aiStrengths, String aiWeaknesses, String aiRecommendations, String preferredLanguage) {
        this.id = id;
        this.userId = userId;
        this.specialization = specialization;
        this.languages = languages;
        this.experienceYears = experienceYears;
        this.previousProjects = previousProjects;
        this.githubUrl = githubUrl;
        this.aiScore = aiScore;
        this.aiSummary = aiSummary;
        this.aiStrengths = aiStrengths;
        this.aiWeaknesses = aiWeaknesses;
        this.aiRecommendations = aiRecommendations;
        this.preferredLanguage = preferredLanguage != null ? preferredLanguage : "tr";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getLanguages() { return languages; }
    public void setLanguages(String languages) { this.languages = languages; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    public String getPreviousProjects() { return previousProjects; }
    public void setPreviousProjects(String previousProjects) { this.previousProjects = previousProjects; }

    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }

    public Integer getAiScore() { return aiScore; }
    public void setAiScore(Integer aiScore) { this.aiScore = aiScore; }

    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }

    public String getAiStrengths() { return aiStrengths; }
    public void setAiStrengths(String aiStrengths) { this.aiStrengths = aiStrengths; }

    public String getAiWeaknesses() { return aiWeaknesses; }
    public void setAiWeaknesses(String aiWeaknesses) { this.aiWeaknesses = aiWeaknesses; }

    public String getAiRecommendations() { return aiRecommendations; }
    public void setAiRecommendations(String aiRecommendations) { this.aiRecommendations = aiRecommendations; }

    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }

    public static ProfileBuilder builder() {
        return new ProfileBuilder();
    }

    public static class ProfileBuilder {
        private Long id;
        private Long userId;
        private String specialization;
        private String languages;
        private Integer experienceYears;
        private String previousProjects;
        private String githubUrl;
        private Integer aiScore;
        private String aiSummary;
        private String aiStrengths;
        private String aiWeaknesses;
        private String aiRecommendations;
        private String preferredLanguage = "tr";

        public ProfileBuilder id(Long id) { this.id = id; return this; }
        public ProfileBuilder userId(Long userId) { this.userId = userId; return this; }
        public ProfileBuilder specialization(String specialization) { this.specialization = specialization; return this; }
        public ProfileBuilder languages(String languages) { this.languages = languages; return this; }
        public ProfileBuilder experienceYears(Integer experienceYears) { this.experienceYears = experienceYears; return this; }
        public ProfileBuilder previousProjects(String previousProjects) { this.previousProjects = previousProjects; return this; }
        public ProfileBuilder githubUrl(String githubUrl) { this.githubUrl = githubUrl; return this; }
        public ProfileBuilder aiScore(Integer aiScore) { this.aiScore = aiScore; return this; }
        public ProfileBuilder aiSummary(String aiSummary) { this.aiSummary = aiSummary; return this; }
        public ProfileBuilder aiStrengths(String aiStrengths) { this.aiStrengths = aiStrengths; return this; }
        public ProfileBuilder aiWeaknesses(String aiWeaknesses) { this.aiWeaknesses = aiWeaknesses; return this; }
        public ProfileBuilder aiRecommendations(String aiRecommendations) { this.aiRecommendations = aiRecommendations; return this; }
        public ProfileBuilder preferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; return this; }

        public Profile build() {
            return new Profile(id, userId, specialization, languages, experienceYears, previousProjects,
                    githubUrl, aiScore, aiSummary, aiStrengths, aiWeaknesses, aiRecommendations, preferredLanguage);
        }
    }
}
