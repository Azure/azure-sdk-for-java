// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.logging;

import com.azure.core.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;

/**
 * This is a fluent logger helper class that wraps a pluggable {@link Logger}.
 *
 * <p>This logger logs formattable messages that use {@code {}} as the placeholder. When a {@link Throwable throwable}
 * is the last argument of the format varargs and the logger is enabled for
 * {@link ClientLogger#verbose(String, Object...) verbose}, the stack trace for the throwable is logged.</p>
 *
 * <p>A minimum logging level threshold is determined by the
 * {@link Configuration#PROPERTY_AZURE_LOG_LEVEL AZURE_LOG_LEVEL} environment configuration. By default logging is
 * <b>disabled</b>.</p>
 *
 * <p><strong>Log level hierarchy</strong></p>
 * <ol>
 * <li>{@link ClientLogger#error(String, Object...) Error}</li>
 * <li>{@link ClientLogger#warning(String, Object...) Warning}</li>
 * <li>{@link ClientLogger#info(String, Object...) Info}</li>
 * <li>{@link ClientLogger#verbose(String, Object...) Verbose}</li>
 * </ol>
 *
 * @see Configuration
 */
public class ClientLogger {
    private final Logger logger;

    /*
     * Indicates that log level is at verbose level.
     */
    private static final int VERBOSE_LEVEL = 1;

    /*
     * Indicates that log level is at information level.
     */
    private static final int INFORMATIONAL_LEVEL = 2;

    /*
     * Indicates that log level is at warning level.
     */
    private static final int WARNING_LEVEL = 3;

    /*
     * Indicates that log level is at error level.
     */
    private static final int ERROR_LEVEL = 4;

    /*
     * Indicates that logging is disabled.
     */
    private static final int DISABLED_LEVEL = 5;

    /**
     * Retrieves a logger for the passed class using the {@link LoggerFactory}.
     *
     * @param clazz Class creating the logger.
     */
    public ClientLogger(Class<?> clazz) {
        this(clazz.getName());
    }

    /**
     * Retrieves a logger for the passed class name using the {@link LoggerFactory}.
     *
     * @param className Class name creating the logger.
     */
    public ClientLogger(String className) {
        logger = LoggerFactory.getLogger(className);
    }

    /**
     * Logs a formattable message that uses {@code {}} as the placeholder at {@code verbose} log level.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging a message at verbose log level.</p>
     *
     * {@codesnippet com.azure.core.util.logging.clientlogger.verbose}
     *
     * @param format The formattable message to log.
     * @param args Arguments for the message. If an exception is being logged, the last argument should be the
     *     {@link Throwable}.
     */
    public void verbose(String format, Object... args) {
        log(VERBOSE_LEVEL, format, args);
    }

    /**
     * Logs a formattable message that uses {@code {}} as the placeholder at {@code informational} log level.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging a message at informational log level.</p>
     *
     * {@codesnippet com.azure.core.util.logging.clientlogger.info}
     *
     * @param format The formattable message to log
     * @param args Arguments for the message. If an exception is being logged, the last argument should be the
     *     {@link Throwable}.
     */
    public void info(String format, Object... args) {
        log(INFORMATIONAL_LEVEL, format, args);
    }

    /**
     * Logs a formattable message that uses {@code {}} as the placeholder at {@code warning} log level.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging a message at warning log level.</p>
     *
     * {@codesnippet com.azure.core.util.logging.clientlogger.warning}
     *
     * @param format The formattable message to log.
     * @param args Arguments for the message. If an exception is being logged, the last argument should be the
     *     {@link Throwable}.
     */
    public void warning(String format, Object... args) {
        log(WARNING_LEVEL, format, args);
    }

    /**
     * Logs a formattable message that uses {@code {}} as the placeholder at {@code error} log level.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging an error with stack trace.</p>
     *
     * {@codesnippet com.azure.core.util.logging.clientlogger.error}
     *
     * @param format The formattable message to log.
     * @param args Arguments for the message. If an exception is being logged, the last argument should be the
     *     {@link Throwable}.
     */
    public void error(String format, Object... args) {
        log(ERROR_LEVEL, format, args);
    }

    /*
     * This method logs the formattable message if the {@code logLevel} is enabled
     *
     * @param logLevel The log level at which this message should be logged
     * @param format The formattable message to log
     * @param args Arguments for the message, if an exception is being logged last argument is the throwable.
     */
    private void log(int logLevel, String format, Object... args) {
        performLogging(generateLoggingConfiguration(logLevel, false), format, args);
    }

    /**
     * Logs the {@link RuntimeException} at the warning level and returns it to be thrown.
     *
     * @param runtimeException RuntimeException to be logged and returned.
     * @return The passed {@code RuntimeException}.
     * @throws NullPointerException If {@code runtimeException} is {@code null}.
     */
    public RuntimeException logExceptionAsWarning(RuntimeException runtimeException) {
        return logException(runtimeException, WARNING_LEVEL);
    }

