// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.tools.lintingextensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Arrays;
import java.util.List;

/**
 * Checks that the exception is created and logged is also thrown.
 */
public class ExceptionCreatedButNotThrownCheck extends AbstractCheck {
    static final String ERROR_MESSAGE = "An exception is created and logged, but not thrown. Ensure the exception is either thrown or not created at all. "
        + "See https://github.com/Azure/azure-sdk-for-java/wiki/Client-core:-logging-exceptions-best-practices for more details.";
    private static final List<String> THROWABLE_AT_LOGGING_METHODS = Arrays.asList(
        ".throwableAtError",
        ".throwableAtWarning");

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
        String name = FullIdent.createFullIdentBelow(token).getText();
        if (THROWABLE_AT_LOGGING_METHODS.stream().anyMatch(name::endsWith) && !isInsideThrow(token)) {
            DetailAST logMethodCall = token.getParent().getParent();
            if (logMethodCall == null || logMethodCall.getType() != TokenTypes.METHOD_CALL) {
                return;
            }

            String nextName = FullIdent.createFullIdentBelow(logMethodCall).getText();
            if (nextName.endsWith(".log")) {
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
