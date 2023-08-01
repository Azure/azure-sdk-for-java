// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.messaging.eventhubs.implementation.PartitionProcessor;
import com.azure.messaging.eventhubs.implementation.PartitionProcessorException;
import com.azure.messaging.eventhubs.implementation.ReactorShim;
import com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsTracer;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.CloseReason;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.InitializationContext;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.PARTITION_ID_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.SEQUENCE_NUMBER_KEY;

/**
 * The partition pump manager that keeps track of all the partition pumps started by this {@link EventProcessorClient}.
 * Each partition pump is an {@link EventHubConsumerClient} that is receiving events from partitions this {@link
 * EventProcessorClient} has claimed ownership of.
 *
 * <p>
 * New partition pumps are created when this {@link EventProcessorClient} claims ownership of a new partition. When the
 * {@link EventProcessorClient} is requested to stop, this class is responsible for stopping all event processing tasks
 * and closing all connections to the Event Hub.
 * </p>
 */
class PartitionPumpManager {
    private static final int MAXIMUM_QUEUE_SIZE = 10000;
    private static final ClientLogger LOGGER = new ClientLogger(PartitionPumpManager.class);

    //TODO (conniey): Add a configurable scheduler size, at the moment we are creating a new elastic scheduler
    // for each partition pump that will have at most number of processors * 4.
    private final int schedulerSize = Runtime.getRuntime().availableProcessors() * 4;
    private final CheckpointStore checkpointStore;
    private final Map<String, PartitionPump> partitionPumps = new ConcurrentHashMap<>();
    private final Supplier<PartitionProcessor> partitionProcessorFactory;
    private final EventHubClientBuilder eventHubClientBuilder;
    private final boolean trackLastEnqueuedEventProperties;
    private final Map<String, EventPosition> initialPartitionEventPosition;
    private final Duration maxWaitTime;
    private final int maxBatchSize;
    private final boolean batchReceiveMode;
    private final int prefetch;
    private final EventHubsTracer tracer;

    /**
     * Creates an instance of partition pump manager.
     *
     * @param checkpointStore The partition manager that is used to store and update checkpoints.
     * @param partitionProcessorFactory The partition processor factory that is used to create new instances of {@link
     * PartitionProcessor} when new partition pumps are started.
     * @param eventHubClientBuilder The client builder used to create new clients (and new connections) for each
     * partition processed by this {@link EventProcessorClient}.
     * @param trackLastEnqueuedEventProperties If set to {@code true}, all events received by this EventProcessorClient
     * will also include the last enqueued event properties for its respective partitions.
     * @param tracer Tracing helper.
     * @param initialPartitionEventPosition Map of initial event positions for partition ids.
     * @param maxBatchSize The maximum batch size to receive per users' process handler invocation.
     * @param maxWaitTime The maximum time to wait to receive a batch or a single event.
     * @param batchReceiveMode The boolean value indicating if this processor is configured to receive in batches or
     * single events.
     */
    PartitionPumpManager(CheckpointStore checkpointStore,
        Supplier<PartitionProcessor> partitionProcessorFactory, EventHubClientBuilder eventHubClientBuilder,
        boolean trackLastEnqueuedEventProperties, EventHubsTracer tracer,
        Map<String, EventPosition> initialPartitionEventPosition, int maxBatchSize, Duration maxWaitTime,
        boolean batchReceiveMode) {
        this.checkpointStore = checkpointStore;
        this.partitionProcessorFactory = partitionProcessorFactory;
        this.eventHubClientBuilder = eventHubClientBuilder;
        this.trackLastEnqueuedEventProperties = trackLastEnqueuedEventProperties;
        this.initialPartitionEventPosition = initialPartitionEventPosition;
        this.maxBatchSize = maxBatchSize;
        this.maxWaitTime = maxWaitTime;
        this.batchReceiveMode = batchReceiveMode;

        this.prefetch = eventHubClientBuilder.getPrefetchCount() == null
            ? EventHubClientBuilder.DEFAULT_PREFETCH_COUNT
            : eventHubClientBuilder.getPrefetchCount();
        this.tracer = tracer;
    }

    /**
     * Stops all partition pumps that are actively consuming events. This method is invoked when the {@link
     * EventProcessorClient} is requested to stop.
     */
    void stopAllPartitionPumps() {
        this.partitionPumps.forEach((partitionId, eventHubConsumer) -> {
            try {
                eventHubConsumer.close();
            } catch (Exception ex) {
                LOGGER.atWarning()
                    .addKeyValue(PARTITION_ID_KEY, partitionId)
                    .log(Messages.FAILED_CLOSE_CONSUMER_PARTITION, ex);
            } finally {
                partitionPumps.remove(partitionId);
            }
        });
    }

