// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.messaging.eventhubs.implementation.PartitionProcessor;
import com.azure.messaging.eventhubs.implementation.PartitionProcessorException;
import com.azure.messaging.eventhubs.implementation.ReactorShim;
import com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsConsumerInstrumentation;
import com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationScope;
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
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.PARTITION_ID_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.SEQUENCE_NUMBER_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.SIGNAL_TYPE_KEY;

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
    private final int prefetch;
    private final EventHubsConsumerInstrumentation instrumentation;
    private final EventProcessorClientOptions options;

    /**
     * Creates an instance of partition pump manager.
     *
     * @param checkpointStore The partition manager that is used to store and update checkpoints.
     * @param partitionProcessorFactory The partition processor factory that is used to create new instances of {@link
     * PartitionProcessor} when new partition pumps are started.
     * @param eventHubClientBuilder The client builder used to create new clients (and new connections) for each
     * partition processed by this {@link EventProcessorClient}.
     * @param instrumentation Tracing and metrics helper.
     * @param options Configuration options.
     */
    PartitionPumpManager(CheckpointStore checkpointStore, Supplier<PartitionProcessor> partitionProcessorFactory,
        EventHubClientBuilder eventHubClientBuilder, EventHubsConsumerInstrumentation instrumentation, EventProcessorClientOptions options) {
        this.checkpointStore = checkpointStore;
        this.partitionProcessorFactory = partitionProcessorFactory;
        this.eventHubClientBuilder = eventHubClientBuilder;
        this.options = options;
        this.prefetch = eventHubClientBuilder.getPrefetchCount() == null
            ? EventHubClientBuilder.DEFAULT_PREFETCH_COUNT
            : eventHubClientBuilder.getPrefetchCount();
        this.instrumentation = instrumentation;
    }

    /**
     * Stops all partition pumps that are actively consuming events. This method is invoked when the {@link
     * EventProcessorClient} is requested to stop.
     */
    Mono<Void> stopAllPartitionPumps() {
        List<String> partitionIds = new ArrayList<>(partitionPumps.keySet());
        return Flux.fromIterable(partitionIds)
            .flatMap(partitionId -> {
                final PartitionPump pump = partitionPumps.remove(partitionId);
                return pump.closeAsync()
                    .doOnError(ex -> LOGGER.atWarning()
                        .addKeyValue(PARTITION_ID_KEY, partitionId)
                        .addKeyValue(SIGNAL_TYPE_KEY, SignalType.ON_ERROR)
                        .log(Messages.FAILED_CLOSE_CONSUMER_PARTITION, ex))
                    .doOnCancel(() -> LOGGER.atWarning()
                        .addKeyValue(PARTITION_ID_KEY, partitionId)
                        .addKeyValue(SIGNAL_TYPE_KEY, SignalType.CANCEL)
                        .log(Messages.FAILED_CLOSE_CONSUMER_PARTITION));
            }).then();
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

        final String partitionId = claimedOwnership.getPartitionId();

        if (partitionPumps.containsKey(partitionId)) {
            LOGGER.atVerbose()
                .addKeyValue(PARTITION_ID_KEY, partitionId)
                .log("Consumer is already running.");

            return;
        }

        try {
            PartitionContext partitionContext = new PartitionContext(claimedOwnership.getFullyQualifiedNamespace(),
                claimedOwnership.getEventHubName(), claimedOwnership.getConsumerGroup(), partitionId);
            PartitionProcessor partitionProcessor = this.partitionProcessorFactory.get();

            InitializationContext initializationContext = new InitializationContext(partitionContext);
            partitionProcessor.initialize(initializationContext);

            EventPosition startFromEventPosition = getInitialEventPosition(partitionId, checkpoint);

            LOGGER.atInfo()
                .addKeyValue(PARTITION_ID_KEY, partitionId)
                .addKeyValue("eventPosition", startFromEventPosition)
                .log("Starting event processing.");

            ReceiveOptions receiveOptions = new ReceiveOptions()
                .setOwnerLevel(0L)
                .setTrackLastEnqueuedEventProperties(options.isTrackLastEnqueuedEventProperties());

            Scheduler scheduler = Schedulers.newBoundedElastic(schedulerSize,
                MAXIMUM_QUEUE_SIZE, "partition-pump-" + partitionId);
            EventHubConsumerAsyncClient eventHubConsumer = eventHubClientBuilder.buildAsyncClient()
                .createConsumer(claimedOwnership.getConsumerGroup(), prefetch, true);
            PartitionPump partitionPump = new PartitionPump(partitionId, eventHubConsumer,
                scheduler);

            partitionPumps.put(partitionId, partitionPump);
            //@formatter:off
            Flux<Flux<PartitionEvent>> partitionEventFlux;
            Flux<PartitionEvent> receiver = eventHubConsumer
                .receiveFromPartition(partitionId, startFromEventPosition, receiveOptions)
                .doOnNext(partitionEvent -> {
                    if (LOGGER.canLogAtLevel(LogLevel.VERBOSE)) {
                        LOGGER.atVerbose()
                            .addKeyValue(PARTITION_ID_KEY, partitionContext.getPartitionId())
                            .addKeyValue(ENTITY_PATH_KEY, partitionContext.getEventHubName())
                            .addKeyValue(SEQUENCE_NUMBER_KEY, partitionEvent.getData().getSequenceNumber())
                            .log("On next.");
                    }
                });

            if (options.getMaxWaitTime() != null) {
                partitionEventFlux = ReactorShim.windowTimeout(receiver, options.getMaxBatchSize(),
                    options.getMaxWaitTime());
            } else {
                partitionEventFlux = receiver.window(options.getMaxBatchSize());
            }

            int prefetchWindows = Math.max(prefetch / options.getMaxBatchSize(), 1);
            partitionEventFlux
                .concatMap(Flux::collectList, 0)
                .publishOn(scheduler, false, prefetchWindows)
                .subscribe(partitionEventBatch -> {
                    processEvents(partitionContext, partitionProcessor, partitionPump,
                        partitionEventBatch);
                },
                    /* EventHubConsumer receive() returned an error */
                    ex -> handleError(claimedOwnership, partitionPump, partitionProcessor, ex, partitionContext),
                    () -> {
                        try {
                            partitionProcessor.close(new CloseContext(partitionContext,
                                CloseReason.EVENT_PROCESSOR_SHUTDOWN));
                        } catch (Throwable e) {
                            LOGGER.atError()
                                .addKeyValue(PARTITION_ID_KEY, partitionContext.getPartitionId())
                                .log("Error occurred calling partitionProcessor.close when closing partition pump.", e);
                        } finally {
                            cleanup(claimedOwnership, partitionPump);
                        }
                    });
            //@formatter:on
        } catch (Exception ex) {
            if (partitionPumps.containsKey(partitionId)) {
                cleanup(claimedOwnership, partitionPumps.get(partitionId));
            }
            throw LOGGER.atError()
                .addKeyValue(PARTITION_ID_KEY, partitionId)
                .log(new PartitionProcessorException(
                    "Error occurred while starting partition pump for partition " + partitionId,
                    ex));
        }
    }

    private void processEvent(PartitionContext partitionContext, PartitionProcessor partitionProcessor,
        EventContext eventContext) {

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
        InstrumentationScope scope = null;

        try {
            if (options.isBatchReceiveMode()) {
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

                scope = instrumentation.startProcess(eventBatchContext);
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

                scope = instrumentation.startProcess(eventContext);

                processEvent(partitionContext, partitionProcessor, eventContext);
            }
        } catch (Throwable throwable) {
            if (scope != null) {
                scope.setError(throwable);
            }
            /* user code for event processing threw an exception - log and bubble up */
            throw LOGGER.logExceptionAsError(new PartitionProcessorException("Error in event processing callback",
                throwable));
        } finally {
            if (scope != null) {
                scope.close();
            }
        }
    }

    Map<String, PartitionPump> getPartitionPumps() {
        return this.partitionPumps;
    }

    EventPosition getInitialEventPosition(String partitionId, Checkpoint checkpoint) {
        // A checkpoint indicates the last known successfully processed event.
        // So, the event position to start a new partition processing should be exclusive of the
        // offset/sequence number in the checkpoint. If no checkpoint is available, start from
        // the position in set in the InitializationContext (either the earliest event in the partition or
        // the user provided initial position)
        if (checkpoint != null && checkpoint.getOffset() != null) {
            return EventPosition.fromOffset(checkpoint.getOffset());
        } else if (checkpoint != null && checkpoint.getSequenceNumber() != null) {
            return EventPosition.fromSequenceNumber(checkpoint.getSequenceNumber());
        }

        if (options.getInitialEventPositionProvider() != null) {
            final EventPosition initialPosition = options.getInitialEventPositionProvider().apply(partitionId);

            if (initialPosition != null) {
                return initialPosition;
            }
        }

        return EventPosition.latest();
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

            try {
                partitionProcessor.processError(new ErrorContext(partitionContext, throwable));
            } catch (Throwable e) {
                LOGGER.atError()
                    .addKeyValue(PARTITION_ID_KEY, partitionContext.getPartitionId())
                    .log("Error occurred calling partitionProcessor.processError.", e);
            }
        }

        // If there was an error on receive, it also marks the end of the event data stream
        // Any exception while receiving events will result in the processor losing ownership
        CloseReason closeReason = CloseReason.LOST_PARTITION_OWNERSHIP;

        try {
            partitionProcessor.close(new CloseContext(partitionContext, closeReason));
        } catch (Throwable e) {
            LOGGER.atError()
                .addKeyValue(PARTITION_ID_KEY, partitionContext.getPartitionId())
                .log("Error occurred calling partitionProcessor.close.", e);
        }

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
