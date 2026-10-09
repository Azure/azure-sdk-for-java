// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.javadoc;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;

import java.util.regex.Pattern;

final class DocumentationClassifier {
    private static final String DOCUMENTATION_MARKERS = "author|version|param|return|throws|exception|see|since"
        + "|serial|serialField|serialData|apiNote|implSpec|implNote|code|docRoot|inheritDoc"
        + "|link|linkplain|literal|value|hidden|index|summary|systemProperty|snippet";
    private static final String STATIC_MARKERS = "formatter|spotless|cspell|CHECKSTYLE|noinspection|NOSONAR|NOPMD"
        + "|NON-NLS-[0-9]+|src_embed|codesnippet";
    private static final Pattern UNSUPPORTED_DIRECTIVE = Pattern.compile(
        "(?im)\\b(?:GEN-BEGIN|GEN-END)\\b|\\b(?:tag|end)::"
            + "|<\\s*/?\\s*(?:generated|auto-generated)\\b|<!--(?!\\s*src_embed\\b)"
            + "|(?:^|[\\s*{])[@#$](?!(?:" + DOCUMENTATION_MARKERS + "|" + STATIC_MARKERS + ")\\b)[A-Za-z]");
    private static final Pattern UNCLASSIFIED_LINE_DIRECTIVE = Pattern.compile(
        "(?im)^\\h*+\\*?\\h*+(?!(?:TODO|FIXME|NOTE|HACK|BEGIN|END|" + STATIC_MARKERS
            + ")\\b)[A-Za-z][A-Za-z0-9_.-]*+\\s*+[:=]");

    private final JavaParser parser = new JavaParser(new ParserConfiguration()
        .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_8)
        .setStoreTokens(false)
        .setAttributeComments(false)
        .setDetectOriginalLineSeparator(false));

    static boolean isEligible(String reason) {
        return "ast-equivalent".equals(reason);
    }

    String compare(String before, String after) {
        // File-level exclusions avoid approximating compiler preprocessing or rebuilding comment attribution.
        if (before.contains("\\u") || after.contains("\\u")) {
            return "unsupported-unicode-escape";
        }
        if (before.contains("@deprecated") || after.contains("@deprecated")) {
            return "unsupported-deprecation";
        }
        ParseResult<CompilationUnit> oldSource = parser.parse(before);
        ParseResult<CompilationUnit> newSource = parser.parse(after);
        if (!complete(oldSource) || !complete(newSource)) {
            return "parse-error";
        }
        if (unsupportedDirectives(oldSource) || unsupportedDirectives(newSource)) {
            return "unsupported-comment-directive";
        }
        if (before.equals(after)) {
            return "no-source-edit";
        }
        return oldSource.getResult().get().toString().equals(newSource.getResult().get().toString())
            ? "ast-equivalent" : "non-documentation-change";
    }

    private static boolean complete(ParseResult<CompilationUnit> source) {
        return source.isSuccessful() && source.getResult().isPresent() && source.getCommentsCollection().isPresent();
    }

    private static boolean unsupportedDirectives(ParseResult<CompilationUnit> source) {
        // With attribution disabled, comments live in the parse result's inventory, not the compilation unit.
        for (Comment comment : source.getCommentsCollection().get().getComments()) {
            String content = comment.getContent();
            if (UNSUPPORTED_DIRECTIVE.matcher(content).find()
                || (!comment.isJavadocComment() && UNCLASSIFIED_LINE_DIRECTIVE.matcher(content).find())) {
                return true;
            }
        }
        return false;
    }
}
