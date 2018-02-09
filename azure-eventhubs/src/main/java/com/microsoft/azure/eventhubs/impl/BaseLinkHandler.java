/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;

public class BaseLinkHandler extends BaseHandler {
    protected static final Logger TRACE_LOGGER = LoggerFactory.getLogger(BaseHandler.class);

    private final IAmqpLink underlyingEntity;

    public BaseLinkHandler(final IAmqpLink amqpLink) {
        this.underlyingEntity = amqpLink;
    }

    @Override
    public void onLinkLocalClose(Event event) {
        Link link = event.getLink();
        if (link != null) {
            if (TRACE_LOGGER.isInfoEnabled()) {
                TRACE_LOGGER.info(String.format("linkName[%s]", link.getName()));
            }
        }

        closeSession(link);
    }

    @Override
    public void onLinkRemoteClose(Event event) {
        final Link link = event.getLink();

        if (link.getLocalState() != EndpointState.CLOSED) {
            link.close();
        }

        if (link != null) {
            ErrorCondition condition = link.getRemoteCondition();
            this.processOnClose(link, condition);
        }

        closeSession(link);
    }

    @Override
    public void onLinkRemoteDetach(Event event) {
        final Link link = event.getLink();

        if (link.getLocalState() != EndpointState.CLOSED) {
            link.close();
        }

        if (link != null) {
            this.processOnClose(link, link.getRemoteCondition());
        }

        closeSession(link);
    }

    public void processOnClose(Link link, ErrorCondition condition) {
        if (condition != null) {
            if (TRACE_LOGGER.isInfoEnabled()) {
                TRACE_LOGGER.info("linkName[" + link.getName() +
                        (condition != null ? "], ErrorCondition[" + condition.getCondition() + ", " + condition.getDescription() + "]" : "], condition[null]"));
            }
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
}
