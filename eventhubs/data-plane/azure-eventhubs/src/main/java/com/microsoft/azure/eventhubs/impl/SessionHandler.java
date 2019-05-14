// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.EventHubException;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Handler;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.reactor.Reactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Iterator;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SessionHandler extends BaseHandler {
    protected static final Logger TRACE_LOGGER = LoggerFactory.getLogger(SessionHandler.class);

    private final String entityName;
    private final Consumer<Session> onRemoteSessionOpen;
    private final BiConsumer<ErrorCondition, Exception> onRemoteSessionOpenError;
    private final Duration openTimeout;
    private final String connectionId;

    private boolean sessionCreated = false;
    private boolean sessionOpenErrorDispatched = false;

    public SessionHandler(final String entityName,
                          final Consumer<Session> onRemoteSessionOpen,
                          final BiConsumer<ErrorCondition, Exception> onRemoteSessionOpenError,
                          final Duration openTimeout,
                          final String connectionId) {
        this.entityName = entityName;
        this.onRemoteSessionOpenError = onRemoteSessionOpenError;
        this.onRemoteSessionOpen = onRemoteSessionOpen;
        this.openTimeout = openTimeout;
        this.connectionId = connectionId;
    }

    @Override
    public void onSessionLocalOpen(Event e) {
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "onSessionLocalOpen connectionId[%s], entityName[%s], condition[%s]",
                    this.connectionId, this.entityName, e.getSession().getCondition() == null ? "none" : e.getSession().getCondition().toString()));
        }

        if (this.onRemoteSessionOpenError != null) {
            ReactorHandler reactorHandler = null;
            final Reactor reactor = e.getReactor();
            final Iterator<Handler> reactorEventHandlers = reactor.getHandler().children();
            while (reactorEventHandlers.hasNext()) {
                final Handler currentHandler = reactorEventHandlers.next();
                if (currentHandler instanceof ReactorHandler) {
                    reactorHandler = (ReactorHandler) currentHandler;
                    break;
                }
            }

            if (reactorHandler == null) {
                this.onRemoteSessionOpenError.accept(
                    null,
                    new EventHubException(
                        false,
                        String.format("OnSessionLocalOpen entityName[%s], reactorHandler: NULL POINTER exception.", this.entityName))
                );
                e.getSession().close();
                return;
            }

            final ReactorDispatcher reactorDispatcher = reactorHandler.getReactorDispatcher();
            final Session session = e.getSession();

            try {
                reactorDispatcher.invoke((int) this.openTimeout.toMillis(), new SessionTimeoutHandler(entityName, connectionId));
            } catch (IOException ioException) {
                if (TRACE_LOGGER.isWarnEnabled()) {
                    TRACE_LOGGER.warn(String.format(Locale.US, "onSessionLocalOpen connectionId[%s], entityName[%s], reactorDispatcherError[%s]",
                            this.connectionId, this.entityName, ioException.getMessage()));
                }

                session.close();
                this.onRemoteSessionOpenError.accept(
                        null,
                        new EventHubException(
                                false,
                                String.format(Locale.US, "onSessionLocalOpen connectionId[%s], entityName[%s], underlying IO of reactorDispatcher faulted with error: %s",
                                        this.connectionId, this.entityName, ioException.getMessage()), ioException));
            }
        }
    }

    @Override
    public void onSessionRemoteOpen(Event e) {
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "onSessionRemoteOpen connectionId[%s], entityName[%s], sessionIncCapacity[%s], sessionOutgoingWindow[%s]",
                    this.connectionId, this.entityName, e.getSession().getIncomingCapacity(), e.getSession().getOutgoingWindow()));
        }

        final Session session = e.getSession();
        if (session != null && session.getLocalState() == EndpointState.UNINITIALIZED) {
            session.open();
        }

        sessionCreated = true;
        if (this.onRemoteSessionOpen != null) {
            this.onRemoteSessionOpen.accept(session);
        }
    }

    @Override
    public void onSessionLocalClose(Event e) {
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "onSessionLocalClose connectionId[%s], entityName[%s], condition[%s]", this.entityName,
                    this.connectionId, e.getSession().getCondition() == null ? "none" : e.getSession().getCondition().toString()));
        }
    }

    @Override
    public void onSessionRemoteClose(Event e) {
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "onSessionRemoteClose connectionId[%s], entityName[%s], condition[%s]", this.entityName,
                    this.connectionId, e.getSession().getRemoteCondition() == null ? "none" : e.getSession().getRemoteCondition().toString()));
        }

        final Session session = e.getSession();
        ErrorCondition condition = session != null ? session.getRemoteCondition() : null;

        if (session != null && session.getLocalState() != EndpointState.CLOSED) {
            if (TRACE_LOGGER.isInfoEnabled()) {
                TRACE_LOGGER.info(String.format(Locale.US, "onSessionRemoteClose closing a local session for connectionId[%s], entityName[%s], condition[%s], description[%s]",
                        this.connectionId, this.entityName, condition != null ? condition.getCondition() : "n/a", condition != null ? condition.getDescription() : "n/a"));
            }

            session.setCondition(session.getRemoteCondition());
            session.close();
        }

        this.sessionOpenErrorDispatched = true;
        if (!sessionCreated && this.onRemoteSessionOpenError != null) {
            this.onRemoteSessionOpenError.accept(condition, null);
        }
    }

    @Override
    public void onSessionFinal(Event e) {
        if (TRACE_LOGGER.isInfoEnabled()) {
            final Session session = e.getSession();
            ErrorCondition condition = session != null ? session.getCondition() : null;

            TRACE_LOGGER.info(String.format(Locale.US, "onSessionFinal connectionId[%s], entityName[%s], condition[%s], description[%s]",
                    this.connectionId, this.entityName, condition != null ? condition.getCondition() : "n/a", condition != null ? condition.getDescription() : "n/a"));
        }
    }

    private class SessionTimeoutHandler extends DispatchHandler {

        private final String entityName;
        private final String connectionId;

        SessionTimeoutHandler(final String entityName, final String connectionId) {
            this.entityName = entityName;
            this.connectionId = connectionId;
        }

        @Override
        public void onEvent() {
            // It is supposed to close a local session to handle timeout exception.
            // However, closing the session can result in NPE because of proton-j bug (https://issues.apache.org/jira/browse/PROTON-1939).
            // And the bug will cause the reactor thread to stop processing pending tasks scheduled on the reactor and
            // as a result task won't be completed at all.

            // TODO: handle timeout error once the proton-j bug is fixed.

            if (!sessionCreated && !sessionOpenErrorDispatched) {
                if (TRACE_LOGGER.isWarnEnabled()) {
                    TRACE_LOGGER.warn(String.format(Locale.US, "SessionTimeoutHandler.onEvent - connectionId[%s], entityName[%s], session open timed out.",
                            this.connectionId, this.entityName));
                }
            }
        }
    }
}
