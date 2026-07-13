package com.devradar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_analyses")
public class ProjectAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String projectName;

    @Column(columnDefinition = "TEXT")
    private String projectDescription;

    private String targetLanguage;

    @Column(columnDefinition = "TEXT")
    private String aiAnalysisResult;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public ProjectAnalysis() {}

    public ProjectAnalysis(Long id, Long userId, String projectName, String projectDescription,
                           String targetLanguage, String aiAnalysisResult, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.targetLanguage = targetLanguage;
        this.aiAnalysisResult = aiAnalysisResult;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getProjectDescription() { return projectDescription; }
    public void setProjectDescription(String projectDescription) { this.projectDescription = projectDescription; }

    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }

    public String getAiAnalysisResult() { return aiAnalysisResult; }
    public void setAiAnalysisResult(String aiAnalysisResult) { this.aiAnalysisResult = aiAnalysisResult; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static ProjectAnalysisBuilder builder() {
        return new ProjectAnalysisBuilder();
    }

    public static class ProjectAnalysisBuilder {
        private Long id;
        private Long userId;
        private String projectName;
        private String projectDescription;
        private String targetLanguage;
        private String aiAnalysisResult;
        private LocalDateTime createdAt;

        public ProjectAnalysisBuilder id(Long id) { this.id = id; return this; }
        public ProjectAnalysisBuilder userId(Long userId) { this.userId = userId; return this; }
        public ProjectAnalysisBuilder projectName(String projectName) { this.projectName = projectName; return this; }
        public ProjectAnalysisBuilder projectDescription(String projectDescription) { this.projectDescription = projectDescription; return this; }
        public ProjectAnalysisBuilder targetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; return this; }
        public ProjectAnalysisBuilder aiAnalysisResult(String aiAnalysisResult) { this.aiAnalysisResult = aiAnalysisResult; return this; }
        public ProjectAnalysisBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ProjectAnalysis build() {
            return new ProjectAnalysis(id, userId, projectName, projectDescription, targetLanguage, aiAnalysisResult, createdAt);
        }
    }
}
