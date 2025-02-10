// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.SessionErrorContext;
import com.azure.core.amqp.implementation.AmqpMetricsProvider;
import com.azure.core.amqp.implementation.ExceptionUtil;
import com.azure.core.amqp.implementation.ReactorDispatcher;
import com.azure.core.util.logging.LoggingEventBuilder;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Session;

import java.time.Duration;
import java.util.Locale;

import static com.azure.core.amqp.implementation.AmqpLoggingUtils.addErrorCondition;
import static com.azure.core.amqp.implementation.ClientConstants.SESSION_ID_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.SESSION_NAME_KEY;

/**
 * Handler for managing a session.
 */
public class SessionHandler extends Handler {
    private final String sessionName;
    private final AmqpMetricsProvider metricsProvider;

    /**
     * Creates a session handler.
     *
     * @param connectionId Identifier for the connection.
     * @param hostname Hostname of the connection.
     * @param sessionName Name of the session.
     * @param reactorDispatcher Reactor dispatcher.
     * @param openTimeout Timeout for opening the session.
     * @param metricProvider Metrics provider.
     */
    public SessionHandler(String connectionId, String hostname, String sessionName, ReactorDispatcher reactorDispatcher,
        Duration openTimeout, AmqpMetricsProvider metricProvider) {
        super(connectionId, hostname);
        this.sessionName = sessionName;
        this.metricsProvider = metricProvider;
    }

    /**
     * Gets the name of the session.
     *
     * @return the session name.
     */
    public String getSessionName() {
        return sessionName;
    }

    /**
     * Gets the error context of the session.
     *
     * @return The error context of the session.
     */
    public AmqpErrorContext getErrorContext() {
        return new SessionErrorContext(getHostname(), sessionName);
    }

    @Override
    public void onSessionLocalOpen(Event e) {
        addErrorCondition(logger.atVerbose(), e.getSession().getCondition()).addKeyValue(SESSION_NAME_KEY, sessionName)
            .addKeyValue(SESSION_ID_KEY, getId())
            .log("onSessionLocalOpen");
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
            .addKeyValue(SESSION_ID_KEY, getId())
            .addKeyValue("sessionIncCapacity", session.getIncomingCapacity())
            .addKeyValue("sessionOutgoingWindow", session.getOutgoingWindow())
            .log("onSessionRemoteOpen");

        onNext(EndpointState.ACTIVE);
    }

    @Override
    public void onSessionLocalClose(Event e) {
        final ErrorCondition condition = (e != null && e.getSession() != null) ? e.getSession().getCondition() : null;

        addErrorCondition(logger.atVerbose(), condition).addKeyValue(SESSION_NAME_KEY, sessionName)
            .addKeyValue(SESSION_ID_KEY, getId())
            .log("onSessionLocalClose");
    }

    @Override
    public void onSessionRemoteClose(Event e) {
        final Session session = e.getSession();
        final ErrorCondition condition = session != null ? session.getRemoteCondition() : null;

        addErrorCondition(logger.atInfo(), condition).addKeyValue(SESSION_NAME_KEY, sessionName)
            .addKeyValue(SESSION_ID_KEY, getId())
            .log("onSessionRemoteClose");

        if (session != null && session.getLocalState() != EndpointState.CLOSED) {
            addErrorCondition(logger.atInfo(), condition).addKeyValue(SESSION_NAME_KEY, sessionName)
                .log("onSessionRemoteClose closing a local session.");

            session.setCondition(session.getRemoteCondition());
            session.close();
        }

        if (condition == null || condition.getCondition() == null) {
            onNext(EndpointState.CLOSED);
        } else {
            final String id = getConnectionId();
            final AmqpErrorContext context = getErrorContext();

            final Exception exception = ExceptionUtil.toException(
                condition.getCondition().toString(), String.format(Locale.US,
                    "onSessionRemoteClose connectionId[%s], entityName[%s] condition[%s]", id, sessionName, condition),
                context);

            metricsProvider.recordHandlerError(AmqpMetricsProvider.ErrorSource.SESSION, condition);
            onError(exception);
        }
    }

    @Override
    public void onSessionFinal(Event e) {
        final Session session = e.getSession();
        final ErrorCondition condition = session != null ? session.getCondition() : null;

        addErrorCondition(logger.atInfo(), condition).addKeyValue(SESSION_NAME_KEY, sessionName)
            .addKeyValue(SESSION_ID_KEY, getId())
            .log("onSessionFinal.");
        close();
    }
}
