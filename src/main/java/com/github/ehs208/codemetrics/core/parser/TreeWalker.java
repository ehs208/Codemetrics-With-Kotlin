package com.github.ehs208.codemetrics.core.parser;

import com.github.ehs208.codemetrics.core.CollectorType;
import com.github.ehs208.codemetrics.core.MetricsModel;
import com.github.ehs208.codemetrics.core.config.MetricsConfiguration;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtObjectDeclaration;
import org.jetbrains.kotlin.psi.KtEnumEntry;

import java.util.Arrays;

import static com.github.ehs208.codemetrics.core.CollectorType.MAX;
import static com.github.ehs208.codemetrics.core.CollectorType.SUM;

public class TreeWalker {

  private MetricsConfiguration configuration;

  TreeWalker() {
    configuration = MetricsConfiguration.getInstance();
  }

  public MetricsModel walk(PsiElement node) {
    MetricsModel model = visit(node, 0, "Collector", false);
    visitNode(node, model);
    return model;
  }

  public void visitNode(PsiElement element, MetricsModel parent) {
    MetricsModel model = getMetrics(element);
    MetricsModel updatedParent = parent;
    if (model != null) {
      parent.getChildren().add(model);
      if (model.isVisible()) {
        updatedParent = model;
      }
    }
    walkChildren(element, updatedParent);
  }

  private void walkChildren(PsiElement element, MetricsModel parent) {
    Arrays.stream(element.getChildren()).forEach(node -> this.visitNode(node, parent));
  }

  private MetricsModel getMetrics(PsiElement element) {
    MetricsModel model = null;
    if (element != null) {
      ASTNode node = element.getNode();
      if (node != null) {
        IElementType elementType = node.getElementType();

        ComplexityHandler handler = HandlerRegistry.get(elementType);

        if (handler != null) {
          model =
              handler
                  .forConfig(configuration)
                  .andThen(p -> visit(element, p.getIncrement(), p.getDescription(), p.isVisible()))
                  .apply(element);
        }
      }
    }
    return model;
  }

  private MetricsModel visit(PsiElement node, int complexity, String description, boolean visible) {
    // Check if it's a class (Java or Kotlin)
    CollectorType collectorType = isClassElement(node) ? MAX : SUM;
    return new MetricsModel(node, complexity, description, true, visible, collectorType);
  }
  
  private boolean isClassElement(PsiElement node) {
    // Java classes
    if (node instanceof PsiClass) return true;
    
    // Kotlin classes
    if (node instanceof KtClass) return true;
    if (node instanceof KtObjectDeclaration) return true;
    if (node instanceof KtEnumEntry) return true;
    
    return false;
  }
}
