package com.github.ehs208.codemetrics.ai;

import com.github.ehs208.codemetrics.ai.config.AiRefactoringConfiguration;
import com.github.ehs208.codemetrics.ai.provider.ClaudeProvider;
import com.github.ehs208.codemetrics.ai.provider.OpenAiProvider;
import com.github.ehs208.codemetrics.ai.provider.GeminiProvider;
import com.github.ehs208.codemetrics.ai.provider.CodexOAuthProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AiProviderRegistry {
    private static final Map<String, AiRefactoringProvider> providers = Collections.synchronizedMap(new LinkedHashMap<>());

    static {
        register(new ClaudeProvider());
        register(new OpenAiProvider());
        register(new GeminiProvider());
        register(new CodexOAuthProvider());
    }

    public static void register(AiRefactoringProvider provider) {
        providers.put(provider.getId(), provider);
    }

    public static AiRefactoringProvider getActiveProvider() {
        String activeId = AiRefactoringConfiguration.getInstance().activeProviderId;
        return providers.get(activeId);
    }

    public static AiRefactoringProvider getProvider(String id) {
        return providers.get(id);
    }

    public static List<AiRefactoringProvider> getAllProviders() {
        return new ArrayList<>(providers.values());
    }
}
