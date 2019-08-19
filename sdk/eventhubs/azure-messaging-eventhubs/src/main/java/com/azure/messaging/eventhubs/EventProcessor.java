// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.PartitionBasedLoadBalancer;
import com.azure.messaging.eventhubs.implementation.PartitionPumpManager;
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

    private static final long INTERVAL_IN_SECONDS = 10; // run the load balancer every 10 seconds
    private static final long INITIAL_DELAY = 0; // start immediately
    private final ClientLogger logger = new ClientLogger(EventProcessor.class);

    private final String identifier;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final PartitionPumpManager partitionPumpManager;
    private final PartitionBasedLoadBalancer partitionBasedLoadBalancer;

    private Disposable runner;
    private Scheduler scheduler;

    /**
     * Package-private constructor. Use {@link EventHubClientBuilder} to create an instance.
     *
     * @param eventHubAsyncClient The {@link EventHubAsyncClient}.
     * @param consumerGroupName The consumer group name used in this event processor to consumer events.
     * @param partitionProcessorFactory The factory to create new partition processor(s).
     * @param initialEventPosition The event position to start processing events from a partition if no checkpoint is
     * available for the partition.
     * @param partitionManager The partition manager this Event Processor will use for reading and writing partition
     * ownership and checkpoint information.
     */
    EventProcessor(EventHubAsyncClient eventHubAsyncClient, String consumerGroupName,
        PartitionProcessorFactory partitionProcessorFactory, EventPosition initialEventPosition,
        PartitionManager partitionManager) {
        Objects.requireNonNull(eventHubAsyncClient, "eventHubAsyncClient cannot be null");
        Objects.requireNonNull(consumerGroupName, "consumerGroupName cannot be null");
        Objects.requireNonNull(partitionProcessorFactory, "partitionProcessorFactory cannot be null");
        Objects.requireNonNull(initialEventPosition, "initialEventPosition cannot be null");
        Objects.requireNonNull(partitionManager, "partitionManager cannot be null");

        this.identifier = UUID.randomUUID().toString();
        logger.info("The instance ID for this event processors is {}", this.identifier);
        this.partitionPumpManager = new PartitionPumpManager(partitionManager, partitionProcessorFactory,
            initialEventPosition, eventHubAsyncClient);
        this.partitionBasedLoadBalancer =
            new PartitionBasedLoadBalancer(partitionManager, eventHubAsyncClient, eventHubAsyncClient.eventHubName(),
                consumerGroupName, identifier, TimeUnit.MINUTES.toSeconds(5), partitionPumpManager);
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
        runner = scheduler.schedulePeriodically(partitionBasedLoadBalancer::loadBalance, INITIAL_DELAY,
            INTERVAL_IN_SECONDS /* TODO: make this configurable */, TimeUnit.SECONDS);
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
        runner.dispose();
        scheduler.dispose();
        this.partitionPumpManager.stopAllPartitionPumps();
    }
}
