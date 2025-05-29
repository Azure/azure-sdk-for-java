// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation.logging;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.instrumentation.InstrumentationContext;

import java.util.function.BiFunction;
import java.util.function.Function;

import static io.clientcore.core.annotations.MetadataProperties.FLUENT;

/**
 * This class is used to create a logging event for exception with contextual
 * information that's consistently populated in exception and log message.
 */
@Metadata(properties = FLUENT)
public class ExceptionLoggingEvent {
    private final LoggingEvent log;

    ExceptionLoggingEvent(LoggingEvent log) {
        this.log = log;
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
    public ExceptionLoggingEvent addKeyValue(String key, String value) {
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
    public ExceptionLoggingEvent addKeyValue(String key, boolean value) {
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
    public ExceptionLoggingEvent addKeyValue(String key, long value) {
        log.addKeyValue(key, value);
        return this;
    }

    /**
     * Adds a key with an Object value to the context of current log
     * and exception being created.
     *
     * @param key Key to associate the provided {@code value} with.
     * @param value The object value.
     * @return The updated {@link ExceptionLoggingEvent} object.
     * @see LoggingEvent#addKeyValue(String, Object)
     */
    public ExceptionLoggingEvent addKeyValue(String key, Object value) {
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
    public ExceptionLoggingEvent setInstrumentationContext(InstrumentationContext context) {
        log.setInstrumentationContext(context);
        return this;
    }

    /**
     * Logs the exception with the provided short message.
     * The full exception message is enriched with the context of the log event.
     *
     * @param throwableFactory Factory method to create the exception using message augmented with additional context
     *                         and the cause of the exception.
     * @param <T> The type of the exception to be created.
     * @return The created exception.
     */
    public <T extends Throwable> T log(Function<String, T> throwableFactory) {
        return logImpl(null, throwableFactory.apply(log.getExceptionMessageWithContext(null, null)));
    }

    /**
     * Logs the exception with the provided short message.
     * The full exception message is enriched with the context of the log event.
     *
     * @param shortMessage The short message to log.
     * @param throwableFactory Factory method to create the exception using message augmented with additional context
     *                         and the cause of the exception.
     * @param <T> The type of the exception to be created.
     * @return The created exception.
     */
    public <T extends Throwable> T log(String shortMessage, Function<String, T> throwableFactory) {
        return logImpl(shortMessage, throwableFactory.apply(log.getExceptionMessageWithContext(shortMessage, null)));
    }

    /**
     * Logs the exception with the provided cause.
     * The full exception message is enriched with the context of the log event.
     *
     * @param cause The cause of the exception.
     * @param throwableFactory Factory method to create the exception using message augmented with additional context
     *                         and the cause of the exception.
     * @param <T> The type of the exception to be created.
     * @return The created exception.
     */
    public <T extends Throwable> T log(Throwable cause, BiFunction<String, Throwable, T> throwableFactory) {
        return logImpl(null,
            throwableFactory.apply(log.getExceptionMessageWithContext(cause.getMessage(), cause), cause));
    }

    /**
     * Logs the exception with the provided short message and cause.
     * The full exception message is enriched with the context of the log event.
     *
     * @param shortMessage The short message to log.
     * @param cause The cause of the exception.
     * @param throwableFactory Factory method to create the exception using message augmented with additional context
     *                         and the cause of the exception.
     * @param <T> The type of the exception to be created.
     * @return The created exception.
     */
    public <T extends Throwable> T log(String shortMessage, Throwable cause,
        BiFunction<String, Throwable, T> throwableFactory) {
        return logImpl(shortMessage,
            throwableFactory.apply(log.getExceptionMessageWithContext(shortMessage, cause), cause));
    }

    private <T extends Throwable> T logImpl(String shortMessage, T exception) {
        // not recording full exception message on logs since it contains context
        log.recordExceptionDetails(exception, null);
        log.log(shortMessage);
        return exception;
    }
}
