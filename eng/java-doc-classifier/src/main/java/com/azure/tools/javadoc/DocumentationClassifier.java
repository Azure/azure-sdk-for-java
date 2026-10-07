// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.javadoc;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaToken;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.JavadocBlockTag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.javaparser.GeneratedJavaParserConstants.JAVADOC_COMMENT;

final class DocumentationClassifier {
    private static final Set<String> BLOCK_TAGS = new HashSet<>(Arrays.asList(
        "author", "version", "param", "return", "throws", "exception", "see", "since",
        "serial", "serialField", "serialData", "apiNote", "implSpec", "implNote"));
    private static final Set<String> INLINE_TAGS = new HashSet<>(Arrays.asList(
        "code", "docRoot", "inheritDoc", "link", "linkplain", "literal", "value"));
    private static final Pattern INLINE_TAG = Pattern.compile("\\{@([A-Za-z][A-Za-z0-9]*)");
    private static final Pattern COMMENT_DIRECTIVE = Pattern.compile(
        "(?im)\\b(?:BEGIN|END)\\s*:|\\b(?:tag|end)::"
            + "|\\b(?:noinspection|NOSONAR|NOPMD|CHECKSTYLE|spotless|formatter|cspell|GEN-BEGIN|GEN-END"
            + "|src_embed|codesnippet)\\b|<!--"
            + "|\\b(?:code\\s+generated|auto[- ]generated|generated\\s+(?:code|by))\\b"
            + "|(?:^|[\\s*])[@#$][A-Za-z]"
            + "|<\\s*/?\\s*(?:editor-fold|generated|auto-generated)\\b"
            + "|^\\s*\\*?\\s*(?!(?:TODO|FIXME|NOTE|HACK)\\b)[A-Za-z][A-Za-z0-9_.-]*\\s*[:=]");

