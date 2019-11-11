// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.logging;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
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
        int environmentLoggingLevel = getEnvironmentLoggingLevel();

        if (canLogAtLevel(logLevel, environmentLoggingLevel)) {
            performLogging(logLevel, environmentLoggingLevel, false, format, args);
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

        int environmentLoggingLevel = getEnvironmentLoggingLevel();

        if (canLogAtLevel(logLevel, environmentLoggingLevel)) {
            performLogging(logLevel, environmentLoggingLevel, true, runtimeException.getMessage(), runtimeException);
        }

        return runtimeException;
    }

    /*
     * Performs the logging.
     *
     * @param format formattable message.
     * @param args Arguments for the message, if an exception is being logged last argument is the throwable.
     */
    private void performLogging(int logLevel, int environmentLogLevel, boolean isExceptionLogging, String format,
        Object... args) {
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
            if (environmentLogLevel > VERBOSE_LEVEL) {
                args = removeThrowable(args);
            }
        }

        switch (logLevel) {
            case VERBOSE_LEVEL:
                logger.debug(format, args);
                break;
            case INFORMATIONAL_LEVEL:
                logger.info(format, args);
                break;
            case WARNING_LEVEL:
                if (!CoreUtils.isNullOrEmpty(throwableMessage)) {
                    format += System.lineSeparator() + throwableMessage;
                }
                logger.warn(format, args);
                break;
            case ERROR_LEVEL:
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

    /*
     * Retrieve the environment logging level which is used to determine if and what we are allowed to log.
     *
     * The value returned from this method should be used throughout a single logging event as it may change during
     * the logging operation, this will help prevent difficult to debug timing issues.
     *
     * @return Environment logging level if set, otherwise DISABLED_LEVEL.
     */
    private int getEnvironmentLoggingLevel() {
        return Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_LOG_LEVEL, DISABLED_LEVEL);
    }

    /*
     * Determines if the environment and logger support logging at the given log level.
     *
     * @param logLevel Logging level for the log message.
     * @param environmentLoggingLevel Logging level the environment is set to support.
     * @return Flag indicating if the environment and logger are configured to support logging at the given log level.
     */
    private boolean canLogAtLevel(int logLevel, int environmentLoggingLevel) {
        // Attempting to log at a level not supported by the environment or logger is null.
        if (logLevel < environmentLoggingLevel || logger == null) {
            return false;
        }

        // Determine if the logger configuration supports logging at the level.
        switch (logLevel) {
            case VERBOSE_LEVEL:
                return logger.isDebugEnabled();
            case INFORMATIONAL_LEVEL:
                return logger.isInfoEnabled();
            case WARNING_LEVEL:
                return logger.isWarnEnabled();
            case ERROR_LEVEL:
                return logger.isErrorEnabled();
            default:
                return false;
        }
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
