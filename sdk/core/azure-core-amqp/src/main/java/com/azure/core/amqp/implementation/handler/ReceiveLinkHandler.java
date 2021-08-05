// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.util.logging.ClientLogger;
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

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

    public ReceiveLinkHandler(String connectionId, String hostname, String linkName, String entityPath) {
        super(connectionId, hostname, entityPath, new ClientLogger(ReceiveLinkHandler.class));
        this.linkName = linkName;
        this.entityPath = entityPath;
    }

    public String getLinkName() {
        return linkName;
    }

    public Flux<Delivery> getDeliveredMessages() {
        return deliveries.asFlux().doOnNext(queuedDeliveries::remove);
    }

    /**
     * Closes the handler by completing the completing the queued deliveries and deliveries then publishes {@link
     * EndpointState#CLOSED}. {@link #getEndpointStates()} is completely closed when {@link #onLinkRemoteClose(Event)},
     * {@link #onLinkRemoteDetach(Event)}, or {@link #onLinkFinal(Event)} is called.
     */
    @Override
    public void close() {
        if (isTerminated.getAndSet(true)) {
            return;
        }

        deliveries.emitComplete((signalType, emitResult) -> {
            logger.verbose("connectionId[{}], entityPath[{}], linkName[{}] Could not emit complete.",
                getConnectionId(), entityPath, linkName);
            return false;
        });
        queuedDeliveries.forEach(delivery -> {
            // abandon the queued deliveries as the receive link handler is closed
            delivery.disposition(new Modified());
            delivery.settle();
        });
        queuedDeliveries.clear();

        onNext(EndpointState.CLOSED);
    }

    @Override
    public void onLinkLocalOpen(Event event) {
        final Link link = event.getLink();
        if (link instanceof Receiver) {
            logger.verbose("onLinkLocalOpen connectionId[{}], entityPath[{}], linkName[{}], localSource[{}]",
                getConnectionId(), entityPath, link.getName(), link.getSource());
        }
    }

    @Override
    public void onLinkRemoteOpen(Event event) {
        final Link link = event.getLink();
        if (!(link instanceof Receiver)) {
            return;
        }

        if (link.getRemoteSource() != null) {
            logger.info("onLinkRemoteOpen connectionId[{}], entityPath[{}], linkName[{}], remoteSource[{}]",
                getConnectionId(), entityPath, link.getName(), link.getRemoteSource());

            if (!isRemoteActive.getAndSet(true)) {
                onNext(EndpointState.ACTIVE);
            }
        } else {
            logger.info("onLinkRemoteOpen connectionId[{}], entityPath[{}], linkName[{}], action[waitingForError]",
                getConnectionId(), entityPath, link.getName());
        }
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
                    logger.info("onDelivery connectionId[{}], entityPath[{}], linkName[{}], updatedLinkCredit[{}],"
                            + " remoteCredit[{}], remoteCondition[{}], delivery.isSettled[{}] Was already settled.",
                        getConnectionId(), entityPath, link.getName(), link.getCredit(), link.getRemoteCredit(),
                        link.getRemoteCondition(), delivery.isSettled());
                } else {
                    logger.warning("connectionId[{}], entityPath[{}] delivery.isSettled[{}] Settled delivery with no "
                            + " link.",
                        getConnectionId(), entityPath, delivery.isSettled());
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
                    deliveries.emitNext(delivery, (signalType, emitResult) -> {
                        logger.warning("connectionId[{}], entityPath[{}], linkName[{}], emitResult[{}] "
                                + "Could not emit delivery. {}",
                            getConnectionId(), entityPath, linkName, emitResult, delivery);
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
            logger.verbose("onDelivery connectionId[{}], linkName[{}], updatedLinkCredit[{}],"
                    + "remoteCredit[{}], remoteCondition[{}], delivery.isPartial[{}], delivery.isSettled[{}]",
                getConnectionId(), link.getName(), link.getCredit(), link.getRemoteCredit(),
                condition != null && condition.getCondition() != null ? condition : "N/A",
                delivery.isPartial(), wasSettled);
        }
    }

    @Override
    public void onLinkLocalClose(Event event) {
        super.onLinkLocalClose(event);

        // Someone called receiver.close() to set the local link state to close. Since the link was never remotely
        // active, we complete getEndpointStates() ourselves.
        if (!isRemoteActive.get()) {
            logger.info("connectionId[{}] linkName[{}] entityPath[{}] Receiver link was never active. Closing endpoint "
                + "states.", getConnectionId(), getLinkName(), entityPath);

            super.close();
        }
    }

    @Override
    public void onLinkRemoteClose(Event event) {
        if (isTerminated.get()) {
            return;
        }

        deliveries.emitComplete((signalType, emitResult) -> {
            logger.info("connectionId[{}] linkName[{}] signalType[{}] emitResult[{}] Could not complete 'deliveries'.",
                getConnectionId(), linkName, signalType, emitResult);
            return false;
        });

        super.onLinkRemoteClose(event);
    }
}
