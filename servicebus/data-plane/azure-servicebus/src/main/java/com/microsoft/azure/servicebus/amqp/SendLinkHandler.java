// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class SendLinkHandler extends BaseLinkHandler {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(SendLinkHandler.class);
    
    private final IAmqpSender msgSender;
    private AtomicBoolean isFirstFlow;

    public SendLinkHandler(final IAmqpSender sender) {
        super(sender);

        this.msgSender = sender;
        this.isFirstFlow = new AtomicBoolean(true);
    }

    @Override
    public void onLinkRemoteOpen(Event event) {
        Link link = event.getLink();
        if (link != null && link instanceof Sender) {
            Sender sender = (Sender) link;
            if (link.getRemoteTarget() != null) {
                TRACE_LOGGER.debug("onLinkRemoteOpen: linkName:{}, remoteTarge:{}", sender.getName(), link.getRemoteTarget());

                if (this.isFirstFlow.compareAndSet(true, false)) {
                    this.msgSender.onOpenComplete(null);
                }
            } else {
                TRACE_LOGGER.debug("onLinkRemoteOpen: linkName:{}, remoteTarget:{}, remoteSource:{}, action:{}", sender.getName(), null, null, "waitingForError");
            }
        }
    }

    @Override
    public void onDelivery(Event event) {
        Delivery delivery = event.getDelivery();

        while (delivery != null) {
            Sender sender = (Sender) delivery.getLink();

            TRACE_LOGGER.debug("onDelivery: linkName:{}, unsettled:{}, credit:{}, deliveryState:{}, delivery.isBuffered:{}, delivery.tag:{}",
                sender.getName(), sender.getUnsettled(), sender.getRemoteCredit(), delivery.getRemoteState(), delivery.isBuffered(), delivery.getTag());
            msgSender.onSendComplete(delivery);
            delivery.settle();

            delivery = sender.current();
        }
    }

    @Override
    public void onLinkFlow(Event event) {
        if (this.isFirstFlow.compareAndSet(true, false)) {
            this.msgSender.onOpenComplete(null);
        }

        Sender sender = event.getSender();
        this.msgSender.onFlow(sender.getRemoteCredit());

        TRACE_LOGGER.debug("onLinkFlow: linkName:{}, unsettled:{}, credit:{}", sender.getName(), sender.getUnsettled(), sender.getCredit());
    }
}
