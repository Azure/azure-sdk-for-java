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
 * This is the starting point for event processor.
 * <p>
 * Event Processor based application consists of one or more instances of {@link EventProcessorAsyncClient} which are
 * set up to consume events from the same Event Hub + consumer group and to balance the workload across different
 * instances and track progress when events are processed.
 * </p>
 */
public class EventProcessorAsyncClient {

    private static final long INTERVAL_IN_SECONDS = 10; // run every 10 seconds
    private static final long INITIAL_DELAY = 0; // start immediately
    private static final long OWNERSHIP_EXPIRATION_TIME_IN_MILLIS = TimeUnit.SECONDS.toMillis(30);
    private final ClientLogger logger = new ClientLogger(EventProcessorAsyncClient.class);

    private final EventHubAsyncClient eventHubAsyncClient;
    private final String consumerGroupName;
    private final EventPosition initialEventPosition;
    private final PartitionProcessorFactory partitionProcessorFactory;
    private final PartitionManager partitionManager;
    private final String identifier;
    private final Map<String, EventHubConsumer> partitionConsumers = new ConcurrentHashMap<>();
    private final String eventHubName;
    private Disposable runner;
    private Scheduler scheduler;
    private AtomicBoolean started = new AtomicBoolean(false);

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
    EventProcessorAsyncClient(EventHubAsyncClient eventHubAsyncClient, String consumerGroupName,
        PartitionProcessorFactory partitionProcessorFactory, EventPosition initialEventPosition,
        PartitionManager partitionManager,
        String eventHubName) {
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
     */
    public synchronized void start() {
        if (!started.compareAndSet(false, true)) {
            logger.info("Event processor is already running");
            return;
        }
        logger.info("Starting a new event processor instance with id {}", this.identifier);
        scheduler = Schedulers.newElastic("EventProcessorAsyncClient");
        runner = scheduler.schedulePeriodically(this::run, INITIAL_DELAY, INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Stops processing events for all partitions owned by this event processor. All {@link PartitionProcessor} will be
     * shutdown and any open resources will be closed.
     * <p>
     * Subsequent calls to stop will be ignored if the event processor is not running.
     * </p>
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
    void run() {
        /* This will run periodically to get new ownership details and close/open new
        consumers when ownership of this instance has changed */
        final Flux<PartitionOwnership> ownershipFlux = partitionManager.listOwnership(eventHubName, consumerGroupName)
            .cache();
        eventHubAsyncClient.getPartitionIds()
            .flatMap(id -> getCandidatePartitions(ownershipFlux, id))
            .flatMap(this::claimOwnership)
            .subscribeOn(Schedulers.newElastic("PartitionPumps"))
            .subscribe(this::receiveEvents);
    }

    /*
     * Get the candidate partitions for claiming ownerships
     */
    private Publisher<? extends PartitionOwnership> getCandidatePartitions(Flux<PartitionOwnership> ownershipFlux,
        String id) {
        return ownershipFlux
            .doOnNext(ownership -> logger
                .info("Ownership flux: partitionId = {}; EH: partitionId = {}", ownership.partitionId(), id))
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

        EventHubConsumer consumer = this.eventHubAsyncClient
            .createConsumer(this.consumerGroupName, partitionOwnership.partitionId(), startFromEventPosition, consumerOptions);
        this.partitionConsumers.put(partitionOwnership.partitionId(), consumer);

        PartitionContext partitionContext = new PartitionContext(partitionOwnership.partitionId(), this.eventHubName,
            this.consumerGroupName);
        CheckpointManager checkpointManager = new CheckpointManager(this.identifier, partitionContext,
            this.partitionManager, null);
        logger.info("Subscribing to receive events from partition {}", partitionOwnership.partitionId());
        PartitionProcessor partitionProcessor = this.partitionProcessorFactory
            .createPartitionProcessor(partitionContext, checkpointManager);
        partitionProcessor.initialize();

        consumer.receive().subscribe(eventData -> partitionProcessor.processEvent(eventData).subscribe(),
            partitionProcessor::processError,
            // Currently, there is no way to distinguish if the receiver was closed because
            // another receiver with higher/same owner level(epoch) connected or because
            // this event processor explicitly called close on this consumer.
            () -> partitionProcessor.close(CloseReason.LOST_PARTITION_OWNERSHIP));
    }
}
