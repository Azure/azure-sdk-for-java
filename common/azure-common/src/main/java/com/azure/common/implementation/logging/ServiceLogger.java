// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.implementation.logging;

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

    /**
     * Retrieves a logger for the passed class using the {@link LoggerFactory}.
     * @param clazz Class creating the logger.
     */
    public ServiceLogger(Class clazz) {
        logger = LoggerFactory.getLogger(clazz);
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
        Throwable throwable = getPotentialThrowable(args);
        boolean canLogDebug = canLogAtLevel(DEBUG_LEVEL);
        boolean canLogThrow = canLogDebug && throwable != null;

        // Found a throwable, strip the last element in the args for formatting the message.
        if (throwable != null) {
            args = Arrays.copyOfRange(args, 0, args.length - 1);

            if (!canLogDebug) {
                throwable = null;
            }
        }

        String message = (args == null || args.length == 0) ? format : String.format(format, args);

        switch (level) {
            case TRACE_LEVEL:
                if (canLogThrow) {
                    logger.trace(message, throwable);
                } else {
                    logger.trace(message);
                }
                break;
            case DEBUG_LEVEL:
                if (canLogThrow) {
                    logger.debug(message, throwable);
                } else {
                    logger.debug(message);
                }
                break;
            case INFO_LEVEL:
                if (canLogThrow) {
                    logger.info(message, throwable);
                } else {
                    logger.info(message);
                }
                break;
            case WARN_LEVEL:
                if (canLogThrow) {
                    logger.warn(message, throwable);
                } else {
                    logger.warn(message);
                }
                break;
            case ERROR_LEVEL:
                if (canLogThrow) {
                    logger.error(message, throwable);
                } else {
                    logger.error(message);
                }
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
     * @return True if logging is enabled.
     */
    private boolean canLogAtLevel(int level) {
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

    /**
     * Retrieves the potential throwable from the arguments.
     * @param args Arguments
     * @return Throwable if the last element in the argument array is a throwable, null otherwise.
     */
    private Throwable getPotentialThrowable(Object... args) {
        if (args == null || args.length == 0) {
            return null;
        }

        Object potentialThrowable = args[args.length - 1];
        return (potentialThrowable instanceof Throwable) ? (Throwable) potentialThrowable : null;
    }
}
