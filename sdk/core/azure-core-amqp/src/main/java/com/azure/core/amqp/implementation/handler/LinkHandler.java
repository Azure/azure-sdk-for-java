// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.LinkErrorContext;
import com.azure.core.amqp.implementation.ExceptionUtil;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;

import java.util.Objects;

import static com.azure.core.amqp.implementation.AmqpErrorCode.TRACKING_ID_PROPERTY;
import static com.azure.core.amqp.implementation.ClientConstants.NOT_APPLICABLE;

/**
 * Base class for AMQP links.
 *
 * @see SendLinkHandler
 * @see ReceiveLinkHandler
 */
abstract class LinkHandler extends Handler {
    private final String entityPath;

    /**
     * Creates an instance with the parameters.
     *
     * @param connectionId Identifier for the connection.
     * @param hostname Hostname of the connection. This could be the DNS hostname or the IP address of the
     *     connection. Usually of the form {@literal "<your-namespace>.service.windows.net"} but can change if the
     *     messages are brokered through an intermediary.
     * @param entityPath The address within the message broker for this link.
     * @param logger Logger to use for messages.
     *
     * @throws NullPointerException if {@code connectionId}, {@code hostname}, {@code entityPath}, or {@code logger} is
     * null.
     */
    LinkHandler(String connectionId, String hostname, String entityPath, ClientLogger logger) {
        super(connectionId, hostname, logger);
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
    }

    @Override
    public void onLinkLocalClose(Event event) {
        final Link link = event.getLink();
        final ErrorCondition condition = link.getCondition();

        logger.verbose("onLinkLocalClose connectionId[{}], linkName[{}], errorCondition[{}], errorDescription[{}]",
            getConnectionId(),
            link.getName(),
            condition != null ? condition.getCondition() : NOT_APPLICABLE,
            condition != null ? condition.getDescription() : NOT_APPLICABLE);
    }

    @Override
    public void onLinkRemoteClose(Event event) {
        handleRemoteLinkClosed("onLinkRemoteClose", event);
    }

    @Override
    public void onLinkRemoteDetach(Event event) {
        handleRemoteLinkClosed("onLinkRemoteDetach", event);
    }

    @Override
    public void onLinkFinal(Event event) {
        final String linkName = event != null && event.getLink() != null
            ? event.getLink().getName()
            : NOT_APPLICABLE;
        logger.info("onLinkFinal connectionId[{}], linkName[{}]", getConnectionId(), linkName);
        close();
    }

    public AmqpErrorContext getErrorContext(Link link) {
        final String referenceId;
        if (link.getRemoteProperties() != null && link.getRemoteProperties().containsKey(TRACKING_ID_PROPERTY)) {
            referenceId = link.getRemoteProperties().get(TRACKING_ID_PROPERTY).toString();
        } else {
            referenceId = link.getName();
        }

        return new LinkErrorContext(getHostname(), entityPath, referenceId, link.getCredit());
    }

    private void handleRemoteLinkClosed(final String eventName, final Event event) {
        final Link link = event.getLink();
        final ErrorCondition condition = link.getRemoteCondition();

        logger.info("{} connectionId[{}] linkName[{}], errorCondition[{}] errorDescription[{}]",
            eventName, getConnectionId(), link.getName(),
            condition != null ? condition.getCondition() : NOT_APPLICABLE,
            condition != null ? condition.getDescription() : NOT_APPLICABLE);

        if (link.getLocalState() != EndpointState.CLOSED) {
            logger.info("connectionId[{}] linkName[{}] state[{}] Local link state is not closed.", getConnectionId(),
                link.getName(), link.getLocalState());

            link.setCondition(condition);
            link.close();
        }

        if (condition != null && condition.getCondition() != null) {
            final Throwable exception = ExceptionUtil.toException(condition.getCondition().toString(),
                condition.getDescription(), getErrorContext(link));

            onError(exception);
        } else {
            super.close();
        }
    }
}
