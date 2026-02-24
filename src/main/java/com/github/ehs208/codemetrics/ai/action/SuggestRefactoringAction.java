package com.github.ehs208.codemetrics.ai.action;

import com.github.ehs208.codemetrics.ai.RefactoringService;
import com.github.ehs208.codemetrics.core.MetricsModel;
import com.github.ehs208.codemetrics.inlay.MetricsHintRenderer;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SuggestRefactoringAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        Project project = event.getData(CommonDataKeys.PROJECT);
        if (editor == null || project == null) return;

        MetricsModel model = findMetricsModelAtCaret(editor);
        if (model == null) return;

        RefactoringService service = project.getService(RefactoringService.class);
        service.suggestRefactoring(editor, model);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        Project project = event.getData(CommonDataKeys.PROJECT);

        boolean enabled = editor != null && project != null
            && findMetricsModelAtCaret(editor) != null;
        event.getPresentation().setEnabledAndVisible(enabled);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Nullable
    private MetricsModel findMetricsModelAtCaret(Editor editor) {
        int offset = editor.getCaretModel().getOffset();
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
