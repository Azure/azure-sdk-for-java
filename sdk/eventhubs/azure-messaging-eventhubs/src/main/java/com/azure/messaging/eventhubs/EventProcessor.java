// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Event Processor provides a convenient mechanism to consume events from all partitions of an Event Hub in the context
 * of a consumer group. Event Processor-based application consists of one or more instances of EventProcessor(s) which
 * are set up to consume events from the same Event Hub, consumer group to balance the workload across different
 * instances and track progress when events are processed. Based on the number of instances running, each Event
 * Processor may own zero or more partitions to balance the workload among all the instances.
 *
 * <p>
 * To create an instance of EventProcessor, use the fluent {@link EventProcessorBuilder}.
 * </p>
 *
 * @see EventProcessorBuilder
 */
public class EventProcessor {

    private static final long INTERVAL_IN_SECONDS = 10; // run every 10 seconds
    private static final long INITIAL_DELAY = 0; // start immediately
    private static final long OWNERSHIP_EXPIRATION_TIME_IN_MILLIS = TimeUnit.SECONDS.toMillis(30);
    private final ClientLogger logger = new ClientLogger(EventProcessor.class);

    private final EventHubAsyncClient eventHubAsyncClient;
    private final String consumerGroupName;
    private final EventPosition initialEventPosition;
    private final PartitionProcessorFactory partitionProcessorFactory;
    private final PartitionManager partitionManager;
    private final String identifier;
    private final Map<String, EventHubAsyncConsumer> partitionConsumers = new ConcurrentHashMap<>();
    private final String eventHubName;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private Disposable runner;
    private Scheduler scheduler;

