// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Check Style Rule: Good Logging Practice
 *  I. Non-static Logger instance
 *  II. Logger variable name should be 'logger'
 *  III. Guard with conditional block check whenever logger's logging method called.
 *  IV. Only one conditional check inside of the if conditional expression
 */
public class GoodLoggingCheck extends AbstractCheck {
    private static String loggerName;
    private static String loggerClass;
    private static String packagePath;

    private static final String FAILED_TO_LOAD_MESSAGE = "\"%s\" class failed to load, GoodLoggingCheck will be ignored.";
    private static final String LOGGER_NAME_ERR = "Incorrect name for Logger: use \"%s\" instead of \"%s\" as Logger instance name";
    private static final String LOGGER_LEVEL_ERR = "Please guard \"%s\" method call with \"%s\" method";
    private static final String STATIC_LOGGER_ERR = "Logger should not be static: remove static modifier";
    private static final String GUARD_BLOCK_ERR = "Please make sure the guard block, if statement, has only one guard check conditional expression";

    // Logger level static final string variables
    private static final String ERROR = "error";
    private static final String WARN = "warn";
    private static final String INFO = "info";
    private static final String DEBUG = "debug";
    private static final String TRACE = "trace";

    private static final String IS_ERROR_ENABLED = "isErrorEnabled";
    private static final String IS_WARN_ENABLED = "isWarnEnabled";
    private static final String IS_INFO_ENABLED = "isInfoEnabled";
    private static final String IS_DEBUG_ENABLED = "isDebugEnabled";
    private static final String IS_TRACE_ENABLED = "isTraceEnabled";

    private Stack<DetailAST> enabledStack = new Stack<>();
    private Map<String, Integer> enabledMap = new HashMap<>();

    private static boolean isPathCorrect; // track 2
    private static boolean hasLoggerImported;
    private Class<?> slf4lLogger;

    private static final Map<String, String> loggerMethodMap = new HashMap<String, String>(){
        {
            put(TRACE, IS_TRACE_ENABLED);
            put(DEBUG, IS_DEBUG_ENABLED);
            put(INFO, IS_INFO_ENABLED);
            put(WARN, IS_WARN_ENABLED);
            put(ERROR, IS_ERROR_ENABLED);
        }
    };

    @Override
    public void init() {
        try {
            this.slf4lLogger = Class.forName(loggerClass);
        } catch (ClassNotFoundException ex) {
            log(0, String.format(FAILED_TO_LOAD_MESSAGE, loggerClass));
        }
    }

    /**
     * Setter to specifies valid identifiers
     * @param loggerName the variable name of logger used
     */
    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    /**
     * Setter to specifies valid identifiers
     * @param loggerClass the logger class path
     */
    public void setLoggerClass(String loggerClass) {
        this.loggerClass = loggerClass;
    }

