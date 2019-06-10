// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation.handler;

import com.azure.core.amqp.exception.ErrorContext;
import com.azure.core.amqp.exception.ExceptionUtil;
import com.azure.core.implementation.logging.ServiceLogger;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Session;

import static com.azure.eventhubs.implementation.ClientConstants.NOT_APPLICABLE;

class LinkHandler extends Handler {
    private final String connectionId;
    private final String hostname;

    ServiceLogger logger = new ServiceLogger(LinkHandler.class);

    LinkHandler(final String connectionId, final String hostname) {
        this.connectionId = connectionId;
        this.hostname = hostname;
    }

    @Override
    public void onLinkLocalClose(Event event) {
        final Link link = event.getLink();
        final ErrorCondition condition = link.getCondition();

        logger.asInfo().log("onLinkLocalClose connectionId[{}], linkName[{}], errorCondition[{}], errorDescription[{}]",
            connectionId, link.getName(),
            condition != null ? condition.getCondition() : NOT_APPLICABLE,
            condition != null ? condition.getDescription() : NOT_APPLICABLE);

        closeSession(link, link.getCondition());
    }

    @Override
    public void onLinkRemoteClose(Event event) {
        final Link link = event.getLink();
        final ErrorCondition condition = link.getRemoteCondition();

        logger.asInfo().log("onLinkRemoteClose connectionId[{}], linkName[{}], errorCondition[{}], errorDescription[{}]",
            connectionId, link.getName(),
            condition != null ? condition.getCondition() : NOT_APPLICABLE,
            condition != null ? condition.getDescription() : NOT_APPLICABLE);

        handleRemoteLinkClosed(event);
    }

    @Override
    public void onLinkRemoteDetach(Event event) {
        final Link link = event.getLink();
        final ErrorCondition condition = link.getCondition();

        logger.asInfo().log("onLinkRemoteClose connectionId[{}], linkName[{}], errorCondition[{}], errorDescription[{}]",
            connectionId, link.getName(),
            condition != null ? condition.getCondition() : NOT_APPLICABLE,
            condition != null ? condition.getDescription() : NOT_APPLICABLE);

        handleRemoteLinkClosed(event);
    }

    @Override
    public void onLinkFinal(Event event) {
        logger.asInfo().log("onLinkFinal clientName[{}],  linkName[{}]", connectionId, event.getLink().getName());
        close();
    }

    private void processOnClose(Link link, ErrorCondition condition) {
        logger.asInfo().log("processOnClose connectionId[{}], linkName[{}], errorCondition[{}], errorDescription[{}]",
            connectionId, link.getName(),
            condition != null ? condition.getCondition() : NOT_APPLICABLE,
            condition != null ? condition.getDescription() : NOT_APPLICABLE);

        if (condition != null) {
            final Throwable exception = ExceptionUtil.toException(condition.getCondition().toString(), condition.getDescription());
            onNext(new ErrorContext(exception, hostname));
        }

        onNext(EndpointState.CLOSED);
    }

    private void closeSession(Link link, ErrorCondition condition) {
        final Session session = link.getSession();

        if (session != null && session.getLocalState() != EndpointState.CLOSED) {
            logger.asInfo().log("closeSession connectionId[{}], linkName[{}], errorCondition[{}], errorDescription[{}]",
                connectionId, link.getName(),
                condition != null ? condition.getCondition() : NOT_APPLICABLE,
                condition != null ? condition.getDescription() : NOT_APPLICABLE);

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

        processOnClose(link, condition);
        closeSession(link, condition);
    }
}
