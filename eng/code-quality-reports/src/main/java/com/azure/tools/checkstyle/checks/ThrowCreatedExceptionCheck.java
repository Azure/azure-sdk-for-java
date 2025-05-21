// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Checks that the exception is created and logged is also thrown.
 */
public class ThrowCreatedExceptionCheck extends AbstractCheck {
    static final String ERROR_MESSAGE = "Exception is created and logged, but is not thrown. Make sure to throw exception or avoid creating it.";
    private static final Set<String> THROWABLE_AT_LOGGING_METHODS = new HashSet<>(Arrays.asList(
        ".throwableAtError",
        ".throwableAtWarning"));

    @Override
    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[] {
            TokenTypes.METHOD_CALL
        };
    }

    @Override
    public void visitToken(DetailAST token) {
        if (token.getType() == TokenTypes.METHOD_CALL) {
            String name = FullIdent.createFullIdentBelow(token).getText();
            if (THROWABLE_AT_LOGGING_METHODS.stream().anyMatch(name::endsWith) && !isInsideThrow(token)) {
                log(token, ERROR_MESSAGE);
            }
        }
    }

    private boolean isInsideThrow(DetailAST methodCallAst) {
        DetailAST parent = methodCallAst.getParent();

        // Walk up to skip DOT or EXPR
        while (parent != null && (parent.getType() == TokenTypes.DOT
            || parent.getType() == TokenTypes.EXPR
            || parent.getType() == TokenTypes.TYPECAST
            || parent.getType() == TokenTypes.METHOD_CALL)) {
            parent = parent.getParent();
        }

        return parent != null && parent.getType() == TokenTypes.LITERAL_THROW;
    }
}
