// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.EventPosition;
import reactor.core.publisher.Mono;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link
 * EventProcessor}. Calling {@link #buildEventProcessor()} constructs a new instance of {@link EventProcessor}.
 *
 * <p>
 * To create an instance of {@link EventProcessor} that processes events with user-provided callback, configure the
 * following fields:
 *
 * <ul>
 * <li><b>Consumer group name.</b></li>
 * <li><b>{@link EventHubAsyncClient}</b> - An asynchronous Event Hub client the {@link EventProcessor} will use for
 * consuming events.</li>
 * <li><b>{@link #processEvent(ProcessEventConsumer)}</b> - A callback for processing new events as they are received.</li>
 * <li><b>{@link PartitionManager}</b> - An instance of PartitionManager. To get started, you can pass an instance of
 * {@link InMemoryPartitionManager}. For production, choose an implementation that will store checkpoints and partition
 * ownership details to a durable store.</li>
 * </ul>
 *
 * <p>
 * <strong>Creating an {@link EventProcessor} using a {@link #processEvent(ProcessEventConsumer)} function</strong>
 * </p>
 * {@codesnippet com.azure.messaging.eventhubs.eventprocessorbuilder.processevent}
 *
 * <p>
 *  To create a more advanced {@link EventProcessor}, use the
 *  {@link #partitionProcessorFactory(PartitionProcessorFactory)} instead of {@link #processEvent(ProcessEventConsumer)}.
 *  The {@link PartitionProcessorFactory} provides the ability to control when checkpoints are updated, handle
 *  errors that might occur during event processing and perform partition-specific initialization of
 *  {@link PartitionProcessor}.
 * </p>
 *
 * <p><strong>Creating an {@link EventProcessor} using {@link PartitionProcessorFactory}</strong></p>
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
    private ProcessEventConsumer processEvent;

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
     * Sets the callback for processing new events as they are received from all partitions this {@link EventProcessor}
     * is responsible for. Instance of a {@link CheckpointManager} is also provided for updating checkpoints.
     *
     * @param processEvent A callback for processing new events as they are received.
     * @return The updated {@link EventProcessorBuilder} instance.
     */
    public EventProcessorBuilder processEvent(ProcessEventConsumer processEvent) {
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
     * Sets the initial event position, defaulting to {@link EventPosition#earliest()} if this property is not set.
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
     * @throws IllegalStateException if either one of {@link #processEvent(ProcessEventConsumer)} or {@link
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
                try {
                    processEvent.processEvent(eventData, partitionContext, checkpointManager);
                    return Mono.empty();
                } catch (Exception ex) {
                    return Mono.error(ex);
                }
            }
        };
    }
}
