/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.amqp.transport.*;
import org.apache.qpid.proton.engine.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class BaseLinkHandler extends BaseHandler {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(BaseLinkHandler.class);

    private final IAmqpLink underlyingEntity;

    public BaseLinkHandler(final IAmqpLink amqpLink) {
        this.underlyingEntity = amqpLink;
    }

    @Override
    public void onLinkLocalClose(Event event) {
        Link link = event.getLink();
        if (link != null) {            
            TRACE_LOGGER.debug("local link close. linkName:{}", link.getName());
        }

        closeSession(link);
    }

    @Override
    public void onLinkRemoteClose(Event event) {
        final Link link = event.getLink();        
        if(link != null)
        {
            TRACE_LOGGER.debug("link remote close. linkName:{}", link.getName());
            if (link.getLocalState() != EndpointState.CLOSED) {
                link.close();
            }

            ErrorCondition condition = link.getRemoteCondition();
            this.processOnClose(link, condition);
            closeSession(link);
        }        
    }

    @Override
    public void onLinkRemoteDetach(Event event) {
        final Link link = event.getLink();
        if(link != null)
        {
            TRACE_LOGGER.debug("link remote detach. linkName:{}", link.getName());
            if (link.getLocalState() != EndpointState.CLOSED) {
                link.close();
            }
            
            this.processOnClose(link, link.getRemoteCondition());
            closeSession(link);
        }
        
    }

    public void processOnClose(Link link, ErrorCondition condition) {
        if (condition != null) {            
            TRACE_LOGGER.debug("linkName:{}, ErrorCondition:{}, {}", link.getName(), condition.getCondition(), condition.getDescription());
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
