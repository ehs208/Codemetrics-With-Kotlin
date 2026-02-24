package com.github.ehs208.codemetrics.ai.action;

import com.github.ehs208.codemetrics.ai.AiProviderRegistry;
import com.github.ehs208.codemetrics.ai.AiRefactoringProvider;
import com.github.ehs208.codemetrics.ai.RefactoringService;
import com.github.ehs208.codemetrics.ai.config.AiRefactoringConfiguration;
import com.github.ehs208.codemetrics.core.MetricsModel;
import com.github.ehs208.codemetrics.inlay.MetricsHintRenderer;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RefactoringIntentionAction extends PsiElementBaseIntentionAction {

    @NotNull
    @Override
    public String getText() {
        return "Reduce complexity with AI refactoring";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "CodeMetrics";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if (editor == null) return false;
        AiRefactoringProvider provider = AiProviderRegistry.getActiveProvider();
        if (provider == null || !provider.isConfigured()) return false;

        String fileName = element.getContainingFile() != null ? element.getContainingFile().getName() : "";
        if (!fileName.endsWith(".java") && !fileName.endsWith(".kt")) return false;

        MetricsModel model = findMetricsModelAtElement(editor, element);
        if (model == null) return false;

        AiRefactoringConfiguration config = AiRefactoringConfiguration.getInstance();
        return model.getCollectedComplexity() >= config.intentionThreshold;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element)
            throws IncorrectOperationException {
        MetricsModel model = findMetricsModelAtElement(editor, element);
        if (model == null) return;

        RefactoringService service = project.getService(RefactoringService.class);
        service.suggestRefactoring(editor, model);
    }

    private MetricsModel findMetricsModelAtElement(Editor editor, PsiElement element) {
        int offset = element.getTextOffset();
        int line = editor.getDocument().getLineNumber(offset);
        int lineStart = editor.getDocument().getLineStartOffset(line);
        int lineEnd = editor.getDocument().getLineEndOffset(line);

        List<Inlay<? extends MetricsHintRenderer>> inlays =
            editor.getInlayModel().getAfterLineEndElementsInRange(
                lineStart, lineEnd, MetricsHintRenderer.class);

        if (!inlays.isEmpty()) {
            return inlays.get(0).getRenderer().getModel();
        }
        return null;
    }
}
