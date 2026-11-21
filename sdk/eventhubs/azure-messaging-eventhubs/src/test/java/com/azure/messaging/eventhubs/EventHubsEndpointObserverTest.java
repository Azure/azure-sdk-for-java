// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.ReactorDispatcher;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.handler.DeliverySettleMode;
import com.azure.core.amqp.implementation.handler.Handler;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import com.azure.core.test.utils.metrics.TestCounter;
import com.azure.core.test.utils.metrics.TestMeasurement;
import com.azure.core.test.utils.metrics.TestMeter;
import com.azure.core.tracing.opentelemetry.OpenTelemetryTracingOptions;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.logging.LogLevel;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.amqp.transport.Source;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Event.Type;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.amqp.transport.Target;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventHubsEndpointObserverTest {
    @Test
    public void openAndInvalidateOncePerSender() {
        try (TestMeter meter = new TestMeter()) {
            TestCounter counter
                = (TestCounter) meter.createLongCounter("az.eventhubs.amqp.lifecycle", "", "{transition}");
            EventHubsEndpointObserver sender = sender(meter);
            Link link = mock(Link.class);
            when(link.getRemoteTarget()).thenReturn(mock(Target.class));
            sender.onUnhandled(event(Type.LINK_INIT, link));
            sender.onUnhandled(event(Type.LINK_REMOTE_OPEN, link));
            sender.onUnhandled(event(Type.LINK_REMOTE_OPEN, link));
            sender.onUnhandled(event(Type.LINK_REMOTE_DETACH, link));
            sender.onUnhandled(event(Type.LINK_REMOTE_CLOSE, link));
            List<TestMeasurement<Long>> measurements = counter.getMeasurements();
            assertEquals(2, measurements.size());
            assertEquals("success", measurements.get(0).getAttributes().get("outcome"));
            assertEquals("invalidated", measurements.get(1).getAttributes().get("outcome"));
            assertEquals("sender", measurements.get(1).getAttributes().get("component"));
        }
    }

    @Test
    public void refusedAttachAndLocalCancellationAreDistinct() {
        try (TestMeter meter = new TestMeter()) {
            TestCounter counter
                = (TestCounter) meter.createLongCounter("az.eventhubs.amqp.lifecycle", "", "{transition}");
            Link link = mock(Link.class);
            EventHubsEndpointObserver rejected = sender(meter);
            rejected.onUnhandled(event(Type.LINK_INIT, link));
            rejected.onUnhandled(event(Type.LINK_REMOTE_OPEN, link));
            rejected.onUnhandled(event(Type.LINK_REMOTE_DETACH, link));
            EventHubsEndpointObserver cancelled = sender(meter);
            cancelled.onUnhandled(event(Type.LINK_INIT, link));
            cancelled.onUnhandled(event(Type.LINK_LOCAL_CLOSE, link));
            cancelled.onUnhandled(event(Type.LINK_FINAL, link));
            assertEquals(2, counter.getMeasurements().size());
            assertEquals("error", counter.getMeasurements().get(0).getAttributes().get("outcome"));
            assertEquals("cancelled", counter.getMeasurements().get(1).getAttributes().get("outcome"));
        }
    }

    @Test
    public void localFailureAndPropagatedFailuresCloseHandshakeWithError() {
        try (TestMeter meter = new TestMeter()) {
            EventHubsHandlerProvider provider = provider(meter, null, "host", "hub");
            ErrorCondition timeout
                = new ErrorCondition(Symbol.getSymbol("amqp:resource-limit-exceeded"), "private diagnostics");
            Link link = mock(Link.class);
            when(link.getCondition()).thenReturn(timeout);
            EventHubsEndpointObserver local = sender(meter);
            local.onUnhandled(event(Type.LINK_INIT, link));
            local.onUnhandled(event(Type.LINK_LOCAL_CLOSE, link));

            EventHubsEndpointObserver child = sender(meter);
            Session session = mock(Session.class);
            when(session.getRemoteCondition()).thenReturn(timeout);
            Event cleanup = event(Type.LINK_LOCAL_CLOSE, mock(Link.class));
            when(cleanup.getSession()).thenReturn(session);
            child.onUnhandled(event(Type.LINK_INIT, null));
            child.onUnhandled(cleanup);

            SendLinkHandler handler = provider.createSendLinkHandler("connection", "host", "sender", "hub");
            EventHubsEndpointObserver signalled = observer(handler);
            signalled.onUnhandled(event(Type.LINK_INIT, null));
            handler.onError(new IllegalStateException("private diagnostics"));
            signalled.onUnhandled(event(Type.LINK_LOCAL_CLOSE, mock(Link.class)));
            List<TestMeasurement<Long>> points
                = meter.getCounters().get("az.eventhubs.amqp.lifecycle").getMeasurements();
            assertEquals(3, points.size());
            points.forEach(point -> assertEquals("error", point.getAttributes().get("outcome")));
        }
    }

    @Test
    public void lifecycleSeriesIdentifyTheNamespaceAndHub() {
        try (TestMeter meter = new TestMeter()) {
            for (String hub : Arrays.asList("first", "second")) {
                String host = hub + ".example.org";
                EventHubsEndpointObserver session = observer(provider(meter, null, host, hub)
                    .createSessionHandler("connection", host, "session", Duration.ofSeconds(1)));
                session.onUnhandled(event(Type.SESSION_INIT, null));
                session.onUnhandled(event(Type.SESSION_REMOTE_OPEN, null));
            }
            List<TestMeasurement<Long>> points
                = meter.getCounters().get("az.eventhubs.amqp.lifecycle").getMeasurements();
            assertEquals(2, points.size());
            for (int i = 0; i < 2; i++) {
                String hub = i == 0 ? "first" : "second";
                Map<String, Object> attributes = points.get(i).getAttributes();
                assertEquals(4, attributes.size());
                assertEquals(hub, attributes.get("messaging.destination.name"));
                assertEquals(hub + ".example.org", attributes.get("server.address"));
            }
        }
    }

    @Test
    public void factoriesAttachOnlyDataLinkObserversOnBothStacks() {
        try (TestMeter meter = new TestMeter()) {
            EventHubsHandlerProvider provider = provider(meter, null, "host", "hub");
            assertNotNull(observer(provider.createConnectionHandler("connection", options("host"))));
            assertNotNull(
                observer(provider.createSessionHandler("connection", "host", "session", Duration.ofSeconds(1))));
            for (String path : Arrays.asList("hub/Partitions/0", "$cbs", "$management", "hub/$management")) {
                boolean data = path.startsWith("hub/Partitions");
                assertEquals(data,
                    observer(provider.createSendLinkHandler("connection", "host", "sender", path)) != null);
                assertEquals(data,
                    observer(provider.createReceiveLinkHandler("connection", "host", "receiver", path)) != null);
                assertEquals(data,
                    observer(provider.createReceiveLinkHandler("connection", "host", "receiver", path,
                        DeliverySettleMode.SETTLE_ON_DELIVERY, false, mock(ReactorDispatcher.class),
                        new AmqpRetryOptions())) != null);
            }
        }
    }

    @Test
    public void disabledTracingAndMetricsAttachOnlyWhenLifecycleLoggingIsEnabled() {
        try (TestMeter meter = new TestMeter(false)) {
            EventHubsHandlerProvider provider = provider(meter, null, "host", "hub");
            assertEquals(EventHubsHandlerProvider.LOGGER.canLogAtLevel(LogLevel.INFORMATIONAL),
                observer(provider.createSendLinkHandler("connection", "host", "sender", "hub")) != null);
            assertNull(meter.getCounters().get("az.eventhubs.amqp.lifecycle"));
        }
    }

    @Test
    public void exportedHandshakeSpansCoverFailureRecoveryAndRejectedReceiver() {
        TestSpanProcessor processor = new TestSpanProcessor("host", "hub", "handshakes");
        try (SdkTracerProvider sdk = SdkTracerProvider.builder().addSpanProcessor(processor).build()) {
            Tracer tracer = TracerProvider.getDefaultProvider()
                .createTracer("test", null, "Microsoft.EventHub", new OpenTelemetryTracingOptions()
                    .setOpenTelemetry(OpenTelemetrySdk.builder().setTracerProvider(sdk).build()));
            EventHubsHandlerProvider provider = provider(null, tracer, "host", "hub");
            EventHubsEndpointObserver connection
                = observer(provider.createConnectionHandler("connection", options("host")));
            connection.onUnhandled(event(Type.CONNECTION_INIT, null));
            connection.onUnhandled(event(Type.TRANSPORT_ERROR, null));
            connection.onUnhandled(event(Type.CONNECTION_FINAL, null));

            EventHubsEndpointObserver session
                = observer(provider.createSessionHandler("connection", "host", "session", Duration.ofSeconds(1)));
            session.onUnhandled(event(Type.SESSION_INIT, null));
            session.onUnhandled(event(Type.SESSION_REMOTE_OPEN, null));
            session.onUnhandled(event(Type.SESSION_LOCAL_CLOSE, null));

            Link link = mock(Link.class);
            when(link.getRemoteTarget()).thenReturn(mock(Target.class));
            for (int attempt = 0; attempt < 2; attempt++) {
                EventHubsEndpointObserver sender = observer(
                    provider.createSendLinkHandler("connection", "host", "partition-sender", "hub/Partitions/0"));
                sender.onUnhandled(event(Type.LINK_INIT, link));
                sender.onUnhandled(event(Type.LINK_REMOTE_OPEN, link));
                sender.onUnhandled(event(Type.LINK_REMOTE_DETACH, link));
            }

            for (boolean accepted : Arrays.asList(false, true)) {
                EventHubsEndpointObserver receiver = observer(provider.createReceiveLinkHandler("connection", "host",
                    "partition-receiver", "hub/ConsumerGroups/group/Partitions/0"));
                if (accepted) {
                    when(link.getRemoteSource()).thenReturn(mock(Source.class));
                }
                receiver.onUnhandled(event(Type.LINK_INIT, link));
                receiver.onUnhandled(event(Type.LINK_REMOTE_OPEN, link));
                receiver.onUnhandled(event(Type.LINK_REMOTE_DETACH, link));
            }
            List<SpanData> spans
                = processor.getEndedSpans().stream().map(s -> s.toSpanData()).collect(Collectors.toList());
            assertEquals(
                Arrays.asList("EventHubs.connection.open", "EventHubs.session.begin", "EventHubs.sender.attach",
                    "EventHubs.sender.attach", "EventHubs.receiver.attach", "EventHubs.receiver.attach"),
                spans.stream().map(SpanData::getName).collect(Collectors.toList()));
            assertEquals(
                Arrays.asList(StatusCode.ERROR, StatusCode.UNSET, StatusCode.UNSET, StatusCode.UNSET, StatusCode.ERROR,
                    StatusCode.UNSET),
                spans.stream().map(s -> s.getStatus().getStatusCode()).collect(Collectors.toList()));
            AttributeKey<String> physicalId = AttributeKey.stringKey("az.eventhubs.amqp.component.id");
            AttributeKey<Long> generation = AttributeKey.longKey("az.eventhubs.amqp.component.generation");
            assertNotEquals(spans.get(2).getAttributes().get(physicalId), spans.get(3).getAttributes().get(physicalId));
            assertNotEquals(spans.get(2).getAttributes().get(generation), spans.get(3).getAttributes().get(generation));
            for (int attempt : Arrays.asList(2, 3)) {
                assertEquals("partition-sender",
                    spans.get(attempt).getAttributes().get(AttributeKey.stringKey("az.eventhubs.amqp.component.name")));
                assertEquals("connection",
                    spans.get(attempt).getAttributes().get(AttributeKey.stringKey("az.eventhubs.amqp.connection.id")));
            }
        }
    }

    private static EventHubsEndpointObserver sender(TestMeter meter) {
        return observer(
            provider(meter, null, "host", "hub").createSendLinkHandler("connection", "host", "sender", "hub"));
    }

    private static EventHubsHandlerProvider provider(TestMeter meter, Tracer tracer, String host, String hub) {
        return new EventHubsHandlerProvider(mock(ReactorProvider.class), meter, tracer, host, hub);
    }

    private static ConnectionOptions options(String host) {
        ConnectionOptions options = mock(ConnectionOptions.class);
        when(options.getTransportType()).thenReturn(AmqpTransportType.AMQP);
        when(options.getHostname()).thenReturn(host);
        when(options.getFullyQualifiedNamespace()).thenReturn(host);
        when(options.getClientOptions()).thenReturn(new ClientOptions());
        when(options.getProduct()).thenReturn("test");
        when(options.getClientVersion()).thenReturn("1");
        return options;
    }

    private static EventHubsEndpointObserver observer(Handler handler) {
        Iterator<org.apache.qpid.proton.engine.Handler> children = handler.children();
        while (children.hasNext()) {
            org.apache.qpid.proton.engine.Handler child = children.next();
            if (child instanceof EventHubsEndpointObserver) {
                return (EventHubsEndpointObserver) child;
            }
        }
        return null;
    }

    private static Event event(Type type, Link link) {
        Event event = mock(Event.class);
        when(event.getType()).thenReturn(type);
        when(event.getLink()).thenReturn(link);
        return event;
    }
}
