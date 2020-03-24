// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.CreateBatchOptions;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.time.Duration;
import java.util.Objects;

public class ServiceBusSenderClient implements Closeable {
    private final ServiceBusSenderAsyncClient asyncClient;
    private final Duration tryTimeout;

    /**
     * Creates a new instance of {@link ServiceBusSenderClient} that sends messages to an Azure Service Bus.
     *
     * @throws NullPointerException if {@code asyncClient} or {@code tryTimeout} is null.
     */
    ServiceBusSenderClient(ServiceBusSenderAsyncClient asyncClient,  Duration tryTimeout) {
        this.asyncClient = Objects.requireNonNull(asyncClient, "'asyncClient' cannot be null.");
        this.tryTimeout = Objects.requireNonNull(tryTimeout, "'tryTimeout' cannot be null.");
    }

    /**
     * Gets the name of the Service Bus resource.
     *
     * @return The name of the Service Bus resource.
     */
    public String getEntityPath() {
        return asyncClient.getEntityNamePath();
    }

    /**
     * Gets the fully qualified namespace.
     *
     * @return The fully qualified namespace.
     */
    public String getFullyQualifiedNamespace() {
        return asyncClient.getFullyQualifiedNamespace();
    }

    /**
     * Sends a message to a Service Bus queue or topic.
     *
     * @param message Message to be sent to Service Bus queue or topic.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     *
     * @throws NullPointerException if {@code message} is {@code null}.
     */
    public void send(ServiceBusMessage message) {
        Objects.requireNonNull(message, "'message' cannot be null.");
        asyncClient.send(message).block(tryTimeout);
    }

    /**
     * Creates a {@link ServiceBusMessageBatch} that can fit as many messages as the transport allows.
     *
     * @return A {@link ServiceBusMessageBatch} that can fit as many messages as the transport allows.
     */
    public ServiceBusMessageBatch createBatch() {
        return asyncClient.createBatch().block(tryTimeout);
    }

    /**
     * Creates an {@link ServiceBusMessageBatch} configured with the options specified.
     *
     * @param options A set of options used to configure the {@link ServiceBusMessageBatch}.
     * @return A new {@link ServiceBusMessageBatch} configured with the given options.
     * @throws NullPointerException if {@code options} is null.
     */
    public ServiceBusMessageBatch createBatch(CreateBatchOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        return asyncClient.createBatch(options).block(tryTimeout);
    }

    /**
     * Sends a message batch to the Azure Service Bus entity this sender is connected to.
     *
     * @param batch of messages which allows client to send maximum allowed size for a batch of messages.
     *
     * @return A {@link Mono} the finishes this operation on service bus resource.
     *
     * @throws NullPointerException if {@code batch} is {@code null}.
     */
    public void send(ServiceBusMessageBatch batch) {
        Objects.requireNonNull(batch, "'batch' cannot be null.");
        asyncClient.send(batch).block(tryTimeout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        asyncClient.close();
    }
}
