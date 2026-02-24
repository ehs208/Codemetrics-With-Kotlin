package com.github.ehs208.codemetrics.ai.provider;

import com.github.ehs208.codemetrics.ai.AiRefactoringProvider;
import com.github.ehs208.codemetrics.ai.AiRefactoringRequest;
import com.github.ehs208.codemetrics.ai.AiRefactoringResponse;
import com.github.ehs208.codemetrics.ai.PromptBuilder;
import com.github.ehs208.codemetrics.ai.ResponseParsingUtils;
import com.github.ehs208.codemetrics.ai.config.AiRefactoringConfiguration;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ClaudeProvider implements AiRefactoringProvider {

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private final HttpClient httpClient;

    public ClaudeProvider() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    }

    @Override
    public String getId() {
        return "Claude";
    }

    @Override
    public String getDisplayName() {
        return "Claude (Anthropic)";
    }

    @Override
    public boolean isConfigured() {
        String apiKey = AiRefactoringConfiguration.getInstance().getApiKey("Claude");
        return apiKey != null && !apiKey.isEmpty();
    }

    @Override
    public CompletableFuture<AiRefactoringResponse> suggestRefactoring(
            AiRefactoringRequest request, Consumer<String> progressCallback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                progressCallback.accept("Building prompt...");
                AiRefactoringConfiguration config = AiRefactoringConfiguration.getInstance();
                String apiKey = config.getApiKey("Claude");

                if (apiKey == null || apiKey.isEmpty()) {
                    throw new RuntimeException("Claude API key not configured. " +
                        "Please set it in Settings > Code Metrics With Kotlin > AI Refactoring.");
                }

                String systemPrompt = PromptBuilder.buildSystemPrompt();
                String userPrompt = PromptBuilder.buildFinalUserPrompt(request);

                progressCallback.accept("Sending request to Claude...");

                JsonObject body = buildRequestBody(config, systemPrompt, userPrompt);

                HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .timeout(Duration.ofSeconds(120))
                    .build();

                HttpResponse<String> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException(
                        "Claude API error (HTTP " + response.statusCode() + "): " + ResponseParsingUtils.extractErrorMessage(response.body()));
                }

                progressCallback.accept("Parsing response...");
                return parseResponse(response.body());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Request was interrupted", e);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Failed to get refactoring suggestion: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public boolean supportsStreaming() {
        return true;
    }

    @Override
    public CompletableFuture<Boolean> validateCredentials() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                AiRefactoringConfiguration config = AiRefactoringConfiguration.getInstance();
                String apiKey = config.getApiKey("Claude");
                if (apiKey == null || apiKey.isEmpty()) return false;

                JsonObject body = new JsonObject();
                body.addProperty("model", config.claudeModel);
                body.addProperty("max_tokens", 10);

                JsonArray messages = new JsonArray();
                JsonObject msg = new JsonObject();
                msg.addProperty("role", "user");
                msg.addProperty("content", "Hi");
                messages.add(msg);
                body.add("messages", messages);

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .timeout(Duration.ofSeconds(15))
                    .build();

                HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
                return response.statusCode() == 200;
            } catch (Exception e) {
                return false;
            }
        });
    }

    private JsonObject buildRequestBody(AiRefactoringConfiguration config,
                                        String systemPrompt, String userPrompt) {
        JsonObject body = new JsonObject();
        body.addProperty("model", config.claudeModel);
        body.addProperty("max_tokens", config.maxTokens);
        body.addProperty("system", systemPrompt);

        JsonArray messages = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", userPrompt);
        messages.add(userMessage);
        body.add("messages", messages);

        return body;
    }

    private AiRefactoringResponse parseResponse(String responseBody) {
        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray content = json.getAsJsonArray("content");

        if (content == null || content.isEmpty()) {
            throw new RuntimeException("No content in Claude response");
        }

        StringBuilder fullText = new StringBuilder();
        for (JsonElement element : content) {
            JsonObject block = element.getAsJsonObject();
            if ("text".equals(block.get("type").getAsString())) {
                fullText.append(block.get("text").getAsString());
            }
        }

        String text = fullText.toString();
        String suggestedCode = ResponseParsingUtils.extractCodeBlock(text);
        String explanation = ResponseParsingUtils.extractExplanation(text);

        int inputTokens = 0;
        int outputTokens = 0;
        if (json.has("usage")) {
            JsonObject usage = json.getAsJsonObject("usage");
            inputTokens = usage.has("input_tokens") ? usage.get("input_tokens").getAsInt() : 0;
            outputTokens = usage.has("output_tokens") ? usage.get("output_tokens").getAsInt() : 0;
        }

        // Estimate cost (Claude Sonnet pricing: $3/M input, $15/M output)
        double estimatedCost = (inputTokens * 3.0 / 1_000_000) + (outputTokens * 15.0 / 1_000_000);

        return new AiRefactoringResponse(suggestedCode, explanation, getDisplayName(),
            inputTokens, outputTokens, estimatedCost);
    }

}
