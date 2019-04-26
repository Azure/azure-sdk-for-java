// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.implementation.logging;

import com.azure.common.logging.AbstractServiceLogger;
import com.azure.common.logging.ServiceLoggerAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This helper class that wraps a {@link Logger} and contains convenience methods for logging.
 */
public class ServiceLogger extends AbstractServiceLogger {
    private static NoopServiceLogger NOOP_LOGGER = new NoopServiceLogger();

    private final Logger logger;

    private int level;
    private Throwable throwable;

    /**
     * Retrieves a logger for the passed class using the {@link LoggerFactory}.
     * @param clazz Class creating the logger.
     */
    public ServiceLogger(Class clazz) {
        logger = LoggerFactory.getLogger(clazz);
    }

    public ServiceLoggerAPI asInformational() {
        if (logger != null && logger.isInfoEnabled()) {
            level = 4;
            return this;
        }

        return NOOP_LOGGER;
    }

    public ServiceLoggerAPI asWarning() {
        if (logger != null && logger.isWarnEnabled()) {
            level = 3;
            return this;
        }

        return NOOP_LOGGER;
    }

    public ServiceLoggerAPI asError() {
        if (logger != null && logger.isErrorEnabled()) {
            level = 2;
            return this;
        }

        return NOOP_LOGGER;
    }

    public ServiceLoggerAPI asDebug() {
        if (logger != null && logger.isDebugEnabled()) {
            level = 1;
            return this;
        }

        return NOOP_LOGGER;
    }

    public ServiceLoggerAPI asTrace() {
        if (logger != null && logger.isTraceEnabled()) {
            level = 0;
            return this;
        }

        return NOOP_LOGGER;
    }

    public ServiceLoggerAPI withStackTrace(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public void log(String message) {
        if (throwable != null && level == 3 && logger.isDebugEnabled()) {
            logger.warn(message, throwable);

        } else {
            switch (level) {
                case 0:
                    logger.trace(message);
                    break;
                case 1:
                    logger.debug(message);
                    break;
                case 2:
                    logger.error(message);
                    break;
                case 3:
                    logger.warn(message);
                    break;
                case 4:
                    logger.info(message);
                    break;
            }
        }

        throwable = null;
        level = -1;
    }

    public void log(String message, Object val) {
        log(String.format(message, val));
    }
}
