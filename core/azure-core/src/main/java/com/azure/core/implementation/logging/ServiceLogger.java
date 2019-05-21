// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.logging;

import com.azure.core.configuration.ConfigurationManager;
import com.azure.core.configuration.BaseConfigurations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This helper class that wraps a {@link Logger} and contains convenience methods for logging.
 */
public class ServiceLogger implements ServiceLoggerAPI {
    private static final NoopServiceLogger NOOP_LOGGER = new NoopServiceLogger();

    private final Logger logger;

    private int level = DEFAULT_LOG_LEVEL;

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
     * Sets the logger to the trace logging level.
     * @return Updated ServiceLogger if trace is enabled, otherwise a no-op logger.
     */
    @Override
    public ServiceLoggerAPI asTrace() {
        return asLevel(TRACE_LEVEL);
    }

    /**
     * Sets the logger to the debug logging level.
     * @return Updated ServiceLogger if debug is enabled, otherwise a no-op logger.
     */
    @Override
    public ServiceLoggerAPI asDebug() {
        return asLevel(DEBUG_LEVEL);
    }

    /**
     * Sets the logger to the info logging level.
     * @return Updated ServiceLogger if info is enabled, otherwise a no-op logger.
     */
    @Override
    public ServiceLoggerAPI asInformational() {
        return asLevel(INFO_LEVEL);
    }

    /**
     * Sets the logger to the warn logging level.
     * @return Updated ServiceLogger if warn is enabled, otherwise a no-op logger.
     */
    @Override
    public ServiceLoggerAPI asWarning() {
        return asLevel(WARN_LEVEL);
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

    /**
     * Performs the logging.
     * @param format Format-able message.
     * @param args Arguments for the message, if an exception is being logged last argument is the throwable.
     */
    private void performLogging(String format, Object... args) {
        switch (level) {
            case TRACE_LEVEL:
                logger.trace(format, args);
                break;
            case DEBUG_LEVEL:
                logger.debug(format, args);
                break;
            case INFO_LEVEL:
                logger.info(format, args);
                break;
            case WARN_LEVEL:
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

    /**
     * Helper method that determines if logging is enabled at a given level.
     * @param level Logging level
     * @return True if the logging level is higher than the minimum logging level and if logging is enabled at the given level.
     */
    private boolean canLogAtLevel(int level) {
        if (level < minimumLoggingLevel()) {
            return false;
        }

        switch (level) {
            case TRACE_LEVEL:
                return logger != null && logger.isTraceEnabled();
            case DEBUG_LEVEL:
                return logger != null && logger.isDebugEnabled();
            case INFO_LEVEL:
                return logger != null && logger.isInfoEnabled();
            case WARN_LEVEL:
                return logger != null && logger.isWarnEnabled();
            case ERROR_LEVEL:
                return logger != null && logger.isErrorEnabled();
            default:
                return false;
        }
    }

    private int minimumLoggingLevel() {
        return ConfigurationManager.getConfiguration().get(BaseConfigurations.AZURE_LOG_LEVEL, DISABLED_LEVEL);
    }
}
