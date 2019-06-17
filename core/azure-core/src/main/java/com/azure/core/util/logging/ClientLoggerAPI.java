// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.logging;

/**
 * Fluent logging API interface.
 */
public interface ClientLoggerAPI {
    int DEFAULT_LOG_LEVEL = 2;

    int TRACE_LEVEL = 0;
    int VERBOSE_LEVEL = 1;
    int INFORMATIONAL_LEVEL = 2;
    int WARNING_LEVEL = 3;
    int ERROR_LEVEL = 4;
    int DISABLED_LEVEL = 5;

    /**
     * To work with verbose logger.
     * @return The verbose logger.
     */
    default ClientLoggerAPI asVerbose() {
        return this;
    }

    /**
     * To work with logger at info level.
     * @return The verbose logger.
     */
    default ClientLoggerAPI asInfo() {
        return this;
    }

    /**
     * To work with logger at warning level.
     * @return The verbose logger.
     */
    default ClientLoggerAPI asWarning() {
        return this;
    }

    /**
     * To work with logger at error level.
     * @return The verbose logger.
     */
    default ClientLoggerAPI asError() {
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
    class NoopServiceLogger implements ClientLoggerAPI {
    }
}
