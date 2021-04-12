// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.AmqpReceiveLinkProcessor;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * A package-private consumer responsible for reading {@link EventData} from a specific Event Hub partition in the
 * context of a specific consumer group.
 */
class EventHubPartitionAsyncConsumer implements AutoCloseable {
    private final ClientLogger logger = new ClientLogger(EventHubPartitionAsyncConsumer.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final AtomicReference<LastEnqueuedEventProperties> lastEnqueuedEventProperties = new AtomicReference<>();
    private final AmqpReceiveLinkProcessor amqpReceiveLinkProcessor;
    private final MessageSerializer messageSerializer;
    private final String fullyQualifiedNamespace;
    private final String eventHubName;
    private final String consumerGroup;
    private final String partitionId;
    private final boolean trackLastEnqueuedEventProperties;
    private final Flux<PartitionEvent> emitterProcessor;
    private final EventPosition initialPosition;

    private volatile Long currentOffset;

    EventHubPartitionAsyncConsumer(AmqpReceiveLinkProcessor amqpReceiveLinkProcessor,
        MessageSerializer messageSerializer, String fullyQualifiedNamespace, String eventHubName, String consumerGroup,
        String partitionId, AtomicReference<Supplier<EventPosition>> currentEventPosition,
        boolean trackLastEnqueuedEventProperties) {
        this.initialPosition = Objects.requireNonNull(currentEventPosition.get().get(),
            "'currentEventPosition.get().get()' cannot be null.");
        this.amqpReceiveLinkProcessor = amqpReceiveLinkProcessor;
        this.messageSerializer = messageSerializer;
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.eventHubName = eventHubName;
        this.consumerGroup = consumerGroup;
        this.partitionId = partitionId;
        this.trackLastEnqueuedEventProperties = trackLastEnqueuedEventProperties;

        if (trackLastEnqueuedEventProperties) {
            lastEnqueuedEventProperties.set(new LastEnqueuedEventProperties(null, null, null, null));
        }

        currentEventPosition.set(() -> {
            final Long offset = currentOffset;
            return offset == null
                ? initialPosition
                : EventPosition.fromOffset(offset);
        });

        this.emitterProcessor = amqpReceiveLinkProcessor
            .map(this::onMessageReceived)
            .doOnNext(event -> {
                // Keep track of the last position so if the link goes down, we don't start from the original location.
                final Long offset = event.getData().getOffset();
                if (offset != null) {
                    currentOffset = offset;
                } else {
                    logger.warning(
                        "Offset for received event should not be null. Partition Id: {}. Consumer group: {}. Data: {}",
                        event.getPartitionContext().getPartitionId(), event.getPartitionContext().getConsumerGroup(),
                        event.getData().getBodyAsString());
                }
            });
    }

    /**
     * Disposes of the consumer by closing the underlying connection to the service.
     */
    @Override
    public void close() {
        if (!isDisposed.getAndSet(true)) {
            if (!amqpReceiveLinkProcessor.isTerminated()) {
                // cancel only if the processor is not already terminated.
                amqpReceiveLinkProcessor.cancel();
            }
            logger.info("Closed consumer for partition {}", this.partitionId);
        }
    }

    /**
     * Begin consuming events until there are no longer any subscribers.
     *
     * @return A stream of events received from the partition.
     */
    Flux<PartitionEvent> receive() {
        return emitterProcessor;
    }

    /**
     * On each message received from the service, it will try to:
     * <ol>
     * <li>Deserialize the message into an {@link EventData}.</li>
     * <li>If {@link ReceiveOptions#getTrackLastEnqueuedEventProperties()} is true, then it will try to update
     * {@link LastEnqueuedEventProperties}.</li>
     * </ol>
     *
     * @param message AMQP message to deserialize.
     *
     * @return The deserialized {@link EventData} with partition information.
     */
    private PartitionEvent onMessageReceived(Message message) {
        final EventData event = messageSerializer.deserialize(message, EventData.class);

        if (trackLastEnqueuedEventProperties) {
            final LastEnqueuedEventProperties enqueuedEventProperties =
                messageSerializer.deserialize(message, LastEnqueuedEventProperties.class);

            if (enqueuedEventProperties != null) {
                final LastEnqueuedEventProperties updated = new LastEnqueuedEventProperties(
                    enqueuedEventProperties.getSequenceNumber(), enqueuedEventProperties.getOffset(),
                    enqueuedEventProperties.getEnqueuedTime(), enqueuedEventProperties.getRetrievalTime());
                lastEnqueuedEventProperties.set(updated);
            }
        }

        final PartitionContext partitionContext = new PartitionContext(fullyQualifiedNamespace, eventHubName,
            consumerGroup, partitionId);
        return new PartitionEvent(partitionContext, event, lastEnqueuedEventProperties.get());
    }
}
