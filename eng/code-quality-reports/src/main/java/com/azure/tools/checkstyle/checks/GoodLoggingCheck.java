// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Good Logging Practice:
 * <ol>
 * <li>A non-static instance logger in a non-static method.</li>
 * <li>ClientLogger in public API should all named 'logger', public API classes are those classes that are declared
 *     as public and that do not exist in an implementation package or subpackage.</li>
 * <li>Should not use any external logger class, only use ClientLogger. No slf4j, log4j, or other logging imports are
 *     allowed.</li>
 * <li>'System.out' and 'System.err' is not allowed as well.</li>
 * </ol>
 */
public class GoodLoggingCheck extends AbstractCheck {
    private static final String CLIENT_LOGGER_PATH = "com.azure.core.util.logging.ClientLogger";
    private static final String CLIENT_LOGGER = "ClientLogger";
    private static final String LOGGER = "logger";
    private static final String STATIC_LOGGER_ERROR = "Use a static ClientLogger instance in a static method.";
    private static final int[] REQUIRED_TOKENS = new int[]{
        TokenTypes.IMPORT,
        TokenTypes.INTERFACE_DEF,
        TokenTypes.CLASS_DEF,
        TokenTypes.LITERAL_NEW,
        TokenTypes.VARIABLE_DEF,
        TokenTypes.METHOD_CALL,
        TokenTypes.METHOD_DEF
    };

    private static final String LOGGER_NAME_ERROR =
        "ClientLogger instance naming: use ''%s'' instead of ''%s'' for consistency.";

    private static final String NOT_CLIENT_LOGGER_ERROR =
        "Do not use %s class. Use ''%s'' as a logging mechanism instead of ''%s''.";

