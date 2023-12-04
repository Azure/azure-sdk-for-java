// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation.instrumentation;

import com.azure.core.test.utils.metrics.TestMeter;
import com.azure.core.util.Context;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServiceBusReceiverInstrumentationTests {
    @Test
    public void testInstrumentNullMessageNoMeter() {
        Tracer tracer = mock(Tracer.class);
        when(tracer.isEnabled()).thenReturn(true);

        ServiceBusReceiverInstrumentation instrumentation = new ServiceBusReceiverInstrumentation(tracer, null,
            "fqdn", "entityPath", null, ReceiverKind.ASYNC_RECEIVER);

        instrumentation.instrumentProcess("span name", null, Context.NONE);
        instrumentation.instrumentSettlement(Mono.just(1), null, Context.NONE, DispositionStatus.ABANDONED);
        verify(tracer, never()).start(anyString(), any(StartSpanOptions.class), any(Context.class));
    }

    @Test
    public void testInstrumentNullMessageNoTracer() {
        Meter meter = new TestMeter();

        ServiceBusReceiverInstrumentation instrumentation = new ServiceBusReceiverInstrumentation(null, meter,
            "fqdn", "entityPath", null, ReceiverKind.ASYNC_RECEIVER);

        // does not throw
        instrumentation.instrumentProcess("span name", null, Context.NONE);
        instrumentation.instrumentSettlement(Mono.just(1), null, Context.NONE, DispositionStatus.ABANDONED);
    }

    @Test
    public void testInstrumentNullMessageDisabled() {
        ServiceBusReceiverInstrumentation instrumentation = new ServiceBusReceiverInstrumentation(null, null,
            "fqdn", "entityPath", null, ReceiverKind.ASYNC_RECEIVER);

        // does not throw
        instrumentation.instrumentProcess("span name", null, Context.NONE);
        instrumentation.instrumentSettlement(Mono.just(1), null, Context.NONE, DispositionStatus.ABANDONED);
    }
}
