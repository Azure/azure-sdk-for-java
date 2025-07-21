// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Check that verifies that exceptions are logged when they are created.
 * The check is skipped if:
 * <ul>
 *   <li>The exception is created in a generated method or service interface.</li>
 *   <li>The exception is a NullPointerException, IllegalArgumentException, IllegalStateException, or
 *   UnsupportedOperationException that are used for immediate input validation.</li>
 * </ul>
 */
public class RawExceptionThrowCheck extends AbstractCheck {
    static final String ERROR_MESSAGE = "Directly throwing a new exception is disallowed. Use the "
        + "\"io.clientcore.core.instrumentation.logging.ClientLogger\" API instead, such as "
        + "\"logger.throwableAtError()\" or \"logger.throwableAtWarning()\". See "
        + "https://github.com/Azure/azure-sdk-for-java/wiki/Client-core:-logging-exceptions-best-practices for more details.";

    private static final String[] IGNORED_EXCEPTIONS = new String[] {
        "NullPointerException",
        "IllegalArgumentException",
        "IllegalStateException",
        "UnsupportedOperationException" };

    private static final String CORE_EXCEPTION_FACTORY_NAME = "CoreException.from";

    /**
     * Creates a new instance of {@link RawExceptionThrowCheck}.
     */
    public RawExceptionThrowCheck() {
    }

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
        return new int[] { TokenTypes.LITERAL_THROW };
    }

    @Override
    public void visitToken(DetailAST token) {
        DetailAST expr = token.findFirstToken(TokenTypes.EXPR);
        if (expr == null) {
            return;
        }

        DetailAST newToken = expr.findFirstToken(TokenTypes.LITERAL_NEW);
        if (newToken != null) {
            if (isNotIgnoredException(FullIdent.createFullIdentBelow(newToken).getText())) {
                log(newToken, ERROR_MESSAGE);
            }

            return;
        }

        DetailAST methodCallToken = expr.findFirstToken(TokenTypes.METHOD_CALL);
        if (methodCallToken != null
            && CORE_EXCEPTION_FACTORY_NAME.equals(FullIdent.createFullIdentBelow(methodCallToken).getText())) {
            log(methodCallToken, ERROR_MESSAGE);
        }
    }

    private static boolean isNotIgnoredException(String exceptionName) {
        for (String ignoredException : IGNORED_EXCEPTIONS) {
            if (exceptionName.endsWith(ignoredException)) {
                return false;
            }
        }

        return true;
    }
}
