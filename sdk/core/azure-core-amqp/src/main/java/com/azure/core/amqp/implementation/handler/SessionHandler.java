// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.SessionErrorContext;
import com.azure.core.amqp.implementation.ExceptionUtil;
import com.azure.core.amqp.implementation.ReactorDispatcher;
import com.azure.core.util.logging.LoggingEventBuilder;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Session;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.RejectedExecutionException;

import static com.azure.core.amqp.implementation.AmqpLoggingUtils.addErrorCondition;
import static com.azure.core.amqp.implementation.ClientConstants.SESSION_NAME_KEY;

public class SessionHandler extends Handler {
    private final String sessionName;
    private final Duration openTimeout;
    private final ReactorDispatcher reactorDispatcher;

    public SessionHandler(String connectionId, String hostname, String sessionName, ReactorDispatcher reactorDispatcher,
                          Duration openTimeout) {
        super(connectionId, hostname);
        this.sessionName = sessionName;
        this.openTimeout = openTimeout;
        this.reactorDispatcher = reactorDispatcher;
    }

    public AmqpErrorContext getErrorContext() {
        return new SessionErrorContext(getHostname(), sessionName);
    }

    @Override
    public void onSessionLocalOpen(Event e) {
        addErrorCondition(logger.atVerbose(), e.getSession().getCondition())
            .addKeyValue(SESSION_NAME_KEY, sessionName)
            .log("onSessionLocalOpen");

        final Session session = e.getSession();

        try {
            reactorDispatcher.invoke(this::onSessionTimeout, this.openTimeout);
        } catch (IOException | RejectedExecutionException ioException) {
            logger.atInfo()
                .addKeyValue(SESSION_NAME_KEY, sessionName)
                .addKeyValue("reactorDispatcherError", ioException.getMessage())
                .log("onSessionLocalOpen");

            session.close();

            final String message =
                String.format(Locale.US, "onSessionLocalOpen connectionId[%s], entityName[%s], underlying IO of"
                        + " reactorDispatcher faulted with error: %s",
                    getConnectionId(), sessionName, ioException.getMessage());
            final Throwable exception = new AmqpException(false, message, ioException, getErrorContext());

            onError(exception);
        }
    }

    @Override
    public void onSessionRemoteOpen(Event e) {
        final Session session = e.getSession();
        LoggingEventBuilder logBuilder;
        if (session.getLocalState() == EndpointState.UNINITIALIZED) {
            logBuilder = logger.atWarning();
            session.open();
        } else {
            logBuilder = logger.atInfo();
        }


        logBuilder.addKeyValue(SESSION_NAME_KEY, sessionName)
            .addKeyValue("sessionIncCapacity", session.getIncomingCapacity())
            .addKeyValue("sessionOutgoingWindow", session.getOutgoingWindow())
            .log("onSessionRemoteOpen");

        onNext(EndpointState.ACTIVE);
    }

    @Override
    public void onSessionLocalClose(Event e) {
        final ErrorCondition condition = (e != null && e.getSession() != null)
            ? e.getSession().getCondition()
            : null;

        addErrorCondition(logger.atVerbose(), condition)
            .addKeyValue(SESSION_NAME_KEY, sessionName)
            .log("onSessionLocalClose");
    }

    @Override
    public void onSessionRemoteClose(Event e) {
        final Session session = e.getSession();
        final ErrorCondition condition = session != null ? session.getRemoteCondition() : null;

        addErrorCondition(logger.atInfo(), condition)
            .addKeyValue(SESSION_NAME_KEY, sessionName)
            .log("onSessionRemoteClose");

        if (session != null && session.getLocalState() != EndpointState.CLOSED) {
            addErrorCondition(logger.atInfo(), condition)
                .addKeyValue(SESSION_NAME_KEY, sessionName)
                .log("onSessionRemoteClose closing a local session.");

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
                    id, sessionName, condition), context);

            onError(exception);
        }
    }

    @Override
    public void onSessionFinal(Event e) {
        final Session session = e.getSession();
        final ErrorCondition condition = session != null ? session.getCondition() : null;

        addErrorCondition(logger.atInfo(), condition)
            .addKeyValue(SESSION_NAME_KEY, sessionName)
            .log("onSessionFinal.");
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
