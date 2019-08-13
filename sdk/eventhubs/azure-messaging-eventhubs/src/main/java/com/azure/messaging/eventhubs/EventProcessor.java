// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.PartitionLoadBalancerStrategy;
import com.azure.messaging.eventhubs.models.EventPosition;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * This is the starting point for event processor.
 * <p>
 * Event Processor based application consists of one or more instances of {@link EventProcessor} which are set up to
 * consume events from the same Event Hub + consumer group and to balance the workload across different instances and
 * track progress when events are processed.
 * </p>
 *
 * <p><strong>Creating an {@link EventProcessor} instance using Event Hub instance connection
 * string</strong></p>
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventprocessor.instantiation}
 *
 * @see EventHubAsyncClient
 * @see EventHubClientBuilder
 */
public class EventProcessor {

    private static final long INTERVAL_IN_SECONDS = 10; // run every 10 seconds
    private static final long INITIAL_DELAY = 0; // start immediately
    private final ClientLogger logger = new ClientLogger(EventProcessor.class);

    private final EventHubAsyncClient eventHubAsyncClient;
    private final String consumerGroupName;
    private final EventPosition initialEventPosition;
    private final PartitionProcessorFactory partitionProcessorFactory;
    private final PartitionManager partitionManager;
    private final String identifier;
    private final String eventHubName;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private PartitionLoadBalancerStrategy partitionLoadBalancerStrategy;

    private Disposable runner;
    private Scheduler scheduler;

    /**
     * Package-private constructor. Use {@link EventHubClientBuilder} to create an instance.
     *
     * @param eventHubAsyncClient The {@link EventHubAsyncClient}.
     * @param consumerGroupName The consumer group name used in this event processor to consumer events.
     * @param partitionProcessorFactory The factory to create new partition processor(s).
     * @param initialEventPosition Initial event position to start consuming events.
     * @param partitionManager The partition manager.
     * @param eventHubName The Event Hub name.
     */
    EventProcessor(EventHubAsyncClient eventHubAsyncClient, String consumerGroupName,
        PartitionProcessorFactory partitionProcessorFactory, EventPosition initialEventPosition,
        PartitionManager partitionManager, String eventHubName) {
        this.eventHubAsyncClient = Objects
            .requireNonNull(eventHubAsyncClient, "eventHubAsyncClient cannot be null");
        this.consumerGroupName = Objects
            .requireNonNull(consumerGroupName, "consumerGroupname cannot be null");
        this.partitionProcessorFactory = Objects
            .requireNonNull(partitionProcessorFactory, "partitionProcessorFactory cannot be null");
        this.partitionManager = Objects
            .requireNonNull(partitionManager, "partitionManager cannot be null");
        this.initialEventPosition = Objects
            .requireNonNull(initialEventPosition, "initialEventPosition cannot be null");
        this.eventHubName = Objects
            .requireNonNull(eventHubName, "eventHubName cannot be null");
        this.identifier = UUID.randomUUID().toString();
        logger.info("The instance ID for this event processors is {}", this.identifier);
    }

    /**
     * The identifier is a unique name given to this event processor instance.
     *
     * @return Identifier for this event processor.
     */
    public String identifier() {
        return this.identifier;
    }

    /**
     * Starts processing of events for all partitions of the Event Hub that this event processor can own, assigning a
     * dedicated {@link PartitionProcessor} to each partition. If there are other Event Processors active for the same
     * consumer group on the Event Hub, responsibility for partitions will be shared between them.
     * <p>
     * Subsequent calls to start will be ignored if this event processor is already running. Calling start after {@link
     * #stop()} is called will restart this event processor.
     * </p>
     *
     * <p><strong>Starting the processor to consume events from all partitions</strong></p>
     * {@codesnippet com.azure.messaging.eventhubs.eventprocessor.startstop}
     */
    public synchronized void start() {
        if (!started.compareAndSet(false, true)) {
            logger.info("Event processor is already running");
            return;
        }
        logger.info("Starting a new event processor instance with id {}", this.identifier);
        scheduler = Schedulers.newElastic("EventProcessor");

        this.partitionLoadBalancerStrategy =
            new PartitionLoadBalancerStrategy(partitionManager, eventHubAsyncClient, eventHubName, consumerGroupName,
                identifier, partitionProcessorFactory, initialEventPosition, TimeUnit.MINUTES.toSeconds(5));

        runner = scheduler.schedulePeriodically(partitionLoadBalancerStrategy::runOnce, INITIAL_DELAY,
            INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Stops processing events for all partitions owned by this event processor. All {@link PartitionProcessor} will be
     * shutdown and any open resources will be closed.
     * <p>
     * Subsequent calls to stop will be ignored if the event processor is not running.
     * </p>
     *
     * <p><strong>Stopping the processor</strong></p>
     * {@codesnippet com.azure.messaging.eventhubs.eventprocessor.startstop}
     */
    public synchronized void stop() {
        if (!started.compareAndSet(true, false)) {
            logger.info("Event processor has already stopped");
            return;
        }

        this.partitionLoadBalancerStrategy.stopAllPartitionPumps();
        runner.dispose();
        scheduler.dispose();
    }
}
