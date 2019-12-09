// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * A package-private consumer responsible for reading {@link EventData} from a specific Event Hub partition in the
 * context of a specific consumer group.
 */
class EventHubPartitionAsyncConsumer implements AutoCloseable {
    private static final AtomicReferenceFieldUpdater<EventHubPartitionAsyncConsumer, AmqpReceiveLink>
        RECEIVE_LINK_FIELD_UPDATER = AtomicReferenceFieldUpdater.newUpdater(
        EventHubPartitionAsyncConsumer.class, AmqpReceiveLink.class, "receiveLink");

    // We don't want to dump too many credits on the link at once. It's easy enough to ask for more.
    private static final int MINIMUM_REQUEST = 0;
    private static final int MAXIMUM_REQUEST = 100;

    private final AtomicInteger creditsToRequest = new AtomicInteger(1);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final AtomicReference<LastEnqueuedEventProperties> lastEnqueuedEventProperties = new AtomicReference<>();
    private final ClientLogger logger = new ClientLogger(EventHubPartitionAsyncConsumer.class);
    private final MessageSerializer messageSerializer;
    private final String fullyQualifiedNamespace;
    private final String eventHubName;
    private final String consumerGroup;
    private final String partitionId;
    private final EmitterProcessor<PartitionEvent> emitterProcessor;
    private final Flux<PartitionEvent> messageFlux;
    private final boolean trackLastEnqueuedEventProperties;

    private volatile AmqpReceiveLink receiveLink;

    EventHubPartitionAsyncConsumer(Mono<AmqpReceiveLink> receiveLinkMono, MessageSerializer messageSerializer,
        String fullyQualifiedNamespace, String eventHubName, String consumerGroup, String partitionId,
        int prefetchCount, boolean trackLastEnqueuedEventProperties) {
        this.messageSerializer = messageSerializer;
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.eventHubName = eventHubName;
        this.consumerGroup = consumerGroup;
        this.partitionId = partitionId;
        this.emitterProcessor = EmitterProcessor.create(prefetchCount, false);
        this.trackLastEnqueuedEventProperties = trackLastEnqueuedEventProperties;

        if (trackLastEnqueuedEventProperties) {
            lastEnqueuedEventProperties.set(new LastEnqueuedEventProperties(null, null, null, null));
        }

        // Caching the created link so we don't invoke another link creation.
        this.messageFlux = receiveLinkMono.cache().flatMapMany(link -> {
            if (RECEIVE_LINK_FIELD_UPDATER.compareAndSet(this, null, link)) {
                logger.info("Created AMQP receive link. Initializing prefetch credits: {}", prefetchCount);
                link.addCredits(prefetchCount);

                link.setEmptyCreditListener(() -> {
                    if (emitterProcessor.hasDownstreams()) {
                        return creditsToRequest.get();
                    } else {
                        logger.verbose("Emitter has no downstream subscribers. Not adding credits.");
                        return 0;
                    }
                });

                link.getEndpointStates().subscribe(
                    state -> {
                        logger.verbose("Endpoint state: {}", state);
                    },
                    error -> {
                        logger.info("Error received in AmqpReceiveLink. {}", error.toString());

                        //TODO (conniey): Propagate error to emitter and re-resubscribe for a link if it is transient.
                        close();
                    },
                    () -> {
                        logger.info("Amqp receive link shutting down.");
                        close();
                    });
            }

            return link.receive().map(message -> onMessageReceived(message));
        }).subscribeWith(emitterProcessor)
            .doOnSubscribe(subscription -> {
                AmqpReceiveLink existingLink = RECEIVE_LINK_FIELD_UPDATER.get(this);
                if (existingLink == null) {
                    logger.info("AmqpReceiveLink not set yet.");
                    return;
                }

                logger.verbose("Subscription received for consumer.");
                if (existingLink.getCredits() == 0) {
                    logger.info("Subscription received and there are no remaining credits on the link. Adding more.");
                    existingLink.addCredits(creditsToRequest.get());
                }
            })
            .doOnRequest(request -> {
                if (request < MINIMUM_REQUEST) {
                    logger.warning(Messages.REQUEST_VALUE_NOT_VALID,
                        MINIMUM_REQUEST, MAXIMUM_REQUEST);
                    return;
                }

                final int newRequest = request > MAXIMUM_REQUEST
                    ? MAXIMUM_REQUEST
                    : (int) request;

                logger.verbose("Back pressure request. Old value: {}. New value: {}", creditsToRequest.get(),
                    newRequest);
                creditsToRequest.set(newRequest);
            });
    }

    /**
     * Disposes of the consumer by closing the underlying connection to the service.
     */
    @Override
    public void close() {
        if (!isDisposed.getAndSet(true)) {
            final AmqpReceiveLink receiveLink = RECEIVE_LINK_FIELD_UPDATER.getAndSet(this, null);
            if (receiveLink != null) {
                receiveLink.close();
            }

            emitterProcessor.onComplete();
        }
    }

    /**
     * Begin consuming events until there are no longer any subscribers.
     *
     * @return A stream of events received from the partition.
     */
    Flux<PartitionEvent> receive() {
        return messageFlux;
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