    /**
     * Checks the state of the connection for the given partition. If the connection is closed, then this method will
     * remove the partition from the list of partition pumps.
     *
     * @param ownership The partition ownership information for which the connection state will be verified.
     */
    void verifyPartitionConnection(PartitionOwnership ownership) {
        final String partitionId = ownership.getPartitionId();
        final PartitionPump partitionPump = partitionPumps.get(partitionId);

        if (partitionPump == null) {

            LOGGER.atInfo()
                .addKeyValue(PARTITION_ID_KEY, partitionId)
                .addKeyValue(ENTITY_PATH_KEY, ownership.getEventHubName())
                .log("No partition pump found for ownership record.");
            return;
        }

        final EventHubConsumerAsyncClient consumerClient = partitionPump.getClient();
        if (consumerClient.isConnectionClosed()) {
            LOGGER.atInfo()
                .addKeyValue(PARTITION_ID_KEY, partitionId)
                .addKeyValue(ENTITY_PATH_KEY, ownership.getEventHubName())
                .log("Connection closed for partition. Removing the consumer.");

            try {
                partitionPump.close();
            } catch (Exception ex) {
                LOGGER.atWarning()
                    .addKeyValue(PARTITION_ID_KEY, partitionId)
                    .log(Messages.FAILED_CLOSE_CONSUMER_PARTITION, ex);
            } finally {
                partitionPumps.remove(partitionId);
            }
        }
    }

    /**
     * Starts a new partition pump for the newly claimed partition. If the partition already has an active partition
     * pump, this will not create a new consumer.
     *
     * @param claimedOwnership The details of partition ownership for which new partition pump is requested to start.
     */
    void startPartitionPump(PartitionOwnership claimedOwnership, Checkpoint checkpoint) {
        if (partitionPumps.containsKey(claimedOwnership.getPartitionId())) {
            LOGGER.atVerbose()
                .addKeyValue(PARTITION_ID_KEY, claimedOwnership.getPartitionId())
                .log("Consumer is already running.");

            return;
        }

        try {
            PartitionContext partitionContext = new PartitionContext(claimedOwnership.getFullyQualifiedNamespace(),
                claimedOwnership.getEventHubName(), claimedOwnership.getConsumerGroup(),
                claimedOwnership.getPartitionId());
            PartitionProcessor partitionProcessor = this.partitionProcessorFactory.get();

            InitializationContext initializationContext = new InitializationContext(partitionContext);
            partitionProcessor.initialize(initializationContext);

            EventPosition startFromEventPosition;
            // A checkpoint indicates the last known successfully processed event.
            // So, the event position to start a new partition processing should be exclusive of the
            // offset/sequence number in the checkpoint. If no checkpoint is available, start from
            // the position in set in the InitializationContext (either the earliest event in the partition or
            // the user provided initial position)
            if (checkpoint != null && checkpoint.getOffset() != null) {
                startFromEventPosition = EventPosition.fromOffset(checkpoint.getOffset());
            } else if (checkpoint != null && checkpoint.getSequenceNumber() != null) {
                startFromEventPosition = EventPosition.fromSequenceNumber(checkpoint.getSequenceNumber());
            } else if (initialPartitionEventPosition.containsKey(claimedOwnership.getPartitionId())) {
                startFromEventPosition = initialPartitionEventPosition.get(claimedOwnership.getPartitionId());
            } else {
                startFromEventPosition = EventPosition.latest();
            }

            LOGGER.atInfo()
                .addKeyValue(PARTITION_ID_KEY, claimedOwnership.getPartitionId())
                .addKeyValue("eventPosition", startFromEventPosition)
                .log("Starting event processing.");

            ReceiveOptions receiveOptions = new ReceiveOptions().setOwnerLevel(0L)
                .setTrackLastEnqueuedEventProperties(trackLastEnqueuedEventProperties);

            Scheduler scheduler = Schedulers.newBoundedElastic(schedulerSize,
                MAXIMUM_QUEUE_SIZE, "partition-pump-" + claimedOwnership.getPartitionId());
            EventHubConsumerAsyncClient eventHubConsumer = eventHubClientBuilder.buildAsyncClient()
                .createConsumer(claimedOwnership.getConsumerGroup(), prefetch, true);
            PartitionPump partitionPump = new PartitionPump(claimedOwnership.getPartitionId(), eventHubConsumer,
                scheduler);

            partitionPumps.put(claimedOwnership.getPartitionId(), partitionPump);
            //@formatter:off
            Flux<Flux<PartitionEvent>> partitionEventFlux;
            Flux<PartitionEvent> receiver = eventHubConsumer
                .receiveFromPartition(claimedOwnership.getPartitionId(), startFromEventPosition, receiveOptions)
                .doOnNext(partitionEvent -> {
                    if (LOGGER.canLogAtLevel(LogLevel.VERBOSE)) {
                        LOGGER.atVerbose()
                            .addKeyValue(PARTITION_ID_KEY, partitionContext.getPartitionId())
                            .addKeyValue(ENTITY_PATH_KEY, partitionContext.getEventHubName())
                            .addKeyValue(SEQUENCE_NUMBER_KEY, partitionEvent.getData().getSequenceNumber())
                            .log("On next.");
                    }
                });

            if (maxWaitTime != null) {
                partitionEventFlux = ReactorShim.windowTimeout(receiver, maxBatchSize, maxWaitTime);
            } else {
                partitionEventFlux = receiver
                    .window(maxBatchSize);
            }
            partitionEventFlux
                .concatMap(Flux::collectList)
                .publishOn(scheduler, false, prefetch)
                .subscribe(partitionEventBatch -> {
                    processEvents(partitionContext, partitionProcessor, partitionPump,
                        partitionEventBatch);
                },
                    /* EventHubConsumer receive() returned an error */
                    ex -> handleError(claimedOwnership, partitionPump, partitionProcessor, ex, partitionContext),
                    () -> {
                        partitionProcessor.close(new CloseContext(partitionContext,
                            CloseReason.EVENT_PROCESSOR_SHUTDOWN));
                        cleanup(claimedOwnership, partitionPump);
                    });
            //@formatter:on
        } catch (Exception ex) {
            if (partitionPumps.containsKey(claimedOwnership.getPartitionId())) {
                cleanup(claimedOwnership, partitionPumps.get(claimedOwnership.getPartitionId()));
            }
            throw LOGGER.atError()
                .addKeyValue(PARTITION_ID_KEY, claimedOwnership.getPartitionId())
                .log(new PartitionProcessorException(
                    "Error occurred while starting partition pump for partition " + claimedOwnership.getPartitionId(),
                    ex));
        }
    }

