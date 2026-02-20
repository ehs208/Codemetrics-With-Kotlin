package com.github.ehs208.codemetrics.toolwindow;

import com.github.ehs208.codemetrics.core.config.MetricsConfiguration;
import com.github.ehs208.codemetrics.toolwindow.ComplexityAnalysisService.ComplexityMethodInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.StatusText;

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
        this.statusLabel = new JLabel("Ready to analyze project complexity");
        
        initializeUI();
        setupEventHandlers();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(0));
        setBackground(JBColor.background());

        // Header panel with title and description
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(JBColor.background());
        headerPanel.setBorder(JBUI.Borders.empty(8, 12));

        JBLabel titleLabel = new JBLabel("Code Complexity Analysis");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JBLabel descriptionLabel = new JBLabel("Find methods with high cyclomatic complexity");
        descriptionLabel.setForeground(JBColor.namedColor("Label.infoForeground", new JBColor(0x808080, 0x8C8C8C)));
        descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(Font.PLAIN, 11f));
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descriptionLabel.setBorder(JBUI.Borders.empty(4, 0, 0, 0));

        headerPanel.add(titleLabel);
        headerPanel.add(descriptionLabel);

        // Top panel with styled refresh button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(JBColor.background());
        topPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        buttonPanel.setBackground(JBColor.background());
        buttonPanel.setBorder(JBUI.Borders.empty(8, 12, 12, 12));

        // Simple, clean button styling
        refreshButton.setPreferredSize(new Dimension(140, 32));
        refreshButton.setFont(refreshButton.getFont().deriveFont(Font.PLAIN, 12f));
        refreshButton.setFocusPainted(false);

        buttonPanel.add(refreshButton);
        topPanel.add(buttonPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Method list with modern styling
        methodList.setCellRenderer(new ModernComplexityListCellRenderer());
        methodList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        methodList.setBackground(JBColor.background());
        methodList.setBorder(JBUI.Borders.empty());

        // Set empty state message using StatusText API
        StatusText emptyText = methodList.getEmptyText();
        emptyText.setText("No complex methods found yet");
        emptyText.appendSecondaryText("Click 'Analyze Project' to scan your codebase", StatusText.DEFAULT_ATTRIBUTES, null);

        JBScrollPane scrollPane = new JBScrollPane(methodList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(JBUI.Borders.empty(0, 12, 0, 12));
        scrollPane.setBackground(JBColor.background());
        add(scrollPane, BorderLayout.CENTER);

        // Status label with modern styling
        statusLabel.setForeground(JBColor.namedColor("Label.infoForeground", new JBColor(0x808080, 0x8C8C8C)));
        statusLabel.setFont(statusLabel.getFont().deriveFont(12f));
        statusLabel.setBorder(JBUI.Borders.empty(8, 12, 8, 12));
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
                if (methods.isEmpty()) {
                    statusLabel.setText("Analysis complete - no complex methods found");
                } else {
                    statusLabel.setText(String.format("Analysis complete - found %d complex method%s",
                        methods.size(), methods.size() == 1 ? "" : "s"));
                }
                refreshButton.setEnabled(true);
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Analysis failed - " + throwable.getMessage());
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

    private static class ModernComplexityListCellRenderer extends JPanel implements ListCellRenderer<ComplexityMethodInfo> {
        private final JLabel iconLabel;
        private final JLabel contentLabel;

        public ModernComplexityListCellRenderer() {
            setLayout(new BorderLayout(8, 0));
            setBorder(JBUI.Borders.empty(8, 12));
            setOpaque(true);

            iconLabel = new JLabel();
            iconLabel.setPreferredSize(new Dimension(16, 16));
            add(iconLabel, BorderLayout.WEST);

            contentLabel = new JLabel();
            contentLabel.setVerticalAlignment(SwingConstants.TOP);
            add(contentLabel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ComplexityMethodInfo> list,
                                                      ComplexityMethodInfo method, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            if (method == null) {
                return this;
            }

            // Set background colors
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                contentLabel.setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                contentLabel.setForeground(list.getForeground());
            }

            // Get complexity level info
            MetricsConfiguration config = MetricsConfiguration.getInstance();
            String levelText = getComplexityLevelText(method.getComplexity(), config);
            Color indicatorColor = getComplexityColor(method.getComplexity(), config);

            // Create colored circle indicator
            iconLabel.setIcon(new ComplexityIndicatorIcon(indicatorColor, 12));

            // Build HTML content with method name in bold, file path and complexity on second line
            String htmlContent = String.format(
                "<html><body style='margin:0;padding:0;'>" +
                "<div style='margin:0;padding:0;'>" +
                "<span style='font-weight:bold;font-size:12px;'>%s</span><br>" +
                "<span style='color:#%s;font-size:11px;'>%s &nbsp;•&nbsp; Complexity: %d (%s)</span>" +
                "</div></body></html>",
                escapeHtml(method.getMethodName()),
                isSelected ? "inherit" : "808080",
                escapeHtml(method.getFileName() + " - " + method.getDescription()),
                method.getComplexity(),
                levelText
            );

            contentLabel.setText(htmlContent);

            return this;
        }

        private String getComplexityLevelText(long complexity, MetricsConfiguration config) {
            if (complexity >= config.complexityLevelExtreme) {
                return "Extreme";
            }
            if (complexity >= config.complexityLevelHigh) {
                return "High";
            }
            if (complexity >= config.complexityLevelNormal) {
                return "Normal";
            }
            if (complexity >= config.complexityLevelLow) {
                return "Low";
            }
            return "Very Low";
        }

        private Color getComplexityColor(long complexity, MetricsConfiguration config) {
            if (complexity >= config.complexityLevelExtreme) {
                return new Color(config.complexityColorExtreme);
            }
            if (complexity >= config.complexityLevelHigh) {
                return new Color(config.complexityColorHigh);
            }
            if (complexity >= config.complexityLevelNormal) {
                return new Color(config.complexityColorNormal);
            }
            if (complexity >= config.complexityLevelLow) {
                return new Color(config.complexityColorLow);
            }
            return JBColor.foreground();
        }

        private String escapeHtml(String text) {
            return text.replace("&", "&amp;")
                      .replace("<", "&lt;")
                      .replace(">", "&gt;")
                      .replace("\"", "&quot;");
        }

        // Inner class for circular complexity indicator icon
        private static class ComplexityIndicatorIcon implements Icon {
            private final Color color;
            private final int size;

            public ComplexityIndicatorIcon(Color color, int size) {
                this.color = color;
                this.size = size;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.fillOval(x + 2, y + 2, size, size);
                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return size + 4;
            }

            @Override
            public int getIconHeight() {
                return size + 4;
            }
        }
    }
}
