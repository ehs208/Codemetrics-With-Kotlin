package com.github.ehs208.codemetrics.util;

import com.github.ehs208.codemetrics.core.config.MetricsConfiguration;
import com.intellij.ui.ColorUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

import static com.github.ehs208.codemetrics.util.ColorContrastUtil.getContrastColor;

public final class ComplexityColorUtil {

  public static ComplexityColorScheme getColorSchemeForComplexity(long summary) {
    Color fontColor;
    Color color;
    MetricsConfiguration configuration = MetricsConfiguration.getInstance();
    ArrayList<Integer> complexities = new ArrayList<>();
    complexities.add(configuration.complexityLevelLow);
    complexities.add(configuration.complexityLevelNormal);
    complexities.add(configuration.complexityLevelHigh);
    complexities.add(configuration.complexityLevelExtreme);
    complexities.sort(Comparator.naturalOrder());

    Integer complexityLevelLow = complexities.get(0);
    Integer complexityLevelNormal = complexities.get(1);
    Integer complexityLevelHigh = complexities.get(2);
    Integer complexityLevelExtreme = complexities.get(3);

    Color extremeColor = new Color(configuration.complexityColorExtreme, true);
    Color highColor = new Color(configuration.complexityColorHigh, true);
    Color normalColor = new Color(configuration.complexityColorNormal, true);
    Color lowColor = new Color(configuration.complexityColorLow, true);

    if (summary >= complexityLevelExtreme) {
      int range = complexityLevelExtreme - complexityLevelHigh;
      if (range == 0 || summary == complexityLevelHigh) {
        // Degenerate case: thresholds are equal or at exact boundary
        color = extremeColor;
      } else {
        double balance = (summary - complexityLevelHigh) / (double) range;
        color = ColorUtil.mix(extremeColor, highColor, 1d / balance);
      }
    } else if (summary >= complexityLevelHigh) {
      int range = complexityLevelHigh - complexityLevelNormal;
      if (range == 0 || summary == complexityLevelNormal) {
        // Degenerate case: thresholds are equal or at exact boundary
        color = highColor;
      } else {
        double balance = (summary - complexityLevelNormal) / (double) range;
        color = ColorUtil.mix(highColor, normalColor, 1d / balance);
      }
    } else if (summary >= complexityLevelNormal) {
      int range = complexityLevelNormal - complexityLevelLow;
      if (range == 0 || summary == complexityLevelLow) {
        // Degenerate case: thresholds are equal or at exact boundary
        color = normalColor;
      } else {
        double balance = (summary - complexityLevelLow) / (double) range;
        color = ColorUtil.mix(normalColor, lowColor, 1d / balance);
      }
    } else {
      color = lowColor;
    }
    fontColor = getContrastColor(color);
    return new ComplexityColorScheme(color, fontColor);
  }

  public static class ComplexityColorScheme {
    private Color color;
    private Color fontColor;

    public ComplexityColorScheme(Color color, Color fontColor) {
      this.color = color;
      this.fontColor = fontColor;
    }

    public Color getColor() {
      return color;
    }

    public Color getFontColor() {
      return fontColor;
    }
  }
}
