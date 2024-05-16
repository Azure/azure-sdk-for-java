// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.util.logging.LoggingEventBuilder;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.amqp.implementation.ClientConstants.CONNECTION_ID_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.EMIT_RESULT_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.ERROR_CONDITION_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.ERROR_DESCRIPTION_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.SIGNAL_TYPE_KEY;

/**
 * Utils for contextual logging.
 */
public final class AmqpLoggingUtils {

    private AmqpLoggingUtils() {
    }

    /**
     * Creates logging context with connectionId.
     *
     * @param connectionId connectionId to be added to the context.
     * @return logging context with connectionId.
     */
    public static Map<String, Object> createContextWithConnectionId(String connectionId) {
        Objects.requireNonNull(connectionId, "'connectionId' cannot be null.");
        // caller should be able to add more context, please keep the map mutable.
        Map<String, Object> globalLoggingContext = new HashMap<>(1);
        globalLoggingContext.put(CONNECTION_ID_KEY, connectionId);

        return globalLoggingContext;
    }

    /**
     * Adds {@link SignalType} under {@code signalType} key and {@link reactor.core.publisher.Sinks.EmitResult}
     * under {@code emitResult} key to the {@link LoggingEventBuilder}
     *
     * @param logBuilder {@link LoggingEventBuilder} to add the properties to.
     * @param signalType {@link SignalType} to be added.
     * @param result {@link Sinks.EmitResult} to be added.
     * @return updated {@link LoggingEventBuilder} for chaining.
     */
    public static LoggingEventBuilder addSignalTypeAndResult(LoggingEventBuilder logBuilder, SignalType signalType,
        Sinks.EmitResult result) {
        return logBuilder.addKeyValue(SIGNAL_TYPE_KEY, signalType).addKeyValue(EMIT_RESULT_KEY, result);
    }

    /**
     * Adds {@link ErrorCondition} to the {@link LoggingEventBuilder}. Writes the {@code getCondition()} under
     * {@code errorCondition} key and {@code getDescription()} under {@code errorDescription} keys.
     * <p>
     * If errorCondition is {@code null} does not add properties.
     *
     * @param logBuilder {@link LoggingEventBuilder} to add the properties to.
     * @param errorCondition {@link ErrorCondition} to be added.
     * @return updated {@link LoggingEventBuilder} for chaining.
     */
    public static LoggingEventBuilder addErrorCondition(LoggingEventBuilder logBuilder, ErrorCondition errorCondition) {
        if (errorCondition != null) {
            if (errorCondition.getCondition() != null) {
                logBuilder.addKeyValue(ERROR_CONDITION_KEY, errorCondition.getCondition());
            }

            if (errorCondition.getDescription() != null) {
                logBuilder.addKeyValue(ERROR_DESCRIPTION_KEY, errorCondition.getDescription());
            }
        }

        return logBuilder;
    }

    /**
     * Adds {@code key} and {@code value} to the {@link LoggingEventBuilder} if value is not null.
     *
     * @param logBuilder {@link LoggingEventBuilder} to add the properties to.
     * @param key key to be added.
     * @param value value to be added.
     * @return updated {@link LoggingEventBuilder} for chaining.
     */
    public static LoggingEventBuilder addKeyValueIfNotNull(LoggingEventBuilder logBuilder, String key, String value) {
        if (value != null) {
            logBuilder.addKeyValue(key, value);
        }

        return logBuilder;
    }

    /**
     * Adds {@link AmqpShutdownSignal} to the {@link LoggingEventBuilder}. Writes
     * <ul>
     *     <li>{@code isTransient()} under {@code isTransient}</li>
     *     <li>{@code isInitiatedByClient()} under {@code isInitiatedByClient}</li>
     *     <li>{@code toString()} under {@code shutdownMessage}</Li>
     * </ul>
     *
     * @param logBuilder {@link LoggingEventBuilder} to add the properties to.
     * @param shutdownSignal {@link AmqpShutdownSignal} to be added.
     * @return updated {@link LoggingEventBuilder} for chaining.
     */
    public static LoggingEventBuilder addShutdownSignal(LoggingEventBuilder logBuilder,
        AmqpShutdownSignal shutdownSignal) {
        return logBuilder.addKeyValue("isTransient", shutdownSignal.isTransient())
            .addKeyValue("isInitiatedByClient", shutdownSignal.isInitiatedByClient())
            // will call toString() when performing logging (if enabled)
            .addKeyValue("shutdownMessage", shutdownSignal);
    }
}
