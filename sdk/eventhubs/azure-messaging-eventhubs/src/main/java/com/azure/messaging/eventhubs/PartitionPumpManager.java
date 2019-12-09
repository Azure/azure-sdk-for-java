// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.messaging.eventhubs.implementation.PartitionProcessor;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.CloseReason;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.InitializationContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import reactor.core.publisher.Signal;

import java.io.Closeable;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.SCOPE_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;

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
    private final EventPosition initialEventPosition;
    private final EventHubClientBuilder eventHubClientBuilder;
    private final TracerProvider tracerProvider;
    private final boolean trackLastEnqueuedEventProperties;

    /**
     * Creates an instance of partition pump manager.
     *
     * @param checkpointStore The partition manager that is used to store and update checkpoints.
     * @param partitionProcessorFactory The partition processor factory that is used to create new instances of {@link
     * PartitionProcessor} when new partition pumps are started.
     * @param initialEventPosition The initial event position to use when a new partition pump is created and no
     * checkpoint for the partition is available.
     * @param eventHubClientBuilder The client builder used to create new clients (and new connections) for each
     * partition processed by this {@link EventProcessorClient}.
     * @param trackLastEnqueuedEventProperties If set to {@code true}, all events received by this
     * EventProcessorClient will also include the last enqueued event properties for it's respective partitions.
     * @param tracerProvider The tracer implementation.
     */
    PartitionPumpManager(CheckpointStore checkpointStore,
        Supplier<PartitionProcessor> partitionProcessorFactory, EventPosition initialEventPosition,
        EventHubClientBuilder eventHubClientBuilder, boolean trackLastEnqueuedEventProperties,
        TracerProvider tracerProvider) {
        this.checkpointStore = checkpointStore;
        this.partitionProcessorFactory = partitionProcessorFactory;
        this.initialEventPosition = initialEventPosition;
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
        if (checkpoint != null && checkpoint.getOffset() != null) {
            startFromEventPosition = EventPosition.fromOffset(checkpoint.getOffset());
        } else if (checkpoint != null && checkpoint.getSequenceNumber() != null) {
            startFromEventPosition = EventPosition.fromSequenceNumber(checkpoint.getSequenceNumber(), true);
        } else {
            startFromEventPosition = initialEventPosition;
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
                Context processSpanContext = startProcessTracingSpan(eventData);
                if (processSpanContext.getData(SPAN_CONTEXT_KEY).isPresent()) {
                    eventData.addContext(SPAN_CONTEXT_KEY, processSpanContext);
                }
                try {
                    partitionProcessor.processEvent(new EventContext(partitionContext, eventData, checkpointStore,
                        partitionEvent.getLastEnqueuedEventProperties()));
                    endProcessTracingSpan(processSpanContext, Signal.complete());
                } catch (Exception ex) {
                    /* event processing threw an exception */
                    handleProcessingError(claimedOwnership, partitionProcessor, ex, partitionContext);
                    endProcessTracingSpan(processSpanContext, Signal.error(ex));

                }
            }, /* EventHubConsumer receive() returned an error */
                ex -> handleReceiveError(claimedOwnership, eventHubConsumer, partitionProcessor, ex, partitionContext),
                () -> partitionProcessor.close(new CloseContext(partitionContext,
                    CloseReason.EVENT_PROCESSOR_SHUTDOWN)));
            // @formatter:on
    }

    private void handleProcessingError(PartitionOwnership claimedOwnership, PartitionProcessor partitionProcessor,
        Throwable error, PartitionContext partitionContext) {
        try {
            // There was an error in process event (user provided code), call process error and if that
            // also fails just log and continue
            partitionProcessor.processError(new ErrorContext(partitionContext, error));
        } catch (Exception ex) {
            logger.warning(Messages.FAILED_WHILE_PROCESSING_ERROR, claimedOwnership.getPartitionId(), ex);
        }
    }

    private void handleReceiveError(PartitionOwnership claimedOwnership, EventHubConsumerAsyncClient eventHubConsumer,
        PartitionProcessor partitionProcessor, Throwable error, PartitionContext partitionContext) {
        try {
            // if there was an error on receive, it also marks the end of the event data stream
            partitionProcessor.processError(new ErrorContext(partitionContext, error));
            CloseReason closeReason = CloseReason.EVENT_HUB_EXCEPTION;
            // If the exception indicates that the partition was stolen (i.e some other consumer with same ownerlevel
            // started consuming the partition), update the closeReason
            // TODO: Find right exception type to determine stolen partition
            if (error instanceof AmqpException) {
                closeReason = CloseReason.LOST_PARTITION_OWNERSHIP;
            }
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
     * Starts a new process tracing span and attached context the EventData object for users.
     */
    private Context startProcessTracingSpan(EventData eventData) {
        Object diagnosticId = eventData.getProperties().get(DIAGNOSTIC_ID_KEY);
        if (diagnosticId == null || !tracerProvider.isEnabled()) {
            return Context.NONE;
        }
        Context spanContext = tracerProvider.extractContext(diagnosticId.toString(), Context.NONE);
        return tracerProvider.startSpan(spanContext, ProcessKind.PROCESS);
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
