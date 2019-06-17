// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.logging;

import com.azure.core.configuration.BaseConfigurations;
import com.azure.core.configuration.Configuration;
import com.azure.core.configuration.ConfigurationManager;
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
 * environment configuration, by default logging is disabled. The default logging level for messages is
 * {@link ClientLogger#asInfo() info}.</p>
 *
 * <p><strong>Log level hierarchy</strong></p>
 * <ol>
 *     <li>{@link ClientLogger#asError() Error}</li>
 *     <li>{@link ClientLogger#asWarning() Warning}</li>
 *     <li>{@link ClientLogger#asInfo() Info}</li>
 *     <li>{@link ClientLogger#asVerbose() Verbose}</li>
 * </ol>
 *
 * @see Configuration
 */
public class ClientLogger implements ClientLoggerAPI {
    private static final NoopClientLogger NOOP_LOGGER = new NoopClientLogger();

    private final Logger logger;

    private int level = DEFAULT_LOG_LEVEL;

    private int configurationLevel;

    /**
     * Retrieves a logger for the passed class using the {@link LoggerFactory}.
     * @param clazz Class creating the logger.
     */
    public ClientLogger(Class clazz) {
        this(clazz.getName());
    }

    /**
     * Retrieves a logger for the passed class name using the {@link LoggerFactory}.
     * @param className Class name creating the logger.
     */
    public ClientLogger(String className) {
        logger = LoggerFactory.getLogger(className);
    }

    /**
     * Sets the logger to the verbose logging level.
     * @return Updated ClientLogger if debug is enabled, otherwise a no-op logger.
     */
    @Override
    public ClientLoggerAPI asVerbose() {
        return asLevel(VERBOSE_LEVEL);
    }

    /**
     * Sets the logger to the info logging level.
     * @return Updated ClientLogger if info is enabled, otherwise a no-op logger.
     */
    @Override
    public ClientLoggerAPI asInfo() {
        return asLevel(INFORMATIONAL_LEVEL);
    }

    /**
     * Sets the logger to the warning logging level.
     * @return Updated ClientLogger if warn is enabled, otherwise a no-op logger.
     */
    @Override
    public ClientLoggerAPI asWarning() {
        return asLevel(WARNING_LEVEL);
    }

    /**
     * Sets the logger to the error logging level.
     * @return Updated ClientLogger if error is enabled, otherwise a no-op logger.
     */
    @Override
    public ClientLoggerAPI asError() {
        return asLevel(ERROR_LEVEL);
    }

    /**
     * Logs a format-able message that uses {@code {}} as the placeholder.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * Logging a message with the default log level
     * <pre>
     * ClientLogger logger = new ClientLogger(Example.class);
     * logger.log("A message");
     * </pre>
     *
     * Logging a format-able warning
     * <pre>
     * ClientLogger logger = new ClientLogger(Example.class);
     * logger.asWarning().log("A format-able message. Hello, {}", name);
     * </pre>
     *
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
     * @param args Arguments for the message, if an exception is being logged last argument is the throwable.
     */
    @Override
    public void log(String format, Object... args) {
        if (canLogAtLevel(level)) {
            performLogging(format, args);
        }

        // Reset the logging level to the default for the next logging request.
        level = DEFAULT_LOG_LEVEL;
    }

    /*
     * Performs the logging.
     * @param format Format-ablgit status
     * asVerbosee message.
     * @param args Arguments for the message, if an exception is being logged last argument is the throwable.
     */
    private void performLogging(String format, Object... args) {
        // If the logging level is less granular than verbose remove the potential throwable from the args.
        if (configurationLevel > VERBOSE_LEVEL) {
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
     * @return Updated ClientLogger if the level is enabled, otherwise a no-op logger.
     */
    private ClientLoggerAPI asLevel(int level) {
        if (canLogAtLevel(level)) {
            this.level = level;
            return this;
        }

        return NOOP_LOGGER;
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
