// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;


import com.azure.core.util.logging.ClientLogger;

import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.FULLY_QUALIFIED_NAMESPACE_KEY;

/**
 * The exception emitted by the mono returned from the MessagePump#begin() API.
 * If available, the {@link TerminatedException#getCause()}} indicates the cause for the termination of message pumping.
 */
final class TerminatedException extends RuntimeException {
    private static final String PUMP_ID_KEY = "pump-id";
    private final long pumpId;
    private final String fqdn;
    private final String entityPath;

    /**
     * Instantiate {@link TerminatedException} representing termination of a MessagePump.
     *
     * @param pumpId The unique identifier of the MessagePump that terminated.
     * @param fqdn The FQDN of the host from which the message was pumping.
     * @param entityPath The path to the entity within the FQDN streaming message.
     * @param detectedAt debug-string indicating where in the async chain the termination occurred or identified.
     */
    TerminatedException(long pumpId, String fqdn, String entityPath, String detectedAt) {
        super(detectedAt);
        this.pumpId = pumpId;
        this.fqdn = fqdn;
        this.entityPath = entityPath;
    }

    /**
     * Instantiate {@link TerminatedException} representing termination of a MessagePump.
     *
     * @param pumpId The unique identifier of the MessagePump that terminated.
     * @param fqdn The FQDN of the host from which the message was pumping.
     * @param entityPath The path to the entity within the FQDN streaming message.
     * @param detectedAt debug-string indicating where in the async chain the termination occurred or identified.
     * @param terminationCause The reason for the termination of the pump.
     */
    TerminatedException(long pumpId, String fqdn, String entityPath, String detectedAt, Throwable terminationCause) {
        super(detectedAt, terminationCause);
        this.pumpId = pumpId;
        this.fqdn = fqdn;
        this.entityPath = entityPath;
    }

    /**
     * Instantiate {@link TerminatedException} that represents the case when the MessagePump terminates by running
     * into the completion.
     *
     * @param pumpId The unique identifier of the pump that terminated.
     * @param fqdn The FQDN of the host from which the message was pumping.
     * @param entityPath The path to the entity within the FQDN streaming message.
     * @return the {@link TerminatedException}.
     */
    static TerminatedException forCompletion(long pumpId, String fqdn, String entityPath) {
        return new TerminatedException(pumpId, fqdn, entityPath, "pumping#reached-completion");
    }

    /**
     * Logs the given {@code message} along with pump identifier, FQDN and entity path.
     *
     * @param logger The logger.
     * @param message The message to log.
     * @param logError should this (TerminatedException) error be logged as well.
     */
    void log(ClientLogger logger, String message, boolean logError) {
        if (logError) {
            final TerminatedException error = this;
            logger.atInfo()
                .addKeyValue(PUMP_ID_KEY, pumpId)
                .addKeyValue(FULLY_QUALIFIED_NAMESPACE_KEY, fqdn)
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .log(message, error);
        } else {
            logger.atInfo()
                .addKeyValue(PUMP_ID_KEY, pumpId)
                .addKeyValue(FULLY_QUALIFIED_NAMESPACE_KEY, fqdn)
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .log(message);
        }
    }
}
