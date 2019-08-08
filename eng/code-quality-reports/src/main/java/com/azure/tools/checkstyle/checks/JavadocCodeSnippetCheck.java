// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.api.JavadocTokenTypes;
import com.puppycrawl.tools.checkstyle.checks.javadoc.AbstractJavadocCheck;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;

import java.util.regex.Pattern;

/**
 * Requirement of Javadoc annotation @codesnippet check:
 *  (1) Use '{@codesnippet ...}' instead of '<code>', '<pre>'.
 *  (2) No multiple lines span at the '{@code ...}'.
 *  (3) naming pattern of codesnippet":
 *    1, For package name, class name, they should all be lower case.
 *       For method name,  the first letter of method name should be lower case.
 *       For parameters, they should only be letters. Concatenate all parameters with dash character.
 *    2, Jonathon will add more requirement specs later.
 */
public class JavadocCodeSnippetCheck extends AbstractJavadocCheck {

    private static final String CLASS_PATH_REGEX = "[a-z.]+";
    private static final String METHOD_NAME_REGEX = "^[a-z][a-zA-Z]+";
    private static final String PARAMETERS_REGEX = "[a-zA-Z-]+";

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
     * Requirement of rules:
     * (1) No usage of '<code>', '<pre>',  use '{@codesnippet ...} instead'.
     * (2) No multiple lines span at the '{@code ...}'
     *
     * @param htmlElementStartNode
     */
    private void checkHtmlElementStart(DetailNode htmlElementStartNode) {
        final DetailNode tagNameNode = JavadocUtil.findFirstToken(htmlElementStartNode, JavadocTokenTypes.HTML_TAG_NAME);
        final String tagName = tagNameNode.getText();
        if ("code".equals(tagName) || "pre".equals(tagName)) {
            log(tagNameNode.getLineNumber(), tagNameNode.getColumnNumber(),
                String.format("Do not use <%s> html tag in javadoc. Use <codesnippet> instead.", tagName));
        }
    }

    /**
     * Check to see if the JAVADOC_INLINE_TAG node is '@code' tag or '@codesnippet' tag.
     * If the JAVADOC_INLINE_TAG is one of two, check if the tag contains new line or leading asterisk,
     * which implies the tag has spanned in multiple lines.
     *
     * @param inlineTagNode JAVADOC_INLINE_TAG javadoc node
     */
    private void checkJavadocInlineTag(DetailNode inlineTagNode) {
        boolean isCodeOrCodeSnippet = false;
        if (JavadocUtil.findFirstToken(inlineTagNode, JavadocTokenTypes.CODE_LITERAL) != null) {
            isCodeOrCodeSnippet = true;
        }
        isCodeOrCodeSnippet |= isCodeSnippet(inlineTagNode);
        if (!isCodeOrCodeSnippet) {
            return;
        }

        for (final DetailNode child : inlineTagNode.getChildren()) {
            final int childType = child.getType();

            if (childType == JavadocTokenTypes.NEWLINE || childType == JavadocTokenTypes.LEADING_ASTERISK) {
                log(child.getLineNumber(), child.getColumnNumber(), "No multiple lines in @code annotation");
            }

            // This code section duplicates the checking on @codesnippet, maven build already failed it
            if (childType == JavadocTokenTypes.DESCRIPTION
                && (!JavadocUtil.containsInBranch(child, JavadocTokenTypes.NEWLINE)
                    || !JavadocUtil.containsInBranch(child, JavadocTokenTypes.LEADING_ASTERISK))) {
                log(child.getLineNumber(), child.getColumnNumber(), "No multiple lines in @codesnippet annotation");
            }
        }
        return;
    }

    /**
     * Check to see if the code snippet name matched the naming pattern.
     *
     * @param inlineTagNode JAVADOC_INLINE_TAG javadoc node
     */
    private void checkCodeSnippetNaming(DetailNode inlineTagNode) {
        if (!isCodeSnippet(inlineTagNode)) {
            return;
        }

        final DetailNode descriptionNode = JavadocUtil.findFirstToken(inlineTagNode, JavadocTokenTypes.DESCRIPTION);
        if (descriptionNode == null) {
            return;
        }

        final DetailNode textNode = JavadocUtil.findFirstToken(descriptionNode, JavadocTokenTypes.TEXT);
        if (textNode == null) {
            return;
        }

        final String namingPattern = textNode.getText();
        // Verify naming pattern spec:
        // 1, For package name, class name, they should all be lower case;
        //    For method name,  the first letter of method name should be lower case.
        //    For parameters, they should only be letters. Concatenate all parameters with dash character
        // 2, Jonathon will add more requirement specs later
        if (!isNamingMatch(namingPattern)) {
            log(textNode.getLineNumber(), textNode.getColumnNumber(),
                String.format("Naming pattern mismatch. It should only contain lower case for the package path and class name for matching regular expression ''%s''" +
                    " and the first letter of method name must be small case for matching regular expression ''%s'', and the parameters should be all small case for matching " +
                    "regular expression ''%s''.", CLASS_PATH_REGEX, METHOD_NAME_REGEX, PARAMETERS_REGEX));
        }
    }

    /**
     * Find if the given JAVADOC_INLINE_TAG is a @codesnippet tag.
     *
     * @param inlineTagNode JAVADOC_INLINE_TAG javadoc node
     * @return true if it is a '@codesnippet' tag, false otherwise.
     */
    private boolean isCodeSnippet(DetailNode inlineTagNode) {
        final DetailNode customNameNode = JavadocUtil.findFirstToken(inlineTagNode, JavadocTokenTypes.CUSTOM_NAME);
        if (customNameNode == null) {
            return false;
        }
        return customNameNode.getText().equals("@codesnippet");
    }

    /**
     *  Find if the given name of code snippet matched the naming pattern specs.
     *
     * @param name code snippet name
     * @return true if match, otherwise, false
     */
    private boolean isNamingMatch(String name) {
        final String[] str = name.split("#");
        boolean isCorrectNamingPattern = true;
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
        isCorrectNamingPattern &= Pattern.compile(CLASS_PATH_REGEX).matcher(className).matches();
        // method name
        final String methodName = classPath.substring(lastIndexOf + 1);
        isCorrectNamingPattern &= Pattern.compile(METHOD_NAME_REGEX).matcher(methodName).matches();

        // parameters of method function
        if (str.length == 2){
            isCorrectNamingPattern &= Pattern.compile(PARAMETERS_REGEX).matcher(str[1]).matches();
        }

        return isCorrectNamingPattern;
    }
}
