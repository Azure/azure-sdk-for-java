// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.Context;
import com.azure.core.amqp.implementation.handler.Handler;
import com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsTracer;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Endpoint;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Event.Type;

import reactor.core.Disposable;

import java.util.concurrent.atomic.AtomicLong;

import static com.azure.core.util.tracing.SpanKind.CLIENT;

/** Each instance observes one endpoint on its Reactor thread. */
final class EventHubsEndpointObserver extends BaseHandler {
    private static final AtomicLong NEXT_ATTEMPT = new AtomicLong();

    enum Component {
        CONNECTION("connection", "open", "EventHubs.connection.open", Type.CONNECTION_INIT, Type.CONNECTION_REMOTE_OPEN,
            Type.CONNECTION_LOCAL_CLOSE, Type.CONNECTION_REMOTE_CLOSE, Type.CONNECTION_FINAL),
        SESSION("session", "begin", "EventHubs.session.begin", Type.SESSION_INIT, Type.SESSION_REMOTE_OPEN,
            Type.SESSION_LOCAL_CLOSE, Type.SESSION_REMOTE_CLOSE, Type.SESSION_FINAL),
        SENDER("sender", "send", "EventHubs.sender.attach", Type.LINK_INIT, Type.LINK_REMOTE_OPEN,
            Type.LINK_LOCAL_CLOSE, Type.LINK_REMOTE_CLOSE, Type.LINK_FINAL),
        RECEIVER("receiver", "receive", "EventHubs.receiver.attach", Type.LINK_INIT, Type.LINK_REMOTE_OPEN,
            Type.LINK_LOCAL_CLOSE, Type.LINK_REMOTE_CLOSE, Type.LINK_FINAL);

        final String label;
        final String operation;
        final String spanName;
        final Type init;
        final Type opened;
        final Type localClose;
        final Type remoteClose;
        final Type terminal;

        Component(String label, String operation, String spanName, Type init, Type opened, Type localClose,
            Type remoteClose, Type terminal) {
            this.label = label;
            this.operation = operation;
            this.spanName = spanName;
            this.init = init;
            this.opened = opened;
            this.localClose = localClose;
            this.remoteClose = remoteClose;
            this.terminal = terminal;
        }
    }

    private final Component component;
    private final String connectionId;
    private final String endpointId;
    private final EventHubsTracer tracer;
    private final EventHubsHandlerProvider telemetry;
    private final String logicalName;
    private final Disposable stateSubscription;
    private volatile boolean handlerFailed;
    private final long attempt = NEXT_ATTEMPT.incrementAndGet();
    private Context opening;
    private boolean active;
    private boolean finished;

    EventHubsEndpointObserver(Component component, Handler handler, String logicalName,
        EventHubsHandlerProvider telemetry) {
        this.component = component;
        this.connectionId = handler.getConnectionId();
        this.endpointId = handler.getId();
        this.logicalName = logicalName;
        this.telemetry = telemetry;
        this.tracer = telemetry.tracer;
        // Some timeouts reach the handler before local cleanup and have no Proton error condition.
        this.stateSubscription = handler.getEndpointStates().subscribe(state -> {
        }, error -> handlerFailed = true);
    }

    @Override
    public void onUnhandled(Event event) {
        if (finished) {
            return;
        }
        Type type = event.getType();
        boolean link = component == Component.SENDER || component == Component.RECEIVER;
        if (type == component.init && opening == null && !active) {
            opening = tracer.startAmqpSpan(component.spanName, component.operation, CLIENT, Context.NONE);
            tracer.identifyEndpoint(opening, component.label, connectionId, endpointId, logicalName, attempt);
            log("started");
        } else if (type == component.opened && opening != null) {
            // A null remote terminus precedes a rejected ATTACH's DETACH.
            if ((component == Component.SENDER && event.getLink().getRemoteTarget() == null)
                || (component == Component.RECEIVER && event.getLink().getRemoteSource() == null)) {
                return;
            }
            completeOpen("success");
            active = true;
        } else if (type == component.remoteClose
            || (link && type == Type.LINK_REMOTE_DETACH)
            || (component == Component.CONNECTION && (type == Type.TRANSPORT_ERROR || type == Type.TRANSPORT_CLOSED))) {
            if (opening != null) {
                completeOpen("error");
            } else if (active) {
                record("invalidated", Context.NONE);
            }
            finished = true;
            stateSubscription.dispose();
        } else if (type == component.terminal
            || type == component.localClose
            || (link && type == Type.LINK_LOCAL_DETACH)) {
            if (opening != null) {
                completeOpen(handlerFailed
                    || hasError(event.getLink())
                    || hasError(event.getSession())
                    || hasError(event.getConnection())
                    || hasError(event.getTransport()) ? "error" : "cancelled");
            }
            finished = true;
            stateSubscription.dispose();
        }
    }

    private static boolean hasError(Endpoint endpoint) {
        return endpoint != null
            && (hasCondition(endpoint.getCondition()) || hasCondition(endpoint.getRemoteCondition()));
    }

    private static boolean hasCondition(ErrorCondition condition) {
        return condition != null && condition.getCondition() != null;
    }

    private void completeOpen(String outcome) {
        record(outcome, opening);
        tracer.endAmqpSpan(opening, outcome);
        opening = null;
    }

    private void record(String outcome, Context context) {
        telemetry.recordLifecycle(component.label, outcome, context);
        log(outcome);
    }

    private void log(String outcome) {
        EventHubsHandlerProvider.LOGGER.atInfo()
            .addKeyValue("component", component.label)
            .addKeyValue("outcome", outcome)
            .addKeyValue("connectionId", connectionId)
            .addKeyValue("endpointId", endpointId)
            .addKeyValue("endpointName", logicalName)
            .addKeyValue("generation", attempt)
            .log("Event Hubs AMQP lifecycle");
    }
}
