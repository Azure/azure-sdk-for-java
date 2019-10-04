// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class BaseLinkHandler extends BaseHandler {
    protected static final Logger TRACE_LOGGER = LoggerFactory.getLogger(BaseLinkHandler.class);

    private final String name;
    private final AmqpLink underlyingEntity;

    public BaseLinkHandler(final AmqpLink amqpLink, final String name) {
        this.name = name;
        this.underlyingEntity = amqpLink;
    }

    @Override
    public void onLinkLocalClose(Event event) {
        final Link link = event.getLink();
        final ErrorCondition condition = link.getCondition();

        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "onLinkLocalClose clientName[%s], linkName[%s], errorCondition[%s], errorDescription[%s]",
                    this.name, link.getName(), condition != null ? condition.getCondition() : "n/a", condition != null ? condition.getDescription() : "n/a"));
        }

        closeSession(link, link.getCondition());
    }

    @Override
    public void onLinkRemoteClose(Event event) {
        final Link link = event.getLink();
        final ErrorCondition condition = link.getRemoteCondition();

        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "onLinkRemoteClose clientName[%s], linkName[%s], errorCondition[%s], errorDescription[%s]",
                    this.name, link.getName(), condition != null ? condition.getCondition() : "n/a", condition != null ? condition.getDescription() : "n/a"));
        }

        handleRemoteLinkClosed(event);
    }

    @Override
    public void onLinkRemoteDetach(Event event) {
        final Link link = event.getLink();
        final ErrorCondition condition = link.getCondition();

        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "onLinkRemoteDetach clientName[%s], linkName[%s], errorCondition[%s], errorDescription[%s]",
                    this.name, link.getName(), condition != null ? condition.getCondition() : "n/a", condition != null ? condition.getDescription() : "n/a"));
        }

        handleRemoteLinkClosed(event);
    }

    public void processOnClose(Link link, ErrorCondition condition) {
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "processOnClose clientName[%s], linkName[%s], errorCondition[%s], errorDescription[%s]",
                    this.name, link.getName(), condition != null ? condition.getCondition() : "n/a", condition != null ? condition.getDescription() : "n/a"));
        }

        this.underlyingEntity.onClose(condition, link.getName());
    }

    public void processOnClose(Link link, Exception exception) {
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "processOnClose clientName[%s],  linkName[%s], exception[%s]",
                    this.name, link.getName(), exception != null ? exception.getMessage() : "n/a"));
        }

        this.underlyingEntity.onError(exception, link.getName());
    }

    private void closeSession(Link link, ErrorCondition condition) {
        final Session session = link.getSession();

        if (session != null && session.getLocalState() != EndpointState.CLOSED) {
            if (TRACE_LOGGER.isInfoEnabled()) {
                TRACE_LOGGER.info(String.format(Locale.US, "closeSession for clientName[%s], linkName[%s], errorCondition[%s], errorDescription[%s]",
                        this.name, link.getName(), condition != null ? condition.getCondition() : "n/a", condition != null ? condition.getDescription() : "n/a"));
            }

            session.setCondition(condition);
            session.close();
        }
    }

    private void handleRemoteLinkClosed(final Event event) {
        final Link link = event.getLink();

        final ErrorCondition condition = link.getRemoteCondition();

        if (link.getLocalState() != EndpointState.CLOSED) {
            link.setCondition(condition);
            link.close();
        }

        this.processOnClose(link, condition);

        this.closeSession(link, condition);
    }
}
