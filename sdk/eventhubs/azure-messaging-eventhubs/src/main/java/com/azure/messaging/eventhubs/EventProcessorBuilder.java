// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionContext;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link
 * EventProcessor}. Calling {@link #buildEventProcessor()} constructs a new instance of {@link EventProcessor}.
 *
 * <p>
 * To create an instance of {@link EventProcessor} that processes events with user-provided callback, configure the
 * following fields:
 *
 * <ul>
 * <li>{@link #consumerGroup(String) Consumer group name}.</li>
 * <li>{@link EventHubAsyncClient} - An asynchronous Event Hub client the {@link EventProcessor} will use for
 * consuming events.</li>
 * <li>{@link PartitionManager} - An instance of PartitionManager. To get started, you can pass an instance of
 * {@link InMemoryPartitionManager}. For production, choose an implementation that will store checkpoints and partition
 * ownership details to a durable store.</li>
 * <li>{@link #partitionProcessorFactory(Supplier) partitionProcessorFactory} - A user-defined {@link Function} that
 * creates
 * new instances of {@link PartitionProcessor} for processing events. Users should extend from
 * {@link PartitionProcessor} abstract class to implement
 * {@link PartitionProcessor#processEvent(PartitionContext, EventData)}.</li>
 * </ul>
 *
 * <p><strong>Creating an {@link EventProcessor}</strong></p>
 * {@codesnippet com.azure.messaging.eventhubs.eventprocessorbuilder.instantiation}
 *
 * @see EventProcessor
 * @see EventHubConsumer
 */
public class EventProcessorBuilder {

    private final ClientLogger logger = new ClientLogger(EventProcessorBuilder.class);

    private Supplier<PartitionProcessor> partitionProcessorFactory;
    private String consumerGroup;
    private PartitionManager partitionManager;
    private EventHubAsyncClient eventHubAsyncClient;

    /**
     * Sets the Event Hub client the {@link EventProcessor} will use to connect to the Event Hub for consuming events.
     *
     * @param eventHubAsyncClient The Event Hub asynchronous client for consuming events.
     * @return The updated {@link EventProcessorBuilder} instance.
     */
    public EventProcessorBuilder eventHubClient(EventHubAsyncClient eventHubAsyncClient) {
        this.eventHubAsyncClient = eventHubAsyncClient;
        return this;
    }

    /**
     * Sets the consumer group name from which the {@link EventProcessor} should consume events.
     *
     * @param consumerGroup The consumer group name this {@link EventProcessor} should consume events.
     * @return The updated {@link EventProcessorBuilder} instance.
     */
    public EventProcessorBuilder consumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
        return this;
    }

    /**
     * Sets the {@link PartitionManager} the {@link EventProcessor} will use for storing partition ownership and
     * checkpoint information.
     *
     * <p>
     * Users can, optionally, provide their own implementation of {@link PartitionManager} which will store ownership
     * and checkpoint information. If not provided, the {@link EventProcessor} will first check for any available
     * implementation of {@link PartitionManager} in the classpath and fallback to {@link InMemoryPartitionManager} if
     * none found.
     * </p>
     *
     * @param partitionManager Implementation of {@link PartitionManager}.
     * @return The updated {@link EventProcessorBuilder} instance.
     */
    public EventProcessorBuilder partitionManager(PartitionManager partitionManager) {
        this.partitionManager = partitionManager;
        return this;
    }

    /**
     * Sets the partition processor factory for creating new instance(s) of {@link PartitionProcessor}.
     * <p>
     * Use this to have finer control over processing events from partitions. This will also allow you to control the
     * frequency of checkpointing.
     * </p>
     *
     * @param partitionProcessorFactory The factory that creates new {@link PartitionProcessor} for each partition.
     * @return The updated {@link EventProcessorBuilder} instance.
     */
    public EventProcessorBuilder partitionProcessorFactory(Supplier<PartitionProcessor> partitionProcessorFactory) {
        this.partitionProcessorFactory = partitionProcessorFactory;
        return this;
    }

    /**
     * This will create a new {@link EventProcessor} configured with the options set in this builder. Each call to this
     * method will return a new instance of {@link EventProcessor}.
     *
     * <p>
     * All partitions processed by this {@link EventProcessor} will start processing from {@link
     * EventPosition#earliest() earliest} available event in the respective partitions.
     * </p>
     *
     * @return A new instance of {@link EventProcessor}.
     */
    public EventProcessor buildEventProcessor() {
        final TracerProvider tracerProvider = new TracerProvider(ServiceLoader.load(Tracer.class));
        return new EventProcessor(eventHubAsyncClient, this.consumerGroup,
            this.partitionProcessorFactory, EventPosition.earliest(), partitionManager, tracerProvider);
    }
}
