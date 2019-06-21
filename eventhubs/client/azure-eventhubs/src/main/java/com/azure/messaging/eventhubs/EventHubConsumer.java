// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.AmqpReceiveLink;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * This is a logical representation of receiving from an Event Hub partition.
 *
 * <p>
 * A {@link EventHubConsumer#receive()} is tied to a Event Hub partitionId + consumer group combination.
 *
 * <ul>
 * <li>If {@link EventHubConsumer} is created where {@link EventHubConsumerOptions#ownerLevel()} has a
 * value, then Event Hubs service will guarantee only one active receiver exists per partitionId and consumer group
 * combination. This is the recommended approach to create a {@link EventHubConsumer}.</li>
 * <li>Multiple consumers per partitionId and consumer group combination can be created by not setting
 * {@link EventHubConsumerOptions#ownerLevel()} when creating receivers.</li>
 * </ul>
 *
 * @see EventHubClient#createConsumer(String, String, EventPosition)
 * @see EventHubClient#createConsumer(String, String, EventPosition, EventHubConsumerOptions)
 */
public class EventHubConsumer implements Closeable {
    private static final AtomicReferenceFieldUpdater<EventHubConsumer, AmqpReceiveLink> RECEIVE_LINK_FIELD_UPDATER =
        AtomicReferenceFieldUpdater.newUpdater(EventHubConsumer.class, AmqpReceiveLink.class, "receiveLink");

    private final Mono<AmqpReceiveLink> receiveLinkMono;
    private final Duration operationTimeout;
    private final AtomicInteger creditsToRequest = new AtomicInteger(1);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final ClientLogger logger = new ClientLogger(EventHubConsumer.class);
    private final EmitterProcessor<EventData> emitterProcessor;
    private final Flux<EventData> messageFlux;

    private volatile AmqpReceiveLink receiveLink;

    EventHubConsumer(Mono<AmqpReceiveLink> receiveLinkMono, EventHubConsumerOptions options, Duration operationTimeout) {
        this.receiveLinkMono = receiveLinkMono;
        this.emitterProcessor = EmitterProcessor.create(options.prefetchCount(), false);
        this.operationTimeout = operationTimeout;

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
        }).subscribeWith(emitterProcessor);

        emitterProcessor.doOnSubscribe(subscription -> {
            if (receiveLink.getCredits() == 0) {
                logger.asInfo().log("Subscription received and there are no remaining credits on the link. Adding more.");
                receiveLink.addCredits(creditsToRequest.get());
            }
        }).doOnRequest(request -> {
            logger.asInfo().log("Back pressure requested. Old value: {}. New value: {}", creditsToRequest.get(), request);
            creditsToRequest.set((int) request);
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

            emitterProcessor.dispose();
        }
    }

    /**
     * Begin receiving events until there are no longer any subscribers, or the parent
     * {@link EventHubClient#close() EventHubClient.close()} is called.
     *
     * @return A stream of events for this receiver.
     */
    public Flux<EventData> receive() {
        return messageFlux;
    }
}
