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
import com.intellij.notification.NotificationType;
import com.intellij.openapi.ui.Messages;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class CodexOAuthProvider implements AiRefactoringProvider {

    private static final String API_URL = "https://chatgpt.com/backend-api/codex/responses";
    private static final String AUTH_URL = "https://auth.openai.com/oauth/authorize";
    private static final String TOKEN_URL = "https://auth.openai.com/oauth/token";
    private static final String CLIENT_ID = "app_EMoamEEZ73f0CkXaXp7hrann";
    private static final String REDIRECT_URI = "http://localhost:1455/auth/callback";
    private static final String API_AUDIENCE = "https://api.openai.com/v1";
    private static final String JWT_CLAIM_PATH = "https://api.openai.com/auth";
    private static final int CALLBACK_PORT = 1455;

    private final HttpClient httpClient;

    public CodexOAuthProvider() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    }

    @Override
    public String getId() {
        return "Codex";
    }

    @Override
    public String getDisplayName() {
        return "Codex (ChatGPT Login)";
    }

    @Override
    public boolean isConfigured() {
        AiRefactoringConfiguration config = AiRefactoringConfiguration.getInstance();
        String accessToken = config.getApiKey("Codex");
        String accountId = config.getApiKey("Codex-account-id");
        return accessToken != null && !accessToken.isEmpty()
            && accountId != null && !accountId.isEmpty();
    }

    /**
     * Check if the user is currently logged in (has valid token + account ID).
     */
    public boolean isLoggedIn() {
        return isConfigured();
    }

    /**
     * Get the logged-in account ID for display.
     */
    public String getAccountId() {
        return AiRefactoringConfiguration.getInstance().getApiKey("Codex-account-id");
    }

    /**
     * Logout: clear stored tokens.
     */
    public void logout() {
        clearAllTokens(AiRefactoringConfiguration.getInstance());
    }

    @Override
    public CompletableFuture<AiRefactoringResponse> suggestRefactoring(
            AiRefactoringRequest request, Consumer<String> progressCallback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                progressCallback.accept("Building prompt...");
                AiRefactoringConfiguration config = AiRefactoringConfiguration.getInstance();

                // Auto-refresh token if expired
                ensureValidToken(config, progressCallback);

                String accessToken = config.getApiKey("Codex");
                String accountId = config.getApiKey("Codex-account-id");

                if (accessToken == null || accessToken.isEmpty()) {
                    throw new RuntimeException("Codex OAuth not configured. " +
                        "Please authenticate in Settings > Code Metrics With Kotlin > AI Refactoring.");
                }

                String systemPrompt = PromptBuilder.buildSystemPrompt();
                String userPrompt = PromptBuilder.buildFinalUserPrompt(request);

                progressCallback.accept("Sending request to Codex...");

                JsonObject body = buildRequestBody(config, systemPrompt, userPrompt);
                HttpRequest httpRequest = buildHttpRequest(accessToken, accountId, body);

                HttpResponse<String> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofString());

                // If 401, try refreshing token once and retry
                if (response.statusCode() == 401) {
                    progressCallback.accept("Token expired, refreshing...");
                    if (refreshAccessToken(config)) {
                        accessToken = config.getApiKey("Codex");
                        httpRequest = buildHttpRequest(accessToken, accountId, body);
                        response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                    }
                }

                if (response.statusCode() != 200) {
                    throw new RuntimeException(
                        "Codex API error (HTTP " + response.statusCode() + "): " + ResponseParsingUtils.extractErrorMessage(response.body()));
                }

                progressCallback.accept("Parsing response...");
                return parseSseResponse(response.body());

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
                String accessToken = config.getApiKey("Codex");
                String accountId = config.getApiKey("Codex-account-id");

                if (accessToken == null || accessToken.isEmpty()) {
                    return false;
                }
                if (accountId == null || accountId.isEmpty()) {
                    return false;
                }

                JsonObject body = new JsonObject();
                body.addProperty("model", config.codexModel);
                body.addProperty("instructions", "Reply with OK.");
                JsonArray input = new JsonArray();
                JsonObject msg = new JsonObject();
                msg.addProperty("role", "user");
                msg.addProperty("content", "Hi");
                input.add(msg);
                body.add("input", input);
                body.addProperty("store", false);
                body.addProperty("stream", true);

                HttpRequest request = buildHttpRequest(accessToken, accountId, body);

                HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

                // Try refresh on 401
                if (response.statusCode() == 401 && refreshAccessToken(config)) {
                    accessToken = config.getApiKey("Codex");
                    request = buildHttpRequest(accessToken, accountId, body);
                    response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                }

                return response.statusCode() == 200;
            } catch (Exception e) {
                return false;
            }
        });
    }

    @Override
    public boolean supportsStreaming() {
        return true;
    }

    // ---- Token Refresh ----

    /**
     * Check if the access token is expired and refresh it proactively.
     */
    private void ensureValidToken(AiRefactoringConfiguration config, Consumer<String> progressCallback) {
        String expiryStr = config.getApiKey("Codex-token-expiry");
        if (expiryStr == null || expiryStr.isEmpty()) return;

        try {
            long expiryEpoch = Long.parseLong(expiryStr);
            // Refresh 60 seconds before actual expiry
            if (Instant.now().getEpochSecond() >= expiryEpoch - 60) {
                progressCallback.accept("Refreshing access token...");
                refreshAccessToken(config);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    /**
     * Use the stored refresh token to obtain a new access token.
     * Returns true if refresh succeeded.
     */
    private synchronized boolean refreshAccessToken(AiRefactoringConfiguration config) {
        String refreshToken = config.getApiKey("Codex-refresh-token");
        if (refreshToken == null || refreshToken.isEmpty()) return false;

        try {
            String body = "grant_type=refresh_token" +
                "&client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8) +
                "&refresh_token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(30))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                clearAllTokens(config);
                return false;
            }

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            if (!json.has("access_token")) {
                clearAllTokens(config);
                return false;
            }

            String newAccessToken = json.get("access_token").getAsString();
            config.setApiKey("Codex", newAccessToken);

            // Update refresh token if a new one is provided (token rotation)
            if (json.has("refresh_token")) {
                config.setApiKey("Codex-refresh-token", json.get("refresh_token").getAsString());
            }

            // Update expiry
            if (json.has("expires_in")) {
                long expiresIn = json.get("expires_in").getAsLong();
                config.setApiKey("Codex-token-expiry",
                    String.valueOf(Instant.now().getEpochSecond() + expiresIn));
            }

            // Update account ID from new token if needed
            String accountId = extractAccountIdFromJwt(newAccessToken);
            if (accountId != null && !accountId.isEmpty()) {
                config.setApiKey("Codex-account-id", accountId);
            }

            return true;
        } catch (Exception e) {
            clearAllTokens(config);
            return false;
        }
    }

    private void clearAllTokens(AiRefactoringConfiguration config) {
        config.setApiKey("Codex", null);
        config.setApiKey("Codex-account-id", null);
        config.setApiKey("Codex-refresh-token", null);
        config.setApiKey("Codex-token-expiry", null);
    }

    // ---- OAuth Flow ----

    public CompletableFuture<Boolean> startOAuthFlow() {
        return CompletableFuture.supplyAsync(() -> {
            HttpServer server = null;
            try {
                String codeVerifier = generateCodeVerifier();
                String codeChallenge = generateCodeChallenge(codeVerifier);
                String state = generateCodeVerifier();

                server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), CALLBACK_PORT), 0);
                final HttpServer finalServer = server;

                CountDownLatch latch = new CountDownLatch(1);
                AtomicReference<String> authCode = new AtomicReference<>();

                server.createContext("/auth/callback", exchange -> {
                    try {
                        String query = exchange.getRequestURI().getQuery();
                        String returnedState = extractQueryParam(query, "state");

                        if (returnedState == null || !returnedState.equals(state)) {
                            sendErrorResponse(exchange, "Invalid state parameter (CSRF check failed)");
                            return;
                        }

                        String code = extractQueryParam(query, "code");
                        if (code != null && !code.isEmpty()) {
                            authCode.set(code);
                            sendSuccessResponse(exchange);
                        } else {
                            String error = extractQueryParam(query, "error");
                            sendErrorResponse(exchange, error != null
                                ? "Authorization failed: " + error
                                : "Authorization failed: no code received");
                        }
                    } finally {
                        latch.countDown();
                    }
                });

                server.start();

                String authorizationUrl = AUTH_URL +
                    "?response_type=code" +
                    "&client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8) +
                    "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                    "&scope=" + URLEncoder.encode("openid profile email offline_access", StandardCharsets.UTF_8) +
                    "&audience=" + URLEncoder.encode(API_AUDIENCE, StandardCharsets.UTF_8) +
                    "&code_challenge=" + URLEncoder.encode(codeChallenge, StandardCharsets.UTF_8) +
                    "&code_challenge_method=S256" +
                    "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8) +
                    "&codex_cli_simplified_flow=true" +
                    "&originator=codex_cli_rs";

                boolean browserOpened = false;
                if (Desktop.isDesktopSupported()
                        && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(URI.create(authorizationUrl));
                        browserOpened = true;
                    } catch (Exception ignored) {
                        // Browser launch failed, fall through to headless
                    }
                }

                if (browserOpened) {
                    boolean received = latch.await(5, TimeUnit.MINUTES);
                    finalServer.stop(0);

                    if (!received || authCode.get() == null) {
                        showNotification("OAuth Failed", "Timed out or cancelled.", NotificationType.ERROR);
                        return false;
                    }
                } else {
                    // Headless fallback: show dialog for manual URL copy/paste
                    finalServer.stop(0);
                    String code = promptHeadlessAuth(authorizationUrl, state);
                    if (code == null || code.isEmpty()) {
                        showNotification("OAuth Cancelled", "Authentication was cancelled.", NotificationType.WARNING);
                        return false;
                    }
                    authCode.set(code);
                }

                return exchangeAndStoreTokens(authCode.get(), codeVerifier);

            } catch (Exception e) {
                showNotification("OAuth Error", e.getMessage(), NotificationType.ERROR);
                return false;
            } finally {
                if (server != null) server.stop(0);
            }
        });
    }

    /**
     * Headless fallback: prompt user to manually copy the auth URL and paste
     * the callback URL after authenticating in their browser.
     * Returns the authorization code, or null if cancelled.
     */
    private String promptHeadlessAuth(String authorizationUrl, String expectedState) {
        AtomicReference<String> result = new AtomicReference<>();
        CountDownLatch dialogLatch = new CountDownLatch(1);

        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
            try {
                String callbackUrl = Messages.showInputDialog(
                    "Could not open browser automatically.\n\n" +
                    "1. Copy the URL below and open it in your browser:\n" +
                    authorizationUrl + "\n\n" +
                    "2. After logging in, copy the full callback URL from your browser\n" +
                    "   (it starts with http://localhost:" + CALLBACK_PORT + "/auth/callback?...)\n\n" +
                    "3. Paste it below:",
                    "Codex OAuth - Manual Login",
                    Messages.getInformationIcon(),
                    "",
                    null);

                if (callbackUrl != null && !callbackUrl.isEmpty()) {
                    try {
                        URI uri = URI.create(callbackUrl.trim());
                        String query = uri.getQuery();

                        // Validate CSRF state parameter
                        String returnedState = extractQueryParam(query, "state");
                        if (returnedState == null || !returnedState.equals(expectedState)) {
                            Messages.showErrorDialog(
                                "Invalid state parameter (CSRF check failed). Please try again.",
                                "OAuth Error");
                            return;
                        }

                        String code = extractQueryParam(query, "code");
                        result.set(code);
                    } catch (IllegalArgumentException e) {
                        Messages.showErrorDialog(
                            "Invalid URL format. Please paste the full callback URL.",
                            "OAuth Error");
                    }
                }
            } finally {
                dialogLatch.countDown();
            }
        });

        try {
            dialogLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return result.get();
    }

    private boolean exchangeAndStoreTokens(String code, String codeVerifier) throws Exception {
        String body = "grant_type=authorization_code" +
            "&client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8) +
            "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
            "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
            "&code_verifier=" + URLEncoder.encode(codeVerifier, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(TOKEN_URL))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .timeout(Duration.ofSeconds(30))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            showNotification("OAuth Failed",
                "Token exchange failed (HTTP " + response.statusCode() + ")", NotificationType.ERROR);
            return false;
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        if (!json.has("access_token")) {
            showNotification("OAuth Failed", "No access token in response.", NotificationType.ERROR);
            return false;
        }

        String accessToken = json.get("access_token").getAsString();
        AiRefactoringConfiguration config = AiRefactoringConfiguration.getInstance();
        config.setApiKey("Codex", accessToken);

        // Store refresh token for automatic renewal
        if (json.has("refresh_token")) {
            config.setApiKey("Codex-refresh-token", json.get("refresh_token").getAsString());
        }

        // Store token expiry time
        if (json.has("expires_in")) {
            long expiresIn = json.get("expires_in").getAsLong();
            config.setApiKey("Codex-token-expiry",
                String.valueOf(Instant.now().getEpochSecond() + expiresIn));
        }

        // Extract chatgpt_account_id from access_token JWT, fallback to id_token
        String accountId = extractAccountIdFromJwt(accessToken);
        if (accountId == null && json.has("id_token")) {
            accountId = extractAccountIdFromJwt(json.get("id_token").getAsString());
        }

        if (accountId != null && !accountId.isEmpty()) {
            config.setApiKey("Codex-account-id", accountId);
            showNotification("OAuth Success", "Authenticated with ChatGPT!", NotificationType.INFORMATION);
        } else {
            showNotification("OAuth Warning",
                "Authenticated but could not extract account ID.", NotificationType.WARNING);
        }

        return true;
    }

    // ---- HTTP Request Builder ----

    private HttpRequest buildHttpRequest(String accessToken, String accountId, JsonObject body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(API_URL))
            .header("Content-Type", "application/json")
            .header("Accept", "text/event-stream")
            .header("Authorization", "Bearer " + accessToken)
            .header("OpenAI-Beta", "responses=experimental")
            .header("originator", "codex_cli_rs")
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .timeout(Duration.ofSeconds(120));

        if (accountId != null && !accountId.isEmpty()) {
            builder.header("chatgpt-account-id", accountId);
        }

        return builder.build();
    }

    /**
     * Build request body for ChatGPT Codex Responses API.
     * Note: max_output_tokens is NOT supported - omit it.
     */
    private JsonObject buildRequestBody(AiRefactoringConfiguration config,
                                        String systemPrompt, String userPrompt) {
        JsonObject body = new JsonObject();
        body.addProperty("model", config.codexModel);
        body.addProperty("instructions", systemPrompt);

        JsonArray inputArray = new JsonArray();
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userPrompt);
        inputArray.add(userMsg);
        body.add("input", inputArray);

        body.addProperty("store", false);
        body.addProperty("stream", true);

        // Add reasoning effort if not empty
        String effort = config.reasoningEffort;
        if (effort != null && !effort.isEmpty() && !"none".equals(effort)) {
            JsonObject reasoning = new JsonObject();
            reasoning.addProperty("effort", effort);
            body.add("reasoning", reasoning);
        }

        return body;
    }

    // ---- SSE Response Parsing ----

    /**
     * Parse SSE (Server-Sent Events) response.
     * Looks for "response.done" or "response.completed" event to extract the final response.
     */
    private AiRefactoringResponse parseSseResponse(String sseBody) {
        JsonObject finalResponse = null;

        for (String line : sseBody.split("\n")) {
            if (!line.startsWith("data: ")) continue;
            String jsonStr = line.substring(6).trim();
            if (jsonStr.isEmpty() || "[DONE]".equals(jsonStr)) continue;

            try {
                JsonObject event = JsonParser.parseString(jsonStr).getAsJsonObject();
                String type = event.has("type") ? event.get("type").getAsString() : "";

                if ("response.done".equals(type) || "response.completed".equals(type)) {
                    finalResponse = event.has("response")
                        ? event.getAsJsonObject("response")
                        : event;
                    break;
                }
            } catch (Exception ignored) {
                // Skip malformed JSON
            }
        }

        if (finalResponse == null) {
            // Fallback: try to parse the entire body as JSON (non-SSE response)
            try {
                finalResponse = JsonParser.parseString(sseBody).getAsJsonObject();
            } catch (Exception e) {
                throw new RuntimeException("Could not parse Codex response. Raw: " +
                    sseBody.substring(0, Math.min(300, sseBody.length())));
            }
        }

        return extractFromResponse(finalResponse);
    }

    private AiRefactoringResponse extractFromResponse(JsonObject json) {
        JsonArray output = json.getAsJsonArray("output");

        if (output == null || output.size() == 0) {
            throw new RuntimeException("No output in Codex response");
        }

        StringBuilder fullText = new StringBuilder();
        for (JsonElement outputItem : output) {
            JsonObject item = outputItem.getAsJsonObject();
            String type = item.has("type") ? item.get("type").getAsString() : "";

            if ("message".equals(type)) {
                JsonArray contentArray = item.getAsJsonArray("content");
                if (contentArray != null) {
                    for (JsonElement contentItem : contentArray) {
                        JsonObject c = contentItem.getAsJsonObject();
                        String cType = c.has("type") ? c.get("type").getAsString() : "";
                        if ("output_text".equals(cType) && c.has("text")) {
                            fullText.append(c.get("text").getAsString());
                        }
                    }
                }
            }
        }

        String text = fullText.toString();
        if (text.isEmpty()) {
            throw new RuntimeException("No text content in Codex response");
        }

        String suggestedCode = ResponseParsingUtils.extractCodeBlock(text);
        String explanation = ResponseParsingUtils.extractExplanation(text);

        int inputTokens = 0;
        int outputTokens = 0;
        if (json.has("usage")) {
            JsonObject usage = json.getAsJsonObject("usage");
            if (usage.has("input_tokens")) inputTokens = usage.get("input_tokens").getAsInt();
            if (usage.has("output_tokens")) outputTokens = usage.get("output_tokens").getAsInt();
        }

        return new AiRefactoringResponse(suggestedCode, explanation, getDisplayName(),
            inputTokens, outputTokens, 0.0);
    }

    // ---- JWT Parsing ----

    private String extractAccountIdFromJwt(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return null;

            String payload = parts[1];
            int padding = (4 - payload.length() % 4) % 4;
            payload = payload + "=".repeat(padding);

            byte[] decoded = Base64.getUrlDecoder().decode(payload);
            String jsonStr = new String(decoded, StandardCharsets.UTF_8);
            JsonObject claims = JsonParser.parseString(jsonStr).getAsJsonObject();

            if (claims.has(JWT_CLAIM_PATH)) {
                JsonObject authClaims = claims.getAsJsonObject(JWT_CLAIM_PATH);
                if (authClaims.has("chatgpt_account_id")) {
                    return authClaims.get("chatgpt_account_id").getAsString();
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // ---- Utility ----

    private String generateCodeVerifier() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateCodeChallenge(String codeVerifier) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    private String extractQueryParam(String query, String param) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(param)) {
                return java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private void sendSuccessResponse(HttpExchange exchange) throws IOException {
        String html = "<html><body><h1>Authentication Successful!</h1>" +
            "<p>You can close this window and return to IntelliJ IDEA.</p></body></html>";
        byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, htmlBytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(htmlBytes); }
    }

    private void sendErrorResponse(HttpExchange exchange, String error) throws IOException {
        String safeError = error.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
        String html = "<html><body><h1>Authentication Failed</h1><p>" + safeError + "</p></body></html>";
        byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(400, htmlBytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(htmlBytes); }
    }

    private void showNotification(String title, String content, NotificationType type) {
        com.intellij.notification.NotificationGroupManager.getInstance()
            .getNotificationGroup("CodeMetrics.AI")
            .createNotification(title, content, type)
            .notify(null);
    }

}
