// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Check Style Rule: Good Logging Practice
 *  I. Non-static Logger instance
 *  II. Logger variable name should be 'logger'
 *  III. Guard with conditional block check whenever logger's logging method called.
 */
public class GoodLoggingCheck extends AbstractCheck {
    private static final String LOGGER_CLASS = "Logger";
    private static final String LOGGER_NAME = "logger";

    private static final String LOGGER_NAME_ERR = "Incorrect name for Logger: use 'logger' instead of '%s' as Logger name";
    private static final String LOGGER_LEVEL_ERR = "Please guard %s method call with %s method";
    private static final String STATIC_LOGGER_ERR = "Logger should not be static: remove static modifier";
    private static final int[] TOKENS = new int[] {
        TokenTypes.METHOD_CALL,
        TokenTypes.VARIABLE_DEF
    };

    private static final Map<String, String> loggerMethodMap = new HashMap<>();

    // Logger level static final string variables
    private static final String FATAL = "fatal";
    private static final String ERROR = "error";
    private static final String WARN = "warn";
    private static final String INFO = "info";
    private static final String DEBUG = "debug";
    private static final String TRACE = "trace";

    private static final String IS_FATAL_ENABLED = "isFatalEnabled";
    private static final String IS_ERROR_ENABLED = "isErrorEnabled";
    private static final String IS_WARN_ENABLED = "isWarnEnabled";
    private static final String IS_INFO_ENABLED = "isInfoEnabled";
    private static final String IS_DEBUG_ENABLED = "isDebugEnabled";
    private static final String IS_TRACE_ENABLED = "isTraceEnabled";




    private static final Set<String> loggerSet = new HashSet<>();

    @Override
    public void init() {
        initLogLevelMap();
    }

