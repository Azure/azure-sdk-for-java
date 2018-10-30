/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

// ServiceBus <-> ProtonReactor interaction 
// handles all recvLink - reactor events
public final class ReceiveLinkHandler extends BaseLinkHandler {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ReceiveLinkHandler.class);
    private final AmqpReceiver amqpReceiver;
    private final Object firstResponse;
    private boolean isFirstResponse;

    public ReceiveLinkHandler(final AmqpReceiver receiver) {
        super(receiver);

        this.amqpReceiver = receiver;
        this.firstResponse = new Object();
        this.isFirstResponse = true;
    }

    @Override
    public void onLinkLocalOpen(Event evt) {
        Link link = evt.getLink();
        if (link instanceof Receiver) {
            Receiver receiver = (Receiver) link;

            if (TRACE_LOGGER.isInfoEnabled()) {
                TRACE_LOGGER.info(
                        String.format("linkName[%s], localSource[%s]", receiver.getName(), receiver.getSource()));
            }
        }
    }

    @Override
    public void onLinkRemoteOpen(Event event) {
        Link link = event.getLink();
        if (link != null && link instanceof Receiver) {
            Receiver receiver = (Receiver) link;
            if (link.getRemoteSource() != null) {
                if (TRACE_LOGGER.isInfoEnabled()) {
                    TRACE_LOGGER.info(String.format(Locale.US, "linkName[%s], remoteSource[%s]", receiver.getName(), link.getRemoteSource()));
                }

                synchronized (this.firstResponse) {
                    this.isFirstResponse = false;
                    this.amqpReceiver.onOpenComplete(null);
                }
            } else {
                if (TRACE_LOGGER.isInfoEnabled()) {
                    TRACE_LOGGER.info(
                            String.format(Locale.US, "linkName[%s], remoteTarget[null], remoteSource[null], action[waitingForError]", receiver.getName()));
                }
            }
        }
    }

    @Override
    public void onDelivery(Event event) {
        synchronized (this.firstResponse) {
            if (this.isFirstResponse) {
                this.isFirstResponse = false;
                this.amqpReceiver.onOpenComplete(null);
            }
        }

        Delivery delivery = event.getDelivery();
        Receiver receiveLink = (Receiver) delivery.getLink();

        // If a message spans across deliveries (for ex: 200k message will be 4 frames (deliveries) 64k 64k 64k 8k),
        // all until "last-1" deliveries will be partial
        // reactor will raise onDelivery event for all of these - we only need the last one
        if (!delivery.isPartial()) {

            // One of our customers hit an issue - where duplicate 'Delivery' events are raised to Reactor in proton-j layer
            // While processing the duplicate event - reactor hits an IllegalStateException in proton-j layer
            // before we fix proton-j - this work around ensures that we ignore the duplicate Delivery event
            if (delivery.isSettled()) {
                if (TRACE_LOGGER.isWarnEnabled()) {
                    TRACE_LOGGER.warn(
                            receiveLink != null
                                    ? String.format(Locale.US, "linkName[%s], updatedLinkCredit[%s], remoteCredit[%s], remoteCondition[%s], delivery.isSettled[%s]",
                                        receiveLink.getName(), receiveLink.getCredit(), receiveLink.getRemoteCredit(), receiveLink.getRemoteCondition(), delivery.isSettled())
                                    : String.format(Locale.US, "delivery.isSettled[%s]", delivery.isSettled()));
                }
            } else {
                this.amqpReceiver.onReceiveComplete(delivery);
            }
        }

        if (TRACE_LOGGER.isTraceEnabled() && receiveLink != null) {
            TRACE_LOGGER.trace(
                    String.format(Locale.US, "linkName[%s], updatedLinkCredit[%s], remoteCredit[%s], remoteCondition[%s], delivery.isPartial[%s]",
                            receiveLink.getName(), receiveLink.getCredit(), receiveLink.getRemoteCredit(), receiveLink.getRemoteCondition(), delivery.isPartial()));
        }
    }
}
