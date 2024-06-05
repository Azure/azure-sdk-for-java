// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;


import com.azure.core.util.logging.ClientLogger;

import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.FULLY_QUALIFIED_NAMESPACE_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.PUMP_ID_KEY;

/**
 * The exception emitted by the mono returned from the {@link MessagePump#begin()} and
 * {@link SessionsMessagePump#begin()} API. If available, the {@link MessagePumpTerminatedException#getCause()}}
 * indicates the cause for the termination of message pumping.
 */
final class MessagePumpTerminatedException extends RuntimeException {
    private final long pumpId;
    private final String fullyQualifiedNamespace;
    private final String entityPath;

    /**
     * Instantiate {@link MessagePumpTerminatedException} representing termination of a MessagePump.
     *
     * @param pumpId The unique identifier of the MessagePump that terminated.
     * @param fullyQualifiedNamespace The fully qualified Service Bus namespace hosting the messaging entity.
     * @param entityPath The path to the messaging entity within the Service Bus namespace streaming message.
     * @param detectedAt debug-string indicating where in the async chain the termination occurred or identified.
     */
    MessagePumpTerminatedException(long pumpId, String fullyQualifiedNamespace, String entityPath, String detectedAt) {
        super(detectedAt);
        this.pumpId = pumpId;
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.entityPath = entityPath;
    }

    /**
     * Instantiate {@link MessagePumpTerminatedException} representing termination of a MessagePump.
     *
     * @param pumpId The unique identifier of the MessagePump that terminated.
     * @param fullyQualifiedNamespace The fully qualified Service Bus namespace hosting the messaging entity.
     * @param entityPath The path to the messaging entity within the Service Bus namespace streaming message.
     * @param detectedAt debug-string indicating where in the async chain the termination occurred or identified.
     * @param terminationCause The reason for the termination of the pump.
     */
    MessagePumpTerminatedException(long pumpId, String fullyQualifiedNamespace, String entityPath, String detectedAt, Throwable terminationCause) {
        super(detectedAt, terminationCause);
        this.pumpId = pumpId;
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.entityPath = entityPath;
    }

    /**
     * Instantiate {@link MessagePumpTerminatedException} that represents the case when the MessagePump terminates by running
     * into the completion.
     *
     * @param pumpId The unique identifier of the pump that terminated.
     * @param fullyQualifiedNamespace The fully qualified Service Bus namespace hosting the messaging entity.
     * @param entityPath The path to the messaging entity within the Service Bus namespace streaming message.
     * @return the {@link MessagePumpTerminatedException}.
     */
    static MessagePumpTerminatedException forCompletion(long pumpId, String fullyQualifiedNamespace, String entityPath) {
        return new MessagePumpTerminatedException(pumpId, fullyQualifiedNamespace, entityPath, "pumping#reached-completion");
    }

    /**
     * Gets the error context describing the reason for the termination of the pump.
     *
     * @return the error context.
     */
    ServiceBusErrorContext getErrorContext() {
        final Throwable cause = super.getCause();
        if (cause == null) {
            return null;
        } else if (cause instanceof ServiceBusException) {
            return new ServiceBusErrorContext(cause, fullyQualifiedNamespace, entityPath);
        } else {
            return new ServiceBusErrorContext(new ServiceBusException(cause, ServiceBusErrorSource.RECEIVE),
                fullyQualifiedNamespace, entityPath);
        }
    }

    /**
     * Logs the given {@code message} along with pump identifier, fully qualified Service Bus namespace and entity path.
     *
     * @param logger The logger.
     * @param message The message to log.
     * @param logError should this (TerminatedException) error be logged as well.
     */
    void log(ClientLogger logger, String message, boolean logError) {
        if (logError) {
            final MessagePumpTerminatedException error = this;
            logger.atInfo()
                .addKeyValue(PUMP_ID_KEY, pumpId)
                .addKeyValue(FULLY_QUALIFIED_NAMESPACE_KEY, fullyQualifiedNamespace)
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .log(message, error);
        } else {
            logger.atInfo()
                .addKeyValue(PUMP_ID_KEY, pumpId)
                .addKeyValue(FULLY_QUALIFIED_NAMESPACE_KEY, fullyQualifiedNamespace)
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .log(message);
        }
    }
}
