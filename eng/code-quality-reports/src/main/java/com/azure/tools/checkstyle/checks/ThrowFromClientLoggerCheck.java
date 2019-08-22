// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * To throw an exception, Must throw it through a 'logger.logExceptionAsError', rather than by directly calling 'throw exception'.
 *
 * Skip check if throwing exception from
 * <ol>
 *   <li>Static method</li>
 *   <li>Static class</li>
 *   <li>Constructor</li>
 * </ol>
 */
public class ThrowFromClientLoggerCheck extends AbstractCheck {
    private static final String LOGGER_LOG_EXCEPTION_AS_ERROR = "logger.logExceptionAsError";
    private static final String LOGGER_LOG_EXCEPTION_AS_WARNING = "logger.logExceptionAsWarning";
    private static final String THROW_LOGGER_EXCEPTION_MESSAGE = "Directly throwing an exception is disallowed. Must "
        + "throw through ''ClientLogger'' API, either of ''%s'' or ''%s'' where ''logger'' is type of ClientLogger from Azure Core package.";

    // A container stores the static status of class, skip this ThrowFromClientLoggerCheck if the class is static
    private final Deque<Boolean> classStaticDeque = new ArrayDeque<>();
    // A container stores the static status of method, skip this ThrowFromClientLoggerCheck if the method is static
    private final Deque<Boolean> methodStaticDeque = new ArrayDeque<>();
    // The variable is used to indicate if current node is still inside of constructor.
    private boolean isInConstructor = false;

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
            TokenTypes.CLASS_DEF,
            TokenTypes.CTOR_DEF,
            TokenTypes.LITERAL_THROW,
            TokenTypes.METHOD_DEF
        };
    }

    @Override
    public void leaveToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.CLASS_DEF:
                classStaticDeque.pop();
                break;
            case TokenTypes.CTOR_DEF:
                isInConstructor = false;
                break;
            case TokenTypes.METHOD_DEF:
                methodStaticDeque.pop();
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    @Override
    public void visitToken(DetailAST token) {
        switch (token.getType()) {
            case TokenTypes.CLASS_DEF:
                DetailAST modifiersToken = token.findFirstToken(TokenTypes.MODIFIERS);
                classStaticDeque.addLast(modifiersToken.branchContains(TokenTypes.LITERAL_STATIC));
                break;
            case TokenTypes.CTOR_DEF:
                isInConstructor = true;
                break;
            case TokenTypes.METHOD_DEF:
                DetailAST methodModifiersToken = token.findFirstToken(TokenTypes.MODIFIERS);
                methodStaticDeque.addLast(methodModifiersToken.branchContains(TokenTypes.LITERAL_STATIC));
                break;
            case TokenTypes.LITERAL_THROW:
                // Skip check if the throw exception from static class, constructor or static method
                if (classStaticDeque.isEmpty() || classStaticDeque.peekLast() || isInConstructor
                    || methodStaticDeque.isEmpty() || methodStaticDeque.peekLast()) {
                    return;
                }
                DetailAST methodCallToken = token.findFirstToken(TokenTypes.EXPR).findFirstToken(TokenTypes.METHOD_CALL);
                if (methodCallToken == null) {
                    log(token, String.format(THROW_LOGGER_EXCEPTION_MESSAGE, LOGGER_LOG_EXCEPTION_AS_ERROR, LOGGER_LOG_EXCEPTION_AS_WARNING));
                    return;
                }

                String methodCallName = FullIdent.createFullIdent(methodCallToken.findFirstToken(TokenTypes.DOT)).getText();
                if (!LOGGER_LOG_EXCEPTION_AS_ERROR.equals(methodCallName) && !LOGGER_LOG_EXCEPTION_AS_WARNING.equals(methodCallName)) {
                    log(token, String.format(THROW_LOGGER_EXCEPTION_MESSAGE, LOGGER_LOG_EXCEPTION_AS_ERROR, LOGGER_LOG_EXCEPTION_AS_WARNING));
                }
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }
}
