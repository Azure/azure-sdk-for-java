// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.logging;

/**
 * Fluent logging API interface.
 */
public interface ServiceLoggerAPI {
    int DEFAULT_LOG_LEVEL = 2;

    int TRACE_LEVEL = 0;
    int DEBUG_LEVEL = 1;
    int INFO_LEVEL = 2;
    int WARN_LEVEL = 3;
    int ERROR_LEVEL = 4;
    int DISABLED_LEVEL = 5;

    default ServiceLoggerAPI asTrace() {
        return this;
    }

    default ServiceLoggerAPI asDebug() {
        return this;
    }

    default ServiceLoggerAPI asInformational() {
        return this;
    }

    default ServiceLoggerAPI asWarning() {
        return this;
    }

    default ServiceLoggerAPI asError() {
        return this;
    }

    /**
     * Format-able message to be logged, if an exception needs to be logged it must be the last argument.
     * @param format Format-able message.
     * @param args Arguments for the message, if an exception is being logged last argument is the throwable.
     */
    default void log(String format, Object... args) {
    }

    /**
     * Logger that doesn't perform any logging.
     */
    class NoopServiceLogger implements ServiceLoggerAPI {
    }
}
