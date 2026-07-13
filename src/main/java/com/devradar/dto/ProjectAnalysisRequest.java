package com.devradar.dto;

import jakarta.validation.constraints.*;

public class ProjectAnalysisRequest {

    @NotBlank(message = "Proje adı gerekli")
    private String projectName;

    @NotBlank(message = "Proje açıklaması gerekli")
    private String projectDescription;

    @NotBlank(message = "Hedef dil/teknoloji gerekli")
    private String targetLanguage;

    // Output/UI language for the AI response: "tr" (default) or "en". Optional — old clients
    // that don't send this keep getting Turkish output exactly as before.
    private String language = "tr";

    public ProjectAnalysisRequest() {}

    public ProjectAnalysisRequest(String projectName, String projectDescription, String targetLanguage) {
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.targetLanguage = targetLanguage;
    }

    public ProjectAnalysisRequest(String projectName, String projectDescription, String targetLanguage, String language) {
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.targetLanguage = targetLanguage;
        this.language = language;
    }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getProjectDescription() { return projectDescription; }
    public void setProjectDescription(String projectDescription) { this.projectDescription = projectDescription; }

    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}
