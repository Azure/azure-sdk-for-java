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
 *  I. Non-static Logger instance
 *  II. Logger variable name should be 'logger'
 */
public class GoodLoggingCheck extends AbstractCheck {
    private static final String PACKAGE_PATH = "com.azure";
    private static final String IMPLEMENTATION_PATH = "com.azure.core.implementation";
    private static final String LOGGER_CLASS = "com.azure.core.implementation.logging.ServiceLogger";
    private static final String SERVICE_LOGGER = "ServiceLogger";
    private static final String SLF4J = "org.slf4j";
    private static final String LOG4J = "org.apache.logging.log4j";

    private static final String LOGGER_NAME_ERR = "ServiceLogger instance naming: use \"%s\" instead of \"%s\" for consistency.";
    private static final String STATIC_LOGGER_ERR = "Reference to ServiceLogger should not be static: remove static modifier.";
    private static final String ONLY_LOGGER_CLASS_ERR = "Use \"%s\" as a logging mechanism instead of \"%s\".";

    private static boolean isPathCorrect; // track 2
    private static boolean isImplPackage;
    private static boolean hasSlf4jImported;
    private static boolean hasLog4jimported;
    private static boolean hasLoggerImported;
    private static String loggerRequiredName;

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
            TokenTypes.PACKAGE_DEF,
            TokenTypes.IMPORT,
            TokenTypes.LITERAL_CLASS,
            TokenTypes.VARIABLE_DEF
        };
    }

    @Override
    public void beginTree(DetailAST rootAST) {
        this.hasLoggerImported = false;
        this.isPathCorrect = false;
        this.isImplPackage = false;
    }

    @Override
    public void visitToken(DetailAST ast) {

        if (ast.getType() == TokenTypes.PACKAGE_DEF) {
            String packageName = FullIdent.createFullIdent(ast.findFirstToken(TokenTypes.DOT)).getText();
            this.isPathCorrect = packageName.startsWith(PACKAGE_PATH);
            this.isImplPackage = packageName.startsWith(IMPLEMENTATION_PATH);

        } else {
            if (this.isPathCorrect) {
                if (this.isImplPackage) {
                    return;
                }
            } else {
                return;
            }
        }

        switch (ast.getType()) {
            case TokenTypes.IMPORT:
                String importClassPath = FullIdent.createFullIdentBelow(ast).getText();
                if (importClassPath != null) {
                    this.hasLoggerImported |= importClassPath.equals(LOGGER_CLASS);
                    this.hasSlf4jImported |= importClassPath.startsWith(SLF4J);
                    this.hasLog4jimported |= importClassPath.startsWith(LOG4J);
                }
                break;
            case TokenTypes.LITERAL_CLASS:
                if (ast.getNextSibling() == null) {
                    break;
                }
                String className = ast.getNextSibling().getText();
                // No check for ServiceLogger class
                if (!SERVICE_LOGGER.equals(className)) {
                    if (this.hasSlf4jImported) {
                        log(ast, String.format(ONLY_LOGGER_CLASS_ERR, LOGGER_CLASS, SLF4J));
                    } else if (this.hasLog4jimported) {
                        log(ast, String.format(ONLY_LOGGER_CLASS_ERR, LOGGER_CLASS, LOG4J));
                    } else {
                        // do nothing
                    }
                }
                break;
            case TokenTypes.VARIABLE_DEF:
                if (this.hasLoggerImported && this.isLoggerType(ast)) {
                    logLoggerInstanceNamingErrorIfFound(ast);
                    logStaticLoggerInstanceErrorIfFound(ast);
                }
                break;
        }
    }

    /**
     * Check if the Logger instance named as logger, log error if not
     * @param varDefAST the VARIABLE_DEF node
     */
    private void logLoggerInstanceNamingErrorIfFound(DetailAST varDefAST) {
        if (varDefAST == null) {
            return;
        }
        DetailAST identAST = varDefAST.findFirstToken(TokenTypes.IDENT);
        if (identAST != null && !identAST.getText().equals(this.loggerRequiredName)) {
            log(varDefAST, String.format(LOGGER_NAME_ERR, this.loggerRequiredName, identAST.getText()));
        }
    }

    /**
     * Check if the Logger is static instance, log as error if it is static instance logger,
     * @param varDefAST the VARIABLE_DEF node
     */
    private void logStaticLoggerInstanceErrorIfFound(DetailAST varDefAST) {
        DetailAST modifierAST = varDefAST.findFirstToken(TokenTypes.MODIFIERS);
        if (modifierAST != null && modifierAST.branchContains(TokenTypes.LITERAL_STATIC)) {
            log(varDefAST, STATIC_LOGGER_ERR);
        }
    }

    /**
     * check if the VARIABLE_DEF AST node type is Logger
     * @param varDefAST VARIABLE_DEF AST node
     * @return true if the variable type is Logger
     */
    private boolean isLoggerType(DetailAST varDefAST) {
        DetailAST typeAST = varDefAST.findFirstToken(TokenTypes.TYPE);
        DetailAST varNameAST = varDefAST.findFirstToken(TokenTypes.IDENT);
        if (typeAST == null || varNameAST == null) {
            return false;
        }
        DetailAST typeNameAST = typeAST.findFirstToken(TokenTypes.IDENT);
        if (typeNameAST != null) {
            String typeName = typeNameAST.getText();
            if (typeName != null && typeName.equals(SERVICE_LOGGER)) {
                return true;
            }
        }
        return false;
    }
}