    /**
     * Setter to specifies valid identifiers
     * @param packagePath package path
     */
    public void setPackagePath(String packagePath) {
        this.packagePath = packagePath;
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
            TokenTypes.METHOD_CALL,
            TokenTypes.VARIABLE_DEF,
            TokenTypes.LITERAL_IF
        };
    }

    @Override
    public void beginTree(DetailAST rootAST) {
        this.hasLoggerImported = false;
        this.isPathCorrect = false;
        enabledMap.clear();
        enabledStack.clear();
    }

    @Override
    public void visitToken(DetailAST ast) {
        if(ast.getType() != TokenTypes.PACKAGE_DEF && !this.isPathCorrect) {
            return;
        }

        switch (ast.getType()) {
            case TokenTypes.PACKAGE_DEF:
                String packageName = FullIdent.createFullIdent(ast.findFirstToken(TokenTypes.DOT)).getText();
                this.isPathCorrect |= packageName.startsWith(packagePath);
                break;
            case TokenTypes.IMPORT:
                String importClassPath = FullIdent.createFullIdentBelow(ast).getText();
                this.hasLoggerImported |= importClassPath.equals(this.slf4lLogger.getName());
                break;
            case TokenTypes.METHOD_CALL:
                if (this.hasLoggerImported) {
                    DetailAST dotAST = ast.findFirstToken(TokenTypes.DOT);
                    if (dotAST == null) {
                        break;
                    }
                    String logger = dotAST.getFirstChild().getText();
                    String methodName = dotAST.getLastChild().getText();
                    // case 1: logger.debug()
                    if (loggerMethodMap.containsKey(methodName)) {
                        if (!logger.equals(this.loggerName)) {
                            log(dotAST.getLineNo(), String.format(LOGGER_NAME_ERR, this.loggerName, logger));
                        }
                        logGuardBlockErrorIfFound(dotAST);
                        break;
                    }
                    // case 2: logger.isDebugEnabled()
                    if (loggerMethodMap.containsValue(methodName)){
                        if (!logger.equals(this.loggerName)) {
                            log(dotAST.getLineNo(), String.format(LOGGER_NAME_ERR, this.loggerName, logger));
                        }
                        if (!this.isGrandparentLiteralIf(ast)) {
                            log(ast.getLineNo(), GUARD_BLOCK_ERR);
                            break;
                        }
                        enabledStack.push(ast.getParent().getParent());
                        enabledMap.put(methodName, enabledMap.getOrDefault(methodName, 0) + 1);
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

    @Override
    public void leaveToken(DetailAST ast) {
        // only check for track 2, otherwise, skip check
        if(!this.isPathCorrect) {
            return;
        }

        switch (ast.getType()) {
            case TokenTypes.LITERAL_IF:
                if (!enabledStack.isEmpty() && enabledStack.peek() == ast) {
                    DetailAST ifAST = enabledStack.pop();
                    // all nodes in the stack should already check the logger method existence
                    String methodText = ifAST.findFirstToken(TokenTypes.EXPR)
                                            .findFirstToken(TokenTypes.METHOD_CALL)
                                            .findFirstToken(TokenTypes.DOT)
                                            .getLastChild()
                                            .getText();
                    if (enabledMap.containsKey(methodText)) {
                        Integer ct = enabledMap.get(methodText);
                        if (ct < 2) {
                            enabledMap.remove(methodText);
                        } else {
                            enabledMap.put(methodText, ct - 1);
                        }
                    }
                }
                break;
        }
    }

    /**
     * Check if the Logger instance named as logger, log error if not
     * @param varDefAST the VARIABLE_DEF node
     */
    private void logLoggerInstanceNamingErrorIfFound(DetailAST varDefAST) {
        DetailAST identAST = varDefAST.findFirstToken(TokenTypes.IDENT);
        if (identAST != null && !identAST.getText().equals(this.loggerName)) {
            log(varDefAST.getLineNo(), String.format(LOGGER_NAME_ERR, this.loggerName, identAST.getText()));
        }
    }

    /**
     * Check if the Logger is static instance, log as error if static instance logger,
     * @param varDefAST the VARIABLE_DEF node
     */
    private void logStaticLoggerInstanceErrorIfFound(DetailAST varDefAST) {
        DetailAST modifierAST = varDefAST.findFirstToken(TokenTypes.MODIFIERS);
        if (modifierAST != null && modifierAST.branchContains(TokenTypes.LITERAL_STATIC)) {
            log(varDefAST.getLineNo(), STATIC_LOGGER_ERR);
        }
    }

    /**
     * check if the VARIABLE_DEF AST node type is Logger
     * @param varDefAST VARIABLE_DEF AST node
     * @return true if the variable type is Logger
     */
    private boolean isLoggerType(DetailAST varDefAST) {
        DetailAST typeAST = varDefAST.findFirstToken(TokenTypes.TYPE);
        if (typeAST == null) {
            return false;
        }
        DetailAST identAST = typeAST.findFirstToken(TokenTypes.IDENT);
        return identAST != null && identAST.getText().equals(this.slf4lLogger.getSimpleName());
    }

    /**
     * only accept single check expression inside of if statement check
     * @param methodCallAST method call AST node in the if conditional check
     * @return true if only one check expression inside of if statement check
     */
    private boolean isGrandparentLiteralIf(DetailAST methodCallAST) {
        return methodCallAST != null
            && methodCallAST.getParent() != null
            && methodCallAST.getParent().getParent() != null
            && methodCallAST.getParent().getParent().getType() == TokenTypes.LITERAL_IF;
    }

    /**
     * log the guard block error if found
     * @param dotAST the logger DOT AST node
     */
    private void logGuardBlockErrorIfFound(DetailAST dotAST) {
        String methodName = dotAST.getLastChild().getText();
        if (loggerMethodMap.containsKey(methodName)) {
            String enabledName = loggerMethodMap.get(methodName);
            if (!enabledMap.containsKey(enabledName)) {
                log(dotAST.getLineNo(), String.format(LOGGER_LEVEL_ERR, methodName, enabledName));
            }
        }
    }
}
