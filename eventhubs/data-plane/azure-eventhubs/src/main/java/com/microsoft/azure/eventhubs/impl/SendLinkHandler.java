// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SendLinkHandler extends BaseLinkHandler {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(SendLinkHandler.class);
    private final AmqpSender msgSender;
    private AtomicBoolean isFirstFlow;

    public SendLinkHandler(final AmqpSender sender) {
        super(sender);

        this.msgSender = sender;
        this.isFirstFlow = new AtomicBoolean(true);
    }

    @Override
    public void onLinkLocalOpen(Event event) {
        Link link = event.getLink();
        if (link instanceof Sender) {
            Sender sender = (Sender) link;
            if (TRACE_LOGGER.isInfoEnabled()) {
                TRACE_LOGGER.info(String.format("onLinkLocalOpen linkName[%s], localTarget[%s]", sender.getName(), sender.getTarget()));
            }
        }
    }

    @Override
    public void onLinkRemoteOpen(Event event) {
        Link link = event.getLink();
        if (link instanceof Sender) {
            Sender sender = (Sender) link;
            if (link.getRemoteTarget() != null) {
                if (TRACE_LOGGER.isInfoEnabled()) {
                    TRACE_LOGGER.info(String.format(Locale.US, "onLinkRemoteOpen linkName[%s], remoteTarget[%s]", sender.getName(), link.getRemoteTarget()));
                }

                if (this.isFirstFlow.compareAndSet(true, false)) {
                    this.msgSender.onOpenComplete(null);
                }

            } else {
                if (TRACE_LOGGER.isInfoEnabled()) {
                    TRACE_LOGGER.info(
                            String.format(Locale.US, "onLinkRemoteOpen linkName[%s], remoteTarget[null], remoteSource[null], action[waitingForError]", sender.getName()));
                }
            }
        }
    }

    @Override
    public void onDelivery(Event event) {
        Delivery delivery = event.getDelivery();

        while (delivery != null) {
            Sender sender = (Sender) delivery.getLink();

            if (TRACE_LOGGER.isTraceEnabled()) {
                TRACE_LOGGER.trace(
                        "onDelivery linkName[" + sender.getName()
                                + "], unsettled[" + sender.getUnsettled() + "], credit[" + sender.getRemoteCredit() + "], deliveryState[" + delivery.getRemoteState()
                                + "], delivery.isBuffered[" + delivery.isBuffered() + "], delivery.id[" + new String(delivery.getTag(), UTF_8) + "]");
            }

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

        if (TRACE_LOGGER.isDebugEnabled()) {
            TRACE_LOGGER.debug("onLinkFlow linkName[" + sender.getName() + "], unsettled[" + sender.getUnsettled() + "], credit[" + sender.getCredit() + "]");
        }
    }
}
