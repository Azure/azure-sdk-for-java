/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.*;
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
        final ErrorCondition condition = link.getCondition();

        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format("onLinkLocalClose linkName[%s], errorCondition[%s], errorDescription[%s]",
                    link.getName(),
                    condition != null ? condition.getCondition() : "n/a",
                    condition != null ? condition.getDescription() : "n/a"));
        }

        closeSession(link, link.getCondition());
    }

    @Override
    public void onLinkRemoteClose(Event event) {
        final Link link = event.getLink();
        final ErrorCondition condition = link.getCondition();

        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format("onLinkRemoteClose linkName[%s], errorCondition[%s], errorDescription[%s]",
                    link.getName(),
                    condition != null ? condition.getCondition() : "n/a",
                    condition != null ? condition.getDescription() : "n/a"));
        }

        handleRemoteLinkClosed(event);
    }

    @Override
    public void onLinkRemoteDetach(Event event) {
        final Link link = event.getLink();
        final ErrorCondition condition = link.getCondition();

        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format("onLinkRemoteDetach linkName[%s], errorCondition[%s], errorDescription[%s]",
                    link.getName(),
                    condition != null ? condition.getCondition() : "n/a",
                    condition != null ? condition.getDescription() : "n/a"));
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
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format("processOnClose linkName[%s], exception[%s]",
                    link.getName(),
                    exception != null ? exception.getMessage() : "n/a"));
        }

        this.underlyingEntity.onError(exception);
    }

    private void closeSession(Link link, ErrorCondition condition) {
        final Session session = link.getSession();

        if (session != null && session.getLocalState() != EndpointState.CLOSED) {
            if (TRACE_LOGGER.isInfoEnabled()) {
                TRACE_LOGGER.info(String.format("closeSession for linkName[%s], errorCondition[%s], errorDescription[%s]",
                        link.getName(),
                        condition != null ? condition.getCondition() : "n/a",
                        condition != null ? condition.getDescription() : "n/a"));
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
