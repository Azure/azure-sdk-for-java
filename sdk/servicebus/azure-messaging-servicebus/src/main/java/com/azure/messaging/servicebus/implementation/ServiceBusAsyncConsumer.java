// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A package-private consumer responsible for reading {@link ServiceBusMessage} from a specific Service Bus.
 */
public class ServiceBusAsyncConsumer implements AutoCloseable {
    private final ClientLogger logger = new ClientLogger(ServiceBusAsyncConsumer.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final ServiceBusReceiveLinkProcessor amqpReceiveLinkProcessor;
    private final MessageSerializer messageSerializer;
    private final String fullyQualifiedNamespace;
    private final String queueName;
    private final EmitterProcessor<ServiceBusMessage> emitterProcessor;

    public ServiceBusAsyncConsumer(ServiceBusReceiveLinkProcessor amqpReceiveLinkProcessor,
                            MessageSerializer messageSerializer, String fullyQualifiedNamespace, String queueName) {
        this.amqpReceiveLinkProcessor = amqpReceiveLinkProcessor;
        this.messageSerializer = messageSerializer;
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.queueName = queueName;

        this.emitterProcessor = amqpReceiveLinkProcessor
            .map(message -> onMessageReceived(message))
            .subscribeWith(EmitterProcessor.create(false));
    }

    /**
     * Disposes of the consumer by closing the underlying connection to the service.
     */
    @Override
    public void close() {
        if (!isDisposed.getAndSet(true)) {
            emitterProcessor.onComplete();
            amqpReceiveLinkProcessor.cancel();
        }
    }

    /**
     * Begin consuming events until there are no longer any subscribers.
     *
     * @return A stream of events received from the partition.
     */
    public Flux<ServiceBusMessage> receive() {
        return emitterProcessor;
    }

    /**
     * On each message received from the service, it will try to:
     * <ol>
     * <li>Deserialize the message into an {@link ServiceBusMessage}.</li>
     * </ol>
     *
     * @param message AMQP message to deserialize.
     *
     * @return The deserialized {@link ServiceBusMessage} with partition information.
     */
    private ServiceBusMessage onMessageReceived(org.apache.qpid.proton.message.Message message) {
        final ServiceBusMessage event = messageSerializer.deserialize(message, ServiceBusMessage.class);
        return new ServiceBusMessage(event.getBody(), event.getSystemProperties(), Context.NONE);
    }
}
