// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.annotation.Immutable;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.models.BatchOptions;
import com.azure.messaging.eventhubs.models.SendOptions;

import java.io.Closeable;
import java.time.Duration;
import java.util.Objects;

/**
 * A producer responsible for transmitting {@link EventData} to a specific Event Hub, grouped together in batches.
 * Depending on the options specified at creation, the producer may be created to allow event data to be automatically
 * routed to an available partition or specific to a partition.
 *
 * <p>
 * Allowing automatic routing of partitions is recommended when:
 * <ul>
 * <li>The sending of events needs to be highly available.</li>
 * <li>The event data should be evenly distributed among all available partitions.</li>
 * </ul>
 * </p>
 *
 * <p>
 * If no {@link SendOptions#getPartitionId() partitionId} is specified, the following rules are used for
 * automatically selecting one:
 *
 * <ol>
 * <li>Distribute the events equally amongst all available partitions using a round-robin approach.</li>
 * <li>If a partition becomes unavailable, the Event Hubs service will automatically detect it and forward the
 * message to another available partition.</li>
 * </ol>
 * </p>
 *
 * <p><strong>Create a producer that routes events to any partition</strong></p>
 * To allow automatic routing of messages to available partition, do not specify the {@link
 * SendOptions#getPartitionId() partitionId} when creating the {@link EventHubProducerClient}.
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubproducerclient.instantiation}
 *
 * <p><strong>Create a producer that publishes events to partition "foo" with a timeout of 45 seconds.</strong></p>
 * Developers can push events to a single partition by specifying the
 * {@link SendOptions#setPartitionId(String) partitionId} when creating an {@link EventHubProducerClient}.
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubproducerclient.instantiation#partitionId}
 *
 * <p><strong>Publish events to the same partition, grouped together using {@link SendOptions#setPartitionKey(String)}
 * </strong></p>
 * If developers want to push similar events to end up at the same partition, but do not require them to go to a
 * specific partition, they can use {@link SendOptions#setPartitionKey(String)}.
 *
 * <p>
 * In the sample below, all the "sandwiches" end up in the same partition, but it could end up in partition 0, 1, etc.
 * of the available partitions. All that matters to the end user is that they are grouped together.
 * </p>
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubproducerclient.send#publisher-sendOptions}
 *
 * <p><strong>Publish events using an {@link EventDataBatch}</strong></p>
 * Developers can create an {@link EventDataBatch}, add the events they want into it, and publish these events together.
 * When creating a {@link EventDataBatch batch}, developers can specify a set of {@link BatchOptions options} to
 * configure this batch.
 *
 * <p>
 * In the scenario below, the developer is creating a networked video game. They want to receive telemetry about their
 * users' gaming systems, but do not want to slow down the network with telemetry. So they limit the size of their
 * {@link EventDataBatch batches} to be no larger than 256 bytes. The events within the batch also get hashed to the
 * same partition because they all share the same {@link BatchOptions#getPartitionKey()}.
 * </p>
 * {@codesnippet com.azure.messaging.eventhubs.eventhubproducerclient.send#eventDataBatch}
 *
 * @see EventHubClient#createProducer()
 * @see EventHubProducerAsyncClient To asynchronously generate events to an Event Hub, see EventHubAsyncProducer.
 */
@Immutable
public class EventHubProducerClient implements Closeable {
    private final EventHubProducerAsyncClient producer;
    private final Duration tryTimeout;

    /**
     * Creates a new instance of {@link EventHubProducerClient} that sends messages to an Azure Event Hub.
     *
     * @throws NullPointerException if {@code producer} or {@code tryTimeout} is null.
     */
    EventHubProducerClient(EventHubProducerAsyncClient producer, Duration tryTimeout) {
        this.producer = Objects.requireNonNull(producer, "'producer' cannot be null.");
        this.tryTimeout = Objects.requireNonNull(tryTimeout, "'tryTimeout' cannot be null.");
    }

    /**
     * Gets the Event Hub name this client interacts with.
     *
     * @return The Event Hub name this client interacts with.
     */
    public String getEventHubName() {
        return producer.getEventHubName();
    }

