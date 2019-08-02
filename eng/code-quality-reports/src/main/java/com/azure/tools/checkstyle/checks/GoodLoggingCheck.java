// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Check Style Rule: Good Logging Practice
 *  <ol>
 *      <li>Non-static Logger instance.</li>
 *      <li>Logger variable name should be 'logger' in public class and not exist in an implementation package.</li>
 *      <li>Should not use any external logger class, only use ClientLogger. No slf4j, log4j, or other logging imports are allowed.</li>
 *      <li>Should refer to the containing class in its constructor:
 *      public class A {
 *          private ClientLogger logger = new ClientLogger(A.class);
 *      }
 *      </li>
 *  </ol>
 */
public class GoodLoggingCheck extends AbstractCheck {
    private static final String CLIENT_LOOGER_PATH = "com.azure.core.util.logging.ClientLogger";
    private static final String CLIENT_LOGGER = "ClientLogger";
    private static final String SLF4J = "org.slf4j";
    private static final String LOG4J = "org.apache.logging.log4j";

    private static final String LOGGER_NAME_ERROR = "ClientLogger instance naming: use ''%s'' instead of ''%s'' for consistency.";
    private static final String STATIC_LOGGER_ERROR = "Reference to ClientLogger should not be static: remove static modifier.";
    private static final String NOT_CLIENT_LOGGER_ERROR = "Do not use external logger class. Use ''%s'' as a logging mechanism instead of ''%s''.";

    private boolean hasSlf4jImported;
    private boolean hasLog4jimported;
    private boolean hasClientLoggerImported;
    private String loggerRequiredName;
    private String className;

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
            TokenTypes.VARIABLE_DEF
        };
    }

    @Override
    public void finishTree(DetailAST ast) {
        hasSlf4jImported = false;
        hasLog4jimported = false;
        hasClientLoggerImported = false;
    }

    @Override
    public void visitToken(DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.IMPORT:
                String importClassPath = FullIdent.createFullIdentBelow(ast).getText();
                hasClientLoggerImported |= importClassPath.equals(CLIENT_LOOGER_PATH);
                hasSlf4jImported |= importClassPath.startsWith(SLF4J);
                hasLog4jimported |= importClassPath.startsWith(LOG4J);
                break;
            case TokenTypes.LITERAL_CLASS:
                if (ast.getNextSibling() == null) {
                    break;
                }
                // Checks no use any external logger class.
                if (hasSlf4jImported) {
                    log(ast, String.format(NOT_CLIENT_LOGGER_ERROR, CLIENT_LOOGER_PATH, SLF4J));
                }
                if (hasLog4jimported) {
                    log(ast, String.format(NOT_CLIENT_LOGGER_ERROR, CLIENT_LOOGER_PATH, LOG4J));
                }

                className = ast.getNextSibling().getText();

                break;
            case TokenTypes.LITERAL_NEW:
                // Check if the containing class matched with class name itself

                DetailAST identToken = ast.findFirstToken(TokenTypes.IDENT);
                if (identToken == null) {
                    return;
                }

                String newClientLogger = identToken.getText();
                if (!newClientLogger.equals(CLIENT_LOGGER)) {
                    return;
                }

                DetailAST elistToken = ast.findFirstToken(TokenTypes.ELIST);
                DetailAST exprToken = elistToken.findFirstToken(TokenTypes.EXPR);
                if (exprToken.getFirstChild().getType() != TokenTypes.DOT) {
                    return;
                }
                String containerClassName = FullIdent.createFullIdentBelow(exprToken).getText();
                containerClassName = containerClassName.substring(0, containerClassName.length() - 6);
                if (!containerClassName.equals(className)) {
                    log(ast, String.format("Not newing a ClientLogger with matching class name. Use ''%s.class'' instead of ''%s.class''", className, containerClassName));
                }
                break;
            case TokenTypes.VARIABLE_DEF:
                if (!hasClientLoggerImported || !isTypeClientLogger(ast)) {
                    return;
                }
                // Check if the Logger instance named as 'logger'.
                DetailAST identAST = ast.findFirstToken(TokenTypes.IDENT);
                if (identAST != null && !identAST.getText().equals(loggerRequiredName)) {
                    log(ast, String.format(LOGGER_NAME_ERROR, loggerRequiredName, identAST.getText()));
                }
                // Check if the Logger is static instance, log as error if it is static instance logger.
                DetailAST modifierAST = ast.findFirstToken(TokenTypes.MODIFIERS);
                if (modifierAST != null && modifierAST.branchContains(TokenTypes.LITERAL_STATIC)) {
                    log(ast, STATIC_LOGGER_ERROR);
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
        DetailAST varNameAST = varDefAST.findFirstToken(TokenTypes.IDENT);
        if (typeAST == null || varNameAST == null) {
            return false;
        }
        DetailAST typeNameAST = typeAST.findFirstToken(TokenTypes.IDENT);
        if (typeNameAST == null) {
            return false;
        }
        String typeName = typeNameAST.getText();
        if (typeName != null && typeName.equals(CLIENT_LOGGER)) {
            return true;
        }
        return false;
    }
}
