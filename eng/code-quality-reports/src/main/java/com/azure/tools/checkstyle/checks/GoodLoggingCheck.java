// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Good Logging Practice:
 * <ol>
 * <li>Non-static Logger instance.</li>
 * <li>Logger variable name should be 'logger' in public class and not exist in an implementation package.</li>
 * <li>Should not use any external logger class, only use ClientLogger. No slf4j, log4j, or other logging imports are allowed.</li>
 * <li>'System.out' and 'System.err' is not allowed as well.</li>
 * </ol>
 */
public class GoodLoggingCheck extends AbstractCheck {
    private static final String CLIENT_LOGGER_PATH = "com.azure.core.util.logging.ClientLogger";
    private static final String CLIENT_LOGGER = "ClientLogger";
    private static final String LOGGER_NAME_ERROR = "ClientLogger instance naming: use ''%s'' instead of ''%s'' for consistency.";
    private static final String STATIC_LOGGER_ERROR = "Reference to ClientLogger should not be static: remove static modifier.";
    private static final String NOT_CLIENT_LOGGER_ERROR = "Do not use external logger class. Use ''%s'' as a logging mechanism instead of ''%s''.";

    private boolean hasClientLoggerImported;
    private String loggerRequiredName;
    private String className;

    private static final Set<String> INVALID_LOG_SET = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "org.slf4j", "org.apache.logging.log4j"
    )));

    /**
     * Setter to specifies valid identifiers
     * @param loggerRequiredName the variable name of logger used
     */
    public void setLoggerName(String loggerRequiredName) {
        this.loggerRequiredName = loggerRequiredName;
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
        return new int[] {
            TokenTypes.IMPORT,
            TokenTypes.LITERAL_CLASS,
            TokenTypes.LITERAL_NEW,
            TokenTypes.VARIABLE_DEF,
            TokenTypes.METHOD_CALL
        };
    }

    @Override
    public void finishTree(DetailAST ast) {
        hasClientLoggerImported = false;
    }

    @Override
    public void visitToken(DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.IMPORT:
                String importClassPath = FullIdent.createFullIdentBelow(ast).getText();
                hasClientLoggerImported = hasClientLoggerImported || importClassPath.equals(CLIENT_LOGGER_PATH);
                for (final String logger : INVALID_LOG_SET) {
                    if (importClassPath.startsWith(logger)) {
                        // Checks no use any external logger class.
                        log(ast, String.format(NOT_CLIENT_LOGGER_ERROR, CLIENT_LOGGER_PATH, logger));
                    }
                }
                break;
            case TokenTypes.LITERAL_CLASS:
                if (ast.getNextSibling() == null) {
                    break;
                }
                className = ast.getNextSibling().getText();
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
                    log(ast, String.format("Do not use Java System class for logging. Use ClientLogger in ''%s'' instead.", CLIENT_LOGGER_PATH));
                }
                break;
            default:
                // Checkstyle complains if there's no default block in switch
                break;
        }
    }

    /**
     * check if the VARIABLE_DEF AST node type is 'ClientLogger'.
     * @param varDefAST VARIABLE_DEF AST node
     * @return true if the variable type is 'ClientLogger'.
     */
    private boolean isTypeClientLogger(DetailAST varDefAST) {
        DetailAST typeAST = varDefAST.findFirstToken(TokenTypes.TYPE);
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
        // Not named 'logger'
        if (identToken == null || !identToken.getText().equals(CLIENT_LOGGER)) {
            return;
        }
        // Edge cases
        final DetailAST elistToken = literalNewToken.findFirstToken(TokenTypes.ELIST);
        final DetailAST exprToken = elistToken.findFirstToken(TokenTypes.EXPR);
        if (exprToken.getFirstChild().getType() != TokenTypes.DOT) {
            return;
        }
        // Check instantiation of ClientLogger
        final String containerClassName = FullIdent.createFullIdentBelow(exprToken).getText();
        // Add suffix of '.class' at the end ot class name
        if (!containerClassName.equals(className + ".class")) {
            log(literalNewToken, String.format("Not newing a ClientLogger with matching class name. Use ''%s.class'' instead of ''%s.class''", className, containerClassName));
        }
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
        // Check if the Logger instance named as 'logger'.
        final DetailAST identAST = varToken.findFirstToken(TokenTypes.IDENT);
        if (identAST != null && !identAST.getText().equals(loggerRequiredName)) {
            log(varToken, String.format(LOGGER_NAME_ERROR, loggerRequiredName, identAST.getText()));
        }
        // Check if the Logger is static instance, log as error if it is static instance logger.
        if (TokenUtil.findFirstTokenByPredicate(varToken, node -> node.getType() == TokenTypes.MODIFIERS && node.branchContains(TokenTypes.LITERAL_STATIC)).isPresent()) {
            log(varToken, STATIC_LOGGER_ERROR);
        }
    }
}
