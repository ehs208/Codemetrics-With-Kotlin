package com.github.ehs208.codemetrics.ai;

import com.github.ehs208.codemetrics.core.MetricsModel;

import java.util.ArrayList;
import java.util.List;

public class PromptBuilder {

    private static final String SYSTEM_PROMPT =
        "You are an expert code refactoring assistant. Your task is to reduce the cyclomatic complexity " +
        "of the given method/function while preserving its exact behavior.\n\n" +
        "Rules:\n" +
        "1. Preserve the method signature (name, parameters, return type) exactly.\n" +
        "2. Preserve all functionality - the refactored code must behave identically.\n" +
        "3. Preserve important comments (TODOs, FIXMEs, license headers, Javadoc, KDoc, and any comment " +
        "that explains business logic or intent). Only remove comments that are purely redundant after refactoring.\n" +
        "4. Focus on reducing complexity by:\n" +
        "   - Extracting complex conditions into well-named boolean variables or methods\n" +
        "   - Using early returns / guard clauses to reduce nesting\n" +
        "   - Replacing complex switch/if chains with polymorphism or strategy patterns where appropriate\n" +
        "   - Simplifying nested loops where possible\n" +
        "   - Using Java/Kotlin standard library methods to replace manual iterations\n" +
        "5. Return ONLY the refactored method code, no surrounding class.\n" +
        "6. Add brief inline comments explaining significant structural changes.\n\n" +
        "CRITICAL: The refactored code MUST have lower cyclomatic complexity than the original. Do not add new control flow (if/else, loops, try/catch, switch/when) unless you remove more than you add.\n" +
        "Response format:\n" +
        "First, output the refactored code wrapped in a code block.\n" +
        "Then, output a brief explanation of what changes were made and why, under a \"## Explanation\" header.";

    public static String buildSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    public static String buildUserPrompt(AiRefactoringRequest request) {
        StringBuilder sb = new StringBuilder();

        sb.append("## Method to Refactor\n\n");
        sb.append("**Language**: ").append(request.getLanguage()).append("\n");
        sb.append("**Method**: `").append(request.getMethodName()).append("`\n");
        sb.append("**Total Complexity**: ").append(request.getComplexityScore())
          .append(" (").append(request.getComplexityLevel()).append(")\n\n");

        if (request.getBreakdown() != null && !request.getBreakdown().isEmpty()) {
            sb.append("## Complexity Breakdown\n\n");
            sb.append("The following elements contribute to the complexity:\n\n");
            for (AiRefactoringRequest.BreakdownEntry entry : request.getBreakdown()) {
                sb.append("- **").append(entry.getDescription()).append("** (+")
                  .append(entry.getComplexity()).append("): `")
                  .append(truncate(entry.getCodeSnippet(), 80)).append("`\n");
            }
            sb.append("\n");
        }

        if (request.getClassContext() != null && !request.getClassContext().isEmpty()) {
            sb.append("## Class Context\n\n```").append(request.getLanguage()).append("\n");
            sb.append(request.getClassContext()).append("\n```\n\n");
        }

        if (request.getImports() != null && !request.getImports().isEmpty()) {
            sb.append("## Available Imports\n\n```").append(request.getLanguage()).append("\n");
            sb.append(request.getImports()).append("\n```\n\n");
        }

        sb.append("## Source Code\n\n```").append(request.getLanguage()).append("\n");
        sb.append(request.getSourceCode()).append("\n```\n\n");

        sb.append("Please refactor this method to reduce its complexity while preserving its behavior.");

        return sb.toString();
    }

    /**
     * Returns additional custom instructions to append to the user prompt.
     * Returns null if no custom prompt is configured.
     * Template variables: {{sourceCode}}, {{complexity}}, {{language}}, {{methodName}}, {{breakdown}}
     */
    @org.jetbrains.annotations.Nullable
    public static String buildCustomPrompt(AiRefactoringRequest request) {
        com.github.ehs208.codemetrics.ai.config.AiRefactoringConfiguration config =
            com.github.ehs208.codemetrics.ai.config.AiRefactoringConfiguration.getInstance();

        if (config.customPromptTemplate == null || config.customPromptTemplate.trim().isEmpty()) {
            return null;
        }

        String template = config.customPromptTemplate;
        template = template.replace("{{sourceCode}}", request.getSourceCode() != null ? request.getSourceCode() : "");
        template = template.replace("{{complexity}}", String.valueOf(request.getComplexityScore()));
        template = template.replace("{{language}}", request.getLanguage() != null ? request.getLanguage() : "");
        template = template.replace("{{methodName}}", request.getMethodName() != null ? request.getMethodName() : "");

        StringBuilder breakdownStr = new StringBuilder();
        if (request.getBreakdown() != null) {
            for (AiRefactoringRequest.BreakdownEntry entry : request.getBreakdown()) {
                breakdownStr.append("- ").append(entry.getDescription())
                    .append(" (+").append(entry.getComplexity()).append(")\n");
            }
        }
        template = template.replace("{{breakdown}}", breakdownStr.toString());

        return template;
    }

    /**
     * Builds the final user prompt: auto-generated user prompt + custom prompt (if any).
     * Structure: System Prompt (separate) + User Prompt (auto) + Custom Prompt (appended)
     */
    public static String buildFinalUserPrompt(AiRefactoringRequest request) {
        String userPrompt = buildUserPrompt(request);
        String customPrompt = buildCustomPrompt(request);

        if (customPrompt != null) {
            userPrompt += "\n\n## Additional Instructions\n\n" + customPrompt;
        }

        return userPrompt;
    }

    public static List<AiRefactoringRequest.BreakdownEntry> buildBreakdown(MetricsModel model) {
        List<AiRefactoringRequest.BreakdownEntry> entries = new ArrayList<>();
        collectBreakdown(model, entries);
        return entries;
    }

    private static void collectBreakdown(MetricsModel model,
                                         List<AiRefactoringRequest.BreakdownEntry> entries) {
        if (model.getComplexity() > 0 && model.getDescription() != null) {
            entries.add(new AiRefactoringRequest.BreakdownEntry(
                model.getDescription(),
                model.getComplexity(),
                model.getText()
            ));
        }
        for (MetricsModel child : model.getChildren()) {
            collectBreakdown(child, entries);
        }
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        String cleaned = text.replaceAll("\\s+", " ").trim();
        if (cleaned.length() <= maxLength) return cleaned;
        return cleaned.substring(0, maxLength) + "...";
    }
}
