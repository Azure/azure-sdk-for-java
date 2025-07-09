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
 * Check that verifies that exceptions are logged when they are created.
 * The check is skipped if:
 * - the exception is created in a generated method or service interface
 * - the exception is a NullPointerException, IllegalArgumentException or UnsupportedOperationException that are used for immediate input validation
 */
public class RawExceptionThrowCheck extends AbstractCheck {
    static final String ERROR_MESSAGE = "Directly throwing a new exception is disallowed. Use the \"io.clientcore.core.instrumentation.logging.ClientLogger\" API instead, "
        + "such as \"logger.throwableAtError()\" or \"logger.throwableAtWarning()\"."
        + "See https://github.com/Azure/azure-sdk-for-java/wiki/Client-core:-logging-exceptions-best-practices for more details.";

    private static final List<String> IGNORED_EXCEPTIONS = Arrays.asList("NullPointerException",
        "IllegalArgumentException",
        "IllegalStateException",
        "UnsupportedOperationException");

    private static final String CORE_EXCEPTION_FACTORY_NAME = "CoreException.from";

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
            TokenTypes.LITERAL_THROW
        };
    }

    @Override
    public void visitToken(DetailAST token) {
        DetailAST expr = token.findFirstToken(TokenTypes.EXPR);
        if (expr == null) {
            return;
        }

        DetailAST newToken = expr.findFirstToken(TokenTypes.LITERAL_NEW);
        if (newToken != null) {
            String name = FullIdent.createFullIdentBelow(newToken).getText();
            if (IGNORED_EXCEPTIONS.stream().noneMatch(name::endsWith)) {
                log(newToken, ERROR_MESSAGE);
            }

            return;
        }

        DetailAST methodCallToken = expr.findFirstToken(TokenTypes.METHOD_CALL);
        if (methodCallToken != null) {
            if (CORE_EXCEPTION_FACTORY_NAME.equals(FullIdent.createFullIdentBelow(methodCallToken).getText()))
            {
                log(methodCallToken, ERROR_MESSAGE);
            }
        }
    }
}
