// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.instrumentation;

import com.azure.core.test.utils.metrics.TestCounter;
import com.azure.core.test.utils.metrics.TestMeter;
import com.azure.core.util.Context;
import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.metrics.LongCounter;
import com.azure.core.util.tracing.Tracer;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InstrumentationScopeTests {
    private static final BiConsumer<EventHubsMetricsProvider, InstrumentationScope> NOOP_METRICS_CALLBACK = (m, s) -> {
    };


    @Test
    public void disabledScope() {
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(null, null, "fullyQualifiedName", "entityName", "consumerGroup", true);
        InstrumentationScope scope = instrumentation.createScope(null);
        assertNotNull(scope);
        assertFalse(scope.isEnabled());
        assertSame(Context.NONE, scope.getSpan());

        scope.setCancelled();
        assertNull(scope.getErrorType());

        scope.setError(new RuntimeException("test"));
        assertNull(scope.getError());

        assertNull(scope.getStartTime());

        scope.close();
    }

    @Test
    public void enabledTracesScope() {
        Tracer tracer = mock(Tracer.class);
        when(tracer.isEnabled()).thenReturn(true);
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(tracer, null, "fullyQualifiedName", "entityName", "consumerGroup", true);
        InstrumentationScope scope = instrumentation.createScope(null);
        assertNotNull(scope);
        assertTrue(scope.isEnabled());

        assertSame(Context.NONE, scope.getSpan());
        scope.close();
    }

    @Test
    public void enabledMetricsScopeNoCallback() {
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(null, new TestMeter(), "fullyQualifiedName", "entityName", "consumerGroup", true);
        InstrumentationScope scope = instrumentation.createScope(null);
        assertNotNull(scope);
        assertNotNull(scope.getStartTime());
        assertTrue(scope.isEnabled());
        scope.close();
    }

    @Test
    public void enabledMetricsScopeWithCallback() {
        TestMeter meter = new TestMeter();
        LongCounter counter = meter.createLongCounter("test.me.counter", null, null);
        TelemetryAttributes attributes = meter.createAttributes(Collections.emptyMap());
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(null, meter, "fullyQualifiedName", "entityName", "consumerGroup", true);

        InstrumentationScope scope = instrumentation.createScope((m, s) -> counter.add(1, attributes, s.getSpan()));
        scope.setSpan(new Context("foo", "bar"));
        assertNotNull(scope);
        assertTrue(scope.isEnabled());
        scope.close();

        TestCounter testCounter = (TestCounter) counter;
        assertEquals(1, testCounter.getMeasurements().size());
        assertEquals(1L, testCounter.getMeasurements().get(0).getValue());
        assertSame(scope.getSpan(), testCounter.getMeasurements().get(0).getContext());
    }

    @Test
    public void recordsStartTimeWhenMetricsEnabledScope() {
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(null, new TestMeter(), "fullyQualifiedName", "entityName", "consumerGroup", true);
        InstrumentationScope scope = instrumentation.createScope(null);
        assertNotNull(scope.getStartTime());
        scope.close();
    }

    @Test
    public void scopeClosesSpan() {
        Tracer tracer = mock(Tracer.class);
        when(tracer.isEnabled()).thenReturn(true);
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(tracer, null, "fullyQualifiedName", "entityName", "consumerGroup", true);

        InstrumentationScope scope = instrumentation.createScope(null);
        scope.close();

        verify(tracer).end(isNull(), isNull(), same(Context.NONE));
    }

    @Test
    public void scopeClosesSpanAndScope() {
        Tracer tracer = mock(Tracer.class);
        when(tracer.isEnabled()).thenReturn(true);
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(tracer, null, "fullyQualifiedName", "entityName", "consumerGroup", true);

        AtomicBoolean spanClosed = new AtomicBoolean();
        when(tracer.makeSpanCurrent(any(Context.class))).thenReturn(() -> spanClosed.set(true));

        AtomicBoolean metricCallbackCalled = new AtomicBoolean();
        InstrumentationScope scope = instrumentation.createScope((m, s) -> metricCallbackCalled.set(true))
                .makeSpanCurrent();

        scope.close();

        assertTrue(spanClosed.get());
        assertTrue(metricCallbackCalled.get());
        verify(tracer).end(isNull(), isNull(), same(Context.NONE));
    }

    @Test
    public void setError() {
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(null, new TestMeter(), "fullyQualifiedName", "entityName", "consumerGroup", true);

        InstrumentationScope scope = instrumentation.createScope(null);
        Throwable error = new RuntimeException("test");
        scope.setError(error);
        assertSame(error, scope.getError());
        assertEquals(RuntimeException.class.getName(), scope.getErrorType());
    }

    @Test
    public void setCancelled() {
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(null, new TestMeter(), "fullyQualifiedName", "entityName", "consumerGroup", true);

        InstrumentationScope scope = instrumentation.createScope(null);
        scope.setCancelled();
        assertNull(scope.getError());
        assertEquals("cancelled", scope.getErrorType());

        Throwable error = new RuntimeException("test");
        scope.setError(error);

        assertEquals("cancelled", scope.getErrorType());
        assertSame(error, scope.getError());
    }
}
