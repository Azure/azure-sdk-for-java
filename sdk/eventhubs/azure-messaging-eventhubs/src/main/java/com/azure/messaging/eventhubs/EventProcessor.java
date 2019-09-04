// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.PartitionBasedLoadBalancer;
import com.azure.messaging.eventhubs.implementation.PartitionPumpManager;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionContext;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Event Processor provides a convenient mechanism to consume events from all partitions of an Event Hub in the context
 * of a consumer group. Event Processor-based application consists of one or more instances of EventProcessor(s) which
 * are set up to consume events from the same Event Hub, consumer group to balance the workload across different
 * instances and track progress when events are processed. Based on the number of instances running, each Event
 * Processor may own zero or more partitions to balance the workload among all the instances.
 *
 * <p>To create an instance of EventProcessor, use the fluent {@link EventProcessorBuilder}.</p>
 *
 * @see EventProcessorBuilder
 */
public class EventProcessor {

    private static final long INTERVAL_IN_SECONDS = 10; // run the load balancer every 10 seconds
    private static final long BASE_JITTER_IN_SECONDS = 2; // the initial delay jitter before starting the processor
    private final ClientLogger logger = new ClientLogger(EventProcessor.class);

    private final String identifier;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final PartitionPumpManager partitionPumpManager;
    private final PartitionBasedLoadBalancer partitionBasedLoadBalancer;
    private final PartitionManager partitionManager;

    private Disposable runner;
    private Scheduler scheduler;

    /**
     * Package-private constructor. Use {@link EventHubClientBuilder} to create an instance.
     *
     * @param eventHubAsyncClient The {@link EventHubAsyncClient}.
     * @param consumerGroup The consumer group name used in this event processor to consumer events.
     * @param partitionProcessorFactory The factory to create new partition processor(s).
     * @param initialEventPosition Initial event position to start consuming events.
     * @param partitionManager The partition manager used for reading and updating partition ownership and checkpoint
     * information.
     * @param tracerProvider The tracer implementation.
     */
    EventProcessor(EventHubAsyncClient eventHubAsyncClient, String consumerGroup,
        Function<PartitionContext, PartitionProcessor> partitionProcessorFactory, EventPosition initialEventPosition,
        PartitionManager partitionManager, TracerProvider tracerProvider) {

        Objects.requireNonNull(eventHubAsyncClient, "eventHubAsyncClient cannot be null");
        Objects.requireNonNull(consumerGroup, "consumerGroup cannot be null");
        Objects.requireNonNull(partitionProcessorFactory, "partitionProcessorFactory cannot be null");
        Objects.requireNonNull(initialEventPosition, "initialEventPosition cannot be null");

        this.partitionManager = partitionManager == null ? findPartitionManager() : partitionManager;
        this.identifier = UUID.randomUUID().toString();
        logger.info("The instance ID for this event processors is {}", this.identifier);
        this.partitionPumpManager = new PartitionPumpManager(partitionManager, partitionProcessorFactory,
            initialEventPosition, eventHubAsyncClient, tracerProvider);
        this.partitionBasedLoadBalancer =
            new PartitionBasedLoadBalancer(this.partitionManager, eventHubAsyncClient,
                eventHubAsyncClient.eventHubName(),
                consumerGroup, identifier, TimeUnit.MINUTES.toSeconds(1), partitionPumpManager);
    }

    /**
     * Looks for a user-defined PartitionManager implementation in classpath.
     * <p>
     * If there are more than one user-defined PartitionManagers, this method will throw an exception. User has to
     * specify a PartitionManager explicitly in {@link EventProcessorBuilder}.
     * </p>
     *
     * @return A {@link PartitionManager} implementation found in classpath, or {@link InMemoryPartitionManager}
     * otherwise.
     */
    private PartitionManager findPartitionManager() {
        ServiceLoader<PartitionManager> partitionManagers = ServiceLoader.load(PartitionManager.class);
        PartitionManager partitionManager = null;

        for (PartitionManager partitionManagerInClassPath : partitionManagers) {
            if (partitionManager != null) {
                // If more than one PartitionManager is found in classpath, throw an exception
                // User has to specify which one to use.
                throw logger.logExceptionAsWarning(
                    new IllegalStateException("Found multiple PartitionManagers in classpath. Specify one in "
                        + "EventProcessorOptions"));
            }
            if (!(partitionManagerInClassPath instanceof InMemoryPartitionManager)) {
                // Don't consider InMemoryPartitionManager.
                partitionManager = partitionManagerInClassPath;
            }
        }

        if (partitionManager == null) {
            // No PartitionManagers found in classpath.
            throw logger.logExceptionAsWarning(
                new IllegalStateException("No PartitionManager implementation found in classpath."));
        }
        return partitionManager;
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
        Double jitterInMillis =
            ThreadLocalRandom.current().nextDouble() * TimeUnit.SECONDS.toMillis(BASE_JITTER_IN_SECONDS);
        // Add a bit of jitter to initialDelay to minimize contention if multiple EventProcessors start at the same time
        runner = scheduler.schedulePeriodically(partitionBasedLoadBalancer::loadBalance, jitterInMillis.longValue(),
            TimeUnit.SECONDS.toMillis(INTERVAL_IN_SECONDS) /* TODO: make this configurable */, TimeUnit.MILLISECONDS);
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
