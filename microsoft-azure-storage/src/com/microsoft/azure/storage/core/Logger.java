/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.core;

import org.slf4j.LoggerFactory;

import com.microsoft.azure.storage.OperationContext;

/**
 * RESERVED FOR INTERNAL USE.
 * 
 * A wrapper around a {@link org.slf4j.Logger} object which allows for performance optimizations around string
 * formatting, better formatted log descriptions, and more library control over when to log.
 */
public class Logger {

    public static void debug(OperationContext opContext, String format) {
        if (shouldLog(opContext)) {
            org.slf4j.Logger logger = opContext == null ? LoggerFactory.getLogger(OperationContext.defaultLoggerName)
                    : opContext.getLogger();
            if (logger.isDebugEnabled()) {
                logger.debug(formatLogEntry(opContext, format));
            }
        }

    }

    public static void debug(OperationContext opContext, String format, Object... args) {
        if (shouldLog(opContext)) {
            org.slf4j.Logger logger = opContext == null ? LoggerFactory.getLogger(OperationContext.defaultLoggerName)
                    : opContext.getLogger();
            if (logger.isDebugEnabled()) {
                logger.debug(formatLogEntry(opContext, format, args));
            }
        }
    }

    public static void debug(OperationContext opContext, String format, Object arg1) {
        if (shouldLog(opContext)) {
            org.slf4j.Logger logger = opContext == null ? LoggerFactory.getLogger(OperationContext.defaultLoggerName)
                    : opContext.getLogger();
            if (logger.isDebugEnabled()) {
                logger.debug(formatLogEntry(opContext, format, arg1));
            }
        }
    }

    public static void debug(OperationContext opContext, String format, Object arg1, Object arg2) {
        if (shouldLog(opContext)) {

            org.slf4j.Logger logger = opContext == null ? LoggerFactory.getLogger(OperationContext.defaultLoggerName)
                    : opContext.getLogger();
            if (logger.isDebugEnabled()) {
                logger.debug(formatLogEntry(opContext, format, arg1, arg2));
            }
        }
    }

    public static void error(OperationContext opContext, String format) {
        if (shouldLog(opContext)) {

            org.slf4j.Logger logger = opContext == null ? LoggerFactory.getLogger(OperationContext.defaultLoggerName)
                    : opContext.getLogger();
            if (logger.isErrorEnabled()) {
                logger.error(formatLogEntry(opContext, format));
            }
        }
    }

    public static void error(OperationContext opContext, String format, Object... args) {
        if (shouldLog(opContext)) {

            org.slf4j.Logger logger = opContext == null ? LoggerFactory.getLogger(OperationContext.defaultLoggerName)
                    : opContext.getLogger();
            if (logger.isErrorEnabled()) {
                logger.error(formatLogEntry(opContext, format, args));
            }
        }
    }

    public static void error(OperationContext opContext, String format, Object arg1) {
        if (shouldLog(opContext)) {

            org.slf4j.Logger logger = opContext == null ? LoggerFactory.getLogger(OperationContext.defaultLoggerName)
                    : opContext.getLogger();
            if (logger.isErrorEnabled()) {
                logger.error(formatLogEntry(opContext, format, arg1));
            }
        }
    }

    public static void error(OperationContext opContext, String format, Object args1, Object args2) {
        if (shouldLog(opContext)) {

            org.slf4j.Logger logger = opContext == null ? LoggerFactory.getLogger(OperationContext.defaultLoggerName)
                    : opContext.getLogger();
            if (logger.isErrorEnabled()) {
                logger.error(formatLogEntry(opContext, format, args1, args2));
            }
        }
    }

    public static void info(OperationContext opContext, String format) {
        if (shouldLog(opContext)) {

            org.slf4j.Logger logger = opContext == null ? LoggerFactory.getLogger(OperationContext.defaultLoggerName)
                    : opContext.getLogger();
            if (logger.isInfoEnabled()) {
                logger.info(formatLogEntry(opContext, format));
            }
        }
    }

    public static void info(OperationContext opContext, String format, Object... args) {
        if (shouldLog(opContext)) {
            org.slf4j.Logger logger = opContext == null ? LoggerFactory.getLogger(OperationContext.defaultLoggerName)
                    : opContext.getLogger();
            if (logger.isInfoEnabled()) {
                logger.info(formatLogEntry(opContext, format, args));
            }
        }
    }

    public static void info(OperationContext opContext, String format, Object arg1) {
        if (shouldLog(opContext)) {

            org.slf4j.Logger logger = opContext == null ? LoggerFactory.getLogger(OperationContext.defaultLoggerName)
                    : opContext.getLogger();
            if (logger.isInfoEnabled()) {
                logger.info(formatLogEntry(opContext, format, arg1));
            }
        }
    }

