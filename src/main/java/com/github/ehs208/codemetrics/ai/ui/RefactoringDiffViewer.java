package com.github.ehs208.codemetrics.ai.ui;

import com.github.ehs208.codemetrics.ai.AiRefactoringResponse;
import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestPanel;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class RefactoringDiffViewer {

    public static void show(Project project, AiRefactoringResponse response,
                            String originalCode, long originalComplexity,
                            String language, Runnable onApply) {

        RefactoringDiffDialog dialog = new RefactoringDiffDialog(
            project, response, originalCode, originalComplexity, language, onApply);
        dialog.show();
    }

    private static class RefactoringDiffDialog extends DialogWrapper {
        private final Project project;
        private final AiRefactoringResponse response;
        private final String originalCode;
        private final long originalComplexity;
        private final String language;
        private final Runnable onApply;
        private DiffRequestPanel diffPanel;

        RefactoringDiffDialog(Project project, AiRefactoringResponse response,
                              String originalCode, long originalComplexity,
                              String language, Runnable onApply) {
            super(project, true);
            this.project = project;
            this.response = response;
            this.originalCode = originalCode;
            this.originalComplexity = originalComplexity;
            this.language = language;
            this.onApply = onApply;

            setTitle("AI Refactoring Suggestion");
            setOKButtonText("Apply Refactoring");
            setCancelButtonText("Close");
            init();
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setPreferredSize(new Dimension(900, 600));

            // Diff viewer
            FileType fileType = "kotlin".equals(language)
                ? FileTypeManager.getInstance().getFileTypeByExtension("kt")
                : FileTypeManager.getInstance().getFileTypeByExtension("java");

            DiffContent original = DiffContentFactory.getInstance().create(originalCode, fileType);
            DiffContent suggested = DiffContentFactory.getInstance()
                .create(response.getSuggestedCode(), fileType);

            SimpleDiffRequest diffRequest = new SimpleDiffRequest(
                "AI Refactoring Suggestion",
                original,
                suggested,
                "Original (Complexity: " + originalComplexity + ")",
                "Suggested by " + response.getProviderName()
            );

            diffPanel = DiffManager.getInstance().createRequestPanel(project, getDisposable(), null);
            diffPanel.setRequest(diffRequest);

            mainPanel.add(diffPanel.getComponent(), BorderLayout.CENTER);

            // Info panel at bottom
            JPanel infoPanel = new JPanel(new BorderLayout());
            infoPanel.setBorder(JBUI.Borders.empty(8, 4));

            // Token usage label (single line)
            if (response.hasTokenUsage()) {
                String tokenInfo = String.format("Tokens: %d input + %d output",
                    response.getInputTokens(), response.getOutputTokens());
                if (response.getEstimatedCost() > 0) {
                    tokenInfo += String.format(" (~$%.4f)", response.getEstimatedCost());
                }
                JLabel tokenLabel = new JLabel(tokenInfo);
                tokenLabel.setFont(tokenLabel.getFont().deriveFont(11f));
                tokenLabel.setForeground(javax.swing.UIManager.getColor("Label.disabledForeground"));
                infoPanel.add(tokenLabel, BorderLayout.NORTH);
            }

            // Explanation area (scrollable, rendered HTML with word wrap)
            String explanationHtml = markdownToHtml(response.getExplanation(), mainPanel);
            JEditorPane explanationPane = new JEditorPane();
            explanationPane.setContentType("text/html");
            explanationPane.setText(explanationHtml);
            explanationPane.setEditable(false);
            explanationPane.setBackground(mainPanel.getBackground());
            explanationPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
            explanationPane.setFont(mainPanel.getFont().deriveFont(12f));
            explanationPane.setBorder(JBUI.Borders.empty(4, 4));
            explanationPane.setCaretPosition(0);

            JScrollPane explanationScroll = new JScrollPane(explanationPane);
            explanationScroll.setBorder(JBUI.Borders.empty());
            explanationScroll.setPreferredSize(new Dimension(0, 150));
            explanationScroll.setMinimumSize(new Dimension(0, 100));
            infoPanel.add(explanationScroll, BorderLayout.CENTER);

            mainPanel.add(infoPanel, BorderLayout.SOUTH);

            return mainPanel;
        }

        @Override
        protected void doOKAction() {
            onApply.run();
            super.doOKAction();
        }

        @NotNull
        @Override
        protected Action @NotNull [] createActions() {
            return new Action[]{getOKAction(), getCancelAction()};
        }
    }

    private static String markdownToHtml(String explanation, JPanel parent) {
        if (explanation == null || explanation.isEmpty()) {
            explanation = "Review the suggested changes above.";
        }

        Color fg = parent.getForeground();
        Color muted = javax.swing.UIManager.getColor("Label.disabledForeground");
        if (muted == null) muted = fg;
        String fgHex = String.format("#%02x%02x%02x", fg.getRed(), fg.getGreen(), fg.getBlue());
        String mutedHex = String.format("#%02x%02x%02x", muted.getRed(), muted.getGreen(), muted.getBlue());

        String text = explanation.trim();

        // Escape HTML entities before markdown transformation
        text = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

        // Headers: ## Title -> <h4>
        text = text.replaceAll("(?m)^###\\s+(.+)$", "<h5>$1</h5>");
        text = text.replaceAll("(?m)^##\\s+(.+)$", "<h4>$1</h4>");

        // Bold: **text** -> <b>
        text = text.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");

        // Inline code: `code` -> <code>
        text = text.replaceAll("`([^`]+)`", "<code>$1</code>");

        // Bullet lists: - item or • item -> <li> (skip empty bullets)
        text = text.replaceAll("(?m)^[-*•]\\s*$", "");
        text = text.replaceAll("(?m)^[-*•]\\s+(.+)$", "<li>$1</li>");
        text = text.replaceAll("(<li>.*?</li>(?:\\s*<li>.*?</li>)*)", "<ul>$1</ul>");

        // Numbered lists: 1. item -> <li>
        text = text.replaceAll("(?m)^\\d+\\.\\s+(.+)$", "<oli>$1</oli>");
        text = text.replaceAll("(<oli>.*?</oli>(?:\\s*<oli>.*?</oli>)*)", "<ol>$1</ol>");
        text = text.replaceAll("oli>", "li>");

        // Line breaks for remaining plain lines
        text = text.replaceAll("(?m)^(?!<[hulo])(.+)$", "<p>$1</p>");

        return "<html><body><table width='100%' border='0' cellpadding='0' cellspacing='0'><tr><td style='font-family:sans-serif;font-size:12px;color:"
            + fgHex + "'>" + text + "</td></tr></table></body></html>";
    }
}
