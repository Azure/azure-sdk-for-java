// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation.logging;

import io.clientcore.core.instrumentation.InstrumentationContext;

import java.util.function.BiFunction;

/**
 * This class is used to create a logging event for exception with contextual
 * information that's consistently populated in exception and log message.
 *
 * @param <T> The type of the exception to be logged.
 */
public class ExceptionLoggingEvent<T extends Throwable> {
    private final LoggingEvent log;
    private final BiFunction<String, Throwable, T> throwableFactory;

    ExceptionLoggingEvent(LoggingEvent log, BiFunction<String, Throwable, T> throwableFactory) {
        this.log = log;
        this.throwableFactory = throwableFactory;
    }

    /**
     * Adds key with String value to the context of current log
     * and exception being created.
     *
     * @param key String key.
     * @param value String value.
     * @return The updated {@link ExceptionLoggingEvent} object.
     *
     * @see LoggingEvent#addKeyValue(String, String)
     */
    public ExceptionLoggingEvent<T> addKeyValue(String key, String value) {
        log.addKeyValue(key, value);

        return this;
    }

    /**
     * Adds a key with a boolean value to the context of current log
     * and exception being created.
     *
     * @param key Key to associate the provided {@code value} with.
     * @param value The boolean value.
     * @return The updated {@link ExceptionLoggingEvent} object.
     * @see LoggingEvent#addKeyValue(String, boolean)
     */
    public ExceptionLoggingEvent<T> addKeyValue(String key, boolean value) {
        log.addKeyValue(key, value);
        return this;
    }

    /**
     * Adds key with long value to the context of current log event being created.
     *
     * @param key Key to associate the provided {@code value} with.
     * @param value The long value.
     * @return The updated {@link ExceptionLoggingEvent} object.
     * @see LoggingEvent#addKeyValue(String, long)
     */
    public ExceptionLoggingEvent<T> addKeyValue(String key, long value) {
        log.addKeyValue(key, value);
        return this;
    }

    /**
     * Sets operation context on the log event being created.
     * It's used to correlate logs between each other and with other telemetry items.
     *
     * @param context operation context.
     * @return The updated {@link ExceptionLoggingEvent} object.
     * @see LoggingEvent#setInstrumentationContext
     */
    public ExceptionLoggingEvent<T> setInstrumentationContext(InstrumentationContext context) {
        log.setInstrumentationContext(context);
        return this;
    }

    /**
     * Logs the exception with the provided short message.
     * The full exception message is enriched with the context of the log event.
     *
     * @param shortMessage The short message to log.
     * @return The created exception.
     */
    public T log(String shortMessage) {
        return log(shortMessage, null);
    }

    /**
     * Logs the exception with the provided cause.
     * The full exception message is enriched with the context of the log event.
     *
     * @param cause The cause of the exception.
     * @return The created exception.
     */
    public T log(Throwable cause) {
        return log(null, cause);
    }

    /**
     * Logs the exception with the provided short message and cause.
     * The full exception message is enriched with the context of the log event.
     *
     * @param shortMessage The short message to log.
     * @param cause The cause of the exception.
     * @return The created exception.
     */
    public T log(String shortMessage, Throwable cause) {
        T ex = throwableFactory.apply(log.getExceptionMessageWithContext(shortMessage, cause), cause);
        log.recordExceptionDetails(ex, null);
        log.log(shortMessage);
        return ex;
    }
}
