// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.implementation.AmqpMetricsProvider;
import com.azure.core.util.logging.LoggingEventBuilder;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Modified;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Extendable;
import org.apache.qpid.proton.engine.Handler;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Receiver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.core.amqp.implementation.AmqpLoggingUtils.addErrorCondition;
import static com.azure.core.amqp.implementation.ClientConstants.EMIT_RESULT_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.LINK_NAME_KEY;


/**
 * Handler that receives events from its corresponding {@link Receiver}. Handlers must be associated to a
 * {@link Receiver} to receive its events.
 *
 * @see BaseHandler#setHandler(Extendable, Handler)
 * @see Receiver
 */
public class ReceiveLinkHandler extends LinkHandler {
    private final String linkName;

    /**
     * Indicates whether or not the link has ever been remotely active (ie. the service has acknowledged that we have
     * opened a send link to the given entityPath.)
     */
    private final AtomicBoolean isRemoteActive = new AtomicBoolean();
    private final AtomicBoolean isTerminated = new AtomicBoolean();
    private final Sinks.Many<Delivery> deliveries = Sinks.many().multicast().onBackpressureBuffer();
    private final Set<Delivery> queuedDeliveries = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final String entityPath;
    private final AmqpMetricsProvider metricsProvider;

    public ReceiveLinkHandler(String connectionId, String hostname, String linkName, String entityPath, AmqpMetricsProvider metricsProvider) {
        super(connectionId, hostname, entityPath, metricsProvider);
        this.linkName = Objects.requireNonNull(linkName, "'linkName' cannot be null.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.metricsProvider = Objects.requireNonNull(metricsProvider, "'metricsProvider' cannot be null.");
    }

    public String getLinkName() {
        return linkName;
    }

    public Flux<Delivery> getDeliveredMessages() {
        return deliveries.asFlux().doOnNext(this::removeQueuedDelivery);
    }

    /**
     * Closes the handler by completing the queued deliveries and deliveries then publishes {@link
     * EndpointState#CLOSED}. {@link #getEndpointStates()} is completely closed when {@link #onLinkRemoteClose(Event)},
     * {@link #onLinkRemoteDetach(Event)}, or {@link #onLinkFinal(Event)} is called.
     */
    @Override
    public void close() {
        if (isTerminated.getAndSet(true)) {
            return;
        }

        clearAndCompleteDeliveries("Could not emit deliveries.close when closing handler.");

        onNext(EndpointState.CLOSED);
    }

    @Override
    public void onLinkLocalOpen(Event event) {
        final Link link = event.getLink();
        if (link instanceof Receiver) {
            logger.atVerbose()
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .addKeyValue(LINK_NAME_KEY, link.getName())
                .addKeyValue("localSource", link.getSource())
                .log("onLinkLocalOpen");
        }
    }

    @Override
    public void onLinkRemoteOpen(Event event) {
        final Link link = event.getLink();
        if (!(link instanceof Receiver)) {
            return;
        }

        LoggingEventBuilder logBuilder =  logger.atInfo()
            .addKeyValue(ENTITY_PATH_KEY, entityPath)
            .addKeyValue(LINK_NAME_KEY, link.getName());

        if (link.getRemoteSource() != null) {
            logBuilder.addKeyValue("remoteSource", link.getRemoteSource());

            if (!isRemoteActive.getAndSet(true)) {
                onNext(EndpointState.ACTIVE);
            }
        } else {
            logBuilder
                .addKeyValue("action", "waitingForError");
        }

        logBuilder.log("onLinkRemoteOpen");
    }

    @Override
    public void onDelivery(Event event) {
        if (!isRemoteActive.getAndSet(true)) {
            onNext(EndpointState.ACTIVE);
        }

        final Delivery delivery = event.getDelivery();
        final Receiver link = (Receiver) delivery.getLink();

        // If a message spans across deliveries (for ex: 200kb message will be 4 frames (deliveries) 64k 64k 64k 8k),
        // all until "last-1" deliveries will be partial
        // reactor will raise onDelivery event for all of these - we only need the last one
        final boolean wasSettled = delivery.isSettled();
        if (!delivery.isPartial()) {
            // One of our customers hit an issue - where duplicate 'Delivery' events are raised to Reactor in
            // proton-j layer
            // While processing the duplicate event - reactor hits an IllegalStateException in proton-j layer
            // before we fix proton-j - this work around ensures that we ignore the duplicate Delivery event
            if (wasSettled) {
                if (link != null) {
                    addErrorCondition(logger.atInfo(), link.getRemoteCondition())
                        .addKeyValue(ENTITY_PATH_KEY, entityPath)
                        .addKeyValue(LINK_NAME_KEY, linkName)
                        .addKeyValue("updatedLinkCredit", link.getCredit())
                        .addKeyValue("remoteCredit", link.getRemoteCredit())
                        .addKeyValue("delivery.isSettled", wasSettled)
                        .log("onDelivery. Was already settled.");
                } else {
                    logger.atWarning()
                        .addKeyValue(ENTITY_PATH_KEY, entityPath)
                        .addKeyValue("delivery.isSettled", wasSettled)
                        .log("Settled delivery with no link.");
                }
            } else {
                if (link.getLocalState() == EndpointState.CLOSED) {
                    // onDelivery() method may get called even after the local and remote link states are CLOSED.
                    // So, when the local link is CLOSED, we just abandon the delivery.
                    // Not settling every delivery will result in `TransportSession` storing all unsettled deliveries
                    // in the session leading to a memory leak when multiple links are opened and closed in the same
                    // session.
                    delivery.disposition(new Modified());
                    delivery.settle();
                } else {
                    queuedDeliveries.add(delivery);
                    metricsProvider.recordReceiveQueue(1);

                    deliveries.emitNext(delivery, (signalType, emitResult) -> {
                        logger.atWarning()
                            .addKeyValue(ENTITY_PATH_KEY, entityPath)
                            .addKeyValue(LINK_NAME_KEY, linkName)
                            .addKeyValue(EMIT_RESULT_KEY, emitResult)
                            .log("Could not emit delivery. {}", delivery);

                        if (emitResult == Sinks.EmitResult.FAIL_OVERFLOW
                            && link.getLocalState() != EndpointState.CLOSED) {
                            link.setCondition(new ErrorCondition(Symbol.getSymbol("delivery-buffer-overflow"),
                                "Deliveries are not processed fast enough. Closing local link."));
                            link.close();

                            return true;
                        } else {
                            return false;
                        }
                    });
                }
            }
        }

        if (link != null) {
            final ErrorCondition condition = link.getRemoteCondition();
            addErrorCondition(logger.atVerbose(), condition)
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .addKeyValue(LINK_NAME_KEY, linkName)
                .addKeyValue("updatedLinkCredit", link.getCredit())
                .addKeyValue("remoteCredit", link.getRemoteCredit())
                .addKeyValue("delivery.isPartial", delivery.isPartial())
                .addKeyValue("delivery.isSettled", wasSettled)
                .log("onDelivery.");
        }
    }

    @Override
    public void onLinkLocalClose(Event event) {
        super.onLinkLocalClose(event);

        // Someone called receiver.close() to set the local link state to close. Since the link was never remotely
        // active, we complete getEndpointStates() ourselves.
        if (!isRemoteActive.get()) {
            logger.atInfo()
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .addKeyValue(LINK_NAME_KEY, linkName)
                .log("Receiver link was never active. Closing endpoint states");

            super.close();
        }
    }

    @Override
    public void onLinkRemoteClose(Event event) {
        clearAndCompleteDeliveries("Could not complete 'deliveries' when remotely closed.");

        super.onLinkRemoteClose(event);
    }

    @Override
    public void onLinkFinal(Event event) {
        // Unlikely though, because RemoteClose() or close() would have been called first.
        // In the case that we haven't cleared the pending deliveries.
        close();

        super.onLinkFinal(event);
    }

    /**
     * Clears all pending deliveries and completes the delivery flux.
     *
     * @param errorMessage Message to output if the close operation fails.
     */
    private void clearAndCompleteDeliveries(String errorMessage) {
        deliveries.emitComplete((signalType, emitResult) -> {
            logger.atVerbose()
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .addKeyValue(LINK_NAME_KEY, linkName)
                .log(errorMessage);
            return false;
        });

        metricsProvider.recordReceiveQueue(-1 * queuedDeliveries.size());

        queuedDeliveries.forEach(delivery -> {
            // abandon the queued deliveries as the receive link handler is closed
            delivery.disposition(new Modified());
            delivery.settle();
        });
        queuedDeliveries.clear();
    }


    private void removeQueuedDelivery(Delivery delivery) {
        queuedDeliveries.remove(delivery);
        metricsProvider.recordReceiveQueue(-1);
    }
}
