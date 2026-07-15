package com.devradar.dto;

import java.util.List;

public class ProjectAnalysisResponse {

    private Long id;
    private String projectName;
    private String targetLanguage;
    private PriceRange marketPriceRange;
    private FreelancerIncome freelancerIncome;
    private String demandLevel;
    private String demandDescription;
    private DevelopmentTime estimatedDevelopmentTime;
    private List<TechRecommendation> recommendedTechStack;
    private List<Enhancement> enhancements;
    private List<Tip> tips;
    private String competitorInsight;
    private List<CompetitorExample> competitors;
    private List<FreelancerPlatform> freelancerPlatforms;
    private String createdAt;
    private String completedSteps;

    // True when this result actually came from Gemini; false when it's fallback/mock data
    // (no API key configured, or the AI call failed).
    private Boolean aiPowered;

    public ProjectAnalysisResponse() {}

    public ProjectAnalysisResponse(Long id, String projectName, String targetLanguage, PriceRange marketPriceRange,
                                   FreelancerIncome freelancerIncome, String demandLevel, String demandDescription,
                                   DevelopmentTime estimatedDevelopmentTime, List<TechRecommendation> recommendedTechStack,
                                   List<Enhancement> enhancements, List<Tip> tips, String competitorInsight,
                                   List<CompetitorExample> competitors, List<FreelancerPlatform> freelancerPlatforms,
                                   String createdAt, String completedSteps, Boolean aiPowered) {
        this.id = id;
        this.projectName = projectName;
        this.targetLanguage = targetLanguage;
        this.marketPriceRange = marketPriceRange;
        this.freelancerIncome = freelancerIncome;
        this.demandLevel = demandLevel;
        this.demandDescription = demandDescription;
        this.estimatedDevelopmentTime = estimatedDevelopmentTime;
        this.recommendedTechStack = recommendedTechStack;
        this.enhancements = enhancements;
        this.tips = tips;
        this.competitorInsight = competitorInsight;
        this.competitors = competitors;
        this.freelancerPlatforms = freelancerPlatforms;
        this.createdAt = createdAt;
        this.completedSteps = completedSteps;
        this.aiPowered = aiPowered;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }

    public PriceRange getMarketPriceRange() { return marketPriceRange; }
    public void setMarketPriceRange(PriceRange marketPriceRange) { this.marketPriceRange = marketPriceRange; }

    public FreelancerIncome getFreelancerIncome() { return freelancerIncome; }
    public void setFreelancerIncome(FreelancerIncome freelancerIncome) { this.freelancerIncome = freelancerIncome; }

    public String getDemandLevel() { return demandLevel; }
    public void setDemandLevel(String demandLevel) { this.demandLevel = demandLevel; }

    public String getDemandDescription() { return demandDescription; }
    public void setDemandDescription(String demandDescription) { this.demandDescription = demandDescription; }

    public DevelopmentTime getEstimatedDevelopmentTime() { return estimatedDevelopmentTime; }
    public void setEstimatedDevelopmentTime(DevelopmentTime estimatedDevelopmentTime) { this.estimatedDevelopmentTime = estimatedDevelopmentTime; }

    public List<TechRecommendation> getRecommendedTechStack() { return recommendedTechStack; }
    public void setRecommendedTechStack(List<TechRecommendation> recommendedTechStack) { this.recommendedTechStack = recommendedTechStack; }

    public List<Enhancement> getEnhancements() { return enhancements; }
    public void setEnhancements(List<Enhancement> enhancements) { this.enhancements = enhancements; }

    public List<Tip> getTips() { return tips; }
    public void setTips(List<Tip> tips) { this.tips = tips; }

    public String getCompetitorInsight() { return competitorInsight; }
    public void setCompetitorInsight(String competitorInsight) { this.competitorInsight = competitorInsight; }

    public List<CompetitorExample> getCompetitors() { return competitors; }
    public void setCompetitors(List<CompetitorExample> competitors) { this.competitors = competitors; }

