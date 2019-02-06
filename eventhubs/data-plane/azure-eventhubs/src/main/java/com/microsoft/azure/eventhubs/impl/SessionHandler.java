/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.EventHubException;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.*;
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

    private boolean sessionCreated = false;
    private boolean sessionOpenErrorDispatched = false;

    public SessionHandler(final String entityName,
                          final Consumer<Session> onRemoteSessionOpen,
                          final BiConsumer<ErrorCondition, Exception> onRemoteSessionOpenError,
                          final Duration openTimeout) {
        this.entityName = entityName;
        this.onRemoteSessionOpenError = onRemoteSessionOpenError;
        this.onRemoteSessionOpen = onRemoteSessionOpen;
        this.openTimeout = openTimeout;
    }

    @Override
    public void onSessionLocalOpen(Event e) {
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "onSessionLocalOpen entityName[%s], condition[%s]", this.entityName,
                    e.getSession().getCondition() == null ? "none" : e.getSession().getCondition().toString()));
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

            final ReactorDispatcher reactorDispatcher = reactorHandler.getReactorDispatcher();
            final Session session = e.getSession();

            try {
                reactorDispatcher.invoke((int) this.openTimeout.toMillis(), new SessionTimeoutHandler(session, entityName));
            } catch (IOException ioException) {
                if (TRACE_LOGGER.isWarnEnabled()) {
                    TRACE_LOGGER.warn(String.format(Locale.US, "onSessionLocalOpen entityName[%s], reactorDispatcherError[%s]",
                            this.entityName, ioException.getMessage()));
                }

                session.close();
                this.onRemoteSessionOpenError.accept(
                        null,
                        new EventHubException(
                                false,
                                String.format("onSessionLocalOpen entityName[%s], underlying IO of reactorDispatcher faulted with error: %s",
                                        this.entityName, ioException.getMessage()), ioException));
            }
        }
    }

    @Override
    public void onSessionRemoteOpen(Event e) {
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "onSessionRemoteOpen entityName[%s], sessionIncCapacity[%s], sessionOutgoingWindow[%s]",
                    this.entityName, e.getSession().getIncomingCapacity(), e.getSession().getOutgoingWindow()));
        }

        final Session session = e.getSession();
        if (session != null && session.getLocalState() == EndpointState.UNINITIALIZED) {
            session.open();
        }

        sessionCreated = true;
        if (this.onRemoteSessionOpen != null)
            this.onRemoteSessionOpen.accept(session);
    }

    @Override
    public void onSessionLocalClose(Event e) {
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "onSessionLocalClose entityName[%s], condition[%s]", this.entityName,
                    e.getSession().getCondition() == null ? "none" : e.getSession().getCondition().toString()));
        }
    }

    @Override
    public void onSessionRemoteClose(Event e) {
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "onSessionRemoteClose entityName[%s], condition[%s]", this.entityName,
                    e.getSession().getRemoteCondition() == null ? "none" : e.getSession().getRemoteCondition().toString()));
        }

        final Session session = e.getSession();
        ErrorCondition condition = session != null ? session.getRemoteCondition() : null;

        if (session != null && session.getLocalState() != EndpointState.CLOSED) {
            if (TRACE_LOGGER.isInfoEnabled()) {
                TRACE_LOGGER.info(String.format(Locale.US, "onSessionRemoteClose closing a local session for entityName[%s], condition[%s], description[%s]",
                        this.entityName,
                        condition != null ? condition.getCondition() : "n/a",
                        condition != null ? condition.getDescription() : "n/a"));
            }

            session.setCondition(session.getRemoteCondition());
            session.close();
        }

        this.sessionOpenErrorDispatched = true;
        if (!sessionCreated && this.onRemoteSessionOpenError != null)
            this.onRemoteSessionOpenError.accept(condition, null);
    }

    @Override
    public void onSessionFinal(Event e) {
        if (TRACE_LOGGER.isInfoEnabled()) {
            final Session session = e.getSession();
            ErrorCondition condition = session != null ? session.getCondition() : null;

            TRACE_LOGGER.info(String.format(Locale.US, "onSessionFinal entityName[%s], condition[%s], description[%s]",
                    this.entityName,
                    condition != null ? condition.getCondition() : "n/a",
                    condition != null ? condition.getDescription() : "n/a"));
        }
    }

    private class SessionTimeoutHandler extends DispatchHandler {

        private final Session session;
        private final String entityName;

        SessionTimeoutHandler(final Session session, final String entityName) {
            this.session = session;
            this.entityName = entityName;
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
                    TRACE_LOGGER.warn(String.format(Locale.US, "SessionTimeoutHandler.onEvent - entityName[%s], session open timed out.",
                            this.entityName));
                }
            }
        }
    }
}
