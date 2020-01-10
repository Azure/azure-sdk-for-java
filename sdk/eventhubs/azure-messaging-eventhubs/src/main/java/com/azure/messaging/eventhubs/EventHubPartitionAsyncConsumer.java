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
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
    private final EmitterProcessor<PartitionEvent> emitterProcessor;

    EventHubPartitionAsyncConsumer(AmqpReceiveLinkProcessor amqpReceiveLinkProcessor,
        MessageSerializer messageSerializer, String fullyQualifiedNamespace, String eventHubName, String consumerGroup,
        String partitionId, AtomicReference<EventPosition> currentEventPosition,
        boolean trackLastEnqueuedEventProperties) {
        this.amqpReceiveLinkProcessor = amqpReceiveLinkProcessor;
        this.messageSerializer = messageSerializer;
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.eventHubName = eventHubName;
        this.consumerGroup = consumerGroup;
        this.partitionId = partitionId;
        this.emitterProcessor = amqpReceiveLinkProcessor
            .map(message -> onMessageReceived(message))
            .doOnNext(event -> {
                // Keep track of the last position so if the link goes down, we don't start from the original location.
                final Long offset = event.getData().getOffset();
                if (offset != null) {
                    currentEventPosition.set(EventPosition.fromOffset(offset));
                } else {
                    logger.warning(
                        "Offset for received event should not be null. Partition Id: {}. Consumer group: {}. Data: {}",
                        event.getPartitionContext().getPartitionId(), event.getPartitionContext().getConsumerGroup(),
                        event.getData().getBodyAsString());
                }
            })
            .subscribeWith(EmitterProcessor.create(false));
        this.trackLastEnqueuedEventProperties = trackLastEnqueuedEventProperties;

        if (trackLastEnqueuedEventProperties) {
            lastEnqueuedEventProperties.set(new LastEnqueuedEventProperties(null, null, null, null));
        }
    }

    /**
     * Disposes of the consumer by closing the underlying connection to the service.
     */
    @Override
    public void close() {
        if (!isDisposed.getAndSet(true)) {
            emitterProcessor.onComplete();
            amqpReceiveLinkProcessor.cancel();
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
