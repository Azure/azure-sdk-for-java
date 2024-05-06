// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.implementation.MessageFlux;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessageUtils;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLinkProcessor;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * A package-private consumer responsible for reading {@link ServiceBusMessage} from a specific Service Bus link.
 */
class ServiceBusAsyncConsumer implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusAsyncConsumer.class);
    private final boolean isV2;
    private final String linkName;
    private final ServiceBusReceiveLinkProcessor linkProcessor;
    private final MessageSerializer messageSerializer;
    private final Flux<ServiceBusReceivedMessage> processor;
    private final MessageFlux messageFlux;

    ServiceBusAsyncConsumer(String linkName, ServiceBusReceiveLinkProcessor linkProcessor,
        MessageSerializer messageSerializer, ReceiverOptions receiverOptions) {
        this.isV2 = false;
        this.linkName = linkName;
        this.linkProcessor = linkProcessor;
        this.messageFlux = null;
        this.messageSerializer = messageSerializer;
        this.processor = linkProcessor
            .map(message -> this.messageSerializer.deserialize(message, ServiceBusReceivedMessage.class));
    }

    ServiceBusAsyncConsumer(String linkName, MessageFlux messageFlux,
        MessageSerializer messageSerializer, ReceiverOptions receiverOptions, ServiceBusReceiverInstrumentation instrumentation) {
        this.isV2 = true;
        this.linkName = linkName;
        this.messageFlux = messageFlux;
        this.linkProcessor = null;
        this.messageSerializer = messageSerializer;

        // This ServiceBusAsyncConsumer is backing ServiceBusReceiverAsyncClient instance (client has instrumentation is enabled).
        final Flux<ServiceBusReceivedMessage> deserialize = messageFlux
            .map(message -> this.messageSerializer.deserialize(message, ServiceBusReceivedMessage.class));
        this.processor = TracingFluxOperator.create(deserialize, instrumentation);
    }

    /**
     * Gets the receive link name.
     *
     * @return The receive link name for the consumer.
     */
    String getLinkName() {
        return linkName;
    }

    /**
     * Begin consuming events until there are no longer any subscribers.
     *
     * @return A stream of events received from the partition.
     */
    Flux<ServiceBusReceivedMessage> receive() {
        return processor;
    }

    Mono<Void> updateDisposition(String lockToken, DispositionStatus dispositionStatus, String deadLetterReason,
        String deadLetterErrorDescription, Map<String, Object> propertiesToModify,
        ServiceBusTransactionContext transactionContext) {

        final DeliveryState deliveryState = MessageUtils.getDeliveryState(dispositionStatus, deadLetterReason,
            deadLetterErrorDescription, propertiesToModify, transactionContext);

        if (deliveryState == null) {
            return monoError(LOGGER,
                new IllegalArgumentException("'dispositionStatus' is not known. status: " + dispositionStatus));
        }
        if (isV2) {
            return messageFlux.updateDisposition(lockToken, deliveryState);
        } else {
            return linkProcessor.updateDisposition(lockToken, deliveryState);
        }
    }

    /**
     * Disposes of the consumer by closing the underlying connection to the service.
     */
    @Override
    public void close() {
        if (isV2) {
            return;
        }
        linkProcessor.dispose();
    }
}
