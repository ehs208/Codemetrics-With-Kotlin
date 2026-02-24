package com.github.ehs208.codemetrics.ai.history;

import java.time.Instant;

public class RefactoringHistoryEntry {
    public long timestamp;
    public String methodName;
    public String fileName;
    public String providerName;
    public long originalComplexity;
    public long newComplexity;
    public boolean applied;
    public String explanation;

    public RefactoringHistoryEntry() {} // For serialization

    public RefactoringHistoryEntry(String methodName, String fileName, String providerName,
                                    long originalComplexity, boolean applied, String explanation) {
        this.timestamp = Instant.now().toEpochMilli();
        this.methodName = methodName;
        this.fileName = fileName;
        this.providerName = providerName;
        this.originalComplexity = originalComplexity;
        this.newComplexity = -1; // Set after re-analysis
        this.applied = applied;
        this.explanation = explanation;
    }

    public String getFormattedTime() {
        java.time.LocalDateTime dt = java.time.LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp), java.time.ZoneId.systemDefault());
        return java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(dt);
    }
}