    /**
     * Package-private constructor. Use {@link EventHubClientBuilder} to create an instance.
     *
     * @param eventHubAsyncClient The {@link EventHubAsyncClient}.
     * @param consumerGroupName The consumer group name used in this event processor to consumer events.
     * @param partitionProcessorFactory The factory to create new partition processor(s).
     * @param initialEventPosition Initial event position to start consuming events.
     * @param partitionManager The partition manager used for reading and updating partition ownership and checkpoint
     * information.
     */
    EventProcessor(EventHubAsyncClient eventHubAsyncClient, String consumerGroupName,
        PartitionProcessorFactory partitionProcessorFactory, EventPosition initialEventPosition,
        PartitionManager partitionManager) {
        this.eventHubAsyncClient = Objects
            .requireNonNull(eventHubAsyncClient, "eventHubAsyncClient cannot be null");
        this.consumerGroupName = Objects
            .requireNonNull(consumerGroupName, "consumerGroupname cannot be null");
        this.partitionProcessorFactory = Objects
            .requireNonNull(partitionProcessorFactory, "partitionProcessorFactory cannot be null");
        this.partitionManager = partitionManager == null ? findPartitionManager() : partitionManager;
        this.initialEventPosition = Objects
            .requireNonNull(initialEventPosition, "initialEventPosition cannot be null");
        this.eventHubName = Objects
            .requireNonNull(eventHubAsyncClient.eventHubName(), "eventHubName cannot be null");
        this.identifier = UUID.randomUUID().toString();
        logger.info("The instance ID for this event processors is {}", this.identifier);
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
        runner = scheduler.schedulePeriodically(this::run, INITIAL_DELAY, INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
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
        this.partitionConsumers.forEach((key, value) -> {
            try {
                logger.info("Closing event hub consumer for partition {}", key);
                value.close();
                logger.info("Closed event hub consumer for partition {}", key);
                partitionConsumers.remove(key);
            } catch (IOException ex) {
                logger.warning("Unable to close event hub consumer for partition {}", key);
            }
        });
        runner.dispose();
        scheduler.dispose();
    }

    /*
     * A simple implementation of an event processor that:
     * 1. Fetches all partition ids from Event Hub
     * 2. Gets the current ownership information of all the partitions from PartitionManager
     * 3. Claims ownership of any partition that doesn't have an owner yet.
     * 4. Starts a new PartitionProcessor and receives events from each of the partitions this instance owns
     */
    private void run() {
        /* This will run periodically to get new ownership details and close/open new
        consumers when ownership of this instance has changed */
        final Flux<PartitionOwnership> ownershipFlux = partitionManager.listOwnership(eventHubName, consumerGroupName)
            .cache();
        eventHubAsyncClient.getPartitionIds()
            .flatMap(id -> getCandidatePartitions(ownershipFlux, id))
            .flatMap(this::claimOwnership)
            .subscribe(this::receiveEvents, ex -> logger.warning("Failed to receive events {}", ex.getMessage()),
                () -> logger.info("Completed starting partition pumps for new partitions owned"));
    }

    /*
     * Get the candidate partitions for claiming ownerships
     */
    private Publisher<? extends PartitionOwnership> getCandidatePartitions(Flux<PartitionOwnership> ownershipFlux,
        String id) {
        return ownershipFlux
            // Ownership has never been claimed, so it won't exist in the list, so we provide a default.
            .filter(ownership -> id.equals(ownership.partitionId()))
            .single(new PartitionOwnership()
                .partitionId(id)
                .eventHubName(this.eventHubName)
                .ownerId(this.identifier)
                .consumerGroupName(this.consumerGroupName)
                .ownerLevel(0L));
    }


    /*
     * Claim ownership of the given partition if it's available
     */
    private Publisher<? extends PartitionOwnership> claimOwnership(PartitionOwnership ownershipInfo) {
        // Claim ownership if:
        // it's not previously owned by any other instance,
        // or if the last modified time is greater than ownership expiration time
        // and previous owner is not this instance
        if (ownershipInfo.lastModifiedTime() == null
            || (System.currentTimeMillis() - ownershipInfo.lastModifiedTime() > OWNERSHIP_EXPIRATION_TIME_IN_MILLIS
            && !ownershipInfo.ownerId().equals(this.identifier))) {
            ownershipInfo.ownerId(this.identifier); // update instance id before claiming ownership
            return partitionManager.claimOwnership(ownershipInfo).doOnComplete(() -> {
                logger.info("Claimed ownership of partition {}", ownershipInfo.partitionId());
            }).doOnError(error -> {
                logger.error("Unable to claim ownership of partition {}", ownershipInfo.partitionId(), error);
            });
        } else {
            return Flux.empty();
        }
    }

    /*
     * Creates a new consumer for given partition and starts receiving events for that partition.
     */
    private void receiveEvents(PartitionOwnership partitionOwnership) {
        EventHubConsumerOptions consumerOptions = new EventHubConsumerOptions();
        consumerOptions.ownerLevel(0L);

        EventPosition startFromEventPosition = partitionOwnership.sequenceNumber() == null ? this.initialEventPosition
            : EventPosition.fromSequenceNumber(partitionOwnership.sequenceNumber(), false);

        EventHubAsyncConsumer consumer = this.eventHubAsyncClient
            .createConsumer(this.consumerGroupName, partitionOwnership.partitionId(), startFromEventPosition,
                consumerOptions);
        this.partitionConsumers.put(partitionOwnership.partitionId(), consumer);

        PartitionContext partitionContext = new PartitionContext(partitionOwnership.partitionId(), this.eventHubName,
            this.consumerGroupName);
        CheckpointManager checkpointManager = new CheckpointManager(this.identifier, partitionContext,
            this.partitionManager, null);
        logger.info("Subscribing to receive events from partition {}", partitionOwnership.partitionId());
        PartitionProcessor partitionProcessor = this.partitionProcessorFactory
            .createPartitionProcessor(partitionContext, checkpointManager);
        partitionProcessor.initialize().subscribe();

        consumer.receive().subscribeOn(Schedulers.newElastic("PartitionPump"))
            .subscribe(eventData -> partitionProcessor.processEvent(eventData).subscribe(unused -> {
            }, partitionProcessor::processError),
                partitionProcessor::processError,
                // Currently, there is no way to distinguish if the receiver was closed because
                // another receiver with higher/same owner level(epoch) connected or because
                // this event processor explicitly called close on this consumer.
                () -> partitionProcessor.close(CloseReason.LOST_PARTITION_OWNERSHIP));
    }
}
