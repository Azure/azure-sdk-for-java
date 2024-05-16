// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.microsoft.azure.eventhubs.EventHubException;

public final class ReceiveLinkHandler extends BaseLinkHandler {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ReceiveLinkHandler.class);
    private final AmqpReceiver amqpReceiver;
    private final String receiverName;
    private final ScheduledExecutorService executor;
    private final Object firstResponse;
    private boolean isFirstResponse;

    public ReceiveLinkHandler(final AmqpReceiver receiver, final String receiverName, final ScheduledExecutorService executor) {
        super(receiver, receiverName);

        this.amqpReceiver = receiver;
        this.receiverName = receiverName;
        this.executor = executor;
        this.firstResponse = new Object();
        this.isFirstResponse = true;
    }

    @Override
    public void onLinkLocalOpen(Event evt) {
        Link link = evt.getLink();
        if (link instanceof Receiver) {
            if (TRACE_LOGGER.isInfoEnabled()) {
                TRACE_LOGGER.info(String.format(Locale.US, "onLinkLocalOpen receiverName[%s], linkName[%s], localSource[%s]",
                        this.receiverName, link.getName(), link.getSource()));
            }
        }
    }

    @Override
    public void onLinkRemoteOpen(Event event) {
        Link link = event.getLink();
        if (link instanceof Receiver) {
            if (link.getRemoteSource() != null) {
                if (TRACE_LOGGER.isInfoEnabled()) {
                    TRACE_LOGGER.info(String.format(Locale.US, "onLinkRemoteOpen receiverName[%s], linkName[%s], remoteSource[%s]",
                            this.receiverName, link.getName(), link.getRemoteSource()));
                }

                synchronized (this.firstResponse) {
                    this.isFirstResponse = false;
                    this.amqpReceiver.onOpenComplete(null);
                }
            } else {
                if (TRACE_LOGGER.isInfoEnabled()) {
                    TRACE_LOGGER.info(String.format(Locale.US, "onLinkRemoteOpen receiverName[%s], linkName[%s], action[waitingForError]",
                            this.receiverName, link.getName()));
                }
            }
        }
    }

    @Override
    public void onLinkFinal(Event event) {
        if (this.isFirstResponse) {
            final Link link = event.getLink();
            if (TRACE_LOGGER.isWarnEnabled()) {
                TRACE_LOGGER.warn(String.format(Locale.US, "onLinkFinal receiverName[%s], linkName[%s] - link never opened",
                    this.receiverName, link.getName()));
            }
            final ErrorCondition condition = link.getCondition();
            final Exception finalOpenError = (condition != null) ? ExceptionUtil.toException(condition) :
                new EventHubException(true, "Link open failed, cause not available");
            this.executor.schedule(new Runnable() {
                @Override
                public void run() {
                    ReceiveLinkHandler.this.amqpReceiver.onOpenComplete(finalOpenError);
                }
            }, AmqpConstants.LINK_ERROR_DELAY_MILLIS, TimeUnit.MILLISECONDS);
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
                                    ? String.format(Locale.US, "onDelivery receiverName[%s], linkName[%s], updatedLinkCredit[%s], remoteCredit[%s], "
                                            + "remoteCondition[%s], delivery.isSettled[%s]",
                                    this.receiverName, receiveLink.getName(), receiveLink.getCredit(), receiveLink.getRemoteCredit(), receiveLink.getRemoteCondition(), delivery.isSettled())
                                    : String.format(Locale.US, "delivery.isSettled[%s]", delivery.isSettled()));
                }
            } else {
                this.amqpReceiver.onReceiveComplete(delivery);
            }
        }

        if (TRACE_LOGGER.isTraceEnabled() && receiveLink != null) {
            TRACE_LOGGER.trace(
                    String.format(Locale.US, "onDelivery receiverName[%s], linkName[%s], updatedLinkCredit[%s], remoteCredit[%s], "
                                   + "remoteCondition[%s], delivery.isPartial[%s]",
                            this.receiverName, receiveLink.getName(), receiveLink.getCredit(), receiveLink.getRemoteCredit(), receiveLink.getRemoteCondition(), delivery.isPartial()));
        }
    }
}
