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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import org.reactivestreams.Subscriber;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * This is the starting point for event processor.
 */
public class EventProcessor {
    private final ClientLogger logger = new ClientLogger(EventProcessor.class);

    private final EventHubAsyncClient eventHubAsyncClient;
    private final String consumerGroupName;
    private final EventPosition initialEventPosition;
    private final BiFunction<PartitionContext, CheckpointManager, Subscriber<EventData>> partitionProcessorFactory;
    private final PartitionManager partitionManager;
    private final Map<String, EventHubConsumer> partitionConsumers = new ConcurrentHashMap<>();
    private Disposable disposable;
    private Scheduler scheduler;

    /**
     * Package-private constructor.
     */
    public EventProcessor(EventHubAsyncClient eventHubAsyncClient, String consumerGroupName,
        BiFunction<PartitionContext, CheckpointManager, Subscriber<EventData>> partitionProcessorFactory,
        EventPosition initialEventPosition, PartitionManager partitionManager) {
        this.eventHubAsyncClient = Objects.requireNonNull(eventHubAsyncClient);
        this.consumerGroupName = Objects.requireNonNull(consumerGroupName, "Consumer name cannot be null");
        this.partitionProcessorFactory = Objects.requireNonNull(partitionProcessorFactory, "partitionProcessorFactory cannot be null");
        this.partitionManager = Objects.requireNonNull(partitionManager, "partitionProcessorFactory cannot be null");
        this.initialEventPosition = Objects
            .requireNonNull(initialEventPosition, "initialEventPosition cannot be null");
    }

    /**
     * Starts the event processor
     */
    public void start() {
        scheduler = Schedulers.newElastic("EventProcessor");
        disposable = scheduler.schedule(() -> run());
    }

    /**
     * Stops the event processor
     */
    public void stop() {
        this.partitionConsumers.entrySet().stream().forEach(entry -> {
            try {
                logger.info("Closing event hub consumer for partition {}", entry.getKey());
                entry.getValue().close();
                partitionConsumers.remove(entry.getKey());
            } catch (IOException ex) {
                logger
                    .warning("Unable to close event hub consumer for partition " + entry.getKey());
            }
        });
        disposable.dispose();
        scheduler.dispose();
        logger.info("Stopping the event processor");
    }

    /* Internal implementation. This is only a simple demo to show how it may look
    - will need to move it to a different class and add more details to this*/
    void run() {
        /* This method should run periodically */
        List<String> eventHubPartitionIds = new ArrayList<>();
        this.eventHubAsyncClient.getPartitionIds().doOnNext(eventHubPartitionIds::add).blockLast();
        logger.info("Found {} partitions", eventHubPartitionIds.size());

        // Get the current ownership information from underlying data store
        List<PartitionOwnership> ownership = new ArrayList<>();
        this.partitionManager
            .listOwnership("", this.consumerGroupName)
            .doOnNext(partitionOwnership -> ownership.add(partitionOwnership))
            .blockLast();

        logger.info("Got ownership info from partition manager");
        List<PartitionOwnership> myOwnership = new ArrayList<>();

        // Find the partitions to claim ownership of
        this.partitionManager
            .claimOwnership(ownership)
            .doOnNext(partitionOwnership -> myOwnership.add(partitionOwnership))
            .blockLast();
        logger.info("Claimed ownership");

        // use the successfully owned partition list to create new consumers
        // and receive events


        // for poc, create consumer for all partitions and start from earliest event position

        // for actual implementation, consumers will be created for newly owned partitions only and will
        // start from the sequence number coming from the data store or from the initial event position (if none exists)
        eventHubPartitionIds.stream().forEach(partitionId -> {
            EventHubConsumer consumer = this.eventHubAsyncClient
                .createConsumer(this.consumerGroupName, partitionId, EventPosition.earliest());

            this.partitionConsumers.put(partitionId, consumer);

            PartitionContext partitionContext = new PartitionContext()
                .partitionId(partitionId)
                .eventHubName("")
                .consumerGroupName(this.consumerGroupName);
            CheckpointManager checkpointManager = new CheckpointManager(partitionContext, this.partitionManager);
            logger.info("Subscribing to receive events from partition {}", partitionId);
            consumer.receive().subscribe(
                this.partitionProcessorFactory.apply(partitionContext, checkpointManager));
        });
    }
}
