// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation.instrumentation;

import com.azure.core.test.utils.metrics.TestMeter;
import com.azure.core.util.Context;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import java.util.Collections;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Note: The 'ServiceBusReceiverInstrumentation::instrumentProcess' API added for v2 will never be invoked with
// a NULL ServiceBusReceivedMessage object, and all 'receive' instrumentation in v2 goes through this API.
//
// In v1, the 'receive' instrumentation goes through 'ServiceBusReceiverInstrumentation::startProcessInstrumentation' API
// and v1 can invoke this API with NULL ServiceBusReceivedMessage object. So the following tests asserts the instrumentation
// paths are safe from NPE in v1.
//
public class ServiceBusReceiverInstrumentationTests {
    @Test
    public void asyncProcessSpanUsesMessageParentWithoutLeakingThreadScope() {
        Tracer tracer = mock(Tracer.class);
        when(tracer.isEnabled()).thenReturn(true);
        String traceparent = "00-0123456789abcdef0123456789abcdef-0123456789abcdef-01";
        Context remoteParent = Context.NONE.addData("remote", "parent");
        when(tracer.extractContext(any())).thenAnswer(invocation -> {
            Function<String, String> getter = invocation.getArgument(0);
            org.junit.jupiter.api.Assertions.assertEquals(traceparent, getter.apply("traceparent"));
            return remoteParent;
        });
        when(tracer.start(anyString(), any(StartSpanOptions.class), any(Context.class))).thenAnswer(invocation -> {
            StartSpanOptions options = invocation.getArgument(1);
            org.junit.jupiter.api.Assertions.assertEquals(remoteParent, options.getRemoteParent());
            return Context.NONE.addData("process-span", "value");
        });
        ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        when(message.getApplicationProperties()).thenReturn(Collections.singletonMap("traceparent", traceparent));
        ServiceBusReceiverInstrumentation instrumentation
            = new ServiceBusReceiverInstrumentation(tracer, null, "namespace", "queue", null, ReceiverKind.PROCESSOR);
        StepVerifier.create(instrumentation.instrumentProcessAsync(message, Mono.empty())).verifyComplete();
        verify(tracer, never()).makeSpanCurrent(any());
    }

    @Test
    public void asyncProcessSpanFollowsHandlerLifetimeAndReactorContext() {
        Tracer tracer = mock(Tracer.class);
        when(tracer.isEnabled()).thenReturn(true);
        Context span = Context.NONE.addData("process-span", "value");
        when(tracer.start(anyString(), any(StartSpanOptions.class), any(Context.class))).thenReturn(span);
        ServiceBusReceiverInstrumentation instrumentation
            = new ServiceBusReceiverInstrumentation(tracer, null, "namespace", "queue", null, ReceiverKind.PROCESSOR);
        ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        Sinks.Empty<Void> finish = Sinks.empty();
        Mono<Void> processing = Mono.deferContextual(context -> {
            org.junit.jupiter.api.Assertions.assertEquals("value", context.get("process-span"));
            return finish.asMono();
        });

        StepVerifier.create(instrumentation.instrumentProcessAsync(message, processing))
            .then(() -> verify(tracer, never()).end(any(), any(), any()))
            .then(finish::tryEmitEmpty)
            .verifyComplete();
        verify(tracer).start(eq("ServiceBus.process"), any(StartSpanOptions.class), any(Context.class));
        verify(tracer).end(isNull(), isNull(), eq(span));
    }

    @Test
    public void asyncProcessSpanRecordsHandlerError() {
        Tracer tracer = mock(Tracer.class);
        when(tracer.isEnabled()).thenReturn(true);
        Context span = Context.NONE.addData("process-span", "value");
        when(tracer.start(anyString(), any(StartSpanOptions.class), any(Context.class))).thenReturn(span);
        ServiceBusReceiverInstrumentation instrumentation
            = new ServiceBusReceiverInstrumentation(tracer, null, "namespace", "queue", null, ReceiverKind.PROCESSOR);
        IllegalStateException error = new IllegalStateException("flush failed");
        ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);

        StepVerifier.create(instrumentation.instrumentProcessAsync(message, Mono.error(error)))
            .expectErrorMessage("flush failed")
            .verify();
        verify(tracer).end(isNull(), eq(error), eq(span));
    }

    @Test
    public void asyncProcessSpanIsCancelled() {
        Tracer tracer = mock(Tracer.class);
        when(tracer.isEnabled()).thenReturn(true);
        Context span = Context.NONE.addData("process-span", "value");
        when(tracer.start(anyString(), any(StartSpanOptions.class), any(Context.class))).thenReturn(span);
        ServiceBusReceiverInstrumentation instrumentation
            = new ServiceBusReceiverInstrumentation(tracer, null, "namespace", "queue", null, ReceiverKind.PROCESSOR);
        StepVerifier.create(instrumentation.instrumentProcessAsync(mock(ServiceBusReceivedMessage.class), Mono.never()))
            .thenCancel()
            .verify();
        verify(tracer).end(eq("cancelled"), isNull(), eq(span));
    }

    @Test
    public void asyncProcessCancellationWithoutTracing() {
        ServiceBusReceiverInstrumentation instrumentation
            = new ServiceBusReceiverInstrumentation(null, null, "namespace", "queue", null, ReceiverKind.PROCESSOR);
        StepVerifier.create(instrumentation.instrumentProcessAsync(mock(ServiceBusReceivedMessage.class), Mono.never()))
            .thenCancel()
            .verify();
    }

    @Test
    public void testInstrumentNullMessageNoMeter() {
        Tracer tracer = mock(Tracer.class);
        when(tracer.isEnabled()).thenReturn(true);

        ServiceBusReceiverInstrumentation instrumentation = new ServiceBusReceiverInstrumentation(tracer, null, "fqdn",
            "entityPath", null, ReceiverKind.ASYNC_RECEIVER);

        instrumentation.startProcessInstrumentation("span name", null, null, Context.NONE);
        instrumentation.instrumentSettlement(Mono.just(1), null, Context.NONE, DispositionStatus.ABANDONED);
        verify(tracer, never()).start(anyString(), any(StartSpanOptions.class), any(Context.class));
    }

    @Test
    public void testInstrumentNullMessageNoTracer() {
        Meter meter = new TestMeter();

        ServiceBusReceiverInstrumentation instrumentation = new ServiceBusReceiverInstrumentation(null, meter, "fqdn",
            "entityPath", null, ReceiverKind.ASYNC_RECEIVER);

        // does not throw
        instrumentation.startProcessInstrumentation("span name", null, null, Context.NONE);
        instrumentation.instrumentSettlement(Mono.just(1), null, Context.NONE, DispositionStatus.ABANDONED);
    }

    @Test
    public void testInstrumentNullMessageDisabled() {
        ServiceBusReceiverInstrumentation instrumentation = new ServiceBusReceiverInstrumentation(null, null, "fqdn",
            "entityPath", null, ReceiverKind.ASYNC_RECEIVER);

        // does not throw
        instrumentation.startProcessInstrumentation("span name", null, null, Context.NONE);
        instrumentation.instrumentSettlement(Mono.just(1), null, Context.NONE, DispositionStatus.ABANDONED);
    }
}
