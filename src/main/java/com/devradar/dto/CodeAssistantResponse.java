package com.devradar.dto;

public class CodeAssistantResponse {
    private String response;
    private String aiProviderUsed;
    private String modelUsed;
    private Integer remainingCredits;

    public CodeAssistantResponse() {}

    public CodeAssistantResponse(String response, String aiProviderUsed, String modelUsed, Integer remainingCredits) {
        this.response = response;
        this.aiProviderUsed = aiProviderUsed;
        this.modelUsed = modelUsed;
        this.remainingCredits = remainingCredits;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getAiProviderUsed() {
        return aiProviderUsed;
    }

    public void setAiProviderUsed(String aiProviderUsed) {
        this.aiProviderUsed = aiProviderUsed;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }

    public Integer getRemainingCredits() {
        return remainingCredits;
    }

    public void setRemainingCredits(Integer remainingCredits) {
        this.remainingCredits = remainingCredits;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String response;
        private String aiProviderUsed;
        private String modelUsed;
        private Integer remainingCredits;

        public Builder response(String response) {
            this.response = response;
            return this;
        }

        public Builder aiProviderUsed(String aiProviderUsed) {
            this.aiProviderUsed = aiProviderUsed;
            return this;
        }

        public Builder modelUsed(String modelUsed) {
            this.modelUsed = modelUsed;
            return this;
        }

        public Builder remainingCredits(Integer remainingCredits) {
            this.remainingCredits = remainingCredits;
            return this;
        }

        public CodeAssistantResponse build() {
            return new CodeAssistantResponse(response, aiProviderUsed, modelUsed, remainingCredits);
        }
    }
}