    // Boolean indicator that indicates if the java class imports ClientLogger
    private boolean hasClientLoggerImported;
    // A LIFO queue stores the class names, pop top element if exist the class name AST node
    private final Queue<String> classNameDeque = Collections.asLifoQueue(new ArrayDeque<>());
    // Collection of Invalid logging packages
    private static final Set<String> INVALID_LOGS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "org.slf4j", "org.apache.logging.log4j", "java.util.logging"
    )));

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
        return REQUIRED_TOKENS;
    }

    @Override
    public void finishTree(DetailAST ast) {
        hasClientLoggerImported = false;
    }

    @Override
    public void leaveToken(DetailAST ast) {
        if (ast.getType() == TokenTypes.CLASS_DEF) {
            classNameDeque.poll();
        }
    }

    @Override
    public void visitToken(DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.IMPORT:
                final String importClassPath = FullIdent.createFullIdentBelow(ast).getText();
                hasClientLoggerImported = hasClientLoggerImported || importClassPath.equals(CLIENT_LOGGER_PATH);

                INVALID_LOGS.forEach(item -> {
                    if (importClassPath.startsWith(item)) {
                        log(ast, String.format(NOT_CLIENT_LOGGER_ERROR, "external logger", CLIENT_LOGGER_PATH, item));
                    }
                });
                break;
            case TokenTypes.CLASS_DEF:
            case TokenTypes.INTERFACE_DEF:
                classNameDeque.offer(ast.findFirstToken(TokenTypes.IDENT).getText());
                break;
            case TokenTypes.LITERAL_NEW:
                checkLoggerInstantiation(ast);
                break;
            case TokenTypes.VARIABLE_DEF:
                checkLoggerNameMatch(ast);
                break;
            case TokenTypes.METHOD_CALL:
                final DetailAST dotToken = ast.findFirstToken(TokenTypes.DOT);
                if (dotToken == null) {
                    return;
                }
                final String methodCallName = FullIdent.createFullIdentBelow(dotToken).getText();
                if (methodCallName.startsWith("System.out") || methodCallName.startsWith("System.err")) {
                    log(ast, String.format(NOT_CLIENT_LOGGER_ERROR, "Java System", CLIENT_LOGGER_PATH, methodCallName));
                }
                break;
            case TokenTypes.METHOD_DEF:
                checkForInvalidStaticLoggerUsage(ast);
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /**
     * Check if the VARIABLE_DEF AST node type is 'ClientLogger'.
     *
     * @param varDefAST VARIABLE_DEF AST node
     * @return true if the variable type is 'ClientLogger'.
     */
    private boolean isTypeClientLogger(DetailAST varDefAST) {
        final DetailAST typeAST = varDefAST.findFirstToken(TokenTypes.TYPE);
        if (typeAST == null) {
            return false;
        }
        return TokenUtil.findFirstTokenByPredicate(typeAST, node ->
            node.getType() == TokenTypes.IDENT && node.getText().equals(CLIENT_LOGGER)
        ).isPresent();
    }

    /**
     * Check if instantiating a matched class name for the same class.
     *
     * @param literalNewToken LITERAL_NEW node
     */
    private void checkLoggerInstantiation(DetailAST literalNewToken) {
        final DetailAST identToken = literalNewToken.findFirstToken(TokenTypes.IDENT);
        // Not ClientLogger instance
        if (identToken == null || !identToken.getText().equals(CLIENT_LOGGER)) {
            return;
        }
        // LITERAL_NEW node always has ELIST node below
        TokenUtil.findFirstTokenByPredicate(literalNewToken.findFirstToken(TokenTypes.ELIST), exprToken -> {
            // Skip check if not EXPR node or if has no DOT node below. EXPR always has children below
            if (exprToken.getType() != TokenTypes.EXPR || exprToken.getFirstChild().getType() != TokenTypes.DOT) {
                return false;
            }
            // Check instantiation of ClientLogger
            final String containerClassName = FullIdent.createFullIdent(exprToken.getFirstChild()).getText();
            // Add suffix of '.class' at the end of class name
            final String className = classNameDeque.peek();
            if (!containerClassName.equals(className + ".class")) {
                log(exprToken, String.format("Not newing a ClientLogger with matching class name. Use ''%s.class'' "
                    + "instead of ''%s''.", className, containerClassName));
            }
            return true;
        });
    }

    /**
     * Check if the given ClientLogger named 'logger'
     *
     * @param varToken VARIABLE_DEF node
     */
    private void checkLoggerNameMatch(DetailAST varToken) {
        if (!hasClientLoggerImported || !isTypeClientLogger(varToken)) {
            return;
        }
        // Check if the Logger instance named as 'logger/LOGGER'.
        final DetailAST identAST = varToken.findFirstToken(TokenTypes.IDENT);
        if (identAST != null && !identAST.getText().equalsIgnoreCase(LOGGER)) {
            log(varToken, String.format(LOGGER_NAME_ERROR, LOGGER, identAST.getText()));
        }
    }

    /**
     * Report error if a static ClientLogger instance used in a non-static method.
     *
     * @param methodDefToken METHOD_DEF AST node
     */
    private void checkForInvalidStaticLoggerUsage(DetailAST methodDefToken) {

        // if not a static method
        if (!(TokenUtil.findFirstTokenByPredicate(methodDefToken,
            node -> node.branchContains(TokenTypes.LITERAL_STATIC)).isPresent())) {

            // error if static `LOGGER` present, LOGGER.*
            if (methodDefToken.findFirstToken(TokenTypes.SLIST) != null) {
                TokenUtil.forEachChild(methodDefToken.findFirstToken(TokenTypes.SLIST), TokenTypes.EXPR, exprToken -> {
                    if (exprToken != null) {
                        DetailAST methodCallToken = exprToken.findFirstToken(TokenTypes.METHOD_CALL);
                        if (methodCallToken != null && methodCallToken.findFirstToken(TokenTypes.DOT) != null) {
                            if (methodCallToken.findFirstToken(TokenTypes.DOT)
                                .findFirstToken(TokenTypes.IDENT).getText().equals(LOGGER.toUpperCase())) {
                                log(methodDefToken, STATIC_LOGGER_ERROR);
                            }
                        }
                    }
                });
            }
        }
    }

}
