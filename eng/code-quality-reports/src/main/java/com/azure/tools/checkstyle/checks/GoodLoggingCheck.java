// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;
import jdk.nashorn.internal.parser.Token;
import org.checkerframework.checker.nullness.Opt;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Check Style Rule: Good Logging Practice
 *  I. Non-static Logger instance
 *  II. Logger variable name should be 'logger'
 *  III. Guard with conditional block check whenever logger's logging method called.
 */
public class GoodLoggingCheck extends AbstractCheck {
    private static String loggerName = "logger"; // by default
    private static String loggerClass = "org.slf4j.Logger"; // by default

    private static final String FAILED_TO_LOAD_MESSAGE = "%s class failed to load, GoodLoggingCheck will be ignored.";
    private static final String LOGGER_NAME_ERR = "Incorrect name for Logger: use 'logger' instead of '%s' as Logger name";
    private static final String LOGGER_LEVEL_ERR = "Please guard %s method call with %s method";
    private static final String STATIC_LOGGER_ERR = "Logger should not be static: remove static modifier";

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

    private static boolean hasLoggerImported;
    private Class<?> slf4lLogger;

    private static final HashMap<String, String> loggerMethodMap = new HashMap<String, String>(){
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
            log(0, String.format(FAILED_TO_LOAD_MESSAGE, "ServiceClient"));
        }
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
            TokenTypes.METHOD_CALL,
            TokenTypes.VARIABLE_DEF
        };
    }

    @Override
    public void beginTree(DetailAST rootAST) {
        this.hasLoggerImported = false;
    }

    @Override
    public void visitToken(DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.IMPORT:
                String importClassPath = FullIdent.createFullIdentBelow(ast).getText();
                this.hasLoggerImported |= importClassPath.equals(this.slf4lLogger.getName());
                break;
            case TokenTypes.VARIABLE_DEF:
                if (hasLoggerImported) {
                    isNameLogger(ast);
                    isNonStaticLoggerInstance(ast);
                }
                break;
            case TokenTypes.METHOD_CALL:
                if (hasLoggerImported) {
                    isGuardConditionChecked(ast);
                }
                break;
        }
    }

    /**
     * Check if the Logger instance named as logger, log as error if not
     * @param varDefAST the VARIABLE_DEF node
     */
    private void isNameLogger(DetailAST varDefAST) {
        Predicate<DetailAST> isType = c -> c.getType() == TokenTypes.TYPE;
        isType = isType.and(c -> c.getFirstChild().getText().equals(this.slf4lLogger.getSimpleName()));
        Optional<DetailAST> isSameType = TokenUtil.findFirstTokenByPredicate(varDefAST, isType);

        Predicate<DetailAST> isLoggerName = c -> c.getType() == TokenTypes.IDENT;
        isLoggerName = isLoggerName.and(c -> c.getText().equals(this.loggerName));

        if (isSameType.filter(isLoggerName).isPresent()) {
            log(varDefAST.getLineNo(), String.format(LOGGER_NAME_ERR, varDefAST.findFirstToken(TokenTypes.IDENT).getText()));
        }
    }

    /**
     * Check if the Logger is static instance, log as error if static instance logger,
     * @param varDefAST the VARIABLE_DEF node
     */
    private void isNonStaticLoggerInstance(DetailAST varDefAST) {
        Predicate<DetailAST> isType = c -> c.getType() == TokenTypes.TYPE;
        isType = isType.and(c -> c.getFirstChild().getText().equals(this.slf4lLogger.getSimpleName()));
        Optional<DetailAST> isSameType = TokenUtil.findFirstTokenByPredicate(varDefAST, isType);

        Predicate<DetailAST> isStatic = c -> c.getType() == TokenTypes.MODIFIERS;
        isStatic = isStatic.and(c -> c.branchContains(TokenTypes.LITERAL_STATIC));

        if (isSameType.filter(isStatic).isPresent()) {
            log(varDefAST.getLineNo(), STATIC_LOGGER_ERR);
        }
    }

    /**
     * Always guard with conditional block whenever a log function called.
     * @param methodCallAST The METHOD_CALL node
     */
    private void isGuardConditionChecked(DetailAST methodCallAST) {
        // only check for logger instance, skip checking otherwise
        Predicate<DetailAST> isLoggerType = c -> c.getType() == TokenTypes.DOT;
        isLoggerType = isLoggerType.and(c -> c.getFirstChild().getText().equals(this.loggerName));
        Optional<DetailAST> isLoggerName = TokenUtil.findFirstTokenByPredicate(methodCallAST, isLoggerType);
        if (isLoggerName.isPresent()) {
            return;
        }

        DetailAST methodName = methodCallAST.findFirstToken(TokenTypes.DOT).getLastChild();
        String methodNameStr = methodName.getText();
        if (!loggerMethodMap.containsKey(methodNameStr)) {
            return;
        }

        if (!isLogLevelMatched(methodCallAST, methodNameStr)) {
            switch (methodNameStr) {
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
        Predicate<DetailAST> hasGuardConditon = c -> c.getParent() != null;
        hasGuardConditon.and(c -> c.getParent().getParent() != null)
        .and(c -> c.getParent().getParent().getType() == TokenTypes.SLIST)
        .and(c -> c.getParent().getParent().getParent() != null)
        .and(c -> c.getParent().getParent().getParent().getType() == TokenTypes.LITERAL_IF);

        if (!hasGuardConditon.test(methodCallRoot)) {
            return false;
        }

        DetailAST ifAST = methodCallRoot.getParent().getParent().getParent();
        Predicate<DetailAST> isMatch = c -> c.getType() == TokenTypes.EXPR;

        isMatch.and(c -> c.findFirstToken(TokenTypes.METHOD_CALL) != null)
        .and(c -> c.findFirstToken(TokenTypes.METHOD_CALL).findFirstToken(TokenTypes.DOT) != null)
        .and(c -> c.findFirstToken(TokenTypes.METHOD_CALL).findFirstToken(TokenTypes.DOT).getLastChild().getType() == TokenTypes.IDENT)
        .and(c -> c.findFirstToken(TokenTypes.METHOD_CALL).findFirstToken(TokenTypes.DOT).getLastChild().getText().equals(loggerMethodMap.get(loggingLevelName)));

        return TokenUtil.findFirstTokenByPredicate(ifAST, isMatch).isPresent();
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
}

