// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Check Style Rule: Good Logging Practice
 *  I. Non-static Logger instance
 *  II. Logger variable name should be 'logger'
 *  III. Guard with conditional block check whenever logger's logging method called.
 */
public class GoodLoggingCheck extends AbstractCheck {
    private static String loggerRequiredName;
    private static String loggerActualName;
    private static String loggerClass;
    private static String packagePath;

    private static final String FAILED_TO_LOAD_MESSAGE = "\"%s\" class failed to load, GoodLoggingCheck will be ignored.";
    private static final String LOGGER_NAME_ERR = "Incorrect name for Logger: use \"%s\" instead of \"%s\" as Logger's instance name";
    private static final String LOGGER_LEVEL_ERR = "Please guard \"%s\" method call with \"%s\" method";
    private static final String STATIC_LOGGER_ERR = "Logger should not be static: remove static modifier";
    private static final String LOGGER_FACTORY_GETLOGGER_ERROR = "Should create a non-static final Logger variable and name it logger, instead of use the LoggerFactory.getLogger() directly";

    private static final String LOGGER_FACTORY_GETLOGGER = "LoggerFactory.getLogger";
    private static final String LOGGER = "Logger";

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

    private Stack<Pair<DetailAST, String>> enabledStack = new Stack<>();
    private Map<String, Integer> logMethodCountMap = new HashMap<>();

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
     * @param loggerRequiredName the variable name of logger used
     */
    public void setLoggerName(String loggerRequiredName) {
        this.loggerRequiredName = loggerRequiredName;
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
        logMethodCountMap.clear();
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
                if (!this.hasLoggerImported) {
                    break;
                }
                // null pointer check
                DetailAST dotAST = ast.findFirstToken(TokenTypes.DOT);
                if (dotAST == null) {
                    break;
                }

                String methodName = dotAST.getLastChild().getText();
                // check if the method name is one of isEnabled method, such as isDebugEnabled()
                if (loggerMethodMap.containsValue(methodName)){ // ex.,isDebugEnabled
                    DetailAST firstIfAstNode = findFirstConditionalIf(ast);
                    if (firstIfAstNode != null && methodName != null) {
                        enabledStack.push(new Pair<>(firstIfAstNode, methodName));
                        logMethodCountMap.put(methodName, logMethodCountMap.getOrDefault(methodName, 0) + 1);
                        break;
                    }
                }
                // skip if the method call is not on
                if (!loggerMethodMap.containsKey(methodName)) {
                    break;
                }
                DetailAST methodCaller = dotAST.getFirstChild();
                String varName = getMethodCallFullStackTrace(methodCaller);

                // skip if not declared logger variable name or LoggerFactory.getLogger(), ex., abc.error()
                if (!varName.equals(this.loggerActualName) && !varName.equals(LOGGER_FACTORY_GETLOGGER)) {
                    break;
                }
                // log the error if the method caller is LoggerFactory.getLogger()
                if (LOGGER_FACTORY_GETLOGGER.equals(varName)) {
                    log(dotAST, LOGGER_FACTORY_GETLOGGER_ERROR);
                }
                // check if the method name is logger method, such as error(), log the error
                if (loggerMethodMap.containsKey(methodName)) {
                    if (!varName.equals(this.loggerRequiredName)) {
                        log(dotAST, String.format(LOGGER_NAME_ERR, this.loggerRequiredName, varName));
                    }
                    logGuardBlockErrorIfFound(dotAST);
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
        if (!this.isPathCorrect) {
            return;
        }
        switch (ast.getType()) {
            case TokenTypes.LITERAL_IF:
                while(!enabledStack.isEmpty() && enabledStack.peek().getKey() == ast) {
                    Pair<DetailAST, String> astNodeAndMethodNamePair = enabledStack.pop();
                    // all nodes in the stack should already check the logger method existence
                    String methodText = astNodeAndMethodNamePair.getValue();
                    if (logMethodCountMap.containsKey(methodText)) {
                        Integer ct = logMethodCountMap.get(methodText);
                        if (ct <= 1) {
                            logMethodCountMap.remove(methodText);
                        } else {
                            logMethodCountMap.put(methodText, ct - 1);
                        }
                    }
                }
                break;
        }
    }

    /**
     * Find the first LITERAL_IF AST ancestor node by given METHOD_CALL AST node
     * @param methodCallAST METHOD_CALL AST node
     * @return the first LITERAL_IF AST node
     */
    private DetailAST findFirstConditionalIf(DetailAST methodCallAST) {
        if (methodCallAST == null || methodCallAST.getType() == TokenTypes.LITERAL_IF) {
            return methodCallAST;
        }
        return findFirstConditionalIf(methodCallAST.getParent());
    }

    /**
     * Get the full method caller and callee string
     * @param methodCaller METHOD_CALL AST node
     * @return the full method call track string
     */
    private String getMethodCallFullStackTrace(DetailAST methodCaller) {
        if (methodCaller == null) {
            return "";
        }
        if (methodCaller.getType() == TokenTypes.IDENT) {
            return methodCaller.getText();
        }
        DetailAST localDotAST =  methodCaller.findFirstToken(TokenTypes.DOT);
        return String.join(".", getMethodCallFullStackTrace(localDotAST.getFirstChild()), localDotAST.getLastChild().getText());
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
            if (typeName != null && typeName.equals(LOGGER)) {
                this.loggerActualName = varNameAST.getText();
                return true;
            }
        }
        return false;
    }

    /**
     * log the guard block error if found
     * @param dotAST the logger DOT AST node
     */
    private void logGuardBlockErrorIfFound(DetailAST dotAST) {
        if (dotAST == null) {
            return;
        }
        String methodName = dotAST.getLastChild().getText();
        if (loggerMethodMap.containsKey(methodName)) {
            String enabledName = loggerMethodMap.get(methodName);
            if (!logMethodCountMap.containsKey(enabledName)) {
                log(dotAST, String.format(LOGGER_LEVEL_ERR, methodName, enabledName));
            }
        }
    }
}
