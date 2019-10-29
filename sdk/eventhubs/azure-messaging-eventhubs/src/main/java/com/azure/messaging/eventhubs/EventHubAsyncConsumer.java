// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * A consumer responsible for reading {@link EventData} from a specific Event Hub partition in the context of a specific
 * consumer group.
 *
 * <ul>
 * <li>If {@link EventHubAsyncConsumer} is created where {@link EventHubConsumerOptions#getOwnerLevel()} has a
 * value, then Event Hubs service will guarantee only one active consumer exists per partitionId and consumer group
 * combination. This consumer is sometimes referred to as an "Epoch Consumer."</li>
 * <li>Multiple consumers per partitionId and consumer group combination can be created by not setting
 * {@link EventHubConsumerOptions#getOwnerLevel()} when creating consumers. This non-exclusive consumer is sometimes
 * referred to as a "Non-Epoch Consumer."</li>
 * </ul>
 *
 * <p><strong>Consuming events from Event Hub</strong></p>
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubasyncconsumer.receive}
 *
 * <p><strong>Rate limiting consumption of events from Event Hub</strong></p>
 *
 * For event consumers that need to limit the number of events they receive at a given time, they can use {@link
 * BaseSubscriber#request(long)}.
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubasyncconsumer.receive#basesubscriber}
 *
 * @see EventHubConnection#createConsumer(String, String, EventPosition, EventHubConsumerOptions)
 */
@Immutable
public class EventHubAsyncConsumer implements Closeable {
    private static final AtomicReferenceFieldUpdater<EventHubAsyncConsumer, AmqpReceiveLink>
        RECEIVE_LINK_FIELD_UPDATER =
        AtomicReferenceFieldUpdater.newUpdater(EventHubAsyncConsumer.class, AmqpReceiveLink.class, "receiveLink");

    // We don't want to dump too many credits on the link at once. It's easy enough to ask for more.
    private static final int MINIMUM_REQUEST = 0;
    private static final int MAXIMUM_REQUEST = 100;

    private final AtomicInteger creditsToRequest = new AtomicInteger(1);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final AtomicReference<LastEnqueuedEventProperties> lastEnqueuedEventProperties = new AtomicReference<>();
    private final ClientLogger logger = new ClientLogger(EventHubAsyncConsumer.class);
    private final MessageSerializer messageSerializer;
    private final EmitterProcessor<EventData> emitterProcessor;
    private final Flux<EventData> messageFlux;
    private final boolean trackLastEnqueuedEventProperties;

    private volatile AmqpReceiveLink receiveLink;

    EventHubAsyncConsumer(Mono<AmqpReceiveLink> receiveLinkMono, MessageSerializer messageSerializer,
                          EventHubConsumerOptions options) {
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.emitterProcessor = EmitterProcessor.create(options.getPrefetchCount(), false);
        this.trackLastEnqueuedEventProperties = options.getTrackLastEnqueuedEventProperties();

        if (options.getTrackLastEnqueuedEventProperties()) {
            lastEnqueuedEventProperties.set(new LastEnqueuedEventProperties(null, null, null, null));
        }

        // Caching the created link so we don't invoke another link creation.
        this.messageFlux = receiveLinkMono.cache().flatMapMany(link -> {
            if (RECEIVE_LINK_FIELD_UPDATER.compareAndSet(this, null, link)) {
                logger.info("Created AMQP receive link. Initializing prefetch credits: {}", options.getPrefetchCount());
                link.addCredits(options.getPrefetchCount());

                link.setEmptyCreditListener(() -> {
                    if (emitterProcessor.hasDownstreams()) {
                        return creditsToRequest.get();
                    } else {
                        logger.verbose("Emitter has no downstream subscribers. Not adding credits.");
                        return 0;
                    }
                });

                link.getErrors().subscribe(error -> {
                    logger.info("Error received in ReceiveLink. {}", error.toString());

                    //TODO (conniey): Surface error to EmitterProcessor.
                });

                link.getShutdownSignals().subscribe(signal -> {
                    logger.info("Shutting down. Initiated by client? {}. Reason: {}",
                        signal.isInitiatedByClient(), signal.toString());

                    try {
                        close();
                    } catch (IOException e) {
                        logger.error("Error closing consumer: {}", e.toString());
                    }
                });
            }

            return link.receive().map(message -> onMessageReceived(message));
        }).subscribeWith(emitterProcessor)
            .doOnSubscribe(subscription -> {
                AmqpReceiveLink existingLink = RECEIVE_LINK_FIELD_UPDATER.get(this);
                if (existingLink == null) {
                    logger.warning("AmqpReceiveLink not set yet.");
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
                    logger.warning("Back pressure request value not valid. It must be between {} and {}.",
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
     *
     * @throws IOException if the underlying transport and its resources could not be disposed.
     */
    @Override
    public void close() throws IOException {
        if (!isDisposed.getAndSet(true)) {
            final AmqpReceiveLink receiveLink = RECEIVE_LINK_FIELD_UPDATER.getAndSet(this, null);
            if (receiveLink != null) {
                receiveLink.close();
            }

            emitterProcessor.onComplete();
        }
    }

    /**
     * Begin consuming events until there are no longer any subscribers, or the parent {@link
     * EventHubConnection#close() EventHubAsyncClient.close()} is called.
     *
     * <p><strong>Consuming events from Event Hub</strong></p>
     *
     * {@codesnippet com.azure.messaging.eventhubs.eventhubasyncconsumer.receive}
     *
     * @return A stream of events for this consumer.
     */
    public Flux<EventData> receive() {
        return messageFlux;
    }

    /**
     * A set of information about the last enqueued event of a partition, as observed by the consumer as events are
     * received from the Event Hubs service.
     *
     * @return {@code null} if {@link EventHubConsumerOptions#getTrackLastEnqueuedEventProperties()} was not set when
     *     creating the consumer. Otherwise, the properties describing the most recently enqueued event in the
     *     partition.
     */
    public LastEnqueuedEventProperties getLastEnqueuedEventProperties() {
        return lastEnqueuedEventProperties.get();
    }

    /**
     * On each message received from the service, it will try to:
     * 1. Deserialize the message into an EventData
     * 2. If {@link EventHubConsumerOptions#getTrackLastEnqueuedEventProperties()} is true, then it will try to update
     *    {@link LastEnqueuedEventProperties}
     *
     * @param message AMQP message to deserialize.
     *
     * @return The deserialized {@link EventData}.
     */
    private EventData onMessageReceived(Message message) {
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

        return event;
    }
}
