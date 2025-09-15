package com.github.ehs208.codemetrics.toolwindow;

import com.github.ehs208.codemetrics.core.config.MetricsConfiguration;
import com.github.ehs208.codemetrics.toolwindow.ComplexityAnalysisService.ComplexityMethodInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ComplexityAnalysisPanel extends JPanel {
    private final Project project;
    private final ComplexityAnalysisService analysisService;
    private final JBList<ComplexityMethodInfo> methodList;
    private final DefaultListModel<ComplexityMethodInfo> listModel;
    private final JButton refreshButton;
    private final JLabel statusLabel;


    public ComplexityAnalysisPanel(Project project) {
        this.project = project;
        this.analysisService = project.getService(ComplexityAnalysisService.class);
        this.listModel = new DefaultListModel<>();
        this.methodList = new JBList<>(listModel);
        this.refreshButton = new JButton("Analyze Project");
        this.statusLabel = new JLabel("Click 'Analyze Project' to scan for complex methods");
        
        initializeUI();
        setupEventHandlers();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(12));
        setBackground(JBColor.background());

        // Top panel with styled refresh button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        topPanel.setBackground(JBColor.background());
        topPanel.setBorder(JBUI.Borders.empty(8, 0, 12, 0));
        
        // Simple, clean button styling
        refreshButton.setPreferredSize(new Dimension(140, 32));
        refreshButton.setFont(refreshButton.getFont().deriveFont(Font.PLAIN, 12f));
        refreshButton.setFocusPainted(false);
        
        topPanel.add(refreshButton);
        add(topPanel, BorderLayout.NORTH);

        // Method list with modern styling
        methodList.setCellRenderer(new ModernComplexityListCellRenderer());
        methodList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        methodList.setBackground(JBColor.background());
        methodList.setBorder(JBUI.Borders.empty());
        
        JBScrollPane scrollPane = new JBScrollPane(methodList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(JBUI.Borders.empty());
        scrollPane.setBackground(JBColor.background());
        add(scrollPane, BorderLayout.CENTER);

        // Status label with modern styling
        statusLabel.setForeground(JBColor.namedColor("Label.infoForeground", new JBColor(0x808080, 0x8C8C8C)));
        statusLabel.setFont(statusLabel.getFont().deriveFont(12f));
        statusLabel.setBorder(JBUI.Borders.empty(8, 0, 0, 0));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        refreshButton.addActionListener(e -> analyzeProject());
        
        methodList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    navigateToSelectedMethod();
                }
            }
        });
    }

    private void analyzeProject() {
        refreshButton.setEnabled(false);
        statusLabel.setText("Analyzing project...");
        listModel.clear();

        analysisService.analyzeProjectComplexity().thenAccept(methods -> {
            SwingUtilities.invokeLater(() -> {
                for (ComplexityMethodInfo method : methods) {
                    listModel.addElement(method);
                }
                statusLabel.setText(String.format("Found %d complex methods", methods.size()));
                refreshButton.setEnabled(true);
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Error during analysis: " + throwable.getMessage());
                refreshButton.setEnabled(true);
            });
            return null;
        });
    }

    private void navigateToSelectedMethod() {
        ComplexityMethodInfo selected = methodList.getSelectedValue();
        if (selected != null) {
            ApplicationManager.getApplication().invokeLater(() -> {
                var file = VirtualFileManager.getInstance().findFileByUrl("file://" + selected.getFilePath());
                if (file != null) {
                    OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file, selected.getTextOffset());
                    FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
                }
            });
        }
    }

    private static class ModernComplexityListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof ComplexityMethodInfo method) {
                // Simple, clean appearance
                setBorder(JBUI.Borders.empty(8, 12));
                
                // Simple text without HTML
                String displayText = String.format("%s (Complexity: %d) - %s [%s]", 
                    method.getMethodName(),
                    method.getComplexity(),
                    method.getFileName(),
                    method.getDescription()
                );
                setText(displayText);
                
                // Clean color scheme
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(getComplexityTextColor(method.getComplexity()));
                }
            }
            
            return this;
        }
        
        private Color getComplexityTextColor(long complexity) {
            MetricsConfiguration config = MetricsConfiguration.getInstance();

            if (complexity >= config.complexityLevelExtreme) {
                return new JBColor(new Color(config.complexityColorExtreme), new Color(config.complexityColorExtreme));
            }
            if (complexity >= config.complexityLevelHigh) {
                return new JBColor(new Color(config.complexityColorHigh), new Color(config.complexityColorHigh));
            }
            if (complexity >= config.complexityLevelNormal) {
                return new JBColor(new Color(config.complexityColorNormal), new Color(config.complexityColorNormal));
            }
            if (complexity >= config.complexityLevelLow) {
                return new JBColor(new Color(config.complexityColorLow), new Color(config.complexityColorLow));
            }
            return JBColor.foreground();
        }
    }
}
