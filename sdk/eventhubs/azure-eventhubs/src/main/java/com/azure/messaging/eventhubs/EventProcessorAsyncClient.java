// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.eventprocessor.models.PartitionContext;
import com.azure.messaging.eventhubs.eventprocessor.models.PartitionOwnership;
import com.azure.messaging.eventhubs.models.EventPosition;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import org.reactivestreams.Subscriber;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * This is the starting point for event processor.
 *
 * Event Processor based application consists of one or more instances of {@link EventProcessorAsyncClient}
 * class which are set up to consume from the same event hub+consumer group and to balance
 * the workload across different instances and track progress when events are processed.
 */
public class EventProcessorAsyncClient {
    private final ClientLogger logger = new ClientLogger(EventProcessorAsyncClient.class);

    private final EventHubAsyncClient eventHubAsyncClient;
    private final String consumerGroupName;
    private final EventPosition initialEventPosition;
    private final BiFunction<PartitionContext, CheckpointManager, Subscriber<EventData>> partitionProcessorFactory;
    private final PartitionManager partitionManager;
    private final Map<String, EventHubConsumer> partitionConsumers = new ConcurrentHashMap<>();
    private final String instanceId;
    private final String eventHubName;
    private Disposable disposable;
    private Scheduler scheduler;

    /**
     * Package-private constructor. Use {@link EventHubClientBuilder} to create an instance
     */
    EventProcessorAsyncClient(EventHubAsyncClient eventHubAsyncClient, String consumerGroupName,
        BiFunction<PartitionContext, CheckpointManager, Subscriber<EventData>> partitionProcessorFactory,
        EventPosition initialEventPosition, PartitionManager partitionManager, String eventHubName) {
        this.eventHubAsyncClient = Objects.requireNonNull(eventHubAsyncClient, "eventHubAsyncClient cannot be null");
        this.consumerGroupName = Objects.requireNonNull(consumerGroupName, "consumerGroupname cannot be null");
        this.partitionProcessorFactory = Objects.requireNonNull(partitionProcessorFactory, "partitionProcessorFactory cannot be null");
        this.partitionManager = Objects.requireNonNull(partitionManager, "partitionManager cannot be null");
        this.initialEventPosition = Objects
            .requireNonNull(initialEventPosition, "initialEventPosition cannot be null");
        this.instanceId = UUID.randomUUID().toString();
        this.eventHubName = eventHubName;
    }

    /**
     * Starts the event processor
     */
    public void start() {

        scheduler = Schedulers.newElastic("EventProcessorAsyncClient");
        disposable = scheduler.schedule(this::run);
    }

    /**
     * Stops the event processor
     */
    public void stop() {
        this.partitionConsumers.forEach((key, value) -> {
            try {
                logger.info("Closing event hub consumer for partition {}", key);
                value.close();
                partitionConsumers.remove(key);
            } catch (IOException ex) {
                logger.warning("Unable to close event hub consumer for partition {}", key);
            }
        });
        disposable.dispose();
        scheduler.dispose();
        logger.info("Stopping the event processor");
    }

    /* Internal implementation. This is only a simple demo to show how it may look
    - will need to move it to a different class and add more details to this */
    void run() {
        /* This should run periodically get new ownership details and close/open new
        consumers when ownership of this instance has changed */

        List<String> eventHubPartitionIds = new ArrayList<>();
        this.eventHubAsyncClient.getPartitionIds().doOnNext(eventHubPartitionIds::add).blockLast();
        logger.info("Found {} partitions", eventHubPartitionIds.size());

        // Get the current ownership information from underlying data store
        List<PartitionOwnership> ownership = new ArrayList<>();
        this.partitionManager
            .listOwnership("", this.consumerGroupName)
            .doOnNext(ownership::add)
            .blockLast();

        logger.info("Got ownership info from partition manager");
        List<PartitionOwnership> myOwnership = new ArrayList<>();

        // Find the partitions to claim ownership
        this.partitionManager
            .claimOwnership(ownership)
            .doOnNext(myOwnership::add)
            .blockLast();
        logger.info("Claimed ownership");

        // use the successfully owned partition list to create new consumers
        // and receive events

        // For poc, create consumer for all partitions and start from earliest event position.
        // For actual implementation, consumers will be created for newly owned partitions only and will
        // start from the sequence number coming from the data store or from the initial event position (if none exists)
        eventHubPartitionIds.forEach(this::receiveEvents);
    }

    /**
     * Creates a new consumer for given partition and starts receiving events for that partition
     */
    private void receiveEvents(String partitionId) {
        EventHubConsumer consumer = this.eventHubAsyncClient
            .createConsumer(this.consumerGroupName, partitionId, this.initialEventPosition);
        this.partitionConsumers.put(partitionId, consumer);
        PartitionContext partitionContext = new PartitionContext()
            .partitionId(partitionId)
            .eventHubName("")
            .consumerGroupName(this.consumerGroupName);
        CheckpointManager checkpointManager = new CheckpointManager(partitionContext, this.partitionManager, null);

        logger.info("Subscribing to receive events from partition {}", partitionId);
        consumer.receive().subscribe(
            this.partitionProcessorFactory.apply(partitionContext, checkpointManager));
    }
}
