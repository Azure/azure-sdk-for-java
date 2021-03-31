// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Sender;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class SendLinkHandler extends LinkHandler {
    private final String linkName;
    private final String entityPath;
    private final AtomicBoolean isFirstFlow = new AtomicBoolean(true);
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
        super.close();
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

            if (isFirstFlow.getAndSet(false)) {
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
        if (isFirstFlow.getAndSet(false)) {
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