    public static void info(OperationContext opContext, String format, Object arg1, Object arg2) {
        if (shouldLog(opContext)) {

            org.slf4j.Logger logger = opContext == null ? LoggerFactory.getLogger(OperationContext.defaultLoggerName)
                    : opContext.getLogger();
            if (logger.isInfoEnabled()) {
                logger.info(formatLogEntry(opContext, format, arg1, arg2));
            }
        }
    }

    public static void trace(OperationContext opContext, String format) {
        if (shouldLog(opContext)) {

            org.slf4j.Logger logger = opContext == null ? LoggerFactory.getLogger(OperationContext.defaultLoggerName)
                    : opContext.getLogger();
            if (logger.isTraceEnabled()) {
                logger.trace(formatLogEntry(opContext, format));
            }
        }
    }

    public static void trace(OperationContext opContext, String format, Object... args) {
        if (shouldLog(opContext)) {

            org.slf4j.Logger logger = opContext == null ? LoggerFactory.getLogger(OperationContext.defaultLoggerName)
                    : opContext.getLogger();
            if (logger.isTraceEnabled()) {
                logger.trace(formatLogEntry(opContext, format, args));
            }
        }
    }

    public static void trace(OperationContext opContext, String format, Object arg1) {
        if (shouldLog(opContext)) {

            org.slf4j.Logger logger = opContext == null ? LoggerFactory.getLogger(OperationContext.defaultLoggerName)
                    : opContext.getLogger();
            if (logger.isTraceEnabled()) {
                logger.trace(formatLogEntry(opContext, format, arg1));
            }
        }
    }

    public static void trace(OperationContext opContext, String format, Object arg1, Object arg2) {
        if (shouldLog(opContext)) {

            org.slf4j.Logger logger = opContext == null ? LoggerFactory.getLogger(OperationContext.defaultLoggerName)
                    : opContext.getLogger();
            if (logger.isTraceEnabled()) {
                logger.trace(formatLogEntry(opContext, format, arg1, arg2));
            }
        }
    }

    public static void warn(OperationContext opContext, String format) {
        if (shouldLog(opContext)) {

            org.slf4j.Logger logger = opContext == null ? LoggerFactory.getLogger(OperationContext.defaultLoggerName)
                    : opContext.getLogger();
            if (logger.isWarnEnabled()) {
                logger.warn(formatLogEntry(opContext, format));
            }
        }
    }

    public static void warn(OperationContext opContext, String format, Object... args) {
        if (shouldLog(opContext)) {

            org.slf4j.Logger logger = opContext == null ? LoggerFactory.getLogger(OperationContext.defaultLoggerName)
                    : opContext.getLogger();
            if (logger.isWarnEnabled()) {
                logger.warn(formatLogEntry(opContext, format, args));
            }
        }
    }

    public static void warn(OperationContext opContext, String format, Object arg1) {
        if (shouldLog(opContext)) {

            org.slf4j.Logger logger = opContext == null ? LoggerFactory.getLogger(OperationContext.defaultLoggerName)
                    : opContext.getLogger();
            if (logger.isWarnEnabled()) {
                logger.warn(formatLogEntry(opContext, format, arg1));
            }
        }
    }

    public static void warn(OperationContext opContext, String format, Object arg1, Object arg2) {
        if (shouldLog(opContext)) {

            org.slf4j.Logger logger = opContext == null ? LoggerFactory.getLogger(OperationContext.defaultLoggerName)
                    : opContext.getLogger();
            if (logger.isWarnEnabled()) {
                logger.warn(formatLogEntry(opContext, format, arg1, arg2));
            }
        }
    }

    public static boolean shouldLog(OperationContext opContext) {
        if (opContext != null) {
            return opContext.isLoggingEnabled();
        }
        else {
            return OperationContext.isLoggingEnabledByDefault();
        }
    }

    private static String formatLogEntry(OperationContext opContext, String format) {
        return String.format("{%s}: {%s}", (opContext == null) ? "*" : opContext.getClientRequestID(),
                format.replace('\n', '.'));
    }

    private static String formatLogEntry(OperationContext opContext, String format, Object... args) {
        return String.format("{%s}: {%s}", (opContext == null) ? "*" : opContext.getClientRequestID(),
                String.format(format, args).replace('\n', '.'));
    }

    private static String formatLogEntry(OperationContext opContext, String format, Object arg1) {
        return String.format("{%s}: {%s}", (opContext == null) ? "*" : opContext.getClientRequestID(),
                String.format(format, arg1).replace('\n', '.'));
    }

    private static String formatLogEntry(OperationContext opContext, String format, Object arg1, Object arg2) {
        return String.format("{%s}: {%s}", (opContext == null) ? "*" : opContext.getClientRequestID(),
                String.format(format, arg1, arg2).replace('\n', '.'));
    }

    private Logger() {
        // no op constructor
        // prevents accidental instantiation
    }
}
