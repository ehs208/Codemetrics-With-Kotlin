package com.github.ehs208.codemetrics.ai;

import com.github.ehs208.codemetrics.ai.config.AiRefactoringConfiguration;
import com.github.ehs208.codemetrics.ai.history.RefactoringHistoryEntry;
import com.github.ehs208.codemetrics.ai.history.RefactoringHistoryService;
import com.github.ehs208.codemetrics.ai.ui.RefactoringDiffViewer;
import com.github.ehs208.codemetrics.core.MetricsModel;
import com.github.ehs208.codemetrics.core.config.MetricsConfiguration;
import com.github.ehs208.codemetrics.core.parser.MetricsParser;
import com.github.ehs208.codemetrics.toolwindow.ComplexityAnalysisService.ComplexityMethodInfo;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtNamedFunction;

import java.util.ArrayList;
import java.util.List;

@Service(Service.Level.PROJECT)
public final class BatchRefactoringService {
    private final Project project;

    public BatchRefactoringService(Project project) {
        this.project = project;
    }

    public void batchRefactorFile(PsiFile psiFile) {
        // Show confirmation dialog before batch refactoring (runs on EDT)
        int confirm = com.intellij.openapi.ui.Messages.showOkCancelDialog(
            project,
            "This will automatically refactor all high-complexity methods in this file.\n" +
                "Each method will be modified without individual confirmation.\n\n" +
                "Do you want to proceed? (You can undo all changes with Ctrl+Z)",
            "Batch Refactor File",
            "Proceed",
            "Cancel",
            com.intellij.icons.AllIcons.General.WarningDialog
        );
        if (confirm != com.intellij.openapi.ui.Messages.OK) return;

        // Get editor for banner display
        com.intellij.openapi.editor.Editor editor = FileEditorManager.getInstance(project)
            .getSelectedTextEditor();

        // Show banner on editor
        final javax.swing.JLabel[] bannerLabelHolder = new javax.swing.JLabel[1];
        if (editor != null) {
            javax.swing.JPanel banner = new javax.swing.JPanel(new java.awt.BorderLayout());
            banner.setBackground(new com.intellij.ui.JBColor(
                new java.awt.Color(0xE8F0FE), new java.awt.Color(0x2D3548)));
            banner.setBorder(com.intellij.util.ui.JBUI.Borders.compound(
                com.intellij.util.ui.JBUI.Borders.customLine(
                    new com.intellij.ui.JBColor(0xC4C4C4, 0x4B4B4B), 0, 0, 1, 0),
                com.intellij.util.ui.JBUI.Borders.empty(6, 12)));

            javax.swing.JLabel spinner = new javax.swing.JLabel(com.intellij.icons.AllIcons.Process.Step_1);
            banner.add(spinner, java.awt.BorderLayout.WEST);

            javax.swing.JLabel label = new javax.swing.JLabel("  Batch refactoring: analyzing file...");
            label.setFont(label.getFont().deriveFont(java.awt.Font.PLAIN, 12f));
            banner.add(label, java.awt.BorderLayout.CENTER);

            bannerLabelHolder[0] = label;
            editor.setHeaderComponent(banner);
        }

        final com.intellij.openapi.editor.Editor editorRef = editor;

        ProgressManager.getInstance().run(
            new Task.Backgroundable(project, "Batch Refactoring: " + psiFile.getName(), true) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    try {
                        indicator.setIndeterminate(false);
                        indicator.setFraction(0.0);
                        indicator.setText("Analyzing file...");

                        List<MethodCandidate> candidates = ApplicationManager.getApplication().runReadAction(
                            (Computable<List<MethodCandidate>>) () -> findRefactoringCandidates(psiFile));

                        if (candidates.isEmpty()) {
                            removeBanner(editorRef);
                            showNotification("No Candidates",
                                "No methods found that meet the complexity threshold.",
                                NotificationType.INFORMATION);
                            return;
                        }

                        // Sort by offset descending to prevent stale PSI references
                        candidates.sort((a, b) -> Integer.compare(
                            b.textOffset, a.textOffset));

                        int total = candidates.size();
                        indicator.setText(String.format("Found %d methods to refactor", total));
                        updateBannerText(bannerLabelHolder[0],
                            String.format("Batch refactoring: found %d methods", total));

                        int successCount = 0;
                        int failureCount = 0;

                        for (int i = 0; i < total; i++) {
                            if (indicator.isCanceled()) {
                                removeBanner(editorRef);
                                showNotification("Batch Refactoring Cancelled",
                                    String.format("Processed %d/%d methods before cancellation.",
                                        i, total),
                                    NotificationType.WARNING);
                                return;
                            }

                            MethodCandidate candidate = candidates.get(i);

                            // Progress: each method gets an equal slice
                            double methodStart = (double) i / total;
                            double methodEnd = (double) (i + 1) / total;

                            indicator.setFraction(methodStart);
                            String progressMsg = String.format("Refactoring %s (%d/%d)...",
                                candidate.methodName, i + 1, total);
                            indicator.setText(progressMsg);
                            updateBannerText(bannerLabelHolder[0],
                                String.format("  Batch refactoring: %s (%d/%d)",
                                    candidate.methodName, i + 1, total));

                            try {
                                boolean success = refactorMethod(psiFile, candidate, indicator,
                                    methodStart, methodEnd);
                                if (success) {
                                    successCount++;
                                } else {
                                    failureCount++;
                                }
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            } catch (Exception e) {
                                failureCount++;
                            }
                        }

                        indicator.setFraction(1.0);
                        removeBanner(editorRef);
                        int finalSuccessCount = successCount;
                        int finalFailureCount = failureCount;
                        ApplicationManager.getApplication().invokeLater(() ->
                            showNotification("Batch Refactoring Complete",
                                String.format("Successfully refactored: %d\nFailed: %d\nTotal: %d",
                                    finalSuccessCount, finalFailureCount, total),
                                NotificationType.INFORMATION)
                        );

                    } catch (Exception e) {
                        removeBanner(editorRef);
                        showNotification("Batch Refactoring Error",
                            "An error occurred: " + e.getMessage(),
                            NotificationType.ERROR);
                    }
                }
            }
        );
    }

    public void processBatch(List<ComplexityMethodInfo> methods) {
        AiRefactoringProvider provider = AiProviderRegistry.getActiveProvider();
        if (provider == null || !provider.isConfigured()) {
            showNotification("AI Provider Not Configured",
                "Please configure an AI provider in Settings \u2192 Code Metrics With Kotlin \u2192 AI Refactoring.",
                NotificationType.WARNING);
            return;
        }

        ProgressManager.getInstance().run(
            new Task.Backgroundable(project, "Batch AI Refactoring", true) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    indicator.setIndeterminate(false);
                    int total = methods.size();
                    int completed = 0;
                    int succeeded = 0;
                    int failed = 0;

                    for (ComplexityMethodInfo method : methods) {
                        if (indicator.isCanceled()) break;

                        indicator.setFraction((double) completed / total);
                        indicator.setText(String.format("Processing %d/%d: %s",
                            completed + 1, total, method.getMethodName()));

                        try {
                            MetricsModel model = method.getModel();
                            if (model == null) {
                                failed++;
                                completed++;
                                continue;
                            }

                            // Resolve PSI element to get actual source code
                            AiRefactoringRequest request = ApplicationManager.getApplication().runReadAction((Computable<AiRefactoringRequest>) () -> {
                                VirtualFile file = VirtualFileManager.getInstance()
                                    .findFileByNioPath(java.nio.file.Path.of(method.getFilePath()));
                                if (file == null) return null;

                                PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                                if (psiFile == null) return null;

                                PsiElement element = psiFile.findElementAt(method.getTextOffset());
                                if (element == null) return null;

                                PsiElement methodElement = findMethodElement(element);
                                if (methodElement == null) return null;

                                RefactoringService refactoringService = project.getService(RefactoringService.class);
                                return refactoringService.buildRequestForMethod(methodElement, model);
                            });

                            if (request == null) {
                                failed++;
                                completed++;
                                continue;
                            }

                            AiRefactoringResponse response = provider.suggestRefactoring(
                                request, text -> indicator.setText2(text)
                            ).orTimeout(120, java.util.concurrent.TimeUnit.SECONDS).join();

                            RefactoringHistoryService historyService = RefactoringHistoryService.getInstance(project);
                            historyService.addEntry(new RefactoringHistoryEntry(
                                method.getMethodName(),
                                method.getFileName(),
                                response.getProviderName(),
                                request.getComplexityScore(),
                                false,
                                response.getExplanation()
                            ));

                            final String suggestedCode = response.getSuggestedCode();
                            final String originalSource = request.getSourceCode();
                            final int textOffset = method.getTextOffset();
                            ApplicationManager.getApplication().invokeLater(() -> {
                                var file = VirtualFileManager.getInstance()
                                    .findFileByNioPath(java.nio.file.Path.of(method.getFilePath()));
                                if (file != null) {
                                    var descriptor = new OpenFileDescriptor(project, file, textOffset);
                                    var editor = FileEditorManager.getInstance(project)
                                        .openTextEditor(descriptor, true);
                                    if (editor != null) {
                                        RefactoringDiffViewer.show(project, response,
                                            request.getSourceCode(),
                                            request.getComplexityScore(),
                                            request.getLanguage(), () -> applyToMethod(file, textOffset, originalSource, suggestedCode));
                                    }
                                }
                            });

                            succeeded++;
                        } catch (Exception e) {
                            failed++;
                        }
                        completed++;
                    }

                    final int s = succeeded, f = failed;
                    NotificationType notifType = f > 0 && s == 0
                        ? NotificationType.ERROR
                        : f > 0 ? NotificationType.WARNING : NotificationType.INFORMATION;
                    String notifTitle = f > 0 && s == 0
                        ? "Batch Refactoring Failed"
                        : "Batch Refactoring Complete";
                    ApplicationManager.getApplication().invokeLater(() ->
                        NotificationGroupManager.getInstance()
                            .getNotificationGroup("CodeMetrics.AI")
                            .createNotification(notifTitle,
                                String.format("Processed %d methods: %d succeeded, %d failed", total, s, f),
                                notifType)
                            .notify(project)
                    );
                }
            }
        );
    }

    private String getComplexityLevel(long complexity, MetricsConfiguration config) {
        if (complexity >= config.complexityLevelExtreme) return config.complexityLevelExtremeDescription;
        if (complexity >= config.complexityLevelHigh) return config.complexityLevelHighDescription;
        if (complexity >= config.complexityLevelNormal) return config.complexityLevelNormalDescription;
        return config.complexityLevelLowDescription;
    }

    private List<MethodCandidate> findRefactoringCandidates(PsiFile psiFile) {
        List<MethodCandidate> candidates = new ArrayList<>();
        AiRefactoringConfiguration config = AiRefactoringConfiguration.getInstance();
        MetricsParser parser = new MetricsParser();
        MetricsModel model = parser.getMetrics(psiFile);

        if (model != null) {
            collectCandidates(model, psiFile, config.intentionThreshold, candidates);
        }

        return candidates;
    }

    private void collectCandidates(MetricsModel model, PsiFile psiFile, int threshold,
                                   List<MethodCandidate> candidates) {
        long complexity = model.getCollectedComplexity();
        String desc = model.getDescription();

        if (complexity >= threshold && desc != null && !desc.equals("Collector")) {
            PsiElement element = psiFile.findElementAt(model.getTextOffset());
            if (element != null) {
                PsiElement methodElement = findMethodElement(element);
                if (methodElement != null) {
                    candidates.add(new MethodCandidate(
                        model.getTextToShow(),
                        model,
                        complexity,
                        model.getTextOffset()
                    ));
                }
            }
        }

        for (MetricsModel child : model.getChildren()) {
            collectCandidates(child, psiFile, threshold, candidates);
        }
    }

    private PsiElement findMethodElement(PsiElement element) {
        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class, false);
        if (method != null) return method;

        KtNamedFunction ktFunction = PsiTreeUtil.getParentOfType(element, KtNamedFunction.class, false);
        if (ktFunction != null) return ktFunction;

        return null;
    }

    private boolean refactorMethod(PsiFile psiFile, MethodCandidate candidate, ProgressIndicator indicator,
                                   double progressStart, double progressEnd) {
        try {
            AiRefactoringProvider provider = AiProviderRegistry.getActiveProvider();
            if (provider == null || !provider.isConfigured()) {
                return false;
            }

            // Phase 1: Build request (10% of method's slice)
            indicator.setFraction(progressStart);

            RefactoringService refactoringService = project.getService(RefactoringService.class);
            AiRefactoringRequest request = ApplicationManager.getApplication().runReadAction((Computable<AiRefactoringRequest>) () -> {
                PsiElement element = psiFile.findElementAt(candidate.textOffset);
                if (element == null) return null;
                PsiElement methodElement = findMethodElement(element);
                if (methodElement == null) return null;
                return refactoringService.buildRequestForMethod(methodElement, candidate.model);
            });

            if (request == null) return false;

            // Phase 2: API call (80% of method's slice) with smooth progress
            double apiStart = progressStart + (progressEnd - progressStart) * 0.1;
            double apiEnd = progressStart + (progressEnd - progressStart) * 0.9;
            indicator.setFraction(apiStart);

            AiRefactoringResponse response = provider.suggestRefactoring(
                request,
                progressText -> {
                    indicator.setText(progressText);
                    // Smooth increment within API phase
                    double current = indicator.getFraction();
                    if (current < apiEnd) {
                        indicator.setFraction(Math.min(current + (apiEnd - apiStart) * 0.15, apiEnd));
                    }
                }
            ).orTimeout(120, java.util.concurrent.TimeUnit.SECONDS).join();

            if (response == null || response.getSuggestedCode() == null) {
                return false;
            }

            // Phase 3: Apply changes (10% of method's slice)
            indicator.setFraction(apiEnd);

            WriteCommandAction.runWriteCommandAction(project,
                "Batch Refactor: " + candidate.methodName, null, () -> {
                    PsiElement element = psiFile.findElementAt(candidate.textOffset);
                    if (element == null) return;
                    PsiElement methodElement = findMethodElement(element);
                    if (methodElement == null) return;
                    Document document = PsiDocumentManager.getInstance(project)
                        .getDocument(psiFile);
                    if (document != null) {
                        int start = methodElement.getTextRange().getStartOffset();
                        int end = methodElement.getTextRange().getEndOffset();
                        document.replaceString(start, end, response.getSuggestedCode());
                        PsiDocumentManager.getInstance(project).commitDocument(document);
                    }
                });

            indicator.setFraction(progressEnd);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private void updateBannerText(javax.swing.JLabel label, String text) {
        if (label == null) return;
        ApplicationManager.getApplication().invokeLater(() -> label.setText(text));
    }

    private void removeBanner(com.intellij.openapi.editor.Editor editor) {
        if (editor == null) return;
        ApplicationManager.getApplication().invokeLater(() -> editor.setHeaderComponent(null));
    }

    private void applyToMethod(VirtualFile file, int textOffset, String originalSource, String suggestedCode) {
        WriteCommandAction.runWriteCommandAction(project, "Apply AI Refactoring", null, () -> {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (psiFile == null) return;

            PsiElement element = psiFile.findElementAt(textOffset);
            if (element == null) return;

            PsiElement methodElement = findMethodElement(element);
            if (methodElement == null) return;

            // Validate PSI hasn't changed since suggestion was generated
            if (!methodElement.getText().equals(originalSource)) {
                ApplicationManager.getApplication().invokeLater(() ->
                    showNotification("Refactoring Aborted",
                        "The code was modified since the suggestion was generated. Please try again.",
                        NotificationType.WARNING)
                );
                return;
            }

            Document document = PsiDocumentManager.getInstance(project)
                .getDocument(psiFile);
            if (document != null) {
                int start = methodElement.getTextRange().getStartOffset();
                int end = methodElement.getTextRange().getEndOffset();
                document.replaceString(start, end, suggestedCode);
                PsiDocumentManager.getInstance(project).commitDocument(document);
            }
        });
    }

    private void showNotification(String title, String content, NotificationType type) {
        ApplicationManager.getApplication().invokeLater(() ->
            NotificationGroupManager.getInstance()
                .getNotificationGroup("CodeMetrics.AI")
                .createNotification(title, content, type)
                .notify(project)
        );
    }

    private static class MethodCandidate {
        final String methodName;
        final MetricsModel model;
        final long complexity;
        final int textOffset;

        MethodCandidate(String methodName, MetricsModel model,
                       long complexity, int textOffset) {
            this.methodName = methodName;
            this.model = model;
            this.complexity = complexity;
            this.textOffset = textOffset;
        }
    }
}
