// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler2;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.monoError;

/**
 * Temporary type to support receive link handler in v1 ({@link ReceiveLinkHandler}) and v2
 * ({@link ReceiveLinkHandler2})
 * side by side.
 * <p>
 * ReceiveLinkHandler2 will become the ReceiveLinkHandler once the side by side support for v1 and v2 stack
 * is removed. At that point the type "ReceiveLinkHandlerWrapper" type will be removed and the Ctr will take
 * "ReceiveLinkHandler".
 * </p>
 * TODO (anu): remove the temporary type once v1's side by side support with v2 is no longer needed.
 */
public final class ReceiveLinkHandlerWrapper {
    private final boolean isV2;
    private final ReceiveLinkHandler receiveLinkHandler;
    private final ReceiveLinkHandler2 receiveLinkHandler2;
    private ClientLogger logger;

    /**
     * Creates an instance of {@link ReceiveLinkHandlerWrapper} that wraps {@link ReceiveLinkHandler}.
     *
     * @param receiveLinkHandler The receive link handler.
     */
    public ReceiveLinkHandlerWrapper(ReceiveLinkHandler receiveLinkHandler) {
        this.isV2 = false;
        this.receiveLinkHandler = receiveLinkHandler;
        this.receiveLinkHandler2 = null;
    }

    /**
     * Creates an instance of {@link ReceiveLinkHandlerWrapper} that wraps {@link ReceiveLinkHandler2}.
     *
     * @param receiveLinkHandler2 The receive link handler.
     */
    public ReceiveLinkHandlerWrapper(ReceiveLinkHandler2 receiveLinkHandler2) {
        this.isV2 = true;
        this.receiveLinkHandler = null;
        this.receiveLinkHandler2 = receiveLinkHandler2;
    }

    /**
     * Sets the logger.
     *
     * @param logger The logger.
     */
    public void setLogger(ClientLogger logger) {
        this.logger = logger;
    }

    /**
     * Whether the receive link handler is v2.
     *
     * @return Whether the receive link handler is v2.
     */
    public boolean isV2() {
        return this.isV2;
    }

    String getConnectionId() {
        return isV2 ? receiveLinkHandler2.getConnectionId() : receiveLinkHandler.getConnectionId();
    }

    /**
     * Gets the link name.
     *
     * @return The link name.
     */
    public String getLinkName() {
        return isV2 ? receiveLinkHandler2.getLinkName() : receiveLinkHandler.getLinkName();
    }

    /**
     * Gets the hostname.
     *
     * @return The hostname.
     */
    public String getHostname() {
        return isV2 ? receiveLinkHandler2.getHostname() : receiveLinkHandler.getHostname();
    }

    Flux<EndpointState> getEndpointStates() {
        if (isV2) {
            return receiveLinkHandler2.getEndpointStates();
        } else {
            return receiveLinkHandler.getEndpointStates();
        }
    }

    Flux<Delivery> getDeliveredMessagesV1() {
        if (isV2) {
            return fluxError(logger, unsupportedOperation("getDeliveredMessagesV1", "V2"));
        }
        return receiveLinkHandler.getDeliveredMessages();
    }

    Flux<Message> getDeliveredMessagesV2() {
        if (!isV2) {
            return fluxError(logger, unsupportedOperation("getDeliveredMessagesV2", "V1"));
        }
        return receiveLinkHandler2.getMessages();
    }

    Mono<Void> sendDisposition(String deliveryTag, DeliveryState deliveryState) {
        if (!isV2) {
            return monoError(logger, unsupportedOperation("updateDisposition", "V1"));
        }
        return receiveLinkHandler2.sendDisposition(deliveryTag, deliveryState);
    }

    Mono<Void> beginClose() {
        if (isV2) {
            return receiveLinkHandler2.preClose();
        } else {
            return Mono.empty();
        }
    }

    void close() {
        if (isV2) {
            receiveLinkHandler2.close();
        } else {
            receiveLinkHandler.close();
        }
    }

    private static RuntimeException unsupportedOperation(String operation, String unsupportedStack) {
        return new UnsupportedOperationException(
            "The " + operation + " is not needed or supported in " + unsupportedStack + ".");
    }
}
