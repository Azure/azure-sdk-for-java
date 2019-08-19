// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.CheckpointManager;
import com.azure.messaging.eventhubs.CloseReason;
import com.azure.messaging.eventhubs.EventHubAsyncClient;
import com.azure.messaging.eventhubs.EventHubAsyncConsumer;
import com.azure.messaging.eventhubs.EventHubConsumer;
import com.azure.messaging.eventhubs.EventProcessor;
import com.azure.messaging.eventhubs.PartitionManager;
import com.azure.messaging.eventhubs.PartitionProcessor;
import com.azure.messaging.eventhubs.PartitionProcessorFactory;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The partition pump manager that keeps track of all the partition pumps started by this {@link EventProcessor}. Each
 * partition pump is an {@link EventHubConsumer} that is receiving events from partitions this {@link EventProcessor}
 * has claimed ownership of.
 *
 * <p>
 * New partition pumps are created when this {@link EventProcessor} claims ownership of a new partition. When the {@link
 * EventProcessor} is requested to stop, this class is responsible for stopping all event processing tasks and closing
 * all connections to the Event Hub.
 * </p>
 */
public class PartitionPumpManager {

    private final ClientLogger logger = new ClientLogger(PartitionPumpManager.class);
    private final Map<String, EventHubAsyncConsumer> partitionPumps = new ConcurrentHashMap<>();
    private final PartitionManager partitionManager;
    private final PartitionProcessorFactory partitionProcessorFactory;
    private final EventPosition initialEventPosition;
    private final EventHubAsyncClient eventHubAsyncClient;

    /**
     * Creates an instance of partition pump manager.
     *
     * @param partitionManager The partition manager that is used by {@link CheckpointManager} to update checkpoints of
     * partitions.
     * @param partitionProcessorFactory The partition processor factory that is used to create new instances of {@link
     * PartitionProcessor} when new partition pumps are started.
     * @param initialEventPosition The initial event position to use when a new partition pump is created and no
     * checkpoint for the partition is available.
     * @param eventHubAsyncClient The client used to receive events from the Event Hub.
     */
    public PartitionPumpManager(PartitionManager partitionManager, PartitionProcessorFactory partitionProcessorFactory,
        EventPosition initialEventPosition, EventHubAsyncClient eventHubAsyncClient) {
        this.partitionManager = partitionManager;
        this.partitionProcessorFactory = partitionProcessorFactory;
        this.initialEventPosition = initialEventPosition;
        this.eventHubAsyncClient = eventHubAsyncClient;
    }

    /**
     * Stops all partition pumps that are actively consuming events. This method is invoked when the {@link
     * EventProcessor} is requested to stop.
     */
    public void stopAllPartitionPumps() {
        this.partitionPumps.forEach((partitionId, eventHubConsumer) -> {
            try {
                eventHubConsumer.close();
            } catch (Exception ex) {
                logger.warning("Failed to close consumer for partition {}", partitionId, ex);
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
    public void startPartitionPump(PartitionOwnership claimedOwnership) {
        if (partitionPumps.containsKey(claimedOwnership.partitionId())) {
            logger.info("Consumer is already running for this partition  {}", claimedOwnership.partitionId());
            return;
        }

        PartitionContext partitionContext = new PartitionContext(claimedOwnership.partitionId(),
            claimedOwnership.eventHubName(), claimedOwnership.consumerGroupName());
        CheckpointManager checkpointManager = new CheckpointManager(claimedOwnership.ownerId(), partitionContext,
            this.partitionManager, null);
        PartitionProcessor partitionProcessor = this.partitionProcessorFactory
            .createPartitionProcessor(partitionContext, checkpointManager);

        EventPosition startFromEventPosition =
            claimedOwnership.sequenceNumber() == null ? initialEventPosition
                : EventPosition.fromSequenceNumber(claimedOwnership.sequenceNumber());

        EventHubConsumerOptions eventHubConsumerOptions = new EventHubConsumerOptions().ownerLevel(0L);
        EventHubAsyncConsumer eventHubConsumer = eventHubAsyncClient
            .createConsumer(claimedOwnership.consumerGroupName(), claimedOwnership.partitionId(),
                startFromEventPosition,
                eventHubConsumerOptions);

        partitionPumps.put(claimedOwnership.partitionId(), eventHubConsumer);
        eventHubConsumer.receive().subscribe(eventData -> {
                try {
                    partitionProcessor.processEvent(eventData).subscribe(unused -> {
                    }, /* event processing returned error */ ex -> handleError(claimedOwnership, eventHubConsumer,
                        partitionProcessor, ex));
                } catch (Exception ex) {
                    /* event processing threw an exception */
                    handleError(claimedOwnership, eventHubConsumer, partitionProcessor, ex);
                }
            },
            /* EventHubConsumer receive() returned an error */
            ex -> handleError(claimedOwnership, eventHubConsumer, partitionProcessor, ex),
            () -> partitionProcessor.close(CloseReason.EVENT_PROCESSOR_SHUTDOWN));
    }

    private void handleError(PartitionOwnership claimedOwnership, EventHubAsyncConsumer eventHubConsumer,
        PartitionProcessor partitionProcessor, Throwable error) {
        try {
            // if there was an error, it also marks the end of the event data stream
            partitionProcessor.processError(error);

            // If the exception indicates that the partition was stolen (i.e some other consumer with same ownerlevel
            // started consuming the partition), close the partition processor
            // TODO: Find right exception type to determine stolen partition
            if (error instanceof AmqpException) {
                partitionProcessor.close(CloseReason.LOST_PARTITION_OWNERSHIP);
            }

            // close the consumer
            eventHubConsumer.close();
        } catch (Exception ex) {
            logger.warning("Failed while processing error on receive {}", claimedOwnership.partitionId(), ex);
        } finally {
            // finally, remove the partition from partitionPumps map
            partitionPumps.remove(claimedOwnership.partitionId());
        }
    }
}
