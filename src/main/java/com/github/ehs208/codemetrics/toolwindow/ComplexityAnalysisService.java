package com.github.ehs208.codemetrics.toolwindow;

import com.github.ehs208.codemetrics.core.MetricsModel;
import com.github.ehs208.codemetrics.core.parser.MetricsParser;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.kotlin.idea.KotlinFileType;
import com.intellij.lang.java.JavaLanguage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service(Service.Level.PROJECT)
public final class ComplexityAnalysisService {
    private final Project project;

    public ComplexityAnalysisService(Project project) {
        this.project = project;
    }

    public CompletableFuture<List<ComplexityMethodInfo>> analyzeProjectComplexity() {
        return CompletableFuture.supplyAsync(() -> {
            return ReadAction.compute(() -> {
                List<ComplexityMethodInfo> results = new ArrayList<>();
                
                // Scan Java files
                Collection<VirtualFile> javaFiles = FileBasedIndex.getInstance()
                    .getContainingFiles(FileTypeIndex.NAME, JavaLanguage.INSTANCE.getAssociatedFileType(),
                        GlobalSearchScope.projectScope(project));
                
                for (VirtualFile file : javaFiles) {
                    analyzeFile(file, results);
                }
                
                // Scan Kotlin files
                Collection<VirtualFile> kotlinFiles = FileBasedIndex.getInstance()
                    .getContainingFiles(FileTypeIndex.NAME, KotlinFileType.INSTANCE,
                        GlobalSearchScope.projectScope(project));
                
                for (VirtualFile file : kotlinFiles) {
                    analyzeFile(file, results);
                }
                
                // Sort by complexity descending
                results.sort((a, b) -> Long.compare(b.getComplexity(), a.getComplexity()));
                
                return results;
            });
        });
    }

    private void analyzeFile(VirtualFile file, List<ComplexityMethodInfo> results) {
        if (!file.isValid()) return;
        
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile == null) return;
        
        MetricsParser parser = new MetricsParser();
        MetricsModel model = parser.getMetrics(psiFile);
        
        if (model != null) {
            collectMethodComplexity(model, file, results);
        }
    }

    private void collectMethodComplexity(MetricsModel model, VirtualFile file, List<ComplexityMethodInfo> results) {
        String desc = model.getDescription();
        long complexity = model.getCollectedComplexity();
        
        // Skip "Collector" (root file node) and only include interesting elements
        if (complexity > 1 && desc != null && !desc.equals("Collector")) {
            results.add(new ComplexityMethodInfo(
                model.getTextToShow(),
                complexity,
                desc,
                file.getPath(),
                model.getTextOffset()
            ));
        }
        
        // Recursively check children
        for (MetricsModel child : model.getChildren()) {
            collectMethodComplexity(child, file, results);
        }
    }

    private boolean isMethodLike(String description) {
        return description != null && (
            description.contains("method") ||
            description.contains("function") ||
            description.contains("constructor") ||
            description.contains("lambda")
        );
    }

    public static class ComplexityMethodInfo {
        private final String methodName;
        private final long complexity;
        private final String description;
        private final String filePath;
        private final int textOffset;

        public ComplexityMethodInfo(String methodName, long complexity, String description, 
                                   String filePath, int textOffset) {
            this.methodName = methodName;
            this.complexity = complexity;
            this.description = description;
            this.filePath = filePath;
            this.textOffset = textOffset;
        }

        public String getMethodName() { return methodName; }
        public long getComplexity() { return complexity; }
        public String getDescription() { return description; }
        public String getFilePath() { return filePath; }
        public int getTextOffset() { return textOffset; }
        
        public String getFileName() {
            return filePath.substring(filePath.lastIndexOf('/') + 1);
        }
    }
}