// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.EventPosition;
import java.util.function.Function;
import reactor.core.publisher.Mono;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link
 * EventProcessor}. Calling {@link #buildEventProcessor()} constructs a new instance of {@link EventProcessor}.
 *
 * <p>
 * To create an instance of {@link EventProcessor} that processes events with user-provided lambda function and
 * checkpoints after successfully processing every event, configure the following fields:
 *
 * <ul>
 * <li>Consumer group name.</li>
 * <li>{@link EventHubAsyncClient} - An asynchronous Event Hub client the {@link EventProcessor} will use for
 * consuming events.</li>
 * <li>{@link #processEvent(Function)} - A lambda function that will be called when new events are received.</li>
 * </ul>
 *
 * <p>
 * <strong>Creating an {@link EventProcessor} using a {@link #processEvent(Function)} function</strong>
 * </p>
 * {@codesnippet com.azure.messaging.eventhubs.eventprocessorbuilder.processevent}
 *
 * <p>
 *  To create a more advanced {@link EventProcessor} that allows you to control the creation of
 *  {@link PartitionProcessor}s and lets you control the frequency of checkpointing, configure the
 *  {@link PartitionProcessorFactory} instead of {@link #processEvent(Function)} lambda function.
 * </p>
 *
 * <p>
 * <strong>Creating an {@link EventProcessor} using {@link PartitionProcessorFactory}</strong>
 * </p>
 * {@codesnippet com.azure.messaging.eventhubs.eventprocessorbuilder.partitionprocessorfactory}
 *
 * @see EventProcessor
 * @see EventHubConsumer
 */
public class EventProcessorBuilder {
    private final ClientLogger logger = new ClientLogger(EventProcessorBuilder.class);

    private EventPosition initialEventPosition;
    private PartitionProcessorFactory partitionProcessorFactory;
    private String consumerGroup;
    private PartitionManager partitionManager;
    private EventHubAsyncClient eventHubAsyncClient;
    private Function<EventData, Mono<Void>> processEvent;

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
     * Sets the lambda function that will be used to process events from all partitions this {@link EventProcessor} is
     * responsible for.
     *
     * @param processEvent The lambda function that will be used to process events.
     * @return The updated {@link EventProcessorBuilder} instance.
     */
    public EventProcessorBuilder processEvent(Function<EventData, Mono<Void>> processEvent) {
        this.processEvent = processEvent;
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
    public EventProcessorBuilder partitionProcessorFactory(PartitionProcessorFactory partitionProcessorFactory) {
        this.partitionProcessorFactory = partitionProcessorFactory;
        return this;
    }

    /**
     * Sets the initial event position. If this property is not set and if checkpoint for a partition doesn't exist,
     * {@link EventPosition#earliest()} will be used as the initial event position to start consuming events.
     *
     * @param initialEventPosition The initial event position.
     * @return The updated {@link EventProcessorBuilder} instance.
     */
    public EventProcessorBuilder initialEventPosition(EventPosition initialEventPosition) {
        this.initialEventPosition = initialEventPosition;
        return this;
    }

    /**
     * This will create a new {@link EventProcessor} configured with the options set in this builder. Each call to this
     * method will return a new instance of {@link EventProcessor}.
     *
     * <p>
     * If the {@link #initialEventPosition(EventPosition) initial event position} is not set, all partitions processed
     * by this {@link EventProcessor} will start processing from {@link EventPosition#earliest() earliest} available
     * event in the respective partitions.
     * </p>
     *
     * @return A new instance of {@link EventProcessor}.
     * @throws IllegalStateException if either one of {@link #processEvent(Function)} or {@link
     * #partitionProcessorFactory(PartitionProcessorFactory)} is not set. This exception will also be thrown if both are
     * set.
     */
    public EventProcessor buildEventProcessor() {
        EventPosition initialEventPosition =
            this.initialEventPosition == null ? EventPosition.earliest()
                : this.initialEventPosition;
        if (processEvent == null && partitionProcessorFactory == null) {
            throw logger.logExceptionAsWarning(
                new IllegalStateException("Either processEvent function or the partitionProcessorFactory "
                    + "must be provided for creating an EventProcessor instance"));
        }

        if (processEvent != null && partitionProcessorFactory != null) {
            throw logger.logExceptionAsWarning(
                new IllegalStateException("Both processEvent function and partitionProcessorFactory cannot "
                    + "be set"));
        }

        if (partitionProcessorFactory == null) {
            partitionProcessorFactory = createPartitionProcessorFactory();
        }

        return new EventProcessor(eventHubAsyncClient, this.consumerGroup,
            this.partitionProcessorFactory, initialEventPosition, partitionManager);
    }

    private PartitionProcessorFactory createPartitionProcessorFactory() {
        return (partitionContext, checkpointManager) -> new PartitionProcessor(partitionContext, checkpointManager) {
            @Override
            public Mono<Void> processEvent(EventData eventData) {
                return processEvent.apply(eventData)
                    .flatMap(unused -> checkpointManager.updateCheckpoint(eventData));
            }
        };
    }
}
