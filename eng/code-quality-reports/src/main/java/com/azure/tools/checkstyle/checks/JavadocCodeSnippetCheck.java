// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.api.JavadocTokenTypes;
import com.puppycrawl.tools.checkstyle.checks.javadoc.AbstractJavadocCheck;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Requirement of Javadoc annotation {@literal @codesnippet}  check:
 * <ol>
 *   <li>Use {@literal {@codesnippet ...}} instead of '<code>', '<pre>', or {@literal {@code ...}) if these tags span
 *       multiple lines. Inline code sample are fine as-is</li>
 *   <ol>Naming pattern of {@literal {@codesnippet}}:
 *     <li>For package name, class name, they should all be lower case;
 *         For method name, the first letter of method name should be lower case;
 *         For parameters, they should only be letters. Concatenate all parameters with dash character.</li>
 *     <li>Jonathan will add more requirement specs later.</li>
 *   </ol>
 * </ol>
 */
public class JavadocCodeSnippetCheck extends AbstractJavadocCheck {
    // Code snippet annotation naming pattern:
    // Check package name, class name, method name, parameters names' naming patterns.
    private static final String CLASS_PATH_REGEX = "[a-z.]+";
    private static final String METHOD_NAME_REGEX = "^[a-z][a-zA-Z]+";
    private static final String PARAMETERS_REGEX = "[a-zA-Z-]+";

    private static final String CODE_SNIPPET_ANNOTATION = "@codesnippet";
    private static final String MULTIPLE_LINE_SPAN_ERROR = "Tag '%s' span in multiple lines. Use @codesnippet annotation" +
        " instead of '%s' to ensure that the code block always compiles.";

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
        switch (token.getType()) {
            case JavadocTokenTypes.HTML_ELEMENT_START:
                checkHtmlElementStart(token);
                break;
            case JavadocTokenTypes.JAVADOC_INLINE_TAG:
                checkJavadocInlineTag(token);
                checkCodeSnippetNaming(token);
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /**
     * Use {@literal {@codesnippet ...}} instead of '<code>', '<pre>', or {@literal {@code ...}) if these tags span
     * multiple lines. Inline code sample are fine as-is.
     *
     * @param htmlElementStartNode HTML_ELEMENT_START node
     */
    private void checkHtmlElementStart(DetailNode htmlElementStartNode) {
        final DetailNode tagNameNode = JavadocUtil.findFirstToken(htmlElementStartNode, JavadocTokenTypes.HTML_TAG_NAME);
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
     * Check to see if the JAVADOC_INLINE_TAG node is {@literal @code} tag. If it is, check if the tag contains a new line
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
     * Check to see if the code snippet name matched the naming pattern.
     * A valid example of naming pattern could be:
     *   com.azure.security.keyvault.keys.cryptography.cryptographyclient.encrypt#symmetric-encrypt
     *
     * For more detail, please refers to this class description javadoc.
     *
     * @param inlineTagNode JAVADOC_INLINE_TAG javadoc node
     */
    private void checkCodeSnippetNaming(DetailNode inlineTagNode) {
        // Only checks on @codesnippet annotation
        final DetailNode customNameNode = JavadocUtil.findFirstToken(inlineTagNode, JavadocTokenTypes.CUSTOM_NAME);
        if (customNameNode == null || !CODE_SNIPPET_ANNOTATION.equals(customNameNode.getText())) {
            return;
        }

        final DetailNode descriptionNode = JavadocUtil.findFirstToken(inlineTagNode, JavadocTokenTypes.DESCRIPTION);
        if (descriptionNode == null) {
            log(descriptionNode.getLineNumber(), descriptionNode.getColumnNumber(),
                String.format("%s has no description.", CODE_SNIPPET_ANNOTATION));
            return;
        }
        // DESCRIPTION always has child TEXT
        final DetailNode textNode = JavadocUtil.findFirstToken(descriptionNode, JavadocTokenTypes.TEXT);
        final String namingPattern = textNode.getText();
        // Verify naming pattern spec:
        // 1. For package name, class name, they should all be lower case;
        //    For method name,  the first letter of method name should be lower case.
        //    For parameters, they should only be letters. Concatenate all parameters with dash character
        // 2. Jonathon will add more requirement specs later
        if (!isNamingMatch(namingPattern)) {
            log(textNode.getLineNumber(), textNode.getColumnNumber(),
                String.format("Naming pattern mismatch. It should only contain lower case for the package path and class name for matching regular expression ''%s''" +
                    " and the first letter of method name must be small case for matching regular expression ''%s'', and the parameters should be all small case for matching " +
                    "regular expression ''%s''.", CLASS_PATH_REGEX, METHOD_NAME_REGEX, PARAMETERS_REGEX));
        }
    }

    /**
     *  Find if the given name of code snippet matched the naming pattern specs.
     *
     * @param name code snippet name
     * @return true if match, otherwise, false
     */
    private boolean isNamingMatch(String name) {
        final String[] str = name.split("#");

        // invalid naming pattern
        if (str.length == 0 || str.length > 2) {
            return false;
        }

        final String classPath = str[0];
        // corner case: empty string will also be false
        if (classPath.isEmpty()) {
            return false;
        }

        final int lastIndexOf = classPath.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return false;
        }

        // full path of the class, check to see if there path only has lower case letters and dot
        final String className = classPath.substring(0, lastIndexOf);
        if (!Pattern.compile(CLASS_PATH_REGEX).matcher(className).matches()) {
            return false;
        }

        // method name
        final String methodName = classPath.substring(lastIndexOf + 1);
        if (!Pattern.compile(METHOD_NAME_REGEX).matcher(methodName).matches()) {
            return false;
        }

        // parameters of method function
        if (str.length == 2 && !Pattern.compile(PARAMETERS_REGEX).matcher(str[1]).matches()) {
            return false;
        }

        return true;
    }

    /**
     *  Find if the given tag node is in-line code sample.
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
