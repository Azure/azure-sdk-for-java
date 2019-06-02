// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation.handler;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorContext;
import com.azure.core.amqp.exception.ExceptionUtil;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.eventhubs.implementation.ReactorDispatcher;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Session;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;

import static com.azure.eventhubs.implementation.ClientConstants.NOT_APPLICABLE;

public class SessionHandler extends Handler {
    private final ServiceLogger logger = new ServiceLogger(SessionHandler.class);

    private final String connectionId;
    private final String host;
    private final String entityName;
    private final Duration openTimeout;
    private final ReactorDispatcher reactorDispatcher;

    public SessionHandler(String connectionId, String host, String entityName, ReactorDispatcher reactorDispatcher,
                   final Duration openTimeout) {
        this.connectionId = connectionId;
        this.host = host;
        this.entityName = entityName;
        this.openTimeout = openTimeout;
        this.reactorDispatcher = reactorDispatcher;
    }

    public ErrorContext getContext(Throwable throwable) {
        return new SessionErrorContext(throwable, host, entityName);
    }

    @Override
    public void onSessionLocalOpen(Event e) {
        logger.asInformational().log("onSessionLocalOpen connectionId[{}], entityName[{}], condition[{}]",
            connectionId, this.entityName,
            e.getSession().getCondition() == null ? NOT_APPLICABLE : e.getSession().getCondition().toString());

        final Session session = e.getSession();

        try {
            reactorDispatcher.invoke(this::onSessionTimeout, this.openTimeout);
        } catch (IOException ioException) {
            logger.asWarning().log("onSessionLocalOpen connectionId[{}], entityName[{}], reactorDispatcherError[{}]",
                connectionId, this.entityName,
                ioException.getMessage());

            session.close();

            final String message = String.format(Locale.US, "onSessionLocalOpen connectionId[%s], entityName[%s], underlying IO of reactorDispatcher faulted with error: %s",
                connectionId, this.entityName, ioException.getMessage());
            final ErrorContext errorContext = new SessionErrorContext(new AmqpException(false, message, ioException), connectionId, entityName);

            onNext(errorContext);
        }
    }

    @Override
    public void onSessionRemoteOpen(Event e) {
        final Session session = e.getSession();

        logger.asInformational().log(
            "onSessionRemoteOpen connectionId[{}], entityName[{}], sessionIncCapacity[{}], sessionOutgoingWindow[{}]",
            connectionId, entityName, session.getIncomingCapacity(), session.getOutgoingWindow());

        if (session.getLocalState() == EndpointState.UNINITIALIZED) {
            session.open();
        }

        onNext(EndpointState.ACTIVE);
    }

    @Override
    public void onSessionLocalClose(Event e) {
        final ErrorCondition condition = e.getSession().getCondition();

        logger.asInformational().log("onSessionLocalClose connectionId[{}], entityName[{}], condition[{}]",
            entityName, connectionId,
            condition == null ? NOT_APPLICABLE : condition.toString());
    }

    @Override
    public void onSessionRemoteClose(Event e) {
        final Session session = e.getSession();

        logger.asInformational().log("onSessionRemoteClose connectionId[{}], entityName[{}], condition[{}]",
            entityName, connectionId,
            session == null || session.getRemoteCondition() == null ? NOT_APPLICABLE : session.getRemoteCondition().toString());

        ErrorCondition condition = session != null ? session.getRemoteCondition() : null;

        if (session != null && session.getLocalState() != EndpointState.CLOSED) {
            logger.asInformational().log(
                "onSessionRemoteClose closing a local session for connectionId[{}], entityName[{}], condition[{}], description[{}]",
                connectionId, entityName,
                condition != null ? condition.getCondition() : NOT_APPLICABLE,
                condition != null ? condition.getDescription() : NOT_APPLICABLE);

            session.setCondition(session.getRemoteCondition());
            session.close();
        }

        onNext(EndpointState.CLOSED);

        if (condition != null) {
            final Exception exception = ExceptionUtil.toException(condition.getCondition().toString(),
                String.format(Locale.US, "onSessionRemoteClose connectionId[%s], entityName[%s]", connectionId, entityName));
            final ErrorContext context = new SessionErrorContext(exception, host, entityName);
            onNext(context);
        }
    }

    @Override
    public void onSessionFinal(Event e) {
        final Session session = e.getSession();
        final ErrorCondition condition = session != null ? session.getCondition() : null;

        logger.asInformational().log("onSessionFinal connectionId[{}], entityName[{}], condition[{}], description[{}]",
            connectionId, entityName,
            condition != null ? condition.getCondition() : NOT_APPLICABLE,
            condition != null ? condition.getDescription() : NOT_APPLICABLE);

        close();
    }

    private void onSessionTimeout() {
        // It is supposed to close a local session to handle timeout exception.
        // However, closing the session can result in NPE because of proton-j bug (https://issues.apache.org/jira/browse/PROTON-1939).
        // And the bug will cause the reactor thread to stop processing pending tasks scheduled on the reactor and
        // as a result task won't be completed at all.

        // TODO: handle timeout error once the proton-j bug is fixed.
        // if (!sessionCreated && !sessionOpenErrorDispatched) {
        //     logger.asWarning().log(
        //     "SessionTimeoutHandler.onEvent - connectionId[{}], entityName[{}], session open timed out.",
        //     this.connectionId, this.entityName);
        // }
    }

    private static class SessionErrorContext extends ErrorContext {
        private static final long serialVersionUID = 7031664116367058509L;
        private final String entityPath;

        SessionErrorContext(Throwable exception, String namespaceName, String entityPath) {
            super(exception, namespaceName);
            this.entityPath = entityPath;
        }

        @Override
        public String toString() {
            return String.format(Locale.US, "NS: %s. EntityPath: %s. Exception: %s", namespaceName(), entityPath, exception());
        }
    }
}
