// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.AmqpReceiveLink;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static com.azure.messaging.eventhubs.EventHubConsumerOptions.MAXIMUM_PREFETCH_COUNT;
import static com.azure.messaging.eventhubs.EventHubConsumerOptions.MINIMUM_PREFETCH_COUNT;

/**
 * A consumer responsible for reading {@link EventData} from a specific Event Hub partition and as a member of a
 * specific consumer group.
 *
 * <ul>
 * <li>If {@link EventHubConsumer} is created where {@link EventHubConsumerOptions#ownerLevel()} has a
 * value, then Event Hubs service will guarantee only one active consumer exists per partitionId and consumer group
 * combination. This consumer is sometimes referred to as an "Epoch Consumer."</li>
 * <li>Multiple consumers per partitionId and consumer group combination can be created by not setting
 * {@link EventHubConsumerOptions#ownerLevel()} when creating consumers. A maximum of five concurrent consumers are
 * allowed on the same partition and consumer group. This non-exclusive consumer is sometimes referred to as a
 * "Non-Epoch Consumer."</li>
 * </ul>
 *
 * <p><strong>Consuming events from Event Hub</strong></p>
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumer.receive}
 *
 * <p><string>Rate limiting consumption of events from Event Hub</string></p>
 *
 * For event consumers that need to limit the number of events they receive at a given time, they can use
 * {@link BaseSubscriber#request(long)}.
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumer.receiveBackpressure}
 *
 * @see EventHubClient#createConsumer(String, String, EventPosition)
 * @see EventHubClient#createConsumer(String, String, EventPosition, EventHubConsumerOptions)
 */
public class EventHubConsumer implements Closeable {
    private static final AtomicReferenceFieldUpdater<EventHubConsumer, AmqpReceiveLink> RECEIVE_LINK_FIELD_UPDATER =
        AtomicReferenceFieldUpdater.newUpdater(EventHubConsumer.class, AmqpReceiveLink.class, "receiveLink");

    private final AtomicInteger creditsToRequest = new AtomicInteger(1);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final ClientLogger logger = new ClientLogger(EventHubConsumer.class);
    private final EmitterProcessor<EventData> emitterProcessor;
    private final Flux<EventData> messageFlux;

    private volatile AmqpReceiveLink receiveLink;

    EventHubConsumer(Mono<AmqpReceiveLink> receiveLinkMono, EventHubConsumerOptions options, Duration operationTimeout) {
        this.emitterProcessor = EmitterProcessor.create(options.prefetchCount(), false);

        // Caching the created link so we don't invoke another link creation.
        this.messageFlux = receiveLinkMono.flatMapMany(link -> {
            if (RECEIVE_LINK_FIELD_UPDATER.compareAndSet(this, null, link)) {
                logger.asInfo().log("Created AMQP receive link. Initialising prefetch credit: {}", options.prefetchCount());
                link.addCredits(options.prefetchCount());

                link.setEmptyCreditListener(() -> {
                    if (emitterProcessor.hasDownstreams()) {
                        return creditsToRequest.get();
                    } else {
                        logger.asVerbose().log("Emitter has no downstream subscribers. Not adding credits.");
                        return 0;
                    }
                });
            }

            return link.receive().map(EventData::new);
        }).timeout(operationTimeout)
            .subscribeWith(emitterProcessor)
            .doOnSubscribe(subscription -> {
                AmqpReceiveLink existingLink = RECEIVE_LINK_FIELD_UPDATER.get(this);
                if (existingLink == null) {
                    logger.asInfo().log("AmqpReceiveLink not set yet.");
                    return;
                }

                logger.asInfo().log("Subscription received for consumer.");
                if (existingLink.getCredits() == 0) {
                    logger.asInfo().log("Subscription received and there are no remaining credits on the link. Adding more.");
                    existingLink.addCredits(creditsToRequest.get());
                }
            })
            .doOnRequest(request -> {
                if (request < MINIMUM_PREFETCH_COUNT) {
                    logger.asWarning().log("Back pressure request value not valid. It must be between {} and {}.",
                        MINIMUM_PREFETCH_COUNT, MAXIMUM_PREFETCH_COUNT);
                    return;
                }

                long newRequest = request > MAXIMUM_PREFETCH_COUNT
                    ? MAXIMUM_PREFETCH_COUNT
                    : request;

                logger.asInfo().log("Back pressure request. Old value: {}. New value: {}", creditsToRequest.get(), newRequest);
                creditsToRequest.set((int) newRequest);
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
        }
    }

    /**
     * Begin consuming events until there are no longer any subscribers, or the parent {@link EventHubClient#close()
     * EventHubClient.close()} is called.
     *
     * <p><strong>Consuming events from Event Hub</strong></p>
     *
     * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumer.receive}
     *
     * @return A stream of events for this consumer.
     */
    public Flux<EventData> receive() {
        return messageFlux;
    }
}
