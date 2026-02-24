package com.github.ehs208.codemetrics.ai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class ResponseParsingUtils {

    private ResponseParsingUtils() {
    }

    public static String extractErrorMessage(String responseBody) {
        try {
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            if (json.has("error")) {
                JsonObject error = json.getAsJsonObject("error");
                if (error != null && error.has("message")) {
                    return error.get("message").getAsString();
                }
            }
        } catch (Exception ignored) {
        }
        return "Unknown error";
    }

    public static String extractCodeBlock(String text) {
        int start = text.indexOf("```");
        if (start == -1) return text.trim();

        int codeStart = text.indexOf('\n', start);
        if (codeStart == -1) return text.trim();
        codeStart++;

        int end = text.indexOf("```", codeStart);
        if (end == -1) return text.substring(codeStart).trim();

        return text.substring(codeStart, end).trim();
    }

    public static String extractExplanation(String text) {
        int explStart = text.indexOf("## Explanation");
        if (explStart == -1) {
            explStart = text.indexOf("**Explanation");
        }
        if (explStart == -1) {
            int lastCodeBlock = text.lastIndexOf("```");
            if (lastCodeBlock == -1) return "";
            int afterBlock = text.indexOf('\n', lastCodeBlock);
            if (afterBlock == -1) return "";
            String trailing = text.substring(afterBlock).trim();
            return trailing.isEmpty() ? "Refactoring suggestion generated." : trailing;
        }
        return text.substring(explStart).trim();
    }
}
