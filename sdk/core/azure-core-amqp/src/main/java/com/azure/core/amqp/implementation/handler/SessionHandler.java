// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.SessionErrorContext;
import com.azure.core.amqp.implementation.ClientConstants;
import com.azure.core.amqp.implementation.ExceptionUtil;
import com.azure.core.amqp.implementation.ReactorDispatcher;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Session;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;

public class SessionHandler extends Handler {
    private final String entityName;
    private final Duration openTimeout;
    private final ReactorDispatcher reactorDispatcher;

    public SessionHandler(String connectionId, String hostname, String entityName, ReactorDispatcher reactorDispatcher,
                          Duration openTimeout) {
        super(connectionId, hostname, new ClientLogger(SessionHandler.class));
        this.entityName = entityName;
        this.openTimeout = openTimeout;
        this.reactorDispatcher = reactorDispatcher;
    }

    public AmqpErrorContext getErrorContext() {
        return new SessionErrorContext(getHostname(), entityName);
    }

    @Override
    public void onSessionLocalOpen(Event e) {
        logger.verbose("onSessionLocalOpen connectionId[{}], entityName[{}], condition[{}]",
            getConnectionId(), this.entityName,
            e.getSession().getCondition() == null
                ? ClientConstants.NOT_APPLICABLE
                : e.getSession().getCondition().toString());

        final Session session = e.getSession();

        try {
            reactorDispatcher.invoke(this::onSessionTimeout, this.openTimeout);
        } catch (IOException ioException) {
            logger.warning("onSessionLocalOpen connectionId[{}], entityName[{}], reactorDispatcherError[{}]",
                getConnectionId(), this.entityName,
                ioException.getMessage());

            session.close();

            final String message =
                String.format(Locale.US, "onSessionLocalOpen connectionId[%s], entityName[%s], underlying IO of"
                        + " reactorDispatcher faulted with error: %s",
                    getConnectionId(), this.entityName, ioException.getMessage());
            final Throwable exception = new AmqpException(false, message, ioException, getErrorContext());

            onError(exception);
        }
    }

    @Override
    public void onSessionRemoteOpen(Event e) {
        final Session session = e.getSession();
        if (session.getLocalState() == EndpointState.UNINITIALIZED) {
            logger.warning("onSessionRemoteOpen connectionId[{}], entityName[{}], sessionIncCapacity[{}],"
                    + " sessionOutgoingWindow[{}] endpoint was uninitialised.",
                getConnectionId(), entityName, session.getIncomingCapacity(), session.getOutgoingWindow());

            session.open();
        } else {
            logger.info("onSessionRemoteOpen connectionId[{}], entityName[{}], sessionIncCapacity[{}],"
                    + " sessionOutgoingWindow[{}]",
                getConnectionId(), entityName, session.getIncomingCapacity(), session.getOutgoingWindow());
        }

        onNext(EndpointState.ACTIVE);
    }

    @Override
    public void onSessionLocalClose(Event e) {
        final ErrorCondition condition = (e != null && e.getSession() != null)
            ? e.getSession().getCondition()
            : null;

        logger.verbose("onSessionLocalClose connectionId[{}], entityName[{}], condition[{}]",
            entityName, getConnectionId(),
            condition == null ? ClientConstants.NOT_APPLICABLE : condition.toString());
    }

    @Override
    public void onSessionRemoteClose(Event e) {
        final Session session = e.getSession();

        logger.info("onSessionRemoteClose connectionId[{}], entityName[{}], condition[{}]",
            entityName,
            getConnectionId(),
            session == null || session.getRemoteCondition() == null
                ? ClientConstants.NOT_APPLICABLE
                : session.getRemoteCondition().toString());

        ErrorCondition condition = session != null ? session.getRemoteCondition() : null;

        if (session != null && session.getLocalState() != EndpointState.CLOSED) {
            logger.info("onSessionRemoteClose closing a local session for connectionId[{}], entityName[{}], "
                    + "condition[{}], description[{}]",
                getConnectionId(), entityName,
                condition != null ? condition.getCondition() : ClientConstants.NOT_APPLICABLE,
                condition != null ? condition.getDescription() : ClientConstants.NOT_APPLICABLE);

            session.setCondition(session.getRemoteCondition());
            session.close();
        }

        if (condition == null || condition.getCondition() == null) {
            onNext(EndpointState.CLOSED);
        } else {
            final String id = getConnectionId();
            final AmqpErrorContext context = getErrorContext();

            final Exception exception = ExceptionUtil.toException(condition.getCondition().toString(),
                String.format(Locale.US, "onSessionRemoteClose connectionId[%s], entityName[%s] condition[%s]",
                    id, entityName, condition), context);

            onError(exception);
        }
    }

    @Override
    public void onSessionFinal(Event e) {
        final Session session = e.getSession();
        final ErrorCondition condition = session != null ? session.getCondition() : null;

        logger.info("onSessionFinal connectionId[{}], entityName[{}], condition[{}], description[{}]",
            getConnectionId(), entityName,
            condition != null ? condition.getCondition() : ClientConstants.NOT_APPLICABLE,
            condition != null ? condition.getDescription() : ClientConstants.NOT_APPLICABLE);

        close();
    }

    private void onSessionTimeout() {
        // It is supposed to close a local session to handle timeout exception.
        // However, closing the session can result in NPE because of proton-j bug (https://issues.apache
        // .org/jira/browse/PROTON-1939).
        // And the bug will cause the reactor thread to stop processing pending tasks scheduled on the reactor and
        // as a result task won't be completed at all.

        // TODO: handle timeout error once the proton-j bug is fixed.
        // if (!sessionCreated && !sessionOpenErrorDispatched) {
        //     logger.warning(
        //     "SessionTimeoutHandler.onEvent - connectionId[{}], entityName[{}], session open timed out.",
        //     this.connectionId, this.entityName);
        // }
    }
}