    @Override
    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return TOKENS;
    }

    @Override
    public int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    @Override
    public void visitToken(DetailAST ast) {

        // skip check if no Logger class imported
        if (!hasLoggerImport(ast)) {
            return;
        }

        switch (ast.getType()) {
            case TokenTypes.VARIABLE_DEF:
                isNameLogger(ast);
                isNonStaticLoggerInstance(ast);
                break;
            case TokenTypes.METHOD_CALL:
                isGuardConditionChecked(ast);
                break;
        }
    }

    /**
     * Check if the file has Logger class imported
     * @param rootAST The root of AST
     * @return true if the class has Logger class imported.
     */
    private boolean hasLoggerImport(DetailAST rootAST) {
        DetailAST importToken = rootAST.findFirstToken(TokenTypes.IMPORT);
        while (importToken != null && importToken.getType() == TokenTypes.IMPORT ) {
            String importClassName = (importToken.findFirstToken(TokenTypes.DOT)).getLastChild().getText();
            if (importClassName.equals(LOGGER_CLASS)) {
                return true;
            }
            importToken = importToken.getNextSibling();
        }
        return false;
    }

    /**
     * Check if the Logger instance named as logger, log as error if not
     * @param varDefAST the VARIABLE_DEF node
     */
    private void isNameLogger(DetailAST varDefAST) {
        String type = varDefAST.findFirstToken(TokenTypes.TYPE).getFirstChild().getText();
        if (type.equals(LOGGER_CLASS)) {
            String varName = varDefAST.findFirstToken(TokenTypes.IDENT).getText();
            loggerSet.add(varName);
            if (!varName.equals(LOGGER_NAME)) {
                log(varDefAST.getLineNo(), String.format(LOGGER_NAME_ERR, varName));
            }
        }
    }

    /**
     * Check if the Logger is static instance, log as error if static instance logger,
     * @param varDefAST the VARIABLE_DEF node
     */
    private void isNonStaticLoggerInstance(DetailAST varDefAST) {

        String type = varDefAST.findFirstToken(TokenTypes.TYPE).getFirstChild().getText();
        if (type.equals(LOGGER_CLASS)) {
            if(varDefAST.findFirstToken(TokenTypes.MODIFIERS).branchContains(TokenTypes.LITERAL_STATIC)) {
                log(varDefAST.getLineNo(), STATIC_LOGGER_ERR);
            }
        }
    }

    /**
     * Always guard with conditional block whenever a log function called.
     * @param methodCallAST The METHOD_CALL node
     */
    private void isGuardConditionChecked(DetailAST methodCallAST) {
        DetailAST loggerName = methodCallAST.findFirstToken(TokenTypes.DOT).getFirstChild();
        // only check for logger instance, skip checking otherwise
        if (!loggerSet.contains(loggerName.getText())) {
            return;
        }

        DetailAST methodName = methodCallAST.findFirstToken(TokenTypes.DOT).getLastChild();
        String methodNameStr = methodName.getText();
        if (isLogLevelMatched(methodCallAST, methodNameStr)) {
            switch (methodNameStr) {
                case FATAL: // log4j only
                    log(methodCallAST.getLineNo(), String.format(LOGGER_LEVEL_ERR, methodNameStr, IS_FATAL_ENABLED));
                    break;
                case ERROR:
                    log(methodCallAST.getLineNo(), String.format(LOGGER_LEVEL_ERR, methodNameStr, IS_ERROR_ENABLED));
                    break;
                case WARN:
                    log(methodCallAST.getLineNo(), String.format(LOGGER_LEVEL_ERR, methodNameStr, IS_WARN_ENABLED));
                    break;
                case INFO:
                    log(methodCallAST.getLineNo(), String.format(LOGGER_LEVEL_ERR, methodNameStr, IS_INFO_ENABLED));
                    break;
                case DEBUG:
                    log(methodCallAST.getLineNo(), String.format(LOGGER_LEVEL_ERR, methodNameStr, IS_DEBUG_ENABLED));
                    break;
                case TRACE:
                    log(methodCallAST.getLineNo(), String.format(LOGGER_LEVEL_ERR, methodNameStr, IS_TRACE_ENABLED));
                    break;
            }
        }

    }


    /**
     * @param methodCallRoot METHOD_CALL node which contains logging method call, such as logger.debug()
     * @param loggingLevelName  logging level method name, such as debug, error, info, etc.
     * @return return true if the logging level match with guard condition block check, otherwise, return false.
     */
    private boolean isLogLevelMatched(DetailAST methodCallRoot, String loggingLevelName) {
        if (methodCallRoot == null || methodCallRoot.getParent() == null) {
            return false;
        }

        DetailAST slistAST = methodCallRoot.getParent().getParent();
        if(slistAST == null || slistAST.getType()!= TokenTypes.SLIST ) {
            return false;
        }

        DetailAST ifAST = slistAST.getParent();
        if (ifAST == null || ifAST.getType() != TokenTypes.LITERAL_IF) {
            return false;
        }

        DetailAST exprAST = ifAST.findFirstToken(TokenTypes.EXPR);
        if (exprAST == null) {
            return false;
        }

        DetailAST methodCallAST = exprAST.findFirstToken(TokenTypes.METHOD_CALL);
        if(methodCallAST == null) {
            return false;
        }

        DetailAST dotAST = methodCallAST.findFirstToken(TokenTypes.DOT);

        if (!(dotAST.getLastChild().getType() == TokenTypes.IDENT)
            || !dotAST.getLastChild().getText().equals(loggerMethodMap.get(loggingLevelName))) {
            return false;
        }

        return true;
    }

    /**
     * initialize the log level mapping
     */
    private void initLogLevelMap() {
        loggerMethodMap.put(TRACE, IS_TRACE_ENABLED);
        loggerMethodMap.put(DEBUG, IS_DEBUG_ENABLED);
        loggerMethodMap.put(INFO,  IS_INFO_ENABLED);
        loggerMethodMap.put(WARN,  IS_WARN_ENABLED);
        loggerMethodMap.put(ERROR, IS_ERROR_ENABLED);
        loggerMethodMap.put(FATAL, IS_FATAL_ENABLED);
    }
}