    public List<FreelancerPlatform> getFreelancerPlatforms() { return freelancerPlatforms; }
    public void setFreelancerPlatforms(List<FreelancerPlatform> freelancerPlatforms) { this.freelancerPlatforms = freelancerPlatforms; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getCompletedSteps() { return completedSteps; }
    public void setCompletedSteps(String completedSteps) { this.completedSteps = completedSteps; }

    public Boolean getAiPowered() { return aiPowered; }
    public void setAiPowered(Boolean aiPowered) { this.aiPowered = aiPowered; }

    public static ProjectAnalysisResponseBuilder builder() {
        return new ProjectAnalysisResponseBuilder();
    }

    public static class ProjectAnalysisResponseBuilder {
        private Long id;
        private String projectName;
        private String targetLanguage;
        private PriceRange marketPriceRange;
        private FreelancerIncome freelancerIncome;
        private String demandLevel;
        private String demandDescription;
        private DevelopmentTime estimatedDevelopmentTime;
        private List<TechRecommendation> recommendedTechStack;
        private List<Enhancement> enhancements;
        private List<Tip> tips;
        private String competitorInsight;
        private List<CompetitorExample> competitors;
        private List<FreelancerPlatform> freelancerPlatforms;
        private String createdAt;
        private String completedSteps;
        private Boolean aiPowered;

        public ProjectAnalysisResponseBuilder id(Long id) { this.id = id; return this; }
        public ProjectAnalysisResponseBuilder projectName(String projectName) { this.projectName = projectName; return this; }
        public ProjectAnalysisResponseBuilder targetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; return this; }
        public ProjectAnalysisResponseBuilder marketPriceRange(PriceRange marketPriceRange) { this.marketPriceRange = marketPriceRange; return this; }
        public ProjectAnalysisResponseBuilder freelancerIncome(FreelancerIncome freelancerIncome) { this.freelancerIncome = freelancerIncome; return this; }
        public ProjectAnalysisResponseBuilder demandLevel(String demandLevel) { this.demandLevel = demandLevel; return this; }
        public ProjectAnalysisResponseBuilder demandDescription(String demandDescription) { this.demandDescription = demandDescription; return this; }
        public ProjectAnalysisResponseBuilder estimatedDevelopmentTime(DevelopmentTime estimatedDevelopmentTime) { this.estimatedDevelopmentTime = estimatedDevelopmentTime; return this; }
        public ProjectAnalysisResponseBuilder recommendedTechStack(List<TechRecommendation> recommendedTechStack) { this.recommendedTechStack = recommendedTechStack; return this; }
        public ProjectAnalysisResponseBuilder enhancements(List<Enhancement> enhancements) { this.enhancements = enhancements; return this; }
        public ProjectAnalysisResponseBuilder tips(List<Tip> tips) { this.tips = tips; return this; }
        public ProjectAnalysisResponseBuilder competitorInsight(String competitorInsight) { this.competitorInsight = competitorInsight; return this; }
        public ProjectAnalysisResponseBuilder competitors(List<CompetitorExample> competitors) { this.competitors = competitors; return this; }
        public ProjectAnalysisResponseBuilder freelancerPlatforms(List<FreelancerPlatform> freelancerPlatforms) { this.freelancerPlatforms = freelancerPlatforms; return this; }
        public ProjectAnalysisResponseBuilder createdAt(String createdAt) { this.createdAt = createdAt; return this; }
        public ProjectAnalysisResponseBuilder completedSteps(String completedSteps) { this.completedSteps = completedSteps; return this; }
        public ProjectAnalysisResponseBuilder aiPowered(Boolean aiPowered) { this.aiPowered = aiPowered; return this; }

        public ProjectAnalysisResponse build() {
            return new ProjectAnalysisResponse(id, projectName, targetLanguage, marketPriceRange, freelancerIncome,
                    demandLevel, demandDescription, estimatedDevelopmentTime, recommendedTechStack, enhancements, tips,
                    competitorInsight, competitors, freelancerPlatforms, createdAt, completedSteps, aiPowered);
        }
    }

    // Nested classes

    public static class PriceRange {
        private Integer min;
        private Integer max;
        private String currency;

        public PriceRange() {}

        public PriceRange(Integer min, Integer max, String currency) {
            this.min = min;
            this.max = max;
            this.currency = currency;
        }

        public Integer getMin() { return min; }
        public void setMin(Integer min) { this.min = min; }

        public Integer getMax() { return max; }
        public void setMax(Integer max) { this.max = max; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public static PriceRangeBuilder builder() {
            return new PriceRangeBuilder();
        }

        public static class PriceRangeBuilder {
            private Integer min;
            private Integer max;
            private String currency;

            public PriceRangeBuilder min(Integer min) { this.min = min; return this; }
            public PriceRangeBuilder max(Integer max) { this.max = max; return this; }
            public PriceRangeBuilder currency(String currency) { this.currency = currency; return this; }

            public PriceRange build() {
                return new PriceRange(min, max, currency);
            }
        }
    }

    public static class FreelancerIncome {
        private PriceRange hourlyRate;
        private PriceRange projectBased;

        public FreelancerIncome() {}

        public FreelancerIncome(PriceRange hourlyRate, PriceRange projectBased) {
            this.hourlyRate = hourlyRate;
            this.projectBased = projectBased;
        }

        public PriceRange getHourlyRate() { return hourlyRate; }
        public void setHourlyRate(PriceRange hourlyRate) { this.hourlyRate = hourlyRate; }

        public PriceRange getProjectBased() { return projectBased; }
        public void setProjectBased(PriceRange projectBased) { this.projectBased = projectBased; }

        public static FreelancerIncomeBuilder builder() {
            return new FreelancerIncomeBuilder();
        }

        public static class FreelancerIncomeBuilder {
            private PriceRange hourlyRate;
            private PriceRange projectBased;

