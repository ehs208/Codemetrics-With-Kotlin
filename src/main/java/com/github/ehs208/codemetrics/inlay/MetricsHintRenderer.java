package com.github.ehs208.codemetrics.inlay;

import com.github.ehs208.codemetrics.core.MetricsModel;
import com.github.ehs208.codemetrics.core.config.MetricsConfiguration;
import com.github.ehs208.codemetrics.util.ComplexityColorUtil;
import com.intellij.codeInsight.daemon.impl.HintRenderer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.ColorUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static com.github.ehs208.codemetrics.util.ColorContrastUtil.getContrastColor;

public class MetricsHintRenderer extends HintRenderer {
  private MetricsModel model;
  private boolean highlighted;

  MetricsHintRenderer(MetricsModel model) {
    super(formatComplexityText(model));
    this.model = model;
  }

  private static String formatComplexityText(MetricsModel model) {
    long complexity = model.getCollectedComplexity();
    MetricsConfiguration config = MetricsConfiguration.getInstance();

    String level;
    if (complexity < config.complexityLevelNormal) {
      level = config.complexityLevelLowDescription;
    } else if (complexity < config.complexityLevelHigh) {
      level = config.complexityLevelNormalDescription;
    } else if (complexity < config.complexityLevelExtreme) {
      level = config.complexityLevelHighDescription;
    } else {
      level = config.complexityLevelExtremeDescription;
    }

    // Format: "15 (High)"
    return complexity + " (" + level + ")";
  }

  protected TextAttributes getTextAttributes(@NotNull Editor editor) {
    TextAttributesKey textAttributesKey =
        this.highlighted
            ? DefaultLanguageHighlighterColors.INLINE_PARAMETER_HINT_HIGHLIGHTED
            : DefaultLanguageHighlighterColors.INLINE_PARAMETER_HINT;

    TextAttributes clone = editor.getColorsScheme().getAttributes(textAttributesKey).clone();

    ComplexityColorUtil.ComplexityColorScheme colorSchemeForComplexity =
        ComplexityColorUtil.getColorSchemeForComplexity(model.getCollectedComplexity());
    int backgroundAlpha = colorSchemeForComplexity.getColor().getAlpha();

    Color backgroundColor = colorSchemeForComplexity.getColor();

    double highlightMultiplier = this.highlighted ? 1 : 0.8d;

    Color defaultBackground = editor.getColorsScheme().getDefaultBackground();
    Color backgroundColorWithAlpha =
        new Color(
            backgroundColor.getRed(),
            backgroundColor.getGreen(),
            backgroundColor.getBlue(),
            (int) (backgroundAlpha * highlightMultiplier));

    Color fontColor =
        getContrastColor(
            ColorUtil.mix(defaultBackground, backgroundColorWithAlpha, highlightMultiplier));
    clone.setForegroundColor(
        new Color(
            fontColor.getRed(),
            fontColor.getGreen(),
            fontColor.getBlue(),
            (int) (fontColor.getAlpha() * highlightMultiplier)));
    clone.setBackgroundColor(backgroundColorWithAlpha);

    // Enhancement: Use bold font for better readability
    clone.setFontType(Font.BOLD);

    return clone;
  }

  public String getContextMenuGroupId(@NotNull Inlay inlay) {
    return "CodeMetricsHints";
  }

  public void setHighlighted(boolean highlighted) {
    this.highlighted = highlighted;
  }

  public MetricsModel getModel() {
    return model;
  }
}
