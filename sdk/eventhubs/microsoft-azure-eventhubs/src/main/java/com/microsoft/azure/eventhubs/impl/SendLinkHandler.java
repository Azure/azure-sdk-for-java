// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.microsoft.azure.eventhubs.EventHubException;

public class SendLinkHandler extends BaseLinkHandler {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(SendLinkHandler.class);
    private final AmqpSender msgSender;
    private final String senderName;
    private final ScheduledExecutorService executor;
    private AtomicBoolean isFirstFlow;

    public SendLinkHandler(final AmqpSender sender, final String senderName, final ScheduledExecutorService executor) {
        super(sender, senderName);

        this.msgSender = sender;
        this.senderName = senderName;
        this.executor = executor;
        this.isFirstFlow = new AtomicBoolean(true);
    }

    @Override
    public void onLinkLocalOpen(Event event) {
        Link link = event.getLink();
        if (link instanceof Sender) {
            if (TRACE_LOGGER.isInfoEnabled()) {
                TRACE_LOGGER.info(String.format(Locale.US, "onLinkLocalOpen senderName[%s], linkName[%s], localTarget[%s]",
                        this.senderName, link.getName(), link.getTarget()));
            }
        }
    }

    @Override
    public void onLinkRemoteOpen(Event event) {
        Link link = event.getLink();
        if (link instanceof Sender) {
            if (link.getRemoteTarget() != null) {
                if (TRACE_LOGGER.isInfoEnabled()) {
                    TRACE_LOGGER.info(String.format(Locale.US, "onLinkRemoteOpen senderName[%s], linkName[%s], remoteTarget[%s]",
                            this.senderName, link.getName(), link.getRemoteTarget()));
                }

                if (this.isFirstFlow.compareAndSet(true, false)) {
                    this.msgSender.onOpenComplete(null);
                }
            } else {
                if (TRACE_LOGGER.isInfoEnabled()) {
                    TRACE_LOGGER.info(String.format(Locale.US, "onLinkRemoteOpen senderName[%s], linkName[%s], remoteTarget[null], remoteSource[null], action[waitingForError]",
                            this.senderName, link.getName()));
                }
            }
        }
    }

    @Override
    public void onLinkFinal(Event event) {
        if (this.isFirstFlow.get()) {
            final Link link = event.getLink();
            if (TRACE_LOGGER.isWarnEnabled()) {
                TRACE_LOGGER.warn(String.format(Locale.US, "onLinkFinal senderName[%s], linkName[%s] - link never opened",
                    this.senderName, link.getName()));
            }
            final ErrorCondition condition = event.getLink().getCondition();
            final Exception finalOpenError = (condition != null) ? ExceptionUtil.toException(condition) :
                new EventHubException(true, "Link open failed, cause not available");
            this.executor.schedule(new Runnable() {
                @Override
                public void run() {
                    SendLinkHandler.this.msgSender.onOpenComplete(finalOpenError);
                }
            }, AmqpConstants.LINK_ERROR_DELAY_MILLIS, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onDelivery(Event event) {
        Delivery delivery = event.getDelivery();

        while (delivery != null) {
            Sender sender = (Sender) delivery.getLink();

            if (TRACE_LOGGER.isTraceEnabled()) {
                TRACE_LOGGER.trace(String.format(Locale.US, "onDelivery senderName[%s], linkName[%s], unsettled[%s], credit[%s], deliveryState[%s], delivery.isBuffered[%s], delivery.id[%s]",
                        this.senderName, sender.getName(), sender.getUnsettled(), sender.getRemoteCredit(), delivery.getRemoteState(), delivery.isBuffered(), new String(delivery.getTag(), StandardCharsets.UTF_8)));
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
            TRACE_LOGGER.debug(String.format(Locale.US, "onLinkFlow senderName[%s], linkName[%s], unsettled[%s], credit[%s]",
                    this.senderName, sender.getName(), sender.getUnsettled(), sender.getCredit()));
        }
    }
}
