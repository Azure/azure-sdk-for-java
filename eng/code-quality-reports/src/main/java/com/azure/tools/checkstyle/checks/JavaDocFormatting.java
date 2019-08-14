// Licensed under the MIT License.
// Copyright (c) Microsoft Corporation. All rights reserved.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.api.JavadocTokenTypes;
import com.puppycrawl.tools.checkstyle.checks.javadoc.AbstractJavadocCheck;

import java.util.Optional;

/**
 *  Description text should only have one space character after the parameter name or '@return' statement.
 *  Text should not start on a new line or have any additional spacing or indentation.
 */
public class JavaDocFormatting extends AbstractJavadocCheck {

    private static final int[] VALID_JAVADOC_PARAM_ORDER = new int[] {
        JavadocTokenTypes.PARAM_LITERAL,
        JavadocTokenTypes.WS,
        JavadocTokenTypes.PARAMETER_NAME,
        JavadocTokenTypes.WS,
        JavadocTokenTypes.DESCRIPTION
    };

    @Override
    public int[] getAcceptableJavadocTokens() {
        return getRequiredJavadocTokens();
    }

    @Override
    public int[] getRequiredJavadocTokens() {
        return new int[] {
            JavadocTokenTypes.JAVADOC_TAG
        };
    }

    @Override
    public int[] getDefaultJavadocTokens() {
        return getRequiredJavadocTokens();
    }

    @Override
    public void visitJavadocToken(DetailNode javaDocTag) {
        if (javaDocTag.getChildren().length > VALID_JAVADOC_PARAM_ORDER.length) {
            log(javaDocTag.getLineNumber(), "Wrong Javadoc format");
        }

    }
}
