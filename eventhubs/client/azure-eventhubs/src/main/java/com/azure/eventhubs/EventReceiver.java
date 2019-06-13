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
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * This is a logical representation of receiving from a EventHub partition.
 *
 * <p>
 * A {@link EventReceiver#receive()} is tied to a Event Hub PartitionId + consumer group combination.
 *
 * <ul>
 * <li>If the {@link EventReceiver} is created where {@link EventReceiverOptions#exclusiveReceiverPriority()} has a
 * value, then Event Hubs service will guarantee only 1 active receiver exists per partitionId and consumer group
 * combination. This is the recommended approach to create a {@link EventReceiver}.</li>
 * <li>Multiple receivers per partitionId and consumer group combination can be created using non-epoch receivers.</li>
 * </ul>
 *
 * @see EventHubClient#createReceiver(String)
 * @see EventHubClient#createReceiver(String, EventReceiverOptions)
 */
public class EventReceiver implements Closeable {
    private static final AtomicReferenceFieldUpdater<EventReceiver, AmqpReceiveLink> RECEIVE_LINK_FIELD_UPDATER =
        AtomicReferenceFieldUpdater.newUpdater(EventReceiver.class, AmqpReceiveLink.class, "receiveLink");

    private final Mono<AmqpReceiveLink> receiveLinkMono;
    private final EventReceiverOptions options;
    private final Duration operationTimeout;
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final ServiceLogger logger = new ServiceLogger(EventReceiver.class);
    private final EmitterProcessor<EventData> emitterProcessor;
    private Flux<EventData> eventDataConnectableFlux;

    private volatile PartitionProperties partitionInformation = null;
    private volatile AmqpReceiveLink receiveLink;

    EventReceiver(Mono<AmqpReceiveLink> receiveLinkMono, EventReceiverOptions options, Duration operationTimeout) {
        this.emitterProcessor = EmitterProcessor.create(options.prefetchCount(), false);

        // Caching the created link so we don't invoke another link creation.
        this.receiveLinkMono = receiveLinkMono.map(link -> {
            if (RECEIVE_LINK_FIELD_UPDATER.compareAndSet(this, null, link)) {
                logger.asInfo().log("Created AMQP receive link. Subscribing to events.");

                //TODO (conniey): subscribe to on EndpointState and errors, etc.

                eventDataConnectableFlux = link.receive().map(EventData::new).subscribeWith(emitterProcessor)
                    .doOnSubscribe(subscription -> {
                        logger.asInfo().log("Subscription received. Adding credits.");
                    }).doOnRequest(request -> {
                        logger.asInfo().log("More events requested: {}", request);
                        final int credits = link.credits();
                        if (credits < request) {
                            logger.asInfo().log("Adding more credits. Current: [{}]. Requested: [{}]", credits, request);
                            link.addCredits((int) request);
                        }
                    });
            }

            return RECEIVE_LINK_FIELD_UPDATER.get(this);
        }).cache();
        this.options = options;
        this.operationTimeout = operationTimeout;
    }

    /**
     * Disposes of the EventReceiver by closing the underlying connection to the service.
     *
     * @throws IOException if the underlying {@link AmqpLink} and its resources could not be disposed.
     */
    @Override
    public void close() throws IOException {
        if (!isDisposed.getAndSet(true)) {
            final AmqpReceiveLink block = receiveLinkMono.block(operationTimeout);
            if (block != null) {
                block.close();
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
        return eventDataConnectableFlux;
    }
}