    private void processEvent(PartitionContext partitionContext, PartitionProcessor partitionProcessor, EventContext eventContext) {

        EventData eventData = eventContext.getEventData();
        try {
            if (LOGGER.canLogAtLevel(LogLevel.VERBOSE)) {

                LOGGER.atVerbose()
                    .addKeyValue(PARTITION_ID_KEY, partitionContext.getPartitionId())
                    .addKeyValue(ENTITY_PATH_KEY, partitionContext.getEventHubName())
                    .log("Processing event.");
            }
            partitionProcessor.processEvent(new EventContext(partitionContext, eventData, checkpointStore,
                eventContext.getLastEnqueuedEventProperties()));
            if (LOGGER.canLogAtLevel(LogLevel.VERBOSE)) {
                LOGGER.atVerbose()
                    .addKeyValue(PARTITION_ID_KEY, partitionContext.getPartitionId())
                    .addKeyValue(ENTITY_PATH_KEY, partitionContext.getEventHubName())
                    .log("Completed processing event.");
            }
        } catch (Throwable throwable) {
            /* user code for event processing threw an exception - log and bubble up */
            throw LOGGER.logExceptionAsError(new PartitionProcessorException("Error in event processing callback",
                throwable));
        }
    }

    private void processEvents(PartitionContext partitionContext, PartitionProcessor partitionProcessor,
        PartitionPump partitionPump, List<PartitionEvent> partitionEventBatch) {
        Throwable exception = null;
        Context span = null;
        AutoCloseable scope = null;

        try {
            if (batchReceiveMode) {
                LastEnqueuedEventProperties[] lastEnqueuedEventProperties = new LastEnqueuedEventProperties[1];
                List<EventData> eventDataList = partitionEventBatch.stream()
                    .map(partitionEvent -> {
                        lastEnqueuedEventProperties[0] = partitionEvent.getLastEnqueuedEventProperties();
                        return partitionEvent.getData();
                    })
                    .collect(Collectors.toList());

                // It's possible when using windowTimeout that in the timeframe, there weren't any events received.
                LastEnqueuedEventProperties enqueuedEventProperties =
                    updateOrGetLastEnqueuedEventProperties(partitionPump, lastEnqueuedEventProperties[0]);

                EventBatchContext eventBatchContext = new EventBatchContext(partitionContext, eventDataList,
                    checkpointStore, enqueuedEventProperties);

                span = tracer.startProcessSpan("EventHubs.process", eventDataList, Context.NONE);
                scope = tracer.makeSpanCurrent(span);
                if (LOGGER.canLogAtLevel(LogLevel.VERBOSE)) {
                    LOGGER.atVerbose()
                        .addKeyValue(PARTITION_ID_KEY, partitionContext.getPartitionId())
                        .addKeyValue(ENTITY_PATH_KEY, partitionContext.getEventHubName())
                        .log("Processing event batch.");
                }

                partitionProcessor.processEventBatch(eventBatchContext);

                if (LOGGER.canLogAtLevel(LogLevel.VERBOSE)) {
                    LOGGER.atVerbose()
                        .addKeyValue(PARTITION_ID_KEY, partitionContext.getPartitionId())
                        .addKeyValue(ENTITY_PATH_KEY, partitionContext.getEventHubName())
                        .log("Completed processing event batch.");
                }
            } else {
                EventData eventData = (partitionEventBatch.size() == 1
                    ? partitionEventBatch.get(0).getData() : null);
                LastEnqueuedEventProperties lastEnqueuedEventProperties = (partitionEventBatch.size() == 1
                    ? partitionEventBatch.get(0).getLastEnqueuedEventProperties() : null);

                // Get the last value we've seen if the current value from this is null.
                LastEnqueuedEventProperties enqueuedEventProperties =
                    updateOrGetLastEnqueuedEventProperties(partitionPump, lastEnqueuedEventProperties);

                EventContext eventContext = new EventContext(partitionContext, eventData, checkpointStore,
                    enqueuedEventProperties);
                span = tracer.startProcessSpan("EventHubs.process", eventData, Context.NONE);
                scope = tracer.makeSpanCurrent(span);

                processEvent(partitionContext, partitionProcessor, eventContext);
            }
        } catch (Throwable throwable) {
            exception = throwable;
            /* user code for event processing threw an exception - log and bubble up */
            throw LOGGER.logExceptionAsError(new PartitionProcessorException("Error in event processing callback",
                throwable));
        } finally {
            tracer.endSpan(exception, span, scope);
        }
    }

