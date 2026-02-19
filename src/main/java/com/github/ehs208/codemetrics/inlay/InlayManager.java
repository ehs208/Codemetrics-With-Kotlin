package com.github.ehs208.codemetrics.inlay;

import com.github.ehs208.codemetrics.core.MetricsModel;
import com.github.ehs208.codemetrics.core.config.MetricsConfiguration;
import com.github.ehs208.codemetrics.core.parser.MetricsParser;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.InlayModel;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class InlayManager {
  private Project project;

  public InlayManager(Project project) {
    this.project = project;
  }

  public void updateInlays(Editor editor, VirtualFile file) {
    ProgressManager.getInstance().run(
        new Task.Backgroundable(project, "Analyzing code complexity for " + file.getName()) {
          @Override
          public void run(@NotNull ProgressIndicator indicator) {
            indicator.setIndeterminate(true);
            indicator.setText("Computing complexity metrics...");

            List<MetricsModel> models = ReadAction.compute(() ->
                computeMetrics(file).collect(java.util.stream.Collectors.toList()));

            Application application = ApplicationManager.getApplication();
            application.invokeLater(() -> {
              disposeInlays(editor);
              models.forEach(model -> installInlay(editor, model));
            });
          }
        }
    );
  }

  private Stream<MetricsModel> computeMetrics(VirtualFile file) {
    if (project.isDisposed() || !file.isValid()) {
      return Stream.empty();
    }
    PsiFile psiFile;
    try {
      psiFile = PsiUtilBase.getPsiFile(project, file);
    } catch (AssertionError e) {
      return Stream.empty();
    }

    MetricsModel model = new MetricsParser().getMetrics(psiFile);
    MetricsConfiguration configuration = MetricsConfiguration.getInstance();
    configuration.validateAndFixState(); // Ensure configuration is valid
    Predicate<MetricsModel> metricsModelPredicate =
        p -> p.isVisible() && p.getCollectedComplexity() >= configuration.hiddenUnder;

    return streamMetricsModels(model, metricsModelPredicate);
  }

  Stream<MetricsModel> streamMetricsModels(
      MetricsModel parentNode, Predicate<MetricsModel> predicate) {
    if (parentNode.getChildren().isEmpty()) {
      return Stream.of(parentNode).filter(predicate);
    } else {
      return parentNode.getChildren().stream()
          .filter(Objects::nonNull)
          .filter(predicate)
          .map((MetricsModel current) -> streamMetricsModels(current, predicate))
          .reduce(Stream.of(parentNode).filter(predicate), Stream::concat);
    }
  }

  private void disposeInlays(Editor editor) {
    List<Inlay<? extends MetricsHintRenderer>> inlays = getInlays(editor);
    inlays.forEach(Disposer::dispose);
  }

  private void installInlay(Editor editor, MetricsModel value) {
    int textOffset = value.getTextOffset();
    InlayModel inlayModel = editor.getInlayModel();
    inlayModel.addAfterLineEndElement(textOffset, false, new MetricsHintRenderer(value));
  }

  @NotNull
  public List<Inlay<? extends MetricsHintRenderer>> getInlays(Editor editor) {
    int maxOffset = editor.getDocument().getTextLength();
    return editor
        .getInlayModel()
        .getAfterLineEndElementsInRange(0, maxOffset, MetricsHintRenderer.class);
  }
}
