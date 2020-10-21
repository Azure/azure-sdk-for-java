// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.LockContainer;
import com.azure.messaging.servicebus.implementation.MessageUtils;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLinkProcessor;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * A package-private consumer responsible for reading {@link ServiceBusMessage} from a specific Service Bus link.
 */
class ServiceBusAsyncConsumer implements AutoCloseable {
    private final ClientLogger logger = new ClientLogger(ServiceBusAsyncConsumer.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final String linkName;
    private final ServiceBusReceiveLinkProcessor linkProcessor;
    private final MessageSerializer messageSerializer;
    private final Flux<ServiceBusReceivedMessage> messageSource;

    ServiceBusAsyncConsumer(String linkName, ServiceBusReceiveLinkProcessor linkProcessor,
        MessageSerializer messageSerializer, ReceiverOptions receiverOptions,
        LockContainer<LockRenewalOperation> messageLockContainer, Function<String, Mono<OffsetDateTime>> onRenewLock) {
        this.linkName = linkName;
        this.linkProcessor = linkProcessor;
        this.messageSerializer = messageSerializer;

        Flux<ServiceBusReceivedMessage> source = linkProcessor
            .map(message -> this.messageSerializer.deserialize(message, ServiceBusReceivedMessage.class))
            .publish(receiverOptions.getPrefetchCount())
            .autoConnect(1);

        if (receiverOptions.isAutoLockRenewEnabled()) {
            this.messageSource = new FluxAutoLockRenew(source, receiverOptions.getMaxLockRenewDuration(),
                messageLockContainer, onRenewLock);
        } else {
            this.messageSource = source;
        }

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
        return messageSource;
    }

    Mono<Void> updateDisposition(String lockToken, DispositionStatus dispositionStatus, String deadLetterReason,
        String deadLetterErrorDescription, Map<String, Object> propertiesToModify,
        ServiceBusTransactionContext transactionContext) {

        final DeliveryState deliveryState = MessageUtils.getDeliveryState(dispositionStatus, deadLetterReason,
            deadLetterErrorDescription, propertiesToModify, transactionContext);

        if (deliveryState == null) {
            return monoError(logger,
                new IllegalArgumentException("'dispositionStatus' is not known. status: " + dispositionStatus));
        }
        return linkProcessor.updateDisposition(lockToken, deliveryState);
    }

    /**
     * Disposes of the consumer by closing the underlying connection to the service.
     */
    @Override
    public void close() {
        if (!isDisposed.getAndSet(true)) {
            linkProcessor.dispose();
        }
    }
}
