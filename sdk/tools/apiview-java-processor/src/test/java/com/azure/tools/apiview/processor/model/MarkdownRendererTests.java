// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MarkdownRendererTests {
    @Test
    public void rendersNestedApiWithoutDocumentation() {
        ApiListing listing = new ApiListing();
        ReviewLine module = listing.addChildLine()
            .addToken(TokenKind.KEYWORD, "module")
            .addToken(TokenKind.MODULE_NAME, "com.azure.widgets")
            .addContextStartTokens();

        module.addChildLine().addToken(new ReviewToken(TokenKind.JAVADOC, "/** Widget client. */").setDocumentation());

        module.addChildLine()
            .addToken(TokenKind.KEYWORD, "public")
            .addToken(TokenKind.RETURN_TYPE, "Widget")
            .addToken(TokenKind.METHOD_NAME, "getWidget", Spacing.NO_SPACE)
            .addToken(TokenKind.PUNCTUATION, "(", Spacing.NO_SPACE)
            .addToken(TokenKind.PARAMETER_TYPE, "String")
            .addToken(TokenKind.PARAMETER_NAME, "name", Spacing.NO_SPACE)
            .addToken(TokenKind.PUNCTUATION, ")", Spacing.NO_SPACE);

        String expected = String.join(System.lineSeparator(), "```java", "module com.azure.widgets {",
            "    public Widget getWidget(String name)", "```");

        String markdown = MarkdownRenderer.render(listing);

        assertEquals(expected, markdown);
        assertFalse(markdown.contains("Widget client"));
    }

    @Test
    public void removesDocumentationTokensFromMixedLines() {
        ApiListing listing = new ApiListing();
        listing.addChildLine()
            .addToken(new ReviewToken(TokenKind.JAVADOC, "documentation").setDocumentation())
            .addToken(TokenKind.KEYWORD, "public")
            .addToken(TokenKind.TYPE_NAME, "Widget", Spacing.NO_SPACE);

        assertEquals(String.join(System.lineSeparator(), "```java", "public Widget", "```"),
            MarkdownRenderer.render(listing));
    }

    @Test
    public void preservesStandaloneAndInlineComments() {
        ApiListing listing = new ApiListing();
        listing.addChildLine().addToken(TokenKind.COMMENT, "// Service Methods:");
        listing.addChildLine()
            .addToken(TokenKind.KEYWORD, "public")
            .addToken(TokenKind.TYPE_NAME, "Widget", Spacing.NO_SPACE)
            .addToken(TokenKind.PUNCTUATION, "=", Spacing.SPACE_BEFORE_AND_AFTER)
            .addToken(TokenKind.KEYWORD, "new")
            .addToken(TokenKind.TYPE_NAME, "Widget", Spacing.NO_SPACE)
            .addToken(TokenKind.PUNCTUATION, "(", Spacing.NO_SPACE)
            .addToken(TokenKind.COMMENT, "/* Elided */", Spacing.NO_SPACE)
            .addToken(TokenKind.PUNCTUATION, ")", Spacing.NO_SPACE);

        assertEquals(String.join(System.lineSeparator(), "```java", "// Service Methods:",
            "public Widget = new Widget(/* Elided */)", "```"), MarkdownRenderer.render(listing));
    }

    @Test
    public void normalizesWhitespaceAtTokenBoundaries() {
        ApiListing listing = new ApiListing();
        listing.addChildLine()
            .addToken(TokenKind.ANNOTATION_NAME, "@Example", Spacing.NO_SPACE)
            .addToken(TokenKind.PUNCTUATION, "(", Spacing.NO_SPACE)
            .addToken(TokenKind.ANNOTATION_PARAMETER_NAME, "value", Spacing.NO_SPACE)
            .addToken(TokenKind.PUNCTUATION, " = ")
            .addToken(TokenKind.ANNOTATION_PARAMETER_VALUE, "left")
            .addToken(TokenKind.PUNCTUATION, " + ")
            .addToken(TokenKind.ANNOTATION_PARAMETER_VALUE, "right", Spacing.NO_SPACE)
            .addToken(TokenKind.PUNCTUATION, ",")
            .addToken(TokenKind.PUNCTUATION, "}", Spacing.SPACE_BEFORE);

        assertEquals(String.join(System.lineSeparator(), "```java", "@Example(value = left + right, }", "```"),
            MarkdownRenderer.render(listing));
    }
}
