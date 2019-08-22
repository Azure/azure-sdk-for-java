// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.handler;

import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Sender;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.UnicastProcessor;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class SendLinkHandler extends LinkHandler {
    private final String senderName;
    private final AtomicBoolean isFirstFlow = new AtomicBoolean(true);
    private final UnicastProcessor<Integer> creditProcessor = UnicastProcessor.create();
    private final DirectProcessor<Delivery> deliveryProcessor = DirectProcessor.create();
    private final FluxSink<Integer> creditSink = creditProcessor.sink();
    private final FluxSink<Delivery> deliverySink = deliveryProcessor.sink();

    public SendLinkHandler(String connectionId, String hostname, String senderName, String entityPath) {
        super(connectionId, hostname, entityPath, new ClientLogger(SendLinkHandler.class));
        this.senderName = senderName;
    }

    public Flux<Integer> getLinkCredits() {
        return creditProcessor;
    }

    public Flux<Delivery> getDeliveredMessages() {
        return deliveryProcessor;
    }

    @Override
    public void close() {
        creditSink.complete();
        deliverySink.complete();
        super.close();
    }

    @Override
    public void onLinkLocalOpen(Event event) {
        final Link link = event.getLink();
        if (link instanceof Sender) {
            logger.verbose("onLinkLocalOpen senderName[{}], linkName[{}], localTarget[{}]",
                senderName, link.getName(), link.getTarget());
        }
    }

    @Override
    public void onLinkRemoteOpen(Event event) {
        final Link link = event.getLink();
        if (link instanceof Sender) {
            if (link.getRemoteTarget() != null) {
                logger.info("onLinkRemoteOpen senderName[{}], linkName[{}], remoteTarget[{}]",
                    senderName, link.getName(), link.getRemoteTarget());

                if (isFirstFlow.getAndSet(false)) {
                    onNext(EndpointState.ACTIVE);
                }
            } else {
                logger.info("onLinkRemoteOpen senderName[{}], linkName[{}], remoteTarget[null], remoteSource[null], action[waitingForError]",
                    senderName, link.getName());
            }
        }
    }

    @Override
    public void onLinkFlow(Event event) {
        if (isFirstFlow.getAndSet(false)) {
            onNext(EndpointState.ACTIVE);
        }

        final Sender sender = event.getSender();
        creditSink.next(sender.getRemoteCredit());

        logger.verbose("onLinkFlow senderName[{}], linkName[{}], unsettled[{}], credit[{}]",
            senderName, sender.getName(), sender.getUnsettled(), sender.getCredit());
    }

    @Override
    public void onDelivery(Event event) {
        Delivery delivery = event.getDelivery();

        while (delivery != null) {
            Sender sender = (Sender) delivery.getLink();

            logger.info("onDelivery senderName[{}], linkName[{}], unsettled[{}], credit[{}], deliveryState[{}], delivery.isBuffered[{}], delivery.id[{}]",
                senderName, sender.getName(), sender.getUnsettled(), sender.getRemoteCredit(),
                delivery.getRemoteState(), delivery.isBuffered(), new String(delivery.getTag(), StandardCharsets.UTF_8));

            deliverySink.next(delivery);
            delivery.settle();
            delivery = sender.current();
        }
    }
}
