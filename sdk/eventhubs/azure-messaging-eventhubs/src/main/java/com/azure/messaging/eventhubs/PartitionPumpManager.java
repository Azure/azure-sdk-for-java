// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.SCOPE_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;

import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.messaging.eventhubs.implementation.PartitionProcessor;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.CloseReason;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.InitializationContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import java.io.Closeable;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import reactor.core.publisher.Signal;

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

    private final ClientLogger logger = new ClientLogger(PartitionPumpManager.class);
    private final CheckpointStore checkpointStore;
    private final Map<String, EventHubConsumerAsyncClient> partitionPumps = new ConcurrentHashMap<>();
    private final Supplier<PartitionProcessor> partitionProcessorFactory;
    private final EventHubClientBuilder eventHubClientBuilder;
    private final TracerProvider tracerProvider;
    private final boolean trackLastEnqueuedEventProperties;

    /**
     * Creates an instance of partition pump manager.
     *
     * @param checkpointStore The partition manager that is used to store and update checkpoints.
     * @param partitionProcessorFactory The partition processor factory that is used to create new instances of {@link
     * PartitionProcessor} when new partition pumps are started.
     * @param eventHubClientBuilder The client builder used to create new clients (and new connections) for each
     * partition processed by this {@link EventProcessorClient}.
     * @param trackLastEnqueuedEventProperties If set to {@code true}, all events received by this EventProcessorClient
     * will also include the last enqueued event properties for it's respective partitions.
     * @param tracerProvider The tracer implementation.
     */
    PartitionPumpManager(CheckpointStore checkpointStore,
        Supplier<PartitionProcessor> partitionProcessorFactory, EventHubClientBuilder eventHubClientBuilder,
        boolean trackLastEnqueuedEventProperties, TracerProvider tracerProvider) {
        this.checkpointStore = checkpointStore;
        this.partitionProcessorFactory = partitionProcessorFactory;
        this.eventHubClientBuilder = eventHubClientBuilder;
        this.trackLastEnqueuedEventProperties = trackLastEnqueuedEventProperties;
        this.tracerProvider = tracerProvider;
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
                logger.warning(Messages.FAILED_CLOSE_CONSUMER_PARTITION, partitionId, ex);
            } finally {
                partitionPumps.remove(partitionId);
            }
        });
    }

    /**
     * Starts a new partition pump for the newly claimed partition. If the partition already has an active partition
     * pump, this will not create a new consumer.
     *
     * @param claimedOwnership The details of partition ownership for which new partition pump is requested to start.
     */
    void startPartitionPump(PartitionOwnership claimedOwnership, Checkpoint checkpoint) {
        if (partitionPumps.containsKey(claimedOwnership.getPartitionId())) {
            logger.info("Consumer is already running for this partition  {}", claimedOwnership.getPartitionId());
            return;
        }

        PartitionContext partitionContext = new PartitionContext(claimedOwnership.getFullyQualifiedNamespace(),
            claimedOwnership.getEventHubName(), claimedOwnership.getConsumerGroup(),
            claimedOwnership.getPartitionId());
        PartitionProcessor partitionProcessor = this.partitionProcessorFactory.get();

        InitializationContext initializationContext = new InitializationContext(partitionContext,
            EventPosition.earliest());
        partitionProcessor.initialize(initializationContext);

        EventPosition startFromEventPosition = null;
        // A checkpoint indicates the last known successfully processed event.
        // So, the event position to start a new partition processing should be exclusive of the
        // offset/sequence number in the checkpoint. If no checkpoint is available, start from
        // the position in set in the InitializationContext (either the earliest event in the partition or
        // the user provided initial position)
        if (checkpoint != null && checkpoint.getOffset() != null) {
            startFromEventPosition = EventPosition.fromOffset(checkpoint.getOffset(), false);
        } else if (checkpoint != null && checkpoint.getSequenceNumber() != null) {
            startFromEventPosition = EventPosition.fromSequenceNumber(checkpoint.getSequenceNumber(), false);
        } else {
            startFromEventPosition = initializationContext.getInitialPosition();
        }

        ReceiveOptions receiveOptions = new ReceiveOptions().setOwnerLevel(0L)
            .setTrackLastEnqueuedEventProperties(trackLastEnqueuedEventProperties);

        EventHubConsumerAsyncClient eventHubConsumer = eventHubClientBuilder.buildAsyncClient()
            .createConsumer(claimedOwnership.getConsumerGroup(), EventHubClientBuilder.DEFAULT_PREFETCH_COUNT);

        partitionPumps.put(claimedOwnership.getPartitionId(), eventHubConsumer);

        // The indentation required by checkstyle is different from the indentation IntelliJ uses.
        // @formatter:off
        eventHubConsumer.receiveFromPartition(claimedOwnership.getPartitionId(), startFromEventPosition, receiveOptions)
            .subscribe(partitionEvent -> {
                EventData eventData = partitionEvent.getData();
                Context processSpanContext = startProcessTracingSpan(eventData, eventHubConsumer.getEventHubName(),
                    eventHubConsumer.getFullyQualifiedNamespace());
                if (processSpanContext.getData(SPAN_CONTEXT_KEY).isPresent()) {
                    eventData.addContext(SPAN_CONTEXT_KEY, processSpanContext);
                }
                try {
                    partitionProcessor.processEvent(new EventContext(partitionContext, eventData, checkpointStore,
                        partitionEvent.getLastEnqueuedEventProperties()));
                    endProcessTracingSpan(processSpanContext, Signal.complete());
                } catch (Throwable throwable) {
                    /* user code for event processing threw an exception - log and bubble up */
                    endProcessTracingSpan(processSpanContext, Signal.error(throwable));
                    throw logger.logExceptionAsError(new RuntimeException("Error in event processing callback",
                        throwable));
                }
            }, /* EventHubConsumer receive() returned an error */
                ex -> handleReceiveError(claimedOwnership, eventHubConsumer, partitionProcessor, ex, partitionContext),
                () -> partitionProcessor.close(new CloseContext(partitionContext,
                    CloseReason.EVENT_PROCESSOR_SHUTDOWN)));
        // @formatter:on
    }

    private void handleReceiveError(PartitionOwnership claimedOwnership, EventHubConsumerAsyncClient eventHubConsumer,
        PartitionProcessor partitionProcessor, Throwable throwable, PartitionContext partitionContext) {
        try {
            logger.warning("Error receiving events for partition {}", partitionContext.getPartitionId(), throwable);
            // if there was an error on receive, it also marks the end of the event data stream
            partitionProcessor.processError(new ErrorContext(partitionContext, throwable));
            // Any exception while receiving events will result in the processor losing ownership
            CloseReason closeReason = CloseReason.LOST_PARTITION_OWNERSHIP;
            partitionProcessor.close(new CloseContext(partitionContext, closeReason));
        } catch (Exception ex) {
            logger.warning(Messages.FAILED_PROCESSING_ERROR_RECEIVE, claimedOwnership.getPartitionId(), ex);
        } finally {
            try {
                // close the consumer
                eventHubConsumer.close();
            } finally {
                // finally, remove the partition from partitionPumps map
                partitionPumps.remove(claimedOwnership.getPartitionId());
            }
        }
    }

    /*
     * Starts a new process tracing span and attaches the returned context to the EventData object for users.
     */
    private Context startProcessTracingSpan(EventData eventData, String eventHubName, String fullyQualifiedNamespace) {
        Object diagnosticId = eventData.getProperties().get(DIAGNOSTIC_ID_KEY);
        if (diagnosticId == null || !tracerProvider.isEnabled()) {
            return Context.NONE;
        }

        Context spanContext = tracerProvider.extractContext(diagnosticId.toString(), Context.NONE);
        Context entityContext = spanContext.addData(ENTITY_PATH_KEY, eventHubName);

        return tracerProvider.startSpan(entityContext.addData(HOST_NAME_KEY, fullyQualifiedNamespace),
            ProcessKind.PROCESS);
    }

    /*
     * Ends the process tracing span and the scope of that span.
     */
    private void endProcessTracingSpan(Context processSpanContext, Signal<Void> signal) {
        Optional<Object> spanScope = processSpanContext.getData(SCOPE_KEY);
        // Disposes of the scope when the trace span closes.
        if (!spanScope.isPresent() || !tracerProvider.isEnabled()) {
            return;
        }
        if (spanScope.get() instanceof Closeable) {
            Closeable close = (Closeable) processSpanContext.getData(SCOPE_KEY).get();
            try {
                close.close();
                tracerProvider.endSpan(processSpanContext, signal);
            } catch (IOException ioException) {
                logger.error(Messages.EVENT_PROCESSOR_RUN_END, ioException);
            }

        } else {
            logger.warning(String.format(Locale.US,
                Messages.PROCESS_SPAN_SCOPE_TYPE_ERROR,
                spanScope.get() != null ? spanScope.getClass() : "null"));
        }
    }
}
