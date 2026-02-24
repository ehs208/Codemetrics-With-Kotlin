package com.github.ehs208.codemetrics.ai;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface AiRefactoringProvider {
    String getId();
    String getDisplayName();
    boolean isConfigured();
    CompletableFuture<AiRefactoringResponse> suggestRefactoring(
        AiRefactoringRequest request, Consumer<String> progressCallback);
    CompletableFuture<Boolean> validateCredentials();
    default boolean supportsStreaming() { return false; }
}
