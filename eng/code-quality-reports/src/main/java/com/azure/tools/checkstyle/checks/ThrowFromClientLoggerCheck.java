// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;

/**
 * To throw an exception, Must throw it through a 'logger.logExceptionAsError', rather than by directly calling 'throw
 * exception'.
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
    private static final String LOGGER_LOG_THROWABLE_AS_ERROR = "logger.logThrowableAsError";
    private static final String LOGGING_BUILDER_LOG_THROWABLE_AS_ERROR = "logger.atError().log";    
    private static final String LOGGER_LOG_EXCEPTION_AS_WARNING = "logger.logExceptionAsWarning";
    private static final String LOGGER_LOG_THROWABLE_AS_WARNING = "logger.logThrowableAsWarning";    
    private static final String LOGGING_BUILDER_LOG_THROWABLE_AS_WARNING = "logger.atWarning().log";

    static final String THROW_LOGGER_EXCEPTION_MESSAGE = String.format("Directly throwing an exception is disallowed. "
        + "Must throw through \"ClientLogger\" API, either of \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", or \"%s\" where \"logger\" is "
        + "type of ClientLogger from Azure Core package.", LOGGER_LOG_EXCEPTION_AS_ERROR,
        LOGGER_LOG_THROWABLE_AS_ERROR, LOGGING_BUILDER_LOG_THROWABLE_AS_ERROR, LOGGER_LOG_EXCEPTION_AS_WARNING, LOGGER_LOG_THROWABLE_AS_WARNING, LOGGING_BUILDER_LOG_THROWABLE_AS_WARNING);

    // A LIFO queue stores the static status of class, skip this ThrowFromClientLoggerCheck if the class is static
    private final Queue<Boolean> classStaticDeque = Collections.asLifoQueue(new ArrayDeque<>());
    // A LIFO queue stores the static status of method, skip this ThrowFromClientLoggerCheck if the method is static
    private final Queue<Boolean> methodStaticDeque = Collections.asLifoQueue(new ArrayDeque<>());
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
                classStaticDeque.poll();
                break;
            case TokenTypes.CTOR_DEF:
                isInConstructor = false;
                break;
            case TokenTypes.METHOD_DEF:
                methodStaticDeque.poll();
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
                classStaticDeque.offer(modifiersToken.branchContains(TokenTypes.LITERAL_STATIC));
                break;
            case TokenTypes.CTOR_DEF:
                isInConstructor = true;
                break;
            case TokenTypes.METHOD_DEF:
                DetailAST methodModifiersToken = token.findFirstToken(TokenTypes.MODIFIERS);
                methodStaticDeque.offer(methodModifiersToken.branchContains(TokenTypes.LITERAL_STATIC));
                break;
            case TokenTypes.LITERAL_THROW:
                // Skip check if the throw exception from static class, constructor or static method
                if (classStaticDeque.isEmpty() || classStaticDeque.peek() || isInConstructor
                    || methodStaticDeque.isEmpty() || methodStaticDeque.peek() 
                    || findLogMethodIdentifier(token)) {
                    return;
                }

                DetailAST methodCallToken =
                    token.findFirstToken(TokenTypes.EXPR).findFirstToken(TokenTypes.METHOD_CALL);
                if (methodCallToken == null) {
                    log(token, THROW_LOGGER_EXCEPTION_MESSAGE);
                    return;
                }

                String methodCallName =
                    FullIdent.createFullIdent(methodCallToken.findFirstToken(TokenTypes.DOT)).getText();
                if (!LOGGER_LOG_EXCEPTION_AS_ERROR.equalsIgnoreCase(methodCallName)
                    && !LOGGER_LOG_THROWABLE_AS_ERROR.equalsIgnoreCase(methodCallName)
                    && !LOGGER_LOG_EXCEPTION_AS_WARNING.equalsIgnoreCase(methodCallName)
                    && !LOGGER_LOG_THROWABLE_AS_WARNING.equalsIgnoreCase(methodCallName)) {
                    log(token, THROW_LOGGER_EXCEPTION_MESSAGE);
                }
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /*
     * Checks if the expression includes call to log(), which verifies logging builder call 
     * e.g. logger.atError().log(ex)
     */
    private static boolean findLogMethodIdentifier(DetailAST root) {
        for (DetailAST ast = root.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (ast.getType() == TokenTypes.METHOD_CALL) {
                DetailAST dot = ast.findFirstToken(TokenTypes.DOT);
                if (dot != null) {
                    DetailAST ident = dot.findFirstToken(TokenTypes.IDENT);
                    if ("log".equals(ident.getText())) {
                        return true;
                    }
                }
            }
            if (findLogMethodIdentifier(ast)) {
                return true;
            }
        }

        return false;
    }
}
