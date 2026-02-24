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

public class GeminiProvider implements AiRefactoringProvider {

    private static final String API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private final HttpClient httpClient;

    public GeminiProvider() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    }

    @Override
    public String getId() {
        return "Gemini";
    }

    @Override
    public String getDisplayName() {
        return "Gemini (Google)";
    }

    @Override
    public boolean isConfigured() {
        String apiKey = AiRefactoringConfiguration.getInstance().getApiKey("Gemini");
        return apiKey != null && !apiKey.isEmpty();
    }

    @Override
    public CompletableFuture<AiRefactoringResponse> suggestRefactoring(
            AiRefactoringRequest request, Consumer<String> progressCallback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                progressCallback.accept("Building prompt...");
                AiRefactoringConfiguration config = AiRefactoringConfiguration.getInstance();
                String apiKey = config.getApiKey("Gemini");

                if (apiKey == null || apiKey.isEmpty()) {
                    throw new RuntimeException("Gemini API key not configured. " +
                        "Please set it in Settings > Code Metrics With Kotlin > AI Refactoring.");
                }

                String systemPrompt = PromptBuilder.buildSystemPrompt();
                String userPrompt = PromptBuilder.buildFinalUserPrompt(request);

                progressCallback.accept("Sending request to Gemini...");

                JsonObject body = buildRequestBody(config, systemPrompt, userPrompt);
                String url = buildApiUrl(config.geminiModel);

                HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .timeout(Duration.ofSeconds(120))
                    .build();

                HttpResponse<String> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException(
                        "Gemini API error (HTTP " + response.statusCode() + "): " + ResponseParsingUtils.extractErrorMessage(response.body()));
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
    public CompletableFuture<Boolean> validateCredentials() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                AiRefactoringConfiguration config = AiRefactoringConfiguration.getInstance();
                String apiKey = config.getApiKey("Gemini");
                if (apiKey == null || apiKey.isEmpty()) return false;

                JsonObject body = new JsonObject();
                JsonObject genConfig = new JsonObject();
                genConfig.addProperty("maxOutputTokens", 10);
                body.add("generationConfig", genConfig);

                JsonArray contents = new JsonArray();
                JsonObject content = new JsonObject();
                JsonArray parts = new JsonArray();
                JsonObject part = new JsonObject();
                part.addProperty("text", "Hi");
                parts.add(part);
                content.add("parts", parts);
                contents.add(content);
                body.add("contents", contents);

                String url = buildApiUrl(config.geminiModel);

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
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

    private String buildApiUrl(String model) {
        return API_BASE_URL + model + ":generateContent";
    }

    private JsonObject buildRequestBody(AiRefactoringConfiguration config,
                                        String systemPrompt, String userPrompt) {
        JsonObject body = new JsonObject();

        // System instruction
        JsonObject systemInstruction = new JsonObject();
        JsonArray systemParts = new JsonArray();
        JsonObject systemPart = new JsonObject();
        systemPart.addProperty("text", systemPrompt);
        systemParts.add(systemPart);
        systemInstruction.add("parts", systemParts);
        body.add("system_instruction", systemInstruction);

        // User content
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", userPrompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        body.add("contents", contents);

        // Generation config
        JsonObject genConfig = new JsonObject();
        genConfig.addProperty("maxOutputTokens", config.maxTokens);
        body.add("generationConfig", genConfig);

        return body;
    }

    private AiRefactoringResponse parseResponse(String responseBody) {
        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray candidates = json.getAsJsonArray("candidates");

        if (candidates == null || candidates.size() == 0) {
            throw new RuntimeException("No candidates in Gemini response");
        }

        JsonObject candidate = candidates.get(0).getAsJsonObject();
        JsonObject content = candidate.getAsJsonObject("content");
        JsonArray parts = content.getAsJsonArray("parts");

        if (parts == null || parts.size() == 0) {
            throw new RuntimeException("No parts in Gemini response");
        }

        String text = parts.get(0).getAsJsonObject().get("text").getAsString();

        String suggestedCode = ResponseParsingUtils.extractCodeBlock(text);
        String explanation = ResponseParsingUtils.extractExplanation(text);

        // Parse usage data for token tracking
        long inputTokens = 0;
        long outputTokens = 0;
        double estimatedCost = 0.0;

        if (json.has("usageMetadata")) {
            JsonObject usage = json.getAsJsonObject("usageMetadata");
            if (usage.has("promptTokenCount")) {
                inputTokens = usage.get("promptTokenCount").getAsLong();
            }
            if (usage.has("candidatesTokenCount")) {
                outputTokens = usage.get("candidatesTokenCount").getAsLong();
            }
        }

        return new AiRefactoringResponse(suggestedCode, explanation, getDisplayName(),
            (int) inputTokens, (int) outputTokens, estimatedCost);
    }

}
