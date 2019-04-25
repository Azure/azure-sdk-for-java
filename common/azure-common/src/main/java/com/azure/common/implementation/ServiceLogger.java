// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This helper class that wraps a {@link Logger} and contains convenience methods for logging.
 */
public class ServiceLogger {
    private final Logger logger;

    /**
     * Retrieves a logger for the passed class using the {@link LoggerFactory}.
     * @param clazz Class creating the logger.
     */
    public ServiceLogger(Class clazz) {
        logger = LoggerFactory.getLogger(clazz);
    }

    /**
     * Logs a warning message is warning logging is enabled.
     * @param s Message to log as a warning.
     */
    public void logWarning(String s) {
        logWarning(s, null);
    }

    /**
     * Logs a warning message is warning logging is enabled.
     * Logs the stack trace of the exception if debug logging is enabled.
     * @param s Message to log as a warning.
     * @param throwable Exception stack trace to log as a warning.
     */
    public void logWarning(String s, Throwable throwable) {
        if (logger == null) {
            return;
        }

        if (throwable != null && logger.isDebugEnabled()) {
            logger.warn(s, throwable);
        } else if (logger.isWarnEnabled()) {
            logger.warn(s);
        }
    }

    /**
     * Logs an informational message if info logging is enabled.
     * @param s Message to log as informational.
     */
    public void logInformational(String s) {
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(s);
        }
    }
}
