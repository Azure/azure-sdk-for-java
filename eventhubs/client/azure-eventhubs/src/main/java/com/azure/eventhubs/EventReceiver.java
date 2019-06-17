// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.AmqpLink;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.eventhubs.implementation.AmqpReceiveLink;
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
 * A {@link EventReceiver#receive()} is tied to a Event Hub partitionId + consumer group combination.
 *
 * <ul>
 * <li>If {@link EventReceiver} is created where {@link EventReceiverOptions#exclusiveReceiverPriority()} has a
 * value, then Event Hubs service will guarantee only 1 active receiver exists per partitionId and consumer group
 * combination. This is the recommended approach to create a {@link EventReceiver}.</li>
 * <li>Multiple receivers per partitionId and consumer group combination can be created by not setting
 * {@link EventReceiverOptions#exclusiveReceiverPriority()} when creating receivers.</li>
 * </ul>
 *
 * @see EventHubClient#createReceiver(String, EventPosition)
 * @see EventHubClient#createReceiver(String, EventPosition, EventReceiverOptions)
 */
public class EventReceiver implements Closeable {
    private static final AtomicReferenceFieldUpdater<EventReceiver, AmqpReceiveLink> RECEIVE_LINK_FIELD_UPDATER =
        AtomicReferenceFieldUpdater.newUpdater(EventReceiver.class, AmqpReceiveLink.class, "receiveLink");

    private final Mono<AmqpReceiveLink> receiveLinkMono;
    private final Duration operationTimeout;
    private final AtomicInteger creditsToRequest = new AtomicInteger(1);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final ServiceLogger logger = new ServiceLogger(EventReceiver.class);
    private final EmitterProcessor<EventData> emitterProcessor;
    private final Flux<EventData> messageFlux;

    private volatile PartitionProperties partitionInformation = null;
    private volatile AmqpReceiveLink receiveLink;

    EventReceiver(Mono<AmqpReceiveLink> receiveLinkMono, EventReceiverOptions options, Duration operationTimeout) {
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
     * Disposes of the EventReceiver by closing the underlying connection to the service.
     *
     * @throws IOException if the underlying {@link AmqpLink} and its resources could not be disposed.
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
     * Gets the most recent information about a partition by the current receiver.
     *
     * @return If {@link EventReceiverOptions}
     */
    public PartitionProperties partitionInformation() {
        return partitionInformation;
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
