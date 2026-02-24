package com.github.ehs208.codemetrics.toolwindow;

import com.github.ehs208.codemetrics.ai.history.RefactoringHistoryEntry;
import com.github.ehs208.codemetrics.ai.history.RefactoringHistoryService;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.StatusText;

import javax.swing.*;
import java.awt.*;

public class RefactoringHistoryPanel extends JPanel {
    private final Project project;
    private final RefactoringHistoryService historyService;
    private final JBList<RefactoringHistoryEntry> historyList;
    private final DefaultListModel<RefactoringHistoryEntry> listModel;

    public RefactoringHistoryPanel(Project project) {
        this.project = project;
        this.historyService = RefactoringHistoryService.getInstance(project);
        this.listModel = new DefaultListModel<>();
        this.historyList = new JBList<>(listModel);

        initializeUI();
        refreshHistory();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(JBColor.background());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(JBColor.background());
        headerPanel.setBorder(JBUI.Borders.empty(8, 12));

        JBLabel titleLabel = new JBLabel("Refactoring History");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(titleLabel);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        buttonPanel.setBackground(JBColor.background());
        buttonPanel.setBorder(JBUI.Borders.empty(4, 12, 8, 12));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshHistory());
        buttonPanel.add(refreshButton);

        JButton clearButton = new JButton("Clear History");
        clearButton.addActionListener(e -> {
            historyService.clearHistory();
            refreshHistory();
        });
        buttonPanel.add(clearButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(JBColor.background());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // List
        historyList.setCellRenderer(new HistoryListCellRenderer());
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyList.setBackground(JBColor.background());

        StatusText emptyText = historyList.getEmptyText();
        emptyText.setText("No refactoring history yet");
        emptyText.appendSecondaryText("AI refactoring suggestions will appear here", StatusText.DEFAULT_ATTRIBUTES, null);

        JBScrollPane scrollPane = new JBScrollPane(historyList);
        scrollPane.setBorder(JBUI.Borders.empty(0, 12, 0, 12));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshHistory() {
        listModel.clear();
        for (RefactoringHistoryEntry entry : historyService.getEntries()) {
            listModel.addElement(entry);
        }
    }

    private static class HistoryListCellRenderer extends JPanel implements ListCellRenderer<RefactoringHistoryEntry> {
        private final JLabel contentLabel;

        public HistoryListCellRenderer() {
            setLayout(new BorderLayout(8, 0));
            setBorder(JBUI.Borders.empty(6, 8));
            setOpaque(true);
            contentLabel = new JLabel();
            add(contentLabel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends RefactoringHistoryEntry> list,
                                                      RefactoringHistoryEntry entry, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            if (entry == null) return this;

            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            contentLabel.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

            String status = entry.applied ? "Applied" : "Suggested";
            String complexityInfo = entry.newComplexity >= 0
                ? String.format("%d -> %d", entry.originalComplexity, entry.newComplexity)
                : String.valueOf(entry.originalComplexity);

            String html = String.format(
                "<html><b>%s</b> - %s<br>" +
                "<span style='color:#808080;font-size:11px;'>%s | %s | Complexity: %s | %s</span></html>",
                escapeHtml(entry.methodName != null ? entry.methodName : "Unknown"),
                status,
                entry.getFormattedTime(),
                entry.providerName != null ? entry.providerName : "Unknown",
                complexityInfo,
                entry.fileName != null ? entry.fileName : ""
            );
            contentLabel.setText(html);
            return this;
        }

        private String escapeHtml(String text) {
            return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }
}
