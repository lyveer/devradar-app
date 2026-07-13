package com.devradar.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class ProfileRequest {

    @NotBlank(message = "Uzmanlık alanı gerekli")
    private String specialization;

    @NotEmpty(message = "En az bir programlama dili seçin")
    private List<String> languages;

    @NotNull(message = "Deneyim süresi gerekli")
    @Min(value = 0, message = "Deneyim süresi negatif olamaz")
    private Integer experienceYears;

    private String previousProjects;

    private String githubUrl;

    // Output/UI language for the AI response: "tr" (default) or "en".
    private String language = "tr";

    public ProfileRequest() {}

    public ProfileRequest(String specialization, List<String> languages, Integer experienceYears,
                          String previousProjects, String githubUrl) {
        this.specialization = specialization;
        this.languages = languages;
        this.experienceYears = experienceYears;
        this.previousProjects = previousProjects;
        this.githubUrl = githubUrl;
    }

    public ProfileRequest(String specialization, List<String> languages, Integer experienceYears,
                          String previousProjects, String githubUrl, String language) {
        this.specialization = specialization;
        this.languages = languages;
        this.experienceYears = experienceYears;
        this.previousProjects = previousProjects;
        this.githubUrl = githubUrl;
        this.language = language;
    }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> languages) { this.languages = languages; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    public String getPreviousProjects() { return previousProjects; }
    public void setPreviousProjects(String previousProjects) { this.previousProjects = previousProjects; }

    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}
