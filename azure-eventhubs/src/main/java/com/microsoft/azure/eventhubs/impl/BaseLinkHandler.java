/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseLinkHandler extends BaseHandler {
    protected static final Logger TRACE_LOGGER = LoggerFactory.getLogger(BaseLinkHandler.class);

    private final AmqpLink underlyingEntity;

    public BaseLinkHandler(final AmqpLink amqpLink) {
        this.underlyingEntity = amqpLink;
    }

    @Override
    public void onLinkLocalClose(Event event) {
        final Link link = event.getLink();
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format("onLinkLocalClose linkName[%s]", link.getName()));
        }

        closeSession(link);
    }

    @Override
    public void onLinkRemoteClose(Event event) {
        final Link link = event.getLink();
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format("onLinkRemoteClose linkName[%s]", link.getName()));
        }

        handleRemoteLinkClosed(event);
    }

    @Override
    public void onLinkRemoteDetach(Event event) {
        final Link link = event.getLink();
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format("onLinkRemoteDetach linkName[%s]", link.getName()));
        }

        handleRemoteLinkClosed(event);
    }

    public void processOnClose(Link link, ErrorCondition condition) {
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format("processOnClose linkName[%s], errorCondition[%s], errorDescription[%s]",
                    link.getName(),
                    condition != null ? condition.getCondition() : "n/a",
                    condition != null ? condition.getDescription() : "n/a"));
        }

        this.underlyingEntity.onClose(condition);
    }

    public void processOnClose(Link link, Exception exception) {
        this.underlyingEntity.onError(exception);
    }

    private void closeSession(Link link) {
        if (link.getSession() != null && link.getSession().getLocalState() != EndpointState.CLOSED)
            link.getSession().close();
    }

    private void handleRemoteLinkClosed(final Event event) {
        final Link link = event.getLink();

        if (link.getLocalState() != EndpointState.CLOSED) {
            link.close();
        }

        final ErrorCondition condition = link.getRemoteCondition();
        this.processOnClose(link, condition);

        closeSession(link);
    }
}
