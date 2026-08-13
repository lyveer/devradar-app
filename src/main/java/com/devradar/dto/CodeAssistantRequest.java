package com.devradar.dto;

import jakarta.validation.constraints.NotBlank;

public class CodeAssistantRequest {
    @NotBlank(message = "Prompt boş olamaz")
    private String prompt;
    private String aiProvider;
    private String language;

    public CodeAssistantRequest() {}

    public CodeAssistantRequest(String prompt, String aiProvider, String language) {
        this.prompt = prompt;
        this.aiProvider = aiProvider;
        this.language = language;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getAiProvider() {
        return aiProvider;
    }

    public void setAiProvider(String aiProvider) {
        this.aiProvider = aiProvider;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
