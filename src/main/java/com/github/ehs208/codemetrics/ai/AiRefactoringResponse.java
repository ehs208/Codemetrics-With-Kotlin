package com.github.ehs208.codemetrics.ai;

public class AiRefactoringResponse {
    private final String suggestedCode;
    private final String explanation;
    private final String providerName;
    private final int inputTokens;
    private final int outputTokens;
    private final double estimatedCost;

    public AiRefactoringResponse(String suggestedCode, String explanation, String providerName) {
        this(suggestedCode, explanation, providerName, 0, 0, 0.0);
    }

    public AiRefactoringResponse(String suggestedCode, String explanation, String providerName,
                                  int inputTokens, int outputTokens, double estimatedCost) {
        this.suggestedCode = suggestedCode;
        this.explanation = explanation;
        this.providerName = providerName;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.estimatedCost = estimatedCost;
    }

    public String getSuggestedCode() { return suggestedCode; }
    public String getExplanation() { return explanation; }
    public String getProviderName() { return providerName; }
    public int getInputTokens() { return inputTokens; }
    public int getOutputTokens() { return outputTokens; }
    public double getEstimatedCost() { return estimatedCost; }
    public boolean hasTokenUsage() { return inputTokens > 0 || outputTokens > 0; }
}