    Map<String, PartitionPump> getPartitionPumps() {
        return this.partitionPumps;
    }

    private void handleError(PartitionOwnership claimedOwnership, PartitionPump partitionPump,
        PartitionProcessor partitionProcessor, Throwable throwable, PartitionContext partitionContext) {
        boolean shouldRethrow = true;
        if (!(throwable instanceof PartitionProcessorException)) {
            shouldRethrow = false;
            // If user code threw an exception in processEvent callback, bubble up the exception

            LOGGER.atWarning()
                .addKeyValue(PARTITION_ID_KEY, partitionContext.getPartitionId())
                .log("Error receiving events from partition.", throwable);

            partitionProcessor.processError(new ErrorContext(partitionContext, throwable));
        }
        // If there was an error on receive, it also marks the end of the event data stream
        // Any exception while receiving events will result in the processor losing ownership
        CloseReason closeReason = CloseReason.LOST_PARTITION_OWNERSHIP;
        partitionProcessor.close(new CloseContext(partitionContext, closeReason));
        cleanup(claimedOwnership, partitionPump);
        if (shouldRethrow) {
            PartitionProcessorException exception = (PartitionProcessorException) throwable;
            throw LOGGER.logExceptionAsError(exception);
        }
    }

    private void cleanup(PartitionOwnership claimedOwnership, PartitionPump partitionPump) {
        try {
            // close the consumer
            LOGGER.atInfo().addKeyValue(PARTITION_ID_KEY, claimedOwnership.getPartitionId())
                .log("Closing consumer.");

            partitionPump.close();
        } finally {
            // finally, remove the partition from partitionPumps map
            LOGGER.atInfo().addKeyValue(PARTITION_ID_KEY, claimedOwnership.getPartitionId())
                .log("Removing partition from list of processing partitions.");
            partitionPumps.remove(claimedOwnership.getPartitionId());
        }
    }

    /**
     * Updates the last enqueued event if it was seen and gets the most up-to-date value.
     *
     * @param partitionPump The partition pump.
     * @param last The last enqueued event properties. Could be null if there were no events seen.
     *
     * @return Updates the partition pump properties if there is a latest value and returns it.
     */
    private LastEnqueuedEventProperties updateOrGetLastEnqueuedEventProperties(PartitionPump partitionPump,
        LastEnqueuedEventProperties last) {

        if (last != null) {
            partitionPump.setLastEnqueuedEventProperties(last);
        }

        return partitionPump.getLastEnqueuedEventProperties();
    }
}