    /**
     * Gets the fully qualified Event Hubs namespace that the connection is associated with. This is likely similar to
     * {@code {yournamespace}.servicebus.windows.net}.
     *
     * @return The fully qualified Event Hubs namespace that the connection is associated with
     */
    public String getFullyQualifiedNamespace() {
        return producer.getFullyQualifiedNamespace();
    }

    /**
     * Retrieves information about an Event Hub, including the number of partitions present and their identifiers.
     *
     * @return The set of information for the Event Hub that this client is associated with.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EventHubProperties getProperties() {
        return producer.getProperties().block(tryTimeout);
    }

    /**
     * Retrieves the identifiers for the partitions of an Event Hub.
     *
     * @return A Flux of identifiers for the partitions of an Event Hub.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public IterableStream<String> getPartitionIds() {
        return new IterableStream<>(producer.getPartitionIds());
    }

    /**
     * Retrieves information about a specific partition for an Event Hub, including elements that describe the available
     * events in the partition event stream.
     *
     * @param partitionId The unique identifier of a partition associated with the Event Hub.
     * @return The set of information for the requested partition under the Event Hub this client is associated with.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PartitionProperties getPartitionProperties(String partitionId) {
        return producer.getPartitionProperties(partitionId).block(tryTimeout);
    }

    /**
     * Creates an {@link EventDataBatch} that can fit as many events as the transport allows.
     *
     * @return A new {@link EventDataBatch} that can fit as many events as the transport allows.
     */
    public EventDataBatch createBatch() {
        return producer.createBatch().block(tryTimeout);
    }

    /**
     * Creates an {@link EventDataBatch} that can fit as many events as the transport allows.
     *
     * @param options A set of options used to configure the {@link EventDataBatch}.
     * @return A new {@link EventDataBatch} that can fit as many events as the transport allows.
     */
    public EventDataBatch createBatch(BatchOptions options) {
        return producer.createBatch(options).block(tryTimeout);
    }

    /**
     * Sends a single event to the associated Event Hub. If the size of the single event exceeds the maximum size
     * allowed, an exception will be triggered and the send will fail.
     *
     * <p>
     * For more information regarding the maximum event size allowed, see
     * <a href="https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-quotas">Azure Event Hubs Quotas and
     * Limits</a>.
     * </p>
     *
     * @param event Event to send to the service.
     */
    public void send(EventData event) {
        producer.send(event).block();
    }

    /**
     * Sends a single event to the associated Event Hub with the send options. If the size of the single event exceeds
     * the maximum size allowed, an exception will be triggered and the send will fail.
     *
     * <p>
     * For more information regarding the maximum event size allowed, see
     * <a href="https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-quotas">Azure Event Hubs Quotas and
     * Limits</a>.
     * </p>
     *
     * @param event Event to send to the service.
     * @param options The set of options to consider when sending this event.
     */
    public void send(EventData event, SendOptions options) {
        producer.send(event, options).block();
    }

    /**
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     *
     * <p>
     * For more information regarding the maximum event size allowed, see
     * <a href="https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-quotas">Azure Event Hubs Quotas and
     * Limits</a>.
     * </p>
     *
     * @param events Events to send to the service.
     */
    public void send(Iterable<EventData> events) {
        producer.send(events).block();
    }

    /**
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     *
     * <p>
     * For more information regarding the maximum event size allowed, see
     * <a href="https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-quotas">Azure Event Hubs Quotas and
     * Limits</a>.
     * </p>
     *
     * @param events Events to send to the service.
     * @param options The set of options to consider when sending this batch.
     */
    public void send(Iterable<EventData> events, SendOptions options) {
        producer.send(events, options).block();
    }

    /**
     * Sends the batch to the associated Event Hub.
     *
     * @param batch The batch to send to the service.
     * @throws NullPointerException if {@code batch} is {@code null}.
     * @see EventHubProducerClient#createBatch()
     * @see EventHubProducerClient#createBatch(BatchOptions)
     */
    public void send(EventDataBatch batch) {
        producer.send(batch).block();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        producer.close();
    }
}
