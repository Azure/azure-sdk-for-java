// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.PartitionProcessor;
import com.azure.messaging.eventhubs.models.EventPosition;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * EventProcessorClient provides a convenient mechanism to consume events from all partitions of an Event Hub in the
 * context of a consumer group. Event Processor-based application consists of one or more instances of
 * EventProcessorClient(s) which are set up to consume events from the same Event Hub, consumer group to balance the
 * workload across different instances and track progress when events are processed. Based on the number of instances
 * running, each EventProcessorClient may own zero or more partitions to balance the workload among all the instances.
 *
 * <p>To create an instance of EventProcessorClient, use the fluent {@link EventProcessorClientBuilder}.</p>
 *
 * @see EventProcessorClientBuilder
 */
@ServiceClient(builder = EventProcessorClientBuilder.class)
public class EventProcessorClient {

    private static final long INTERVAL_IN_SECONDS = 10; // run the load balancer every 10 seconds
    private static final long BASE_JITTER_IN_SECONDS = 2; // the initial delay jitter before starting the processor
    private final ClientLogger logger = new ClientLogger(EventProcessorClient.class);

    private final String identifier;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final PartitionPumpManager partitionPumpManager;
    private final PartitionBasedLoadBalancer partitionBasedLoadBalancer;
    private final CheckpointStore checkpointStore;

    private final AtomicReference<Disposable> runner = new AtomicReference<>();
    private final AtomicReference<Scheduler> scheduler = new AtomicReference<>();

    /**
     * Package-private constructor. Use {@link EventHubClientBuilder} to create an instance.
     *
     * @param eventHubClientBuilder The {@link EventHubClientBuilder}.
     * @param consumerGroup The consumer group name used in this event processor to consumer events.
     * @param partitionProcessorFactory The factory to create new partition processor(s).
     * @param initialEventPosition Initial event position to start consuming events.
     * @param checkpointStore The store used for reading and updating partition ownership and checkpoints. information.
     * @param trackLastEnqueuedEventProperties If set to {@code true}, all events received by this
     * EventProcessorClient will also include the last enqueued event properties for it's respective partitions.
     * @param tracerProvider The tracer implementation.
     */
    EventProcessorClient(EventHubClientBuilder eventHubClientBuilder, String consumerGroup,
        Supplier<PartitionProcessor> partitionProcessorFactory, EventPosition initialEventPosition,
        CheckpointStore checkpointStore, boolean trackLastEnqueuedEventProperties, TracerProvider tracerProvider) {

        Objects.requireNonNull(eventHubClientBuilder, "eventHubClientBuilder cannot be null.");
        Objects.requireNonNull(consumerGroup, "consumerGroup cannot be null.");
        Objects.requireNonNull(partitionProcessorFactory, "partitionProcessorFactory cannot be null.");
        Objects.requireNonNull(initialEventPosition, "initialEventPosition cannot be null.");

        this.checkpointStore = Objects.requireNonNull(checkpointStore, "checkpointStore cannot be null");
        this.identifier = UUID.randomUUID().toString();
        logger.info("The instance ID for this event processors is {}", this.identifier);
        this.partitionPumpManager = new PartitionPumpManager(checkpointStore, partitionProcessorFactory,
            initialEventPosition, eventHubClientBuilder, trackLastEnqueuedEventProperties, tracerProvider);
        EventHubAsyncClient eventHubAsyncClient = eventHubClientBuilder.buildAsyncClient();
        this.partitionBasedLoadBalancer =
            new PartitionBasedLoadBalancer(this.checkpointStore, eventHubAsyncClient,
                eventHubAsyncClient.getFullyQualifiedNamespace().toLowerCase(Locale.ROOT),
                eventHubAsyncClient.getEventHubName().toLowerCase(Locale.ROOT),
                consumerGroup.toLowerCase(Locale.ROOT), identifier, TimeUnit.MINUTES.toSeconds(1),
                partitionPumpManager);
    }

    /**
     * The identifier is a unique name given to this event processor instance.
     *
     * @return Identifier for this event processor.
     */
    public String getIdentifier() {
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
     * {@codesnippet com.azure.messaging.eventhubs.eventprocessorclient.startstop}
     */
    public synchronized void start() {
        if (!started.compareAndSet(false, true)) {
            logger.info("Event processor is already running");
            return;
        }
        logger.info("Starting a new event processor instance with id {}", this.identifier);
        scheduler.set(Schedulers.newElastic("EventProcessor"));
        Double jitterInMillis =
            ThreadLocalRandom.current().nextDouble() * TimeUnit.SECONDS.toMillis(BASE_JITTER_IN_SECONDS);
        // Add a bit of jitter to initialDelay to minimize contention if multiple EventProcessors start at the same time
        runner.set(scheduler.get().schedulePeriodically(partitionBasedLoadBalancer::loadBalance,
            jitterInMillis.longValue(), TimeUnit.SECONDS.toMillis(INTERVAL_IN_SECONDS), TimeUnit.MILLISECONDS));
    }

    /**
     * Stops processing events for all partitions owned by this event processor. All {@link PartitionProcessor} will be
     * shutdown and any open resources will be closed.
     * <p>
     * Subsequent calls to stop will be ignored if the event processor is not running.
     * </p>
     *
     * <p><strong>Stopping the processor</strong></p>
     * {@codesnippet com.azure.messaging.eventhubs.eventprocessorclient.startstop}
     */
    public synchronized void stop() {
        if (!started.compareAndSet(true, false)) {
            logger.info("Event processor has already stopped");
            return;
        }
        runner.get().dispose();
        scheduler.get().dispose();
        this.partitionPumpManager.stopAllPartitionPumps();
    }
}
