// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.LinkErrorContext;
import com.azure.core.amqp.implementation.AmqpMetricsProvider;
import com.azure.core.amqp.implementation.ExceptionUtil;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;

import java.util.Objects;

import static com.azure.core.amqp.implementation.AmqpErrorCode.TRACKING_ID_PROPERTY;
import static com.azure.core.amqp.implementation.AmqpLoggingUtils.addErrorCondition;
import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.LINK_NAME_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.NOT_APPLICABLE;

/**
 * Base class for AMQP links.
 *
 * @see SendLinkHandler
 * @see ReceiveLinkHandler
 */
abstract class LinkHandler extends Handler {
    private final String entityPath;
    private final AmqpMetricsProvider metricsProvider;

    /**
     * Creates an instance with the parameters.
     *
     * @param connectionId Identifier for the connection.
     * @param hostname Hostname of the connection. This could be the DNS hostname or the IP address of the
     *     connection. Usually of the form {@literal "<your-namespace>.service.windows.net"} but can change if the
     *     messages are brokered through an intermediary.
     * @param entityPath The address within the message broker for this link.
     *
     * @throws NullPointerException if {@code connectionId}, {@code hostname}, {@code entityPath}, or {@code logger} is
     * null.
     */
    LinkHandler(String connectionId, String hostname, String entityPath, AmqpMetricsProvider metricsProvider) {
        super(connectionId, hostname);
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.metricsProvider = metricsProvider;
    }

    @Override
    public void onLinkLocalClose(Event event) {
        final Link link = event.getLink();
        final ErrorCondition condition = link.getCondition();

        addErrorCondition(logger.atVerbose(), condition).addKeyValue(LINK_NAME_KEY, link.getName())
            .addKeyValue(ENTITY_PATH_KEY, entityPath)
            .log("onLinkLocalClose");
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
        final String linkName = event != null && event.getLink() != null ? event.getLink().getName() : NOT_APPLICABLE;
        logger.atInfo()
            .addKeyValue(LINK_NAME_KEY, linkName)
            .addKeyValue(ENTITY_PATH_KEY, entityPath)
            .log("onLinkFinal");

        // Be explicit about wanting to call Handler.close(). When we receive onLinkFinal, the service and proton-j are
        // releasing this link. So we want to complete the endpoint states.
        super.close();
    }

    public AmqpErrorContext getErrorContext(Link link) {
        return getErrorContext(getHostname(), entityPath, link);
    }

    static AmqpErrorContext getErrorContext(String hostName, String entityPath, Link link) {
        final String referenceId;
        if (link.getRemoteProperties() != null && link.getRemoteProperties().containsKey(TRACKING_ID_PROPERTY)) {
            referenceId = link.getRemoteProperties().get(TRACKING_ID_PROPERTY).toString();
        } else {
            referenceId = link.getName();
        }

        return new LinkErrorContext(hostName, entityPath, referenceId, link.getCredit());
    }

    private void handleRemoteLinkClosed(final String eventName, final Event event) {
        final Link link = event.getLink();
        final ErrorCondition condition = link.getRemoteCondition();

        addErrorCondition(logger.atInfo(), condition).addKeyValue(LINK_NAME_KEY, link.getName())
            .addKeyValue(ENTITY_PATH_KEY, entityPath)
            .log(eventName);

        if (link.getLocalState() != EndpointState.CLOSED) {
            logger.atInfo()
                .addKeyValue(LINK_NAME_KEY, link.getName())
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .addKeyValue("state", link.getLocalState())
                .log("Local link state is not closed.");

            link.setCondition(condition);
            link.close();
        }

        if (condition != null && condition.getCondition() != null) {
            metricsProvider.recordHandlerError(AmqpMetricsProvider.ErrorSource.LINK, condition);
            final Throwable exception = ExceptionUtil.toException(condition.getCondition().toString(),
                condition.getDescription(), getErrorContext(link));

            onError(exception);
        } else {
            super.close();
        }
    }
}
