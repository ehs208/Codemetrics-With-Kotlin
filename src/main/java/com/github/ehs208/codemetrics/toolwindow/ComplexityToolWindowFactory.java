package com.github.ehs208.codemetrics.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class ComplexityToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JBTabbedPane tabbedPane = new JBTabbedPane();

        ComplexityAnalysisPanel analysisPanel = new ComplexityAnalysisPanel(project);
        tabbedPane.addTab("Analysis", analysisPanel);

        RefactoringHistoryPanel historyPanel = new RefactoringHistoryPanel(project);
        tabbedPane.addTab("History", historyPanel);

        Content content = ContentFactory.getInstance().createContent(tabbedPane, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }
}