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
 * argument of the format varargs and the logger is enabled for {@link ClientLogger#asVerbose() verbose} logging the
 * stack trace for the throwable will be included in the log message.</p>
 *
 * <p>A minimum logging level threshold is determined by the {@link BaseConfigurations#AZURE_LOG_LEVEL AZURE_LOG_LEVEL}
 * environment configuration, by default logging is disabled.</p>
 *
 * <p><strong>Log level hierarchy</strong></p>
 * <ol>
 * <li>{@link ClientLogger#asError() Error}</li>
 * <li>{@link ClientLogger#asWarning() Warning}</li>
 * <li>{@link ClientLogger#asInfo() Info}</li>
 * <li>{@link ClientLogger#asVerbose() Verbose}</li>
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
    private int level = DEFAULT_LOG_LEVEL;

    private int configurationLevel;

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
     * Sets the logger to the verbose logging level.
     *
     * @return Updated ClientLogger if verbose is enabled.
     */
    public ClientLogger asVerbose() {
        return asLevel(VERBOSE_LEVEL);
    }

    /**
     * Sets the logger to the info logging level.
     *
     * @return Updated ClientLogger if info is enabled.
     */
    public ClientLogger asInfo() {
        return asLevel(INFORMATIONAL_LEVEL);
    }

    /**
     * Sets the logger to the warning logging level.
     *
     * @return Updated ClientLogger if warn is enabled.
     */
    public ClientLogger asWarning() {
        return asLevel(WARNING_LEVEL);
    }

    /**
     * Sets the logger to the error logging level.
     *
     * @return Updated ClientLogger if error is enabled..
     */
    public ClientLogger asError() {
        return asLevel(ERROR_LEVEL);
    }

    /**
     * Logs a format-able message that uses {@code {}} as the placeholder.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>
     * Logging a message with the default log level
     * <pre>
     * ClientLogger logger = new ClientLogger(Example.class);
     * logger.log("A message");
     * </pre>
     * <p>
     * Logging a format-able warning
     * <pre>
     * ClientLogger logger = new ClientLogger(Example.class);
     * logger.asWarning().log("A format-able message. Hello, {}", name);
     * </pre>
     * <p>
     * Logging an error with stack trace
     * <pre>
     * ClientLogger logger = new ClientLogger(Example.class);
     * try {
     *    upload(resource);
     * } catch (Throwable ex) {
     *    logger.asError().log("Failed to upload {}", resource.name(), ex);
     * }
     * </pre>
     *
     * @param format Format-able message.
     * @param args   Arguments for the message, if an exception is being logged last argument is the throwable.
     */
    public void log(String format, Object... args) {
        if (canLogAtLevel(level)) {
            performLogging(format, true, args);
        }
    }

    /**
     * This will log, if error log level is enabled , and throw the runtime exception.
     * @param runtimeException to be thrown. It will do nothing if {@code null} is provided.
     */
    public void logAndThrow(RuntimeException runtimeException) {
        if(runtimeException == null) {
            return;
        }
        // By default we are treating runtime exception as error ?
        level = ERROR_LEVEL;
        if (canLogAtLevel(level)) {
            performLogging(runtimeException.getClass().getName(), false, runtimeException);
        }
        throw runtimeException;
    }

    /*
     * Performs the logging.
     * @param format Format-able message.
     * @param args Arguments for the message, if an exception is being logged last argument is the throwable.
     */
    private void performLogging(String format, boolean removeThroawable, Object... args) {
        // If the logging level is less granular than verbose remove the potential throwable from the args.
        if (removeThroawable && configurationLevel > VERBOSE_LEVEL) {
            args = attemptToRemoveThrowable(args);
        }
        switch (level) {
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

    /*
     * Helper method to set the logging level.
     * @param level Logging level
     * @return Updated ClientLogger if the level is enabled.
     */
    private ClientLogger asLevel(int level) {
        if (canLogAtLevel(level)) {
            this.level = level;
        }

        return this;
    }

    /*
     * Helper method that determines if logging is enabled at a given level.
     * @param level Logging level
     * @return True if the logging level is higher than the minimum logging level and if logging is enabled at the given level.
     */
    private boolean canLogAtLevel(int level) {
        // Check the configuration level every time the logger is called in case it has changed.
        configurationLevel = ConfigurationManager.getConfiguration().get(BaseConfigurations.AZURE_LOG_LEVEL, DISABLED_LEVEL);
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

    /*
     * Removes the last element from the arguments if it is a throwable.
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