            public FreelancerIncomeBuilder hourlyRate(PriceRange hourlyRate) { this.hourlyRate = hourlyRate; return this; }
            public FreelancerIncomeBuilder projectBased(PriceRange projectBased) { this.projectBased = projectBased; return this; }

            public FreelancerIncome build() {
                return new FreelancerIncome(hourlyRate, projectBased);
            }
        }
    }

    public static class DevelopmentTime {
        private Integer minWeeks;
        private Integer maxWeeks;
        private String description;

        public DevelopmentTime() {}

        public DevelopmentTime(Integer minWeeks, Integer maxWeeks, String description) {
            this.minWeeks = minWeeks;
            this.maxWeeks = maxWeeks;
            this.description = description;
        }

        public Integer getMinWeeks() { return minWeeks; }
        public void setMinWeeks(Integer minWeeks) { this.minWeeks = minWeeks; }

        public Integer getMaxWeeks() { return maxWeeks; }
        public void setMaxWeeks(Integer maxWeeks) { this.maxWeeks = maxWeeks; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public static DevelopmentTimeBuilder builder() {
            return new DevelopmentTimeBuilder();
        }

        public static class DevelopmentTimeBuilder {
            private Integer minWeeks;
            private Integer maxWeeks;
            private String description;

            public DevelopmentTimeBuilder minWeeks(Integer minWeeks) { this.minWeeks = minWeeks; return this; }
            public DevelopmentTimeBuilder maxWeeks(Integer maxWeeks) { this.maxWeeks = maxWeeks; return this; }
            public DevelopmentTimeBuilder description(String description) { this.description = description; return this; }

            public DevelopmentTime build() {
                return new DevelopmentTime(minWeeks, maxWeeks, description);
            }
        }
    }

    public static class TechRecommendation {
        private String name;
        private String purpose;

        public TechRecommendation() {}

        public TechRecommendation(String name, String purpose) {
            this.name = name;
            this.purpose = purpose;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }

        public static TechRecommendationBuilder builder() {
            return new TechRecommendationBuilder();
        }

        public static class TechRecommendationBuilder {
            private String name;
            private String purpose;

            public TechRecommendationBuilder name(String name) { this.name = name; return this; }
            public TechRecommendationBuilder purpose(String purpose) { this.purpose = purpose; return this; }

            public TechRecommendation build() {
                return new TechRecommendation(name, purpose);
            }
        }
    }

    public static class Enhancement {
        private String title;
        private String description;

        public Enhancement() {}

        public Enhancement(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public static EnhancementBuilder builder() {
            return new EnhancementBuilder();
        }

        public static class EnhancementBuilder {
            private String title;
            private String description;

            public EnhancementBuilder title(String title) { this.title = title; return this; }
            public EnhancementBuilder description(String description) { this.description = description; return this; }

            public Enhancement build() {
                return new Enhancement(title, description);
            }
        }
    }

    public static class Tip {
        private String title;
        private String description;

        public Tip() {}

        public Tip(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public static TipBuilder builder() {
            return new TipBuilder();
        }

        public static class TipBuilder {
            private String title;
            private String description;

            public TipBuilder title(String title) { this.title = title; return this; }
            public TipBuilder description(String description) { this.description = description; return this; }

            public Tip build() {
                return new Tip(title, description);
            }
        }
    }

    public static class CompetitorExample {
        private String name;
        private String url;

        public CompetitorExample() {}

        public CompetitorExample(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public static CompetitorExampleBuilder builder() {
            return new CompetitorExampleBuilder();
        }

        public static class CompetitorExampleBuilder {
            private String name;
            private String url;

            public CompetitorExampleBuilder name(String name) { this.name = name; return this; }
            public CompetitorExampleBuilder url(String url) { this.url = url; return this; }

            public CompetitorExample build() {
                return new CompetitorExample(name, url);
            }
        }
    }

    public static class FreelancerPlatform {
        private String name;
        private String estimatedPrice;
        private String url;

        public FreelancerPlatform() {}

        public FreelancerPlatform(String name, String estimatedPrice, String url) {
            this.name = name;
            this.estimatedPrice = estimatedPrice;
            this.url = url;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEstimatedPrice() { return estimatedPrice; }
        public void setEstimatedPrice(String estimatedPrice) { this.estimatedPrice = estimatedPrice; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public static FreelancerPlatformBuilder builder() {
            return new FreelancerPlatformBuilder();
        }

        public static class FreelancerPlatformBuilder {
            private String name;
            private String estimatedPrice;
            private String url;

            public FreelancerPlatformBuilder name(String name) { this.name = name; return this; }
            public FreelancerPlatformBuilder estimatedPrice(String estimatedPrice) { this.estimatedPrice = estimatedPrice; return this; }
            public FreelancerPlatformBuilder url(String url) { this.url = url; return this; }

            public FreelancerPlatform build() {
                return new FreelancerPlatform(name, estimatedPrice, url);
            }
        }
    }
}
