// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.annotation.Immutable;
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
 * SendOptions#getPartitionId() partitionId} when creating the {@link EventHubProducer}.
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubproducer.instantiation}
 *
 * <p><strong>Create a producer that publishes events to partition "foo" with a timeout of 45 seconds.</strong></p>
 * Developers can push events to a single partition by specifying the
 * {@link SendOptions#setPartitionId(String) partitionId} when creating an {@link EventHubProducer}.
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubproducer.instantiation#partitionId}
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
 * {@codesnippet com.azure.messaging.eventhubs.eventhubproducer.send#publisher-sendOptions}
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
 * {@codesnippet com.azure.messaging.eventhubs.eventhubproducer.send#eventDataBatch}
 *
 * @see EventHubClient#createProducer()
 * @see EventHubAsyncProducerClient To asynchronously generate events to an Event Hub, see EventHubAsyncProducer.
 */
@Immutable
public class EventHubProducer implements Closeable {
    private final EventHubAsyncProducerClient producer;
    private final Duration tryTimeout;

    /**
     * Creates a new instance of {@link EventHubProducer} that sends messages to an Azure Event Hub.
     *
     * @throws NullPointerException if {@code producer} or {@code tryTimeout} is null.
     */
    EventHubProducer(EventHubAsyncProducerClient producer, Duration tryTimeout) {
        this.producer = Objects.requireNonNull(producer, "'producer' cannot be null.");
        this.tryTimeout = Objects.requireNonNull(tryTimeout, "'tryTimeout' cannot be null.");
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
     * @see EventHubProducer#createBatch()
     * @see EventHubProducer#createBatch(BatchOptions)
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
