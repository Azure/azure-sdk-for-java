// Licensed under the MIT License.
// Copyright (c) Microsoft Corporation. All rights reserved.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.api.JavadocTokenTypes;
import com.puppycrawl.tools.checkstyle.checks.javadoc.AbstractJavadocCheck;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;

/**
 *  Description text should only have one space character after the parameter name or {@code @return} statement.
 *  Text should not start on a new line or have any additional spacing or indentation.
 */
public class JavaDocFormatting extends AbstractJavadocCheck {

    private static final String JAVA_DOC_RETURN = "javadoc return";
    private static final String JAVA_DOC_PARAMETER = "javadoc parameter";
    private static final String JAVA_DOC_THROW = "javadoc throw";
    private static final String JAVA_DOC_DEPRECATED = "javadoc deprecated";

    private static final String ERROR_DESCRIPTION_ON_NEW_LINE = "Description for %s must be on same the same line.";
    private static final String ERROR_NO_DESCRIPTION = "Description is missing for %s. Consider adding a description.";
    private static final String ERROR_NO_WS_AFTER_IDENT = "No white space after %s. Consider fixing format.";
    private static final String ERROR_EXTRA_SPACE = "Only one white space is expected after %s. Consider removing extra spaces.";

    @Override
    public int[] getAcceptableJavadocTokens() {
        return getRequiredJavadocTokens();
    }

    @Override
    public int[] getRequiredJavadocTokens() {
        return new int[] {
            JavadocTokenTypes.PARAMETER_NAME,
            JavadocTokenTypes.RETURN_LITERAL,
            JavadocTokenTypes.THROWS_LITERAL,
            JavadocTokenTypes.DEPRECATED_LITERAL,
        };
    }

    @Override
    public int[] getDefaultJavadocTokens() {
        return getRequiredJavadocTokens();
    }

    @Override
    public void visitJavadocToken(DetailNode javaDocTag) {
        switch (javaDocTag.getType()) {
            case JavadocTokenTypes.RETURN_LITERAL:
                evaluateValidFormat(javaDocTag, JAVA_DOC_RETURN);
            case JavadocTokenTypes.PARAMETER_NAME:
                evaluateValidFormat(javaDocTag, JAVA_DOC_PARAMETER);
                break;
            case JavadocTokenTypes.THROWS_LITERAL:
                // Evaluate what is the format after the CLASS_NAME of a @throw
                DetailNode throwFormat = JavadocUtil.getNextSibling(javaDocTag, JavadocTokenTypes.CLASS_NAME);
                evaluateValidFormat(throwFormat, JAVA_DOC_THROW);
            case JavadocTokenTypes.DEPRECATED_LITERAL:
                evaluateValidFormat(javaDocTag, JAVA_DOC_DEPRECATED);
            default:
                break;
        }
    }

    /*
     * Function receives a DetailNode as the start token and then validates what comes after that node.
     * valid format is:
     * - A single white space is expected after the node. (no NEW_LINE) or other token
     *
     */
    private void evaluateValidFormat(DetailNode javaDocTag, String identifier) {
        DetailNode nextNodeAfterParameterName = JavadocUtil.getNextSibling(javaDocTag);

        if (nextNodeAfterParameterName.getType() == JavadocTokenTypes.NEWLINE) {
            if (JavadocUtil.getNextSibling(nextNodeAfterParameterName, JavadocTokenTypes.DESCRIPTION) != null) {
                // Description on next line or after some other lines/spaces/staff
                log(javaDocTag.getLineNumber(), String.format(ERROR_DESCRIPTION_ON_NEW_LINE, identifier));
            } else {
                // No description for parameter name
                log(javaDocTag.getLineNumber(), String.format(ERROR_NO_DESCRIPTION, identifier));
            }
        } else if (nextNodeAfterParameterName.getType() != JavadocTokenTypes.WS) {
            log(javaDocTag.getLineNumber(), String.format(ERROR_NO_WS_AFTER_IDENT, identifier));
        } else if (!nextNodeAfterParameterName.getText().equals(" ")) {
            log(javaDocTag.getLineNumber(), String.format(ERROR_EXTRA_SPACE, identifier));
        }
    }
}
