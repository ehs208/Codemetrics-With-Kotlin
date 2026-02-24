package com.github.ehs208.codemetrics.ai;

import java.util.List;

public class AiRefactoringRequest {
    private final String sourceCode;
    private final String language;
    private final long complexityScore;
    private final String complexityLevel;
    private final List<BreakdownEntry> breakdown;
    private final String classContext;
    private final String imports;
    private final String methodName;

    public AiRefactoringRequest(String sourceCode, String language, long complexityScore,
                                String complexityLevel, List<BreakdownEntry> breakdown,
                                String classContext, String imports, String methodName) {
        this.sourceCode = sourceCode;
        this.language = language;
        this.complexityScore = complexityScore;
        this.complexityLevel = complexityLevel;
        this.breakdown = breakdown;
        this.classContext = classContext;
        this.imports = imports;
        this.methodName = methodName;
    }

    public String getSourceCode() { return sourceCode; }
    public String getLanguage() { return language; }
    public long getComplexityScore() { return complexityScore; }
    public String getComplexityLevel() { return complexityLevel; }
    public List<BreakdownEntry> getBreakdown() { return breakdown; }
    public String getClassContext() { return classContext; }
    public String getImports() { return imports; }
    public String getMethodName() { return methodName; }

    public static class BreakdownEntry {
        private final String description;
        private final int complexity;
        private final String codeSnippet;

        public BreakdownEntry(String description, int complexity, String codeSnippet) {
            this.description = description;
            this.complexity = complexity;
            this.codeSnippet = codeSnippet;
        }

        public String getDescription() { return description; }
        public int getComplexity() { return complexity; }
        public String getCodeSnippet() { return codeSnippet; }
    }
}
