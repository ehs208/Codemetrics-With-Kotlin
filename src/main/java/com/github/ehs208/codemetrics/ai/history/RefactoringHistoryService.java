package com.github.ehs208.codemetrics.ai.history;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service(Service.Level.PROJECT)
@State(name = "CodeMetricsRefactoringHistory",
       storages = {@Storage("CodeMetricsRefactoringHistory.xml")})
public final class RefactoringHistoryService implements PersistentStateComponent<RefactoringHistoryService> {

    private static final int MAX_ENTRIES = 100;

    public List<RefactoringHistoryEntry> entries = Collections.synchronizedList(new ArrayList<>());

    public static RefactoringHistoryService getInstance(Project project) {
        return project.getService(RefactoringHistoryService.class);
    }

    public void addEntry(RefactoringHistoryEntry entry) {
        synchronized (entries) {
            entries.add(0, entry); // Most recent first
            while (entries.size() > MAX_ENTRIES) {
                entries.remove(entries.size() - 1);
            }
        }
    }

    public List<RefactoringHistoryEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    public void clearHistory() {
        entries.clear();
    }

    @Nullable
    @Override
    public RefactoringHistoryService getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull RefactoringHistoryService state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
