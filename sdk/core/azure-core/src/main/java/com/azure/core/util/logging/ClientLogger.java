// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.logging;

import com.azure.core.implementation.logging.DefaultLogger;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import java.util.Arrays;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

import static com.azure.core.implementation.LoggingUtil.getEnvironmentLoggingLevel;

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
    private final boolean isFromEnv;

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
     * @throws RuntimeException it is an error.
     */
    public ClientLogger(String className) {
        Logger initLogger = LoggerFactory.getLogger(className);

        isFromEnv = initLogger instanceof NOPLogger;
        logger = isFromEnv ? new DefaultLogger(className) : initLogger;
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
        log(LogLevel.VERBOSE, format, args);
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
        log(LogLevel.INFORMATIONAL, format, args);
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
        log(LogLevel.WARNING, format, args);
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
        log(LogLevel.ERROR, format, args);
    }

    /*
     * This method logs the formattable message if the {@code logLevel} is enabled
     *
     * @param logLevel The log level at which this message should be logged
     * @param format The formattable message to log
     * @param args Arguments for the message, if an exception is being logged last argument is the throwable.
     */
    private void log(LogLevel logLevel, String format, Object... args) {
        LogLevel rootLogLevel = getConfiguredLogLevel();
        if (canLogAtLevel(logLevel, rootLogLevel)) {
            performLogging(logLevel, rootLogLevel, false, format, args);
        }
    }

    /**
     * Logs the {@link RuntimeException} at the warning level and returns it to be thrown.
     *
     * @param runtimeException RuntimeException to be logged and returned.
     * @return The passed {@code RuntimeException}.
     * @throws NullPointerException If {@code runtimeException} is {@code null}.
     */
    public RuntimeException logExceptionAsWarning(RuntimeException runtimeException) {
        return logException(runtimeException, LogLevel.WARNING);
    }

    /**
     * Logs the {@link RuntimeException} at the error level and returns it to be thrown.
     *
     * @param runtimeException RuntimeException to be logged and returned.
     * @return The passed {@code RuntimeException}.
     * @throws NullPointerException If {@code runtimeException} is {@code null}.
     */
    public RuntimeException logExceptionAsError(RuntimeException runtimeException) {
        return logException(runtimeException, LogLevel.ERROR);
    }

    private RuntimeException logException(RuntimeException runtimeException, LogLevel logLevel) {
        Objects.requireNonNull(runtimeException, "'runtimeException' cannot be null.");
        LogLevel rootLogLevel = getConfiguredLogLevel();

        if (canLogAtLevel(logLevel, rootLogLevel)) {
            performLogging(logLevel, rootLogLevel, true, runtimeException.getMessage(), runtimeException);
        }

        return runtimeException;
    }

    /*
     * Performs the logging.
     *
     * @param format formattable message.
     * @param args Arguments for the message, if an exception is being logged last argument is the throwable.
     */
    private void performLogging(LogLevel logLevel, LogLevel rootLogLevel,
        boolean isExceptionLogging, String format, Object... args) {
        // If the logging level is less granular than verbose remove the potential throwable from the args.
        String throwableMessage = "";
        if (doesArgsHaveThrowable(args)) {
            // If we are logging an exception the format string is already the exception message, don't append it.
            if (!isExceptionLogging) {
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
            if (rootLogLevel.getLogLevel() > LogLevel.VERBOSE.getLogLevel()) {
                args = removeThrowable(args);
            }
        }

        switch (logLevel) {
            case VERBOSE:
                logger.debug(format, args);
                break;
            case INFORMATIONAL:
                logger.info(format, args);
                break;
            case WARNING:
                if (!CoreUtils.isNullOrEmpty(throwableMessage)) {
                    format += System.lineSeparator() + throwableMessage;
                }
                logger.warn(format, args);
                break;
            case ERROR:
                if (!CoreUtils.isNullOrEmpty(throwableMessage)) {
                    format += System.lineSeparator() + throwableMessage;
                }
                logger.error(format, args);
                break;
            default:
                // Don't do anything, this state shouldn't be possible.
                break;
        }

    }

    /**
     * Determines if the app or environment logger support logging at the given log level.
     *
     * @param logLevel Logging level for the log message.
     * @return Flag indicating if the environment and logger are configured to support logging at the given log level.
     */
    public boolean canLogAtLevel(LogLevel logLevel) {
        LogLevel rootLogLevel = getConfiguredLogLevel();
        return canLogAtLevel(logLevel, rootLogLevel);
    }

    private boolean canLogAtLevel(LogLevel logLevel, LogLevel allowedLogLevel) {
        // Attempting to log at a level not supported by the SLF4J configuration or env variable.
        return logLevel.getLogLevel() >= allowedLogLevel.getLogLevel();
    }

    /**
     * Checking the system log with the preference order of slf4j and environment variable.
     *
     * @return The log level.
     */
    private LogLevel getConfiguredLogLevel() {
        if (isFromEnv) {
            return getEnvironmentLoggingLevel();
        }
        if (logger.isDebugEnabled()) {
            return LogLevel.VERBOSE;
        }
        if (logger.isInfoEnabled()) {
            return LogLevel.INFORMATIONAL;
        }
        if (logger.isWarnEnabled()) {
            return LogLevel.WARNING;
        }
        if (logger.isErrorEnabled()) {
            return LogLevel.ERROR;
        }
        return LogLevel.NOT_SET;
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
}
