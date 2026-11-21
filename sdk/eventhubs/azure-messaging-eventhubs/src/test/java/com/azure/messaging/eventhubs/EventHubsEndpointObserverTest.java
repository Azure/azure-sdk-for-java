// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.test.utils.metrics.TestCounter;
import com.azure.core.test.utils.metrics.TestMeasurement;
import com.azure.core.test.utils.metrics.TestMeter;
import com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsTracer;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Event.Type;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.amqp.transport.Target;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventHubsEndpointObserverTest {
    @Test
    public void openAndInvalidateOncePerSender() {
        try (TestMeter meter = new TestMeter()) {
            TestCounter counter
                = (TestCounter) meter.createLongCounter("az.eventhubs.amqp.lifecycle", "", "{transition}");
            EventHubsEndpointObserver sender = observer(meter, counter);
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
            EventHubsEndpointObserver rejected = observer(meter, counter);
            rejected.onUnhandled(event(Type.LINK_INIT, link));
            rejected.onUnhandled(event(Type.LINK_REMOTE_OPEN, link));
            rejected.onUnhandled(event(Type.LINK_REMOTE_DETACH, link));
            EventHubsEndpointObserver cancelled = observer(meter, counter);
            cancelled.onUnhandled(event(Type.LINK_INIT, link));
            cancelled.onUnhandled(event(Type.LINK_LOCAL_CLOSE, link));
            cancelled.onUnhandled(event(Type.LINK_FINAL, link));
            assertEquals(2, counter.getMeasurements().size());
            assertEquals("error", counter.getMeasurements().get(0).getAttributes().get("outcome"));
            assertEquals("cancelled", counter.getMeasurements().get(1).getAttributes().get("outcome"));
        }
    }

    private static EventHubsEndpointObserver observer(TestMeter meter, TestCounter counter) {
        return new EventHubsEndpointObserver(EventHubsEndpointObserver.Component.SENDER, "connection", "sender",
            new EventHubsTracer(null, "example.org", "hub", null), meter, counter);
    }

    private static Event event(Type type, Link link) {
        Event event = mock(Event.class);
        when(event.getType()).thenReturn(type);
        when(event.getLink()).thenReturn(link);
        return event;
    }
}
