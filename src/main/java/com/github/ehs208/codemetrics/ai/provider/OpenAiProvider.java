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

public class OpenAiProvider implements AiRefactoringProvider {

    private static final String API_URL = "https://api.openai.com/v1/responses";
    private final HttpClient httpClient;

    public OpenAiProvider() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    }

    @Override
    public String getId() {
        return "OpenAI";
    }

    @Override
    public String getDisplayName() {
        return "OpenAI";
    }

    @Override
    public boolean isConfigured() {
        String apiKey = AiRefactoringConfiguration.getInstance().getApiKey("OpenAI");
        return apiKey != null && !apiKey.isEmpty();
    }

    @Override
    public CompletableFuture<AiRefactoringResponse> suggestRefactoring(
            AiRefactoringRequest request, Consumer<String> progressCallback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                progressCallback.accept("Building prompt...");
                AiRefactoringConfiguration config = AiRefactoringConfiguration.getInstance();
                String apiKey = config.getApiKey("OpenAI");

                if (apiKey == null || apiKey.isEmpty()) {
                    throw new RuntimeException("OpenAI API key not configured. " +
                        "Please set it in Settings > Code Metrics With Kotlin > AI Refactoring.");
                }

                String systemPrompt = PromptBuilder.buildSystemPrompt();
                String userPrompt = PromptBuilder.buildFinalUserPrompt(request);

                progressCallback.accept("Sending request to OpenAI...");

                JsonObject body = buildRequestBody(config, systemPrompt, userPrompt);

                HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .timeout(Duration.ofSeconds(120))
                    .build();

                HttpResponse<String> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException(
                        "OpenAI API error (HTTP " + response.statusCode() + "): " + ResponseParsingUtils.extractErrorMessage(response.body()));
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
                String apiKey = config.getApiKey("OpenAI");
                if (apiKey == null || apiKey.isEmpty()) return false;

                JsonObject body = new JsonObject();
                body.addProperty("model", config.openaiModel);
                body.addProperty("input", "Hi");
                body.addProperty("max_output_tokens", 32);
                body.addProperty("store", false);
                addOpenAiTextConfig(body, config);
                addOpenAiReasoning(body, config);

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
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

    public boolean supportsStreaming() {
        return true;
    }

    private JsonObject buildRequestBody(AiRefactoringConfiguration config,
                                        String systemPrompt, String userPrompt) {
        JsonObject body = new JsonObject();
        body.addProperty("model", config.openaiModel);
        body.addProperty("instructions", systemPrompt);
        body.addProperty("max_output_tokens", config.maxTokens);
        body.addProperty("store", false);
        addOpenAiTextConfig(body, config);
        addOpenAiReasoning(body, config);

        JsonArray input = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", userPrompt);
        input.add(userMessage);
        body.add("input", input);

        return body;
    }

    private void addOpenAiReasoning(JsonObject body, AiRefactoringConfiguration config) {
        String effort = normalizeOpenAiReasoningEffort(config.openaiModel, config.reasoningEffort);
        if (effort == null) {
            return;
        }

        JsonObject reasoning = new JsonObject();
        reasoning.addProperty("effort", effort);
        body.add("reasoning", reasoning);
    }

    private String normalizeOpenAiReasoningEffort(String model, String effort) {
        if (!supportsReasoning(model) || effort == null || effort.isEmpty()) {
            return null;
        }

        if (isProReasoningModel(model)) {
            if ("none".equals(effort) || "minimal".equals(effort) || "low".equals(effort)) {
                return "medium";
            }
            return effort;
        }

        if (supportsNoneAndXHighReasoning(model)) {
            return "minimal".equals(effort) ? "low" : effort;
        }

        if (supportsNoneReasoning(model)) {
            if ("minimal".equals(effort)) {
                return "low";
            }
            if ("xhigh".equals(effort)) {
                return "high";
            }
            return effort;
        }

        if (model.startsWith("o")) {
            if ("none".equals(effort) || "minimal".equals(effort)) {
                return "low";
            }
            if ("xhigh".equals(effort)) {
                return "high";
            }
            return effort;
        }

        if ("none".equals(effort)) {
            return "minimal";
        }
        if ("xhigh".equals(effort)) {
            return "high";
        }
        return effort;
    }

    private boolean supportsReasoning(String model) {
        return model != null && (model.startsWith("gpt-5") || model.startsWith("o"));
    }

    private boolean isProReasoningModel(String model) {
        return model != null && (model.equals("gpt-5.5-pro")
            || model.equals("gpt-5.4-pro")
            || model.equals("gpt-5.2-pro")
            || model.equals("gpt-5-pro"));
    }

    private boolean supportsNoneAndXHighReasoning(String model) {
        return model != null && (
            model.equals("gpt-5.5")
                || model.startsWith("gpt-5.5-")
                || model.equals("gpt-5.4")
                || model.startsWith("gpt-5.4-")
                || model.equals("gpt-5.2")
                || model.startsWith("gpt-5.2-"))
            && !isProReasoningModel(model);
    }

    private boolean supportsNoneReasoning(String model) {
        return model != null && (model.equals("gpt-5.1") || model.startsWith("gpt-5.1-"))
            && !model.contains("codex");
    }

    private void addOpenAiTextConfig(JsonObject body, AiRefactoringConfiguration config) {
        if (!supportsTextVerbosity(config.openaiModel)) {
            return;
        }

        String verbosity = config.openAiTextVerbosity;
        if (verbosity == null || verbosity.isEmpty()) {
            return;
        }

        JsonObject text = new JsonObject();
        JsonObject format = new JsonObject();
        format.addProperty("type", "text");
        text.add("format", format);
        text.addProperty("verbosity", verbosity);
        body.add("text", text);
    }

    private boolean supportsTextVerbosity(String model) {
        return model != null && model.startsWith("gpt-5");
    }

    private AiRefactoringResponse parseResponse(String responseBody) {
        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
        String text = extractOutputText(json);

        String suggestedCode = ResponseParsingUtils.extractCodeBlock(text);
        String explanation = ResponseParsingUtils.extractExplanation(text);

        long inputTokens = 0;
        long outputTokens = 0;
        double estimatedCost = 0.0;

        if (json.has("usage")) {
            JsonObject usage = json.getAsJsonObject("usage");
            if (usage.has("input_tokens")) {
                inputTokens = usage.get("input_tokens").getAsLong();
            }
            if (usage.has("output_tokens")) {
                outputTokens = usage.get("output_tokens").getAsLong();
            }
        }

        return new AiRefactoringResponse(suggestedCode, explanation, getDisplayName(),
            (int) inputTokens, (int) outputTokens, estimatedCost);
    }

    private String extractOutputText(JsonObject json) {
        if (hasString(json, "output_text")) {
            return json.get("output_text").getAsString();
        }

        JsonArray output = json.getAsJsonArray("output");
        if (output == null || output.size() == 0) {
            throw new RuntimeException("No output in OpenAI response");
        }

        StringBuilder fullText = new StringBuilder();
        for (JsonElement outputElement : output) {
            if (!outputElement.isJsonObject()) {
                continue;
            }
            JsonObject outputItem = outputElement.getAsJsonObject();
            JsonArray content = outputItem.getAsJsonArray("content");
            if (content == null) {
                continue;
            }
            for (JsonElement contentElement : content) {
                if (!contentElement.isJsonObject()) {
                    continue;
                }
                JsonObject contentItem = contentElement.getAsJsonObject();
                String type = hasString(contentItem, "type") ? contentItem.get("type").getAsString() : "";
                if (("output_text".equals(type) || "text".equals(type)) && hasString(contentItem, "text")) {
                    fullText.append(contentItem.get("text").getAsString());
                }
            }
        }

        if (fullText.length() == 0) {
            throw new RuntimeException("No text output in OpenAI response");
        }

        return fullText.toString();
    }

    private boolean hasString(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() && json.get(key).isJsonPrimitive();
    }

}