    private final JavaParser parser = new JavaParser(new ParserConfiguration()
        .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_8)
        .setStoreTokens(true)
        .setAttributeComments(true));

    static boolean isEligible(String reason) {
        return "javadoc-only".equals(reason) || "whitespace-only".equals(reason)
            || "ordinary-comment-only".equals(reason) || "non-code-only".equals(reason);
    }

    String compare(String before, String after) {
        // Reject even potentially escaped Unicode input rather than approximate javac's preprocessing.
        if (before.contains("\\u") || after.contains("\\u")) {
            return "unsupported-unicode-escape";
        }
        ParsedSource oldSource = parse(before);
        ParsedSource newSource = parse(after);
        if (oldSource == null || newSource == null) {
            return "parse-error";
        }
        if (oldSource.tokens.size() != newSource.tokens.size()) {
            return "non-documentation-change";
        }

        for (int i = 0; i < oldSource.tokens.size(); i++) {
            JavaToken oldToken = oldSource.tokens.get(i);
            JavaToken newToken = newSource.tokens.get(i);
            if (oldToken.getKind() != newToken.getKind() || !oldToken.getText().equals(newToken.getText())) {
                return "non-documentation-change";
            }
        }
        if (!oldSource.directives.equals(newSource.directives)) {
            return "unsupported-comment-directive";
        }
        if (oldSource.documentation.size() != newSource.documentation.size()) {
            return "documentation-attachment-change";
        }

        boolean changedDocumentation = false;
        for (int i = 0; i < oldSource.documentation.size(); i++) {
            DeclarationDocumentation oldDoc = oldSource.documentation.get(i);
            DeclarationDocumentation newDoc = newSource.documentation.get(i);
            if (oldDoc.position != newDoc.position || !oldDoc.declarationKey.equals(newDoc.declarationKey)) {
                return "documentation-attachment-change";
            }
            if (normalizeLineEndings(oldDoc.comment.getContent())
                .equals(normalizeLineEndings(newDoc.comment.getContent()))) {
                continue;
            }
            if (!oldDoc.supported || !newDoc.supported
                || !supportedDocumentation(oldDoc.comment) || !supportedDocumentation(newDoc.comment)) {
                return "unsupported-documentation";
            }
            changedDocumentation = true;
        }
        if (before.equals(after)) {
            return "no-source-edit";
        }
        boolean changedComments = !oldSource.ordinaryComments.equals(newSource.ordinaryComments);
        boolean changedWhitespace = !oldSource.whitespace.equals(newSource.whitespace);
        if (!changedDocumentation && !changedComments) {
            return "whitespace-only";
        }
        if (!changedWhitespace && changedDocumentation && !changedComments) {
            return "javadoc-only";
        }
        if (!changedWhitespace && changedComments && !changedDocumentation) {
            return "ordinary-comment-only";
        }
        return "non-code-only";
    }

    private ParsedSource parse(String source) {
        ParseResult<CompilationUnit> result = parser.parse(source);
        if (!result.isSuccessful() || !result.getResult().isPresent()) {
            return null;
        }
        CompilationUnit unit = result.getResult().get();
        if (!unit.getTokenRange().isPresent()) {
            return null;
        }
        ParsedSource parsed = new ParsedSource();
        List<JavaToken> comments = new ArrayList<>();
        Map<JavaToken, Integer> indices = new IdentityHashMap<>();
        StringBuilder whitespace = new StringBuilder();
        for (JavaToken token : unit.getTokenRange().get()) {
            indices.put(token, parsed.tokens.size());
            if (token.getCategory().isComment()) {
                comments.add(token);
            } else if (token.getCategory().isWhitespace()) {
                whitespace.append(token.getText());
            } else {
                parsed.whitespace.add(whitespace.toString());
                whitespace.setLength(0);
                parsed.tokens.add(token);
            }
        }
        parsed.whitespace.add(whitespace.toString());

        Map<JavaToken, JavadocComment> documentation = new IdentityHashMap<>();
        for (Comment comment : unit.getAllContainedComments()) {
            if (comment.isJavadocComment() && comment.getTokenRange().isPresent()) {
                documentation.put(comment.getTokenRange().get().getBegin(), comment.asJavadocComment());
            }
        }
        for (JavaToken token : comments) {
            int position = indices.get(token);
            if (token.getKind() == JAVADOC_COMMENT) {
                JavadocComment comment = documentation.get(token);
                if (comment == null) {
                    return null;
                }
                Node declaration = comment.getCommentedNode().orElse(null);
                String key = "orphan";
                if (declaration != null && declaration.getTokenRange().isPresent()) {
                    Integer begin = indices.get(declaration.getTokenRange().get().getBegin());
                    Integer end = indices.get(declaration.getTokenRange().get().getEnd());
                    if (begin == null || end == null) {
                        return null;
                    }
                    // Significant-token positions stay stable when whitespace or ordinary comments change.
                    key = declaration.getClass().getName() + ":" + begin + ":" + end;
                }
                parsed.documentation.add(new DeclarationDocumentation(comment, key, position,
                    supportedDeclaration(declaration)));
            } else {
                String fingerprint = position + ":" + token.getKind() + ":" + normalizeLineEndings(token.getText());
                parsed.ordinaryComments.add(fingerprint);
                String content = token.getText().substring(2);
                if (COMMENT_DIRECTIVE.matcher(content).find()) {
                    JavaToken previous = position > 0 ? parsed.tokens.get(position - 1) : null;
                    JavaToken next = position < parsed.tokens.size() ? parsed.tokens.get(position) : null;
                    parsed.directives.add(sameLine(previous, token) + ":" + sameLine(token, next) + ":" + fingerprint);
                }
            }
        }
        return parsed;
    }

    private static String normalizeLineEndings(String value) {
        return value.replace("\r\n", "\n").replace('\r', '\n');
    }

    private static boolean sameLine(JavaToken first, JavaToken second) {
        if (first == null || second == null) {
            return false;
        }
        Range firstRange = first.getRange().orElse(null);
        Range secondRange = second.getRange().orElse(null);
        return firstRange != null && secondRange != null && firstRange.end.line == secondRange.begin.line;
    }

    private static boolean supportedDeclaration(Node node) {
        return node instanceof TypeDeclaration || node instanceof CallableDeclaration
            || node instanceof FieldDeclaration || node instanceof EnumConstantDeclaration
            || node instanceof AnnotationMemberDeclaration;
    }

    private static boolean supportedDocumentation(JavadocComment comment) {
        if (comment.getContent().contains("@deprecated")) {
            return false;
        }
        for (JavadocBlockTag tag : comment.parse().getBlockTags()) {
            if (!BLOCK_TAGS.contains(tag.getTagName())) {
                return false;
            }
        }
        Matcher inlineTags = INLINE_TAG.matcher(comment.getContent());
        while (inlineTags.find()) {
            if (!INLINE_TAGS.contains(inlineTags.group(1))) {
                return false;
            }
        }
        return true;
    }

    private static final class ParsedSource {
        private final List<JavaToken> tokens = new ArrayList<>();
        private final List<String> whitespace = new ArrayList<>();
        private final List<String> ordinaryComments = new ArrayList<>();
        private final List<String> directives = new ArrayList<>();
        private final List<DeclarationDocumentation> documentation = new ArrayList<>();
    }

    private static final class DeclarationDocumentation {
        private final JavadocComment comment;
        private final String declarationKey;
        private final int position;
        private final boolean supported;

        private DeclarationDocumentation(JavadocComment comment, String declarationKey, int position,
            boolean supported) {
            this.comment = comment;
            this.declarationKey = declarationKey;
            this.position = position;
            this.supported = supported;
        }
    }
}