    /**
     * Logs the {@link RuntimeException} at the error level and returns it to be thrown.
     *
     * @param runtimeException RuntimeException to be logged and returned.
     * @return The passed {@code RuntimeException}.
     * @throws NullPointerException If {@code runtimeException} is {@code null}.
     */
    public RuntimeException logExceptionAsError(RuntimeException runtimeException) {
        return logException(runtimeException, ERROR_LEVEL);
    }

    private RuntimeException logException(RuntimeException runtimeException, int logLevel) {
        Objects.requireNonNull(runtimeException, "'runtimeException' cannot be null.");

        performLogging(generateLoggingConfiguration(logLevel, true), runtimeException.getMessage(), runtimeException);
        return runtimeException;
    }

    /*
     * Performs the logging.
     *
     * @param format formattable message.
     * @param args Arguments for the message, if an exception is being logged last argument is the throwable.
     */
    private void performLogging(LoggingConfiguration loggingConfiguration, String format, Object... args) {
        // Only log if the level is enabled.
        if (loggingConfiguration.canLog()) {
            return;
        }

        // If the logging level is less granular than verbose remove the potential throwable from the args.
        String throwableMessage = "";
        if (doesArgsHaveThrowable(args)) {
            // If we are logging an exception the format string is already the exception message, don't append it.
            if (!loggingConfiguration.isExceptionLogging()) {
                Object throwable = args[args.length - 1];

                // This is true from before but is needed to appease SpotBugs.
                if (throwable instanceof Throwable) {
                    throwableMessage = ((Throwable) throwable).getMessage();
                }
            }

            /*
             * Environment is logging at a level higher than verbose, strip out the throwable as it would log its
             * stack trace which is only expected when logging at a verbose level.
             */
            if (loggingConfiguration.getEnvironmentLogLevel() > VERBOSE_LEVEL) {
                args = removeThrowable(args);
            }
        }

        switch (loggingConfiguration.getLogLevel()) {
            case VERBOSE_LEVEL:
                logger.debug(format, args);
                break;
            case INFORMATIONAL_LEVEL:
                logger.info(format, args);
                break;
            case WARNING_LEVEL:
                logger.warn(format + throwableMessage, args);
                break;
            case ERROR_LEVEL:
                logger.error(format + throwableMessage, args);
                break;
            default:
                // Don't do anything, this state shouldn't be possible.
                break;
        }
    }

    /*
     * Helper method that generates logging configurations.
     *
     * @param level Logging level for the log message.
     * @param isExceptionLogging Flag indicating if the logging being performed is for an exception.
     * @return Configurations used at log time such as the ability to log, the environment logging level, the level
     * to log the message, and the type of logging being performed.
     */
    private LoggingConfiguration generateLoggingConfiguration(int level, boolean isExceptionLogging) {
        // Check the configuration level every time the logger is called in case it has changed.
        int environmentLogLevel = Configuration.getGlobalConfiguration()
            .get(Configuration.PROPERTY_AZURE_LOG_LEVEL, DISABLED_LEVEL);

        if (level < environmentLogLevel) {
            return new LoggingConfiguration(environmentLogLevel, level, false, isExceptionLogging);
        }

        boolean canLog;
        switch (level) {
            case VERBOSE_LEVEL:
                canLog = (logger != null && logger.isDebugEnabled());
                break;
            case INFORMATIONAL_LEVEL:
                canLog = (logger != null && logger.isInfoEnabled());
                break;
            case WARNING_LEVEL:
                canLog = (logger != null && logger.isWarnEnabled());
                break;
            case ERROR_LEVEL:
                canLog = (logger != null && logger.isErrorEnabled());
                break;
            default:
                canLog = false;
                break;
        }

        return new LoggingConfiguration(environmentLogLevel, level, canLog, isExceptionLogging);
    }

    /*
     * Determines if the arguments contains a throwable that would be logged, SLF4J logs a throwable if it is the last
     * element in the argument list.
     *
     * @param args The arguments passed to format the log message.
     * @return True if the last element is a throwable, false otherwise.
     */
    private boolean doesArgsHaveThrowable(Object... args) {
        if (args.length == 0) {
            return false;
        }

        return args[args.length - 1] instanceof Throwable;
    }

    /*
     * Removes the last element from the arguments as it is a throwable.
     *
     * @param args The arguments passed to format the log message.
     * @return The arguments with the last element removed.
     */
    private Object[] removeThrowable(Object... args) {
        return Arrays.copyOf(args, args.length - 1);
    }

    /*
     * Helper class which contains logging configuration.
     */
    private static class LoggingConfiguration {
        private final int environmentLogLevel;
        private final int logLevel;
        private final boolean canLog;
        private final boolean isExceptionLogging;

        LoggingConfiguration(int environmentLogLevel, int logLevel, boolean canLog, boolean isExceptionLogging) {
            this.environmentLogLevel = environmentLogLevel;
            this.logLevel = logLevel;
            this.canLog = canLog;
            this.isExceptionLogging = isExceptionLogging;
        }

        int getEnvironmentLogLevel() {
            return environmentLogLevel;
        }

        int getLogLevel() {
            return logLevel;
        }

        boolean canLog() {
            return canLog;
        }

        boolean isExceptionLogging() {
            return isExceptionLogging;
        }
    }
}
