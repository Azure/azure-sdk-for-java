// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.instrumentation;

import com.azure.core.test.utils.metrics.TestMeter;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.Tracer;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

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
    @Test
    public void disabledScope() {
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(null, null, "fullyQualifiedName", "entityName", "consumerGroup", true);
        InstrumentationScope scope = instrumentation.createScope();
        assertNotNull(scope);
        assertFalse(scope.isEnabled());
        assertSame(Context.NONE, scope.getSpan());

        scope.setCancelled();
        assertNull(scope.getErrorType());

        scope.setError(new RuntimeException("test"));
        assertNull(scope.getError());

        scope.recordStartTime();
        assertNull(scope.getStartTime());

        scope.close();
    }

    @Test
    public void enabledTracesScope() {
        Tracer tracer = mock(Tracer.class);
        when(tracer.isEnabled()).thenReturn(true);
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(tracer, null, "fullyQualifiedName", "entityName", "consumerGroup", true);
        InstrumentationScope scope = instrumentation.createScope();
        assertNotNull(scope);
        assertTrue(scope.isEnabled());

        assertSame(Context.NONE, scope.getSpan());
        scope.close();
    }

    @Test
    public void enabledMetricsScope() {
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(null, new TestMeter(), "fullyQualifiedName", "entityName", "consumerGroup", true);
        InstrumentationScope scope = instrumentation.createScope();
        assertNotNull(scope);
        assertTrue(scope.isEnabled());
    }

    @Test
    public void recordsStartTimeWhenMetricsEnabledScope() {
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(null, new TestMeter(), "fullyQualifiedName", "entityName", "consumerGroup", true);
        InstrumentationScope scope = instrumentation.createScope().recordStartTime();
        assertNotNull(scope.getStartTime());
        scope.close();
    }

    @Test
    public void scopeClosesSpan() {
        Tracer tracer = mock(Tracer.class);
        when(tracer.isEnabled()).thenReturn(true);
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(tracer, null, "fullyQualifiedName", "entityName", "consumerGroup", true);

        InstrumentationScope scope = instrumentation.createScope();
        scope.close();

        verify(tracer).end(isNull(), isNull(), same(Context.NONE));
    }

    @Test
    public void scopeClosesSpanAndScope() {
        Tracer tracer = mock(Tracer.class);
        when(tracer.isEnabled()).thenReturn(true);
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(tracer, null, "fullyQualifiedName", "entityName", "consumerGroup", true);

        AtomicBoolean closed = new AtomicBoolean();
        when(tracer.makeSpanCurrent(any(Context.class))).thenReturn(() -> closed.set(true));

        InstrumentationScope scope = instrumentation.createScope()
                .makeSpanCurrent();

        scope.close();

        assertTrue(closed.get());
        verify(tracer).end(isNull(), isNull(), same(Context.NONE));
    }

    @Test
    public void setError() {
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(null, new TestMeter(), "fullyQualifiedName", "entityName", "consumerGroup", true);

        InstrumentationScope scope = instrumentation.createScope();
        Throwable error = new RuntimeException("test");
        scope.setError(error);
        assertSame(error, scope.getError());
        assertEquals(RuntimeException.class.getName(), scope.getErrorType());
    }

    @Test
    public void setCancelled() {
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(null, new TestMeter(), "fullyQualifiedName", "entityName", "consumerGroup", true);

        InstrumentationScope scope = instrumentation.createScope();
        scope.setCancelled();
        assertNull(scope.getError());
        assertEquals("cancelled", scope.getErrorType());

        Throwable error = new RuntimeException("test");
        scope.setError(error);

        assertEquals("cancelled", scope.getErrorType());
        assertSame(error, scope.getError());
    }
}
