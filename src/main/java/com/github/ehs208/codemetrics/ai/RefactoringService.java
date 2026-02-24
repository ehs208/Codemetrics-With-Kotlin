package com.github.ehs208.codemetrics.ai;

import com.github.ehs208.codemetrics.ai.config.AiRefactoringConfiguration;
import com.github.ehs208.codemetrics.ai.history.RefactoringHistoryEntry;
import com.github.ehs208.codemetrics.ai.history.RefactoringHistoryService;
import com.github.ehs208.codemetrics.ai.ui.RefactoringDiffViewer;
import com.github.ehs208.codemetrics.core.MetricsModel;
import com.github.ehs208.codemetrics.core.config.MetricsConfiguration;
import com.github.ehs208.codemetrics.core.parser.MetricsParser;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtNamedFunction;

import javax.swing.*;
import java.awt.*;
import java.util.List;

@Service(Service.Level.PROJECT)
public final class RefactoringService {
    private final Project project;

    public RefactoringService(Project project) {
        this.project = project;
    }

    public void suggestRefactoring(Editor editor, MetricsModel model) {
        AiRefactoringProvider provider = AiProviderRegistry.getActiveProvider();
        if (provider == null || !provider.isConfigured()) {
            showNotification("AI Provider Not Configured",
                "Please configure an AI provider in Settings \u2192 Code Metrics With Kotlin \u2192 AI Refactoring.",
                NotificationType.WARNING);
            return;
        }

        // Show banner on editor (must run on EDT)
        final JLabel[] bannerLabelHolder = new JLabel[1];
        Runnable showBanner = () -> {
            JPanel banner = new JPanel(new BorderLayout());
            banner.setBackground(new JBColor(new Color(0xE8F0FE), new Color(0x2D3548)));
            banner.setBorder(JBUI.Borders.compound(
                JBUI.Borders.customLine(new JBColor(0xC4C4C4, 0x4B4B4B), 0, 0, 1, 0),
                JBUI.Borders.empty(6, 12)));

            JLabel spinner = new JLabel(AllIcons.Process.Step_1);
            banner.add(spinner, BorderLayout.WEST);

            JLabel label = new JLabel("  Analyzing complexity with " + provider.getDisplayName() + "...");
            label.setFont(label.getFont().deriveFont(Font.PLAIN, 12f));
            banner.add(label, BorderLayout.CENTER);

            bannerLabelHolder[0] = label;
            editor.setHeaderComponent(banner);
        };

        if (SwingUtilities.isEventDispatchThread()) {
            showBanner.run();
        } else {
            ApplicationManager.getApplication().invokeLater(showBanner);
        }

        ProgressManager.getInstance().run(
            new Task.Backgroundable(project, "AI Refactoring Analysis", true) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    indicator.setIndeterminate(false);
                    indicator.setFraction(0.1);
                    indicator.setText("Preparing refactoring request...");

                    try {
                        AiRefactoringRequest request = ApplicationManager.getApplication().runReadAction(
                            (Computable<AiRefactoringRequest>) () -> buildRequest(editor, model));

                        if (request == null) {
                            removeBanner(editor);
                            showNotification("Refactoring Error",
                                "Could not extract method source code at the given position.",
                                NotificationType.ERROR);
                            return;
                        }

                        indicator.setFraction(0.3);
                        updateBannerText(bannerLabelHolder[0],
                            "Sending request to " + provider.getDisplayName() + "...");

                        AiRefactoringResponse response = provider.suggestRefactoring(
                            request,
                            progressText -> {
                                indicator.setText(progressText);
                                updateBannerText(bannerLabelHolder[0], progressText);
                                indicator.setFraction(
                                    Math.min(indicator.getFraction() + 0.1, 0.9));
                            }
                        ).orTimeout(120, java.util.concurrent.TimeUnit.SECONDS).join();

                        indicator.setFraction(1.0);
                        indicator.setText("Done");
                        removeBanner(editor);

                        // Record suggestion in history
                        RefactoringHistoryService historyService = RefactoringHistoryService.getInstance(project);
                        RefactoringHistoryEntry historyEntry = new RefactoringHistoryEntry(
                            request.getMethodName(),
                            ApplicationManager.getApplication().runReadAction((Computable<String>) () -> {
                                Document doc = editor.getDocument();
                                PsiFile pf = PsiDocumentManager.getInstance(project).getPsiFile(doc);
                                return pf != null ? pf.getName() : "unknown";
                            }),
                            response.getProviderName(),
                            request.getComplexityScore(),
                            false,
                            response.getExplanation()
                        );
                        historyService.addEntry(historyEntry);

                        ApplicationManager.getApplication().invokeLater(() ->
                            RefactoringDiffViewer.show(
                                project,
                                response,
                                request.getSourceCode(),
                                request.getComplexityScore(),
                                request.getLanguage(),
                                () -> applyRefactoring(editor, model, response.getSuggestedCode(), historyEntry)
                            )
                        );

                    } catch (Exception e) {
                        removeBanner(editor);
                        String message = e.getCause() != null
                            ? e.getCause().getMessage() : e.getMessage();
                        showNotification("AI Refactoring Failed",
                            message != null ? message : "Unknown error occurred.",
                            NotificationType.ERROR);
                    }
                }
            }
        );
    }

    private void updateBannerText(JLabel label, String text) {
        if (label == null) return;
        ApplicationManager.getApplication().invokeLater(() ->
            label.setText("  " + text));
    }

    private void removeBanner(Editor editor) {
        ApplicationManager.getApplication().invokeLater(() ->
            editor.setHeaderComponent(null));
    }

    @Nullable
    public AiRefactoringRequest buildRequestForMethod(PsiElement methodElement, MetricsModel model) {
        PsiFile psiFile = methodElement.getContainingFile();
        if (psiFile == null) return null;

        String sourceCode = methodElement.getText();
        String language = psiFile.getName().endsWith(".kt") ? "kotlin" : "java";

        long complexityScore = model.getCollectedComplexity();
        MetricsConfiguration config = MetricsConfiguration.getInstance();
        String complexityLevel = getComplexityLevel(complexityScore, config);

        List<AiRefactoringRequest.BreakdownEntry> breakdown =
            PromptBuilder.buildBreakdown(model);

        String classContext = "";
        AiRefactoringConfiguration aiConfig = AiRefactoringConfiguration.getInstance();
        if (aiConfig.includeClassContext) {
            classContext = extractClassContext(methodElement);
        }

        String imports = extractImports(psiFile);
        String methodName = model.getTextToShow();

        return new AiRefactoringRequest(
            sourceCode, language, complexityScore, complexityLevel,
            breakdown, classContext, imports, methodName
        );
    }

    @Nullable
    private AiRefactoringRequest buildRequest(Editor editor, MetricsModel model) {
        Document document = editor.getDocument();
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (psiFile == null) return null;

        PsiElement element = psiFile.findElementAt(model.getTextOffset());
        if (element == null) return null;

        PsiElement methodElement = findMethodElement(element);
        if (methodElement == null) return null;

        String sourceCode = methodElement.getText();
        String language = psiFile.getName().endsWith(".kt") ? "kotlin" : "java";

        long complexityScore = model.getCollectedComplexity();
        MetricsConfiguration config = MetricsConfiguration.getInstance();
        String complexityLevel = getComplexityLevel(complexityScore, config);

        List<AiRefactoringRequest.BreakdownEntry> breakdown =
            PromptBuilder.buildBreakdown(model);

        String classContext = "";
        AiRefactoringConfiguration aiConfig = AiRefactoringConfiguration.getInstance();
        if (aiConfig.includeClassContext) {
            classContext = extractClassContext(methodElement);
        }

        String imports = extractImports(psiFile);
        String methodName = model.getTextToShow();

        return new AiRefactoringRequest(
            sourceCode, language, complexityScore, complexityLevel,
            breakdown, classContext, imports, methodName
        );
    }

    @Nullable
    private PsiElement findMethodElement(PsiElement element) {
        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class, false);
        if (method != null) return method;

        KtNamedFunction ktFunction =
            PsiTreeUtil.getParentOfType(element, KtNamedFunction.class, false);
        if (ktFunction != null) return ktFunction;

        return null;
    }

    private String extractClassContext(PsiElement methodElement) {
        PsiElement parent = methodElement.getParent();
        if (parent == null) return "";

        StringBuilder context = new StringBuilder();

        if (parent instanceof PsiClass psiClass) {
            if (psiClass.getModifierList() != null) {
                context.append(psiClass.getModifierList().getText()).append(" ");
            }
            context.append("class ").append(psiClass.getName());
            if (psiClass.getExtendsList() != null
                && psiClass.getExtendsList().getReferenceElements().length > 0) {
                context.append(" extends ").append(psiClass.getExtendsList().getText());
            }
            if (psiClass.getImplementsList() != null
                && psiClass.getImplementsList().getReferenceElements().length > 0) {
                context.append(" implements ").append(psiClass.getImplementsList().getText());
            }
            context.append(" {\n");
            for (PsiField field : psiClass.getFields()) {
                context.append("    ").append(field.getText()).append("\n");
            }
            context.append("    // ... methods ...\n}");
        } else {
            String parentText = parent.getText();
            int bodyStart = parentText.indexOf('{');
            if (bodyStart > 0) {
                context.append(parentText, 0, Math.min(bodyStart + 1, parentText.length()));
                context.append("\n    // ... members ...\n}");
            }
        }

        return context.toString();
    }

    private String extractImports(PsiFile psiFile) {
        if (psiFile instanceof PsiJavaFile javaFile) {
            PsiImportList importList = javaFile.getImportList();
            return importList != null ? importList.getText() : "";
        }
        StringBuilder imports = new StringBuilder();
        for (String line : psiFile.getText().split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("import ")) {
                imports.append(trimmed).append("\n");
            }
        }
        return imports.toString();
    }

    private String getComplexityLevel(long complexity, MetricsConfiguration config) {
        if (complexity >= config.complexityLevelExtreme) return config.complexityLevelExtremeDescription;
        if (complexity >= config.complexityLevelHigh) return config.complexityLevelHighDescription;
        if (complexity >= config.complexityLevelNormal) return config.complexityLevelNormalDescription;
        return config.complexityLevelLowDescription;
    }

    private void applyRefactoring(Editor editor, MetricsModel model, String suggestedCode, RefactoringHistoryEntry historyEntry) {
        Document document = editor.getDocument();
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (psiFile == null) return;

        // Capture original source code for validation
        String originalSourceCode = ApplicationManager.getApplication().runReadAction((Computable<String>) () -> {
            PsiElement element = psiFile.findElementAt(model.getTextOffset());
            if (element == null) return null;
            PsiElement methodElement = findMethodElement(element);
            return methodElement != null ? methodElement.getText() : null;
        });

        if (originalSourceCode == null) {
            showNotification("Refactoring Aborted",
                "Could not locate the original method. Please try again.",
                NotificationType.WARNING);
            return;
        }

        WriteCommandAction.runWriteCommandAction(project, "Apply AI Refactoring", null, () -> {
            PsiElement element = psiFile.findElementAt(model.getTextOffset());
            if (element == null) return;

            PsiElement methodElement = findMethodElement(element);
            if (methodElement == null) return;

            // Validate PSI hasn't changed
            String currentText = methodElement.getText();
            if (!currentText.equals(originalSourceCode)) {
                // Document was modified, skip to avoid corruption
                ApplicationManager.getApplication().invokeLater(() ->
                    showNotification("Refactoring Aborted",
                        "The code was modified since the suggestion was generated. Please try again.",
                        NotificationType.WARNING)
                );
                return;
            }

            int start = methodElement.getTextRange().getStartOffset();
            int end = methodElement.getTextRange().getEndOffset();

            document.replaceString(start, end, suggestedCode);
            PsiDocumentManager.getInstance(project).commitDocument(document);
        });

        // Re-analyze complexity after PSI is fully committed
        PsiDocumentManager.getInstance(project).performWhenAllCommitted(() -> {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                try {
                    ApplicationManager.getApplication().runReadAction((Runnable) () -> {
                        Document doc = editor.getDocument();
                        PsiFile psiFile2 = PsiDocumentManager.getInstance(project).getPsiFile(doc);
                        if (psiFile2 != null) {
                            MetricsParser parser = new MetricsParser();
                            MetricsModel newModel = parser.getMetrics(psiFile2);
                            if (newModel != null) {
                                long newComplexity = findMethodComplexity(newModel, model.getTextToShow());
                                long oldComplexity = model.getCollectedComplexity();
                                if (newComplexity >= 0 && newComplexity != oldComplexity) {
                                    long reduction = oldComplexity - newComplexity;
                                    int percent = 0;
                                    if (oldComplexity > 0) {
                                        percent = (int) ((reduction * 100) / oldComplexity);
                                    }
                                    showNotification("Complexity Reduced",
                                        String.format("Complexity reduced: %d → %d (%d%% reduction)",
                                            oldComplexity, newComplexity, percent),
                                        NotificationType.INFORMATION);

                                    // Update history entry
                                    historyEntry.newComplexity = newComplexity;
                                    historyEntry.applied = true;
                                } else {
                                    showNotification("Refactoring Applied",
                                        "AI refactoring has been applied. You can undo with Ctrl+Z.",
                                        NotificationType.INFORMATION);
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    showNotification("Refactoring Applied",
                        "AI refactoring has been applied. You can undo with Ctrl+Z.",
                        NotificationType.INFORMATION);
                }
            });
        });
    }

    private long findMethodComplexity(MetricsModel root, String methodName) {
        if (methodName != null && methodName.equals(root.getTextToShow())) {
            return root.getCollectedComplexity();
        }
        for (MetricsModel child : root.getChildren()) {
            long result = findMethodComplexity(child, methodName);
            if (result >= 0) return result;
        }
        return -1;
    }

    private void showNotification(String title, String content, NotificationType type) {
        ApplicationManager.getApplication().invokeLater(() ->
            NotificationGroupManager.getInstance()
                .getNotificationGroup("CodeMetrics.AI")
                .createNotification(title, content, type)
                .notify(project)
        );
    }
}
