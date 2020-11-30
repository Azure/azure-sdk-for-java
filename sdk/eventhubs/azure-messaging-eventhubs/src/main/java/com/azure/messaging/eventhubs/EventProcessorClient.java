// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.PartitionProcessor;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    private static final long BASE_JITTER_IN_SECONDS = 2; // the initial delay jitter before starting the processor
    private final ClientLogger logger = new ClientLogger(EventProcessorClient.class);

    private final String identifier;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final PartitionPumpManager partitionPumpManager;
    private final PartitionBasedLoadBalancer partitionBasedLoadBalancer;
    private final CheckpointStore checkpointStore;

    private final AtomicReference<ScheduledFuture<?>> runner = new AtomicReference<>();
    private final AtomicReference<ScheduledExecutorService> scheduler = new AtomicReference<>();
    private final String fullyQualifiedNamespace;
    private final String eventHubName;
    private final String consumerGroup;
    private final Duration loadBalancerUpdateInterval;

    /**
     * Package-private constructor. Use {@link EventHubClientBuilder} to create an instance.
     *
     * @param eventHubClientBuilder The {@link EventHubClientBuilder}.
     * @param consumerGroup The consumer group name used in this event processor to consumer events.
     * @param partitionProcessorFactory The factory to create new partition processor(s).
     * @param checkpointStore The store used for reading and updating partition ownership and checkpoints. information.
     * @param trackLastEnqueuedEventProperties If set to {@code true}, all events received by this EventProcessorClient
     * will also include the last enqueued event properties for it's respective partitions.
     * @param tracerProvider The tracer implementation.
     * @param processError Error handler for any errors that occur outside the context of a partition.
     * @param initialPartitionEventPosition Map of initial event positions for partition ids.
     * @param maxBatchSize The maximum batch size to receive per users' process handler invocation.
     * @param maxWaitTime The maximum time to wait to receive a batch or a single event.
     * @param batchReceiveMode The boolean value indicating if this processor is configured to receive in batches or
     * single events.
     * @param loadBalancerUpdateInterval The time duration between load balancing update cycles.
     * @param partitionOwnershipExpirationInterval The time duration after which the ownership of partition expires.
     * @param loadBalancingStrategy The load balancing strategy to use.
     */
    EventProcessorClient(EventHubClientBuilder eventHubClientBuilder, String consumerGroup,
        Supplier<PartitionProcessor> partitionProcessorFactory, CheckpointStore checkpointStore,
        boolean trackLastEnqueuedEventProperties, TracerProvider tracerProvider, Consumer<ErrorContext> processError,
        Map<String, EventPosition> initialPartitionEventPosition, int maxBatchSize, Duration maxWaitTime,
        boolean batchReceiveMode, Duration loadBalancerUpdateInterval, Duration partitionOwnershipExpirationInterval,
        LoadBalancingStrategy loadBalancingStrategy) {

        Objects.requireNonNull(eventHubClientBuilder, "eventHubClientBuilder cannot be null.");
        Objects.requireNonNull(consumerGroup, "consumerGroup cannot be null.");
        Objects.requireNonNull(partitionProcessorFactory, "partitionProcessorFactory cannot be null.");

        EventHubAsyncClient eventHubAsyncClient = eventHubClientBuilder.buildAsyncClient();

        this.checkpointStore = Objects.requireNonNull(checkpointStore, "checkpointStore cannot be null");
        this.identifier = UUID.randomUUID().toString();
        this.fullyQualifiedNamespace = eventHubAsyncClient.getFullyQualifiedNamespace().toLowerCase(Locale.ROOT);
        this.eventHubName = eventHubAsyncClient.getEventHubName().toLowerCase(Locale.ROOT);
        this.consumerGroup = consumerGroup.toLowerCase(Locale.ROOT);
        this.loadBalancerUpdateInterval = loadBalancerUpdateInterval;

        logger.info("The instance ID for this event processors is {}", this.identifier);
        this.partitionPumpManager = new PartitionPumpManager(checkpointStore, partitionProcessorFactory,
            eventHubClientBuilder, trackLastEnqueuedEventProperties, tracerProvider, initialPartitionEventPosition,
            maxBatchSize, maxWaitTime, batchReceiveMode);
        this.partitionBasedLoadBalancer =
            new PartitionBasedLoadBalancer(this.checkpointStore, eventHubAsyncClient,
                this.fullyQualifiedNamespace, this.eventHubName, this.consumerGroup, this.identifier,
                partitionOwnershipExpirationInterval.getSeconds(), this.partitionPumpManager, processError,
                loadBalancingStrategy);
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
        if (!isRunning.compareAndSet(false, true)) {
            logger.info("Event processor is already running");
            return;
        }
        logger.info("Starting a new event processor instance with id {}", this.identifier);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        scheduler.set(executor);
        // Add a bit of jitter to initialDelay to minimize contention if multiple EventProcessors start at the same time
        Double jitterInMillis =
            ThreadLocalRandom.current().nextDouble() * TimeUnit.SECONDS.toMillis(BASE_JITTER_IN_SECONDS);

        runner.set(scheduler.get().scheduleWithFixedDelay(partitionBasedLoadBalancer::loadBalance,
            jitterInMillis.longValue(), loadBalancerUpdateInterval.toMillis(), TimeUnit.MILLISECONDS));
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
        if (!isRunning.compareAndSet(true, false)) {
            logger.info("Event processor has already stopped");
            return;
        }
        runner.get().cancel(true);
        scheduler.get().shutdown();
        stopProcessing();
    }

    /**
     * Returns {@code true} if the event processor is running. If the event processor is already running, calling {@link
     * #start()} has no effect.
     *
     * @return {@code true} if the event processor is running.
     */
    public synchronized boolean isRunning() {
        return isRunning.get();
    }

    private void stopProcessing() {
        partitionPumpManager.stopAllPartitionPumps();

        // finally, remove ownerid from checkpointstore as the processor is shutting down
        checkpointStore.listOwnership(fullyQualifiedNamespace, eventHubName, consumerGroup)
            .filter(ownership -> identifier.equals(ownership.getOwnerId()))
            .map(ownership -> ownership.setOwnerId(""))
            .collect(Collectors.toList())
            .flatMapMany(checkpointStore::claimOwnership)
            .blockLast(Duration.ofSeconds(10)); // block until the checkpoint store is updated
    }
}
