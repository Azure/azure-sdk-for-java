// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.util.Context;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.ReactorDispatcher;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import com.azure.core.amqp.implementation.handler.DeliverySettleMode;
import com.azure.core.amqp.implementation.handler.Handler;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler2;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.core.util.metrics.LongCounter;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsTracer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_DESTINATION_NAME;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.SERVER_ADDRESS;

/** Attaches lifecycle observers to the actual Proton endpoints on both AMQP stacks. */
final class EventHubsHandlerProvider extends ReactorHandlerProvider {
    static final ClientLogger LOGGER = new ClientLogger(EventHubsHandlerProvider.class);
    final EventHubsTracer tracer;
    private final String namespace;
    private final String entity;
    private final Meter meter;
    private final LongCounter lifecycle;

    EventHubsHandlerProvider(ReactorProvider provider, Meter meter, Tracer tracer, String namespace, String entity) {
        super(provider, meter);
        this.tracer = new EventHubsTracer(tracer, namespace, entity, null);
        this.meter = meter;
        this.namespace = namespace;
        this.entity = entity;
        this.lifecycle = meter != null && meter.isEnabled()
            ? meter.createLongCounter("az.eventhubs.amqp.lifecycle", "AMQP endpoint creation and invalidation",
                "{transition}")
            : null;
    }

    private <T extends Handler> T observe(T handler, EventHubsEndpointObserver.Component component,
        String logicalName) {
        if (tracer.isEnabled()
            || (lifecycle != null && lifecycle.isEnabled())
            || LOGGER.canLogAtLevel(LogLevel.INFORMATIONAL)) {
            handler.add(new EventHubsEndpointObserver(component, handler, logicalName, this));
        }
        return handler;
    }

    void recordLifecycle(String component, String outcome, Context context) {
        if (lifecycle != null && lifecycle.isEnabled()) {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(SERVER_ADDRESS, namespace);
            attributes.put(MESSAGING_DESTINATION_NAME, entity);
            attributes.put("component", component);
            attributes.put("outcome", outcome);
            lifecycle.add(1, meter.createAttributes(attributes), context);
        }
    }

    private <T extends Handler> T observeLink(T handler, EventHubsEndpointObserver.Component component, String name,
        String path) {
        // CBS and management request/response links share these AMQP factories with Event Hubs data links.
        return "$cbs".equals(path) || "$management".equals(path) || path.endsWith("/$management")
            ? handler
            : observe(handler, component, name);
    }

    @Override
    public ConnectionHandler createConnectionHandler(String connectionId, ConnectionOptions options) {
        return observe(super.createConnectionHandler(connectionId, options),
            EventHubsEndpointObserver.Component.CONNECTION, namespace);
    }

    @Override
    public SessionHandler createSessionHandler(String connectionId, String host, String name, Duration timeout) {
        return observe(super.createSessionHandler(connectionId, host, name, timeout),
            EventHubsEndpointObserver.Component.SESSION, name);
    }

    @Override
    public SendLinkHandler createSendLinkHandler(String connectionId, String host, String name, String path) {
        return observeLink(super.createSendLinkHandler(connectionId, host, name, path),
            EventHubsEndpointObserver.Component.SENDER, name, path);
    }

    @Override
    public ReceiveLinkHandler createReceiveLinkHandler(String connectionId, String host, String name, String path) {
        return observeLink(super.createReceiveLinkHandler(connectionId, host, name, path),
            EventHubsEndpointObserver.Component.RECEIVER, name, path);
    }

    @Override
    public ReceiveLinkHandler2 createReceiveLinkHandler(String connectionId, String host, String name, String path,
        DeliverySettleMode settleMode, boolean includeTag, ReactorDispatcher dispatcher, AmqpRetryOptions retry) {
        return observeLink(
            super.createReceiveLinkHandler(connectionId, host, name, path, settleMode, includeTag, dispatcher, retry),
            EventHubsEndpointObserver.Component.RECEIVER, name, path);
    }
}
