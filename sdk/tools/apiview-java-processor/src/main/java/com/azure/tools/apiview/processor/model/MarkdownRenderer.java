package com.azure.tools.apiview.processor.model;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders an API listing as concise Java Markdown without documentation tokens.
 */
public final class MarkdownRenderer {
    private static final String INDENT = "    ";

    private MarkdownRenderer() {
    }

    /**
     * Writes an API listing as fenced Java Markdown without documentation tokens.
     *
     * @param apiListing The API listing to render.
     * @param outputFile The destination Markdown file.
     */
    public static void write(APIListing apiListing, File outputFile) {
        try {
            Files.write(outputFile.toPath(), render(apiListing).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Renders an API listing as fenced Java Markdown without documentation tokens.
     *
     * @param apiListing The API listing to render.
     * @return The rendered Markdown.
     */
    public static String render(APIListing apiListing) {
        List<String> lines = new ArrayList<>();
        lines.add("```java");
        renderLines(apiListing.getChildren(), 0, lines);
        lines.add("```");
        return String.join(System.lineSeparator(), lines);
    }

    private static void renderLines(List<ReviewLine> reviewLines, int indentLevel, List<String> output) {
        String indent = repeatIndent(indentLevel);

        for (ReviewLine line : reviewLines) {
            List<ReviewToken> tokens = line.getTokens();
            if (tokens.isEmpty()) {
                output.add("");
            } else {
                String renderedLine = renderTokens(tokens);
                if (!renderedLine.isEmpty()) {
                    output.add(indent + renderedLine);
                }
            }

            if (!line.getChildren().isEmpty()) {
                renderLines(line.getChildren(), indentLevel + 1, output);
            }
        }
    }

    private static String renderTokens(List<ReviewToken> tokens) {
        StringBuilder line = new StringBuilder();

        for (ReviewToken token : tokens) {
            if (token.isDocumentation()) {
                continue;
            }

            // Comments are review context, including method groupings, API shape hints, and inline values.
            String value = token.getValue();
            int start = 0;
            int end = value.length();
            while (start < end && Character.isWhitespace(value.charAt(start))) {
                start++;
            }
            while (end > start && Character.isWhitespace(value.charAt(end - 1))) {
                end--;
            }

            boolean prefixSpace = token.hasPrefixSpace() || start > 0;
            boolean suffixSpace = token.hasSuffixSpace() || end < value.length();
            if (prefixSpace && line.length() > 0 && !Character.isWhitespace(line.charAt(line.length() - 1))) {
                line.append(' ');
            }

            line.append(value, start, end);

            if (suffixSpace && line.length() > 0 && !Character.isWhitespace(line.charAt(line.length() - 1))) {
                line.append(' ');
            }
        }

        int length = line.length();
        while (length > 0 && Character.isWhitespace(line.charAt(length - 1))) {
            length--;
        }
        return line.substring(0, length);
    }

    private static String repeatIndent(int indentLevel) {
        StringBuilder indent = new StringBuilder(indentLevel * INDENT.length());
        for (int i = 0; i < indentLevel; i++) {
            indent.append(INDENT);
        }
        return indent.toString();
    }
}
