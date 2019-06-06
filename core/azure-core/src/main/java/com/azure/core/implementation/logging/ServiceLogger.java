// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.logging;

import com.azure.core.configuration.ConfigurationManager;
import com.azure.core.configuration.BaseConfigurations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * This helper class that wraps a {@link Logger} and contains convenience methods for logging.
 */
public class ServiceLogger implements ServiceLoggerAPI {
    private static final NoopServiceLogger NOOP_LOGGER = new NoopServiceLogger();

    private final Logger logger;

    private int level = DEFAULT_LOG_LEVEL;

    private int configurationLevel;

    /**
     * Retrieves a logger for the passed class using the {@link LoggerFactory}.
     * @param clazz Class creating the logger.
     */
    public ServiceLogger(Class clazz) {
        this(clazz.getName());
    }

    /**
     * Retrieves a logger for the passed class name using the {@link LoggerFactory}.
     * @param className Class name creating the logger.
     */
    public ServiceLogger(String className) {
        logger = LoggerFactory.getLogger(className);
    }

    /**
     * Sets the logger to the verbose logging level.
     * @return Updated ServiceLogger if debug is enabled, otherwise a no-op logger.
     */
    @Override
    public ServiceLoggerAPI asVerbose() {
        return asLevel(VERBOSE_LEVEL);
    }

    /**
     * Sets the logger to the info logging level.
     * @return Updated ServiceLogger if info is enabled, otherwise a no-op logger.
     */
    @Override
    public ServiceLoggerAPI asInfo() {
        return asLevel(INFORMATIONAL_LEVEL);
    }

    /**
     * Sets the logger to the warning logging level.
     * @return Updated ServiceLogger if warn is enabled, otherwise a no-op logger.
     */
    @Override
    public ServiceLoggerAPI asWarning() {
        return asLevel(WARNING_LEVEL);
    }

    /**
     * Sets the logger to the error logging level.
     * @return Updated ServiceLogger if error is enabled, otherwise a no-op logger.
     */
    @Override
    public ServiceLoggerAPI asError() {
        return asLevel(ERROR_LEVEL);
    }

    /**
     * Format-able message to be logged, if an exception needs to be logged it must be the last argument.
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
     * @param format Format-able message.
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
     * @return Updated ServiceLogger if the level is enabled, otherwise a no-op logger.
     */
    private ServiceLoggerAPI asLevel(int level) {
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
