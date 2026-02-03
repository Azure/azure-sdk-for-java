// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Arrays;
import java.util.List;

/**
 * Checks that the message provided to "logger.throwableAt*().log" is static (not created using String.format).
 */
public class StringFormattedExceptionMessageCheck extends AbstractCheck {
    static final String ERROR_MESSAGE = "Short message passed to \"logger.throwableAt*().log\" should be static. "
        + "Provide dynamic components using the \"addKeyValue(key, value)\" method instead. "
        + "See https://github.com/Azure/azure-sdk-for-java/wiki/Client-core:-logging-exceptions-best-practices for more details.";
    private static final List<String> THROWABLE_AT_LOGGING_METHODS
        = Arrays.asList(".throwableAtError", ".throwableAtWarning");

    private static final String LOG_METHOD_NAME = ".log";
    private static final String STRING_FORMAT_METHOD_NAME = "String.format";

    /**
     * Creates a new instance of {@link StringFormattedExceptionMessageCheck}.
     */
    public StringFormattedExceptionMessageCheck() {
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
        return new int[] { TokenTypes.METHOD_CALL };
    }

    @Override
    public void visitToken(DetailAST token) {
        String name = FullIdent.createFullIdentBelow(token).getText();
        if (THROWABLE_AT_LOGGING_METHODS.stream().anyMatch(name::endsWith)) {
            DetailAST logMethodCall = token.getParent().getParent();
            if (logMethodCall == null || logMethodCall.getType() != TokenTypes.METHOD_CALL) {
                return;
            }

            String nextName = FullIdent.createFullIdentBelow(logMethodCall).getText();
            if (nextName.endsWith(LOG_METHOD_NAME)) {
                DetailAST elist = logMethodCall.findFirstToken(TokenTypes.ELIST);
                if (elist == null) {
                    return;
                }

                // first param of `log()` method
                DetailAST logExpr = elist.findFirstToken(TokenTypes.EXPR);
                if (logExpr == null) {
                    return;
                }

                DetailAST firstParam = logExpr.getFirstChild();
                // flag String.format
                if (firstParam.getType() == TokenTypes.METHOD_CALL) {
                    String logFirstArgMethod = FullIdent.createFullIdentBelow(logExpr.getFirstChild()).getText();
                    if (logFirstArgMethod.endsWith(STRING_FORMAT_METHOD_NAME)) {
                        log(logExpr, ERROR_MESSAGE);
                    }
                } else if (firstParam.getType() == TokenTypes.PLUS) {
                    // flag if dynamic, i.e. a combination of string literal and ident
                    DetailAST firstIdent = logExpr.getFirstChild().findFirstToken(TokenTypes.IDENT);
                    DetailAST firstLiteral = logExpr.getFirstChild().findFirstToken(TokenTypes.STRING_LITERAL);
                    if (firstIdent != null && firstLiteral != null) {
                        log(firstParam, ERROR_MESSAGE);
                    }
                }
            }
        }
    }
}
