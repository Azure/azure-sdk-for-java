// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.logging;

import com.azure.core.util.configuration.BaseConfigurations;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.util.configuration.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * This is a fluent logger helper class that wraps a plug-able {@link Logger}.
 *
 * <p>This logger logs format-able messages that use {@code {}} as the placeholder. When a throwable is the last
 * argument of the format varargs and the logger is enabled for {@link ClientLogger#logAsVerbose(String, Object...) verbose} logging the
 * stack trace for the throwable will be included in the log message.</p>
 *
 * <p>A minimum logging level threshold is determined by the {@link BaseConfigurations#AZURE_LOG_LEVEL AZURE_LOG_LEVEL}
 * environment configuration, by default logging is disabled.</p>
 *
 * <p><strong>Log level hierarchy</strong></p>
 * <ol>
 * <li>{@link ClientLogger#logAsError(String, Object...) Error}</li>
 * <li>{@link ClientLogger#logAsWarning(String, Object...) Warning}</li>
 * <li>{@link ClientLogger#logAsInfo(String, Object...) Info}</li>
 * <li>{@link ClientLogger#logAsVerbose(String, Object...) Verbose}</li>
 * </ol>
 *
 * @see Configuration
 */
public class ClientLogger {
    private final Logger logger;

    /*
     * Indicate that log level is at trace level.
     */
    private static final int TRACE_LEVEL = 0;

    /*
     * Indicate that log level is at verbose level.
     */
    private static final int VERBOSE_LEVEL = 1;

    /*
     * Indicate that log level is at information level.
     */
    private static final int INFORMATIONAL_LEVEL = 2;

    /*
     * Indicate that log level is at warning level.
     */
    private static final int WARNING_LEVEL = 3;

    /*
     * Indicate that log level is at error level.
     */
    private static final int ERROR_LEVEL = 4;

    /*
     * Indicate that logging is disabled.
     */
    private static final int DISABLED_LEVEL = 5;

    private static final int DEFAULT_LOG_LEVEL = DISABLED_LEVEL;

    /**
     * Retrieves a logger for the passed class using the {@link LoggerFactory}.
     *
     * @param clazz Class creating the logger.
     */
    public ClientLogger(Class clazz) {
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
     * Logs a format-able message that uses {@code {}} as the placeholder at {@code verbose} log level
     *
     * <p><strong>Code Samples</strong></p>
     * <p>
     * Logging a message with the default log level
     * <pre>
     * ClientLogger logger = new ClientLogger(Example.class);
     * logger.logAsVerbose("A format-able message. Hello, {}", name);
     * </pre>
     *
     * @param format The format-able message to log
     * @param args Arguments for the message, if an exception is being logged last argument is the throwable.
     */
    public void logAsVerbose(String format, Object... args) {
        log(VERBOSE_LEVEL, format, args);
    }

    /**
     * Logs a format-able message that uses {@code {}} as the placeholder at {@code informational} log level
     *
     * <p><strong>Code Samples</strong></p>
     * <p>
     * Logging a message at informational log level
     * <pre>
     * ClientLogger logger = new ClientLogger(Example.class);
     * logger.logAsInfo("A format-able message. Hello, {}", name);
     * </pre>
     *
     * @param format The format-able message to log
     * @param args Arguments for the message, if an exception is being logged last argument is the throwable.
     */
    public void logAsInfo(String format, Object... args) {
        log(INFORMATIONAL_LEVEL, format, args);
    }

    /**
     * Logs a format-able message that uses {@code {}} as the placeholder at {@code warning} log level
     *
     * <p><strong>Code Samples</strong></p>
     * <p>
     * Logging a message at warning log level
     * <pre>
     * ClientLogger logger = new ClientLogger(Example.class);
     * logger.logAsWarning("A format-able message. Hello, {}", name);
     * </pre>
     *
     * @param format The format-able message to log
     * @param args Arguments for the message, if an exception is being logged last argument is the throwable.
     */
    public void logAsWarning(String format, Object... args) {
        log(WARNING_LEVEL, format, args);
    }

    /**
     * Logs a format-able message that uses {@code {}} as the placeholder at {@code error} log level
     *
     * <p><strong>Code Samples</strong></p>
     * <p>
     * Logging an error with stack trace
     * <pre>
     * ClientLogger logger = new ClientLogger(Example.class);
     * try {
     *    upload(resource);
     * } catch (Throwable ex) {
     *    logger.logAsError("Failed to upload {}", resource.name(), ex);
     * }
     * </pre>
     *
     * @param format The format-able message to log
     * @param args Arguments for the message, if an exception is being logged last argument is the throwable.
     */
    public void logAsError(String format, Object... args) {
        log(ERROR_LEVEL, format, args);
    }

    /**
     * Logs a format-able message that uses {@code {}} as the placeholder at {@code default} log level
     *
     * <p><strong>Code Samples</strong></p>
     * <p>
     * Logging a message at default log level
     * <pre>
     * ClientLogger logger = new ClientLogger(Example.class);
     * logger.log("A message");
     * </pre>
     *
     * @param format The format-able message
     * @param args Arguments for the message, if an exception is being logged last argument is the throwable.
     */
    public void log(String format, Object... args) {
        log(DEFAULT_LOG_LEVEL, format, args);
    }

    /**
     * This method logs the format-able message if the {@code logLevel} is enabled
     *
     * @param logLevel The log level at which this message should be logged
     * @param format The format-able message
     * @param args Arguments for the message, if an exception is being logged last argument is the throwable.
     */
    private void log(int logLevel, String format, Object... args) {
        if (canLogAtLevel(logLevel)) {
            performLogging(logLevel, format, args);
        }
    }

    /**
     * Performs the logging.
     *
     * @param format Format-able message.
     * @param args Arguments for the message, if an exception is being logged last argument is the throwable.
     */
    private void performLogging(int logLevel, String format, Object... args) {
        // If the logging level is less granular than verbose remove the potential throwable from the args.
        if (logLevel > VERBOSE_LEVEL) {
            args = attemptToRemoveThrowable(args);
        }

        switch (logLevel) {
            case VERBOSE_LEVEL:
                logger.debug(format, args);
                break;
            case INFORMATIONAL_LEVEL:
                logger.info(format, args);
                break;
            case WARNING_LEVEL:
                logger.warn(format, args);
                break;
            case ERROR_LEVEL:
                logger.error(format, args);
                break;
            default:
                // Don't do anything, this state shouldn't be possible.
                break;
        }
    }

    /**
     * Helper method that determines if logging is enabled at a given level.
     * @param level Logging level
     * @return True if the logging level is higher than the minimum logging level and if logging is enabled at the given level.
     */
    private boolean canLogAtLevel(int level) {
        // Check the configuration level every time the logger is called in case it has changed.
        int configurationLevel = ConfigurationManager.getConfiguration().get(BaseConfigurations.AZURE_LOG_LEVEL, DISABLED_LEVEL);
        if (level < configurationLevel) {
            return false;
        }

        switch (level) {
            case VERBOSE_LEVEL:
                return logger != null && logger.isDebugEnabled();
            case INFORMATIONAL_LEVEL:
                return logger != null && logger.isInfoEnabled();
            case WARNING_LEVEL:
                return logger != null && logger.isWarnEnabled();
            case ERROR_LEVEL:
                return logger != null && logger.isErrorEnabled();
            default:
                return false;
        }
    }

    /**
     * Removes the last element from the arguments if it is a throwable.
     *
     * @param args Arguments
     * @return The arguments with the last element removed if it was a throwable, otherwise the unmodified arguments.
     */
    private Object[] attemptToRemoveThrowable(Object... args) {
        if (args.length == 0) {
            return args;
        }

        Object potentialThrowable = args[args.length - 1];
        if (potentialThrowable instanceof Throwable) {
            return Arrays.copyOf(args, args.length - 1);
        }
        return args;
    }
}
