package com.github.ehs208.codemetrics.ai.action;

import com.github.ehs208.codemetrics.ai.AiProviderRegistry;
import com.github.ehs208.codemetrics.ai.BatchRefactoringService;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class BatchRefactoringAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) return;

        BatchRefactoringService service = project.getService(BatchRefactoringService.class);
        service.batchRefactorFile(psiFile);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        var provider = AiProviderRegistry.getActiveProvider();

        boolean enabled = project != null
            && psiFile != null
            && (psiFile.getName().endsWith(".java") || psiFile.getName().endsWith(".kt"))
            && provider != null
            && provider.isConfigured();

        e.getPresentation().setEnabledAndVisible(enabled);
    }
}
