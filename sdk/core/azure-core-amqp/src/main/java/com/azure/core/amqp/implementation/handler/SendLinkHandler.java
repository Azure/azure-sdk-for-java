// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Extendable;
import org.apache.qpid.proton.engine.Handler;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Sender;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handler that receives events from its corresponding {@link Sender}. Handlers must be associated to a {@link Sender}
 * to receive its events.
 *
 * @see BaseHandler#setHandler(Extendable, Handler)
 * @see Sender
 */
public class SendLinkHandler extends LinkHandler {
    private final String linkName;
    private final String entityPath;
    /**
     * Indicates whether or not the link has ever been remotely active (ie. the service has acknowledged that we have
     * opened a send link to the given entityPath.)
     */
    private final AtomicBoolean isRemoteActive = new AtomicBoolean();
    private final AtomicBoolean isTerminated = new AtomicBoolean();
    private final Sinks.Many<Integer> creditProcessor = Sinks.many().unicast().onBackpressureBuffer();
    private final Sinks.Many<Delivery> deliveryProcessor = Sinks.many().multicast().onBackpressureBuffer();

    public SendLinkHandler(String connectionId, String hostname, String linkName, String entityPath) {
        super(connectionId, hostname, entityPath, new ClientLogger(SendLinkHandler.class));
        this.linkName = linkName;
        this.entityPath = entityPath;
    }

    public String getLinkName() {
        return linkName;
    }

    public Flux<Integer> getLinkCredits() {
        return creditProcessor.asFlux();
    }

    public Flux<Delivery> getDeliveredMessages() {
        return deliveryProcessor.asFlux();
    }

    /**
     * Closes the handler by completing the completing the delivery and link credit fluxes and publishes {@link
     * EndpointState#CLOSED}. {@link #getEndpointStates()} is completely closed when {@link #onLinkRemoteClose(Event)},
     * {@link #onLinkRemoteDetach(Event)}, or {@link #onLinkFinal(Event)} is called.
     */
    @Override
    public void close() {
        if (isTerminated.getAndSet(true)) {
            return;
        }

        creditProcessor.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
        deliveryProcessor.emitComplete((signalType, emitResult) -> {
            logger.verbose("connectionId[{}] linkName[{}] result[{}] Unable to emit complete on deliverySink.",
                getConnectionId(), linkName, emitResult);
            return false;
        });

        onNext(EndpointState.CLOSED);
    }

    @Override
    public void onLinkLocalOpen(Event event) {
        final Link link = event.getLink();
        if (link instanceof Sender) {
            logger.verbose("onLinkLocalOpen connectionId[{}], entityPath[{}], linkName[{}], localTarget[{}]",
                getConnectionId(), entityPath, link.getName(), link.getTarget());
        }
    }

    @Override
    public void onLinkRemoteOpen(Event event) {
        final Link link = event.getLink();
        if (!(link instanceof Sender)) {
            return;
        }

        if (link.getRemoteTarget() != null) {
            logger.info("onLinkRemoteOpen connectionId[{}], entityPath[{}], linkName[{}], remoteTarget[{}]",
                getConnectionId(), entityPath, link.getName(), link.getRemoteTarget());

            if (!isRemoteActive.getAndSet(true)) {
                onNext(EndpointState.ACTIVE);
            }
        } else {
            logger.info("onLinkRemoteOpen connectionId[{}], entityPath[{}], linkName[{}], remoteTarget[null],"
                    + " remoteSource[null], action[waitingForError]",
                getConnectionId(), entityPath, link.getName());
        }
    }

    @Override
    public void onLinkFlow(Event event) {
        if (!isRemoteActive.getAndSet(true)) {
            onNext(EndpointState.ACTIVE);
        }

        final Sender sender = event.getSender();
        final int credits = sender.getRemoteCredit();
        creditProcessor.emitNext(credits, (signalType, emitResult) -> {
            logger.verbose("connectionId[{}] linkName[{}] result[{}] Unable to emit credits [{}].",
                getConnectionId(), linkName, emitResult, credits);
            return false;
        });

        logger.verbose("onLinkFlow connectionId[{}] linkName[{}] unsettled[{}] credit[{}]",
            getConnectionId(), sender.getName(), sender.getUnsettled(), sender.getCredit());
    }

    @Override
    public void onLinkLocalClose(Event event) {
        super.onLinkLocalClose(event);

        // Someone called sender.close() to set the local link state to close. Since the link was never remotely
        // active, we complete getEndpointStates() ourselves.
        if (!isRemoteActive.get()) {
            logger.info("connectionId[{}] linkName[{}] entityPath[{}] Sender link was never active. Closing endpoint "
                + "states.", getConnectionId(), getLinkName(), entityPath);

            super.close();
        }
    }

    @Override
    public void onDelivery(Event event) {
        Delivery delivery = event.getDelivery();

        while (delivery != null) {
            final Sender sender = (Sender) delivery.getLink();
            final String deliveryTag = new String(delivery.getTag(), StandardCharsets.UTF_8);

            logger.verbose("onDelivery connectionId[{}] linkName[{}] unsettled[{}] credit[{}],"
                    + " deliveryState[{}] delivery.isBuffered[{}] delivery.id[{}]",
                getConnectionId(), sender.getName(), sender.getUnsettled(), sender.getRemoteCredit(),
                delivery.getRemoteState(), delivery.isBuffered(), deliveryTag);

            deliveryProcessor.emitNext(delivery, (signalType, emitResult) -> {
                logger.warning("connectionId[{}] linkName[{}] result[{}] Unable to emit delivery [{}].",
                    getConnectionId(), linkName, emitResult, deliveryTag);

                return emitResult == Sinks.EmitResult.FAIL_OVERFLOW;
            });

            delivery.settle();
            delivery = sender.current();
        }
    }
}
