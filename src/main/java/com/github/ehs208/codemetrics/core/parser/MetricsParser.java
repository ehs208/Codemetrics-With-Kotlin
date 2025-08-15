package com.github.ehs208.codemetrics.core.parser;

import com.github.ehs208.codemetrics.core.MetricsModel;
import com.intellij.psi.PsiElement;

public class MetricsParser {

  public MetricsModel getMetrics(PsiElement root) {
    return new TreeWalker().walk(root);
  }
}
