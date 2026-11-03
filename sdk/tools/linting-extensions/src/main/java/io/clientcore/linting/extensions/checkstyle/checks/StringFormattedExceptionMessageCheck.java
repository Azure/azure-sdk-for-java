// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Checks that the message provided to "logger.throwableAt*().log" is static (not created using String.format).
 */
public class StringFormattedExceptionMessageCheck extends SdkCheck {
    static final String ERROR_MESSAGE = "Short message passed to \"logger.throwableAt*().log\" should be static. "
        + "Provide dynamic components using the \"addKeyValue(key, value)\" method instead. "
        + "See https://github.com/Azure/azure-sdk-for-java/blob/main/docs/azure-v2.md#logging-best-practices for more details.";
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
        if (isIgnoredSource() || SdkCheckUtils.findThrowableLoggingEvent(token) == null) {
            return;
        }
        DetailAST arguments = token.findFirstToken(TokenTypes.ELIST);
        DetailAST message = arguments == null ? null : arguments.findFirstToken(TokenTypes.EXPR);
        if (message == null || message.getFirstChild() == null) {
            return;
        }

        DetailAST firstParam = message.getFirstChild();
        if (firstParam.getType() == TokenTypes.METHOD_CALL
            && "format".equals(SdkCheckUtils.getCalledMethodName(firstParam))) {
            if (FullIdent.createFullIdentBelow(firstParam).getText().endsWith(STRING_FORMAT_METHOD_NAME)) {
                log(message, ERROR_MESSAGE);
            }
        } else if (firstParam.getType() == TokenTypes.PLUS
            && firstParam.findFirstToken(TokenTypes.IDENT) != null
            && firstParam.findFirstToken(TokenTypes.STRING_LITERAL) != null) {
            log(firstParam, ERROR_MESSAGE);
        }
    }
}
