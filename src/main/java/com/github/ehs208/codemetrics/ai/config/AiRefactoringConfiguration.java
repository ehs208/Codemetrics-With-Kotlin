package com.github.ehs208.codemetrics.ai.config;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "CodeMetricsAiConfiguration",
    storages = {@Storage("CodeMetricsAiConfiguration.xml")})
public class AiRefactoringConfiguration implements PersistentStateComponent<AiRefactoringConfiguration> {

    public String activeProviderId = "Claude";
    public String claudeModel = "claude-opus-4-8";
    public int maxTokens = 8192;
    public boolean includeClassContext = true;
    public String openaiModel = "gpt-5.5";
    public String geminiModel = "gemini-3.5-flash";
    public String codexModel = "gpt-5.3-codex";
    public String reasoningEffort = "medium";
    public String claudeEffort = "high";
    public String geminiThinkingLevel = "high";
    public String openAiTextVerbosity = "medium";
    public int intentionThreshold = 5;
    public String customPromptTemplate = "";

    // Cache API keys to avoid slow PasswordSafe.get() calls on EDT (e.g. during isModified() polling)
    // Uses ConcurrentHashMap for thread safety; empty string "" is sentinel for "key not set"
    private static final String NO_KEY_SENTINEL = "";
    private final java.util.concurrent.ConcurrentHashMap<String, String> apiKeyCache = new java.util.concurrent.ConcurrentHashMap<>();

    public static AiRefactoringConfiguration getInstance() {
        return ApplicationManager.getApplication().getService(AiRefactoringConfiguration.class);
    }

    @Nullable
    @Override
    public AiRefactoringConfiguration getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AiRefactoringConfiguration state) {
        XmlSerializerUtil.copyBean(state, this);
        migrateRetiredModels();
    }

    /**
     * Move persisted settings off models that providers have retired, so existing
     * users are not stuck sending requests that always fail.
     * Gemini 3 Pro Preview was shut down (2026-03-09).
     */
    private void migrateRetiredModels() {
        if ("gemini-3-pro-preview".equals(geminiModel)) {
            geminiModel = "gemini-3.5-flash";
        }
    }

    public void setApiKey(String providerId, String apiKey) {
        CredentialAttributes attributes = createCredentialAttributes(providerId);
        if (apiKey == null || apiKey.isEmpty()) {
            PasswordSafe.getInstance().set(attributes, null);
            apiKeyCache.put(providerId, NO_KEY_SENTINEL);
        } else {
            PasswordSafe.getInstance().set(attributes, new Credentials(providerId, apiKey));
            apiKeyCache.put(providerId, apiKey);
        }
    }

    @Nullable
    public String getApiKey(String providerId) {
        String cached = apiKeyCache.get(providerId);
        if (cached != null) {
            return NO_KEY_SENTINEL.equals(cached) ? null : cached;
        }
        // PasswordSafe.get() is a slow operation prohibited inside read actions
        // (e.g. when called from IntentionAction.isAvailable during highlighting).
        // On cache miss inside a read action, return null; the cache will be populated
        // on the next call from a non-read context (settings panel, actual refactoring).
        if (ApplicationManager.getApplication().isReadAccessAllowed()) {
            return null;
        }
        CredentialAttributes attributes = createCredentialAttributes(providerId);
        Credentials credentials = PasswordSafe.getInstance().get(attributes);
        String key = credentials != null ? credentials.getPasswordAsString() : null;
        apiKeyCache.put(providerId, key != null ? key : NO_KEY_SENTINEL);
        return key;
    }

    private CredentialAttributes createCredentialAttributes(String key) {
        return new CredentialAttributes(
            CredentialAttributesKt.generateServiceName("CodeMetrics.AI", key)
        );
    }
}
