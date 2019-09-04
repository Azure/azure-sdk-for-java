// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.api.JavadocTokenTypes;
import com.puppycrawl.tools.checkstyle.checks.javadoc.AbstractJavadocCheck;
import com.puppycrawl.tools.checkstyle.utils.BlockCommentPosition;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Javadoc Inline tag check:
 * <ol>
 * <li>Use {@literal {@codesnippet ...}} instead of {@literal <code>}, {@literal <pre>}, or {@literal {@code ...}}
 * if these tags span multiple lines. Inline code sample are fine as-is</li>
 * <li>No check on class-level Javadoc</li>
 * </ol>
 */
public class JavadocInlineTagCheck extends AbstractJavadocCheck {
    private static final String MULTIPLE_LINE_SPAN_ERROR = "Tag '%s' spans multiple lines. Use @codesnippet annotation"
        + " instead of '%s' to ensure that the code block always compiles.";

    // HTML tag set that need to be checked to see if there tags span on multiple lines.
    private static final Set<String> CHECK_TAGS = Collections.unmodifiableSet(new HashSet<>(
        Arrays.asList("pre", "code")));

    @Override
    public int[] getDefaultJavadocTokens() {
        return getRequiredJavadocTokens();
    }

    @Override
    public int[] getRequiredJavadocTokens() {
        return new int[] {
            JavadocTokenTypes.HTML_ELEMENT_START,
            JavadocTokenTypes.JAVADOC_INLINE_TAG
        };
    }

    @Override
    public void visitJavadocToken(DetailNode token) {
        DetailAST blockCommentToken = getBlockCommentAst();
        //  Skip check on class-level Javadoc
        if (!BlockCommentPosition.isOnMethod(blockCommentToken)
            && !BlockCommentPosition.isOnConstructor(blockCommentToken)) {
            return;
        }

        switch (token.getType()) {
            case JavadocTokenTypes.HTML_ELEMENT_START:
                checkHtmlElementStart(token);
                break;
            case JavadocTokenTypes.JAVADOC_INLINE_TAG:
                checkJavadocInlineTag(token);
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /**
     * Use {@literal {@codesnippet ...}} instead of {@code '<code>'}, {@code'<pre>'}, or {@literal {@code ...}}
     * if these tags span multiple lines. Inline code sample are fine as-is.
     *
     * @param htmlElementStartNode HTML_ELEMENT_START node
     */
    private void checkHtmlElementStart(DetailNode htmlElementStartNode) {
        final DetailNode tagNameNode =
            JavadocUtil.findFirstToken(htmlElementStartNode, JavadocTokenTypes.HTML_TAG_NAME);
        // HTML tags are case-insensitive
        final String tagName = tagNameNode.getText().toLowerCase();
        if (!CHECK_TAGS.contains(tagName)) {
            return;
        }

        final String tagNameBracket = "<" + tagName + ">";
        final DetailNode htmlTagNode = htmlElementStartNode.getParent();
        if (!isInlineCode(htmlTagNode)) {
            log(htmlTagNode.getLineNumber(), htmlTagNode.getColumnNumber(),
                String.format(MULTIPLE_LINE_SPAN_ERROR, tagNameBracket, tagNameBracket));
        }
    }

    /**
     * Check to see if the JAVADOC_INLINE_TAG node is {@literal @code} tag. If it is, check if the tag contains a new
     * line
     * or a leading asterisk, which implies the tag has spanned in multiple lines.
     *
     * @param inlineTagNode JAVADOC_INLINE_TAG javadoc node
     */
    private void checkJavadocInlineTag(DetailNode inlineTagNode) {
        final DetailNode codeLiteralNode = JavadocUtil.findFirstToken(inlineTagNode, JavadocTokenTypes.CODE_LITERAL);
        if (codeLiteralNode == null) {
            return;
        }

        final String codeLiteral = codeLiteralNode.getText();
        if (!isInlineCode(inlineTagNode)) {
            log(codeLiteralNode.getLineNumber(), codeLiteralNode.getColumnNumber(),
                String.format(MULTIPLE_LINE_SPAN_ERROR, codeLiteral, codeLiteral));
        }
    }

    /**
     * Find if the given tag node is in-line code sample.
     * @param node A given node that could be HTML_TAG or JAVADOC_INLINE_TAG
     * @return false if it is a code block, otherwise, return true if it is a in-line code.
     */
    private boolean isInlineCode(DetailNode node) {
        for (final DetailNode child : node.getChildren()) {
            final int childType = child.getType();
            if (childType == JavadocTokenTypes.NEWLINE || childType == JavadocTokenTypes.LEADING_ASTERISK) {
                return false;
            }
        }
        return true;
    }
}
