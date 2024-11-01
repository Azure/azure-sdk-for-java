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
 * <p>
 * Skip check if throwing exception from
 * <ol>
 *   <li>Static method</li>
 *   <li>Static class</li>
 *   <li>Constructor</li>
 * </ol>
 */
public class ThrowFromClientLoggerCheck extends AbstractCheck {
    static final String THROW_LOGGER_EXCEPTION_MESSAGE = "Directly throwing an exception is disallowed. Must throw "
        + "through \"ClientLogger\" API, either of \"logger.logExceptionAsError\", \"logger.logThrowableAsError\", "
        + "\"logger.atError().log\", \"logger.logExceptionAsWarning\", \"logger.logThrowableAsWarning\", or "
        + "\"logger.atWarning().log\" where \"logger\" is type of ClientLogger from Azure Core package.";

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
                if (isInConstructor
                    || Boolean.TRUE.equals(classStaticDeque.peek())
                    || Boolean.TRUE.equals(methodStaticDeque.peek())
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
                if (throwStatementNotLogged(methodCallName)) {
                    log(token, THROW_LOGGER_EXCEPTION_MESSAGE);
                }
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    private static boolean throwStatementNotLogged(String methodCallName) {
        if (methodCallName.length() != 26 && methodCallName.length() != 28) {
            // Checking specifically for methodCallName equaling one of:
            //
            // logger.logExceptionAsError
            // logger.logThrowableAsError
            // logger.logExceptionAsWarning
            // logger.logThrowableAsWarning
            //
            // Which have string lengths of 26 and 28, so this is a fast litmus check.
            return true;
        }

        // logger. is match case-insensitive as both instance and static loggers are supported (logger, LOGGER).
        // Everything after this is case-sensitive as method names can't change.
        if (!"logger.".regionMatches(true, 0, methodCallName, 0, 7)) {
            // Throw statement doesn't begin with "logger."
            return true;
        }

        // Only check for direct usages of ClientLogger logging here, elsewhere checks for LoggingEventBuilder logging.
        if (!"log".regionMatches(0, methodCallName, 7, 3)) {
            return true;
        }

        if ("ExceptionAs".regionMatches(0, methodCallName, 10, 11)) {
            return !methodCallName.endsWith("Error") && !methodCallName.endsWith("Warning");
        } else if ("ThrowableAs".regionMatches(0, methodCallName, 10, 11)) {
            return !methodCallName.endsWith("Error") && !methodCallName.endsWith("Warning");
        }

        return true;
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
