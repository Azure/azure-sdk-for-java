// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.implementation.tracing.ProcessKind;
import com.azure.core.implementation.tracing.Tracer;
import com.azure.core.util.Context;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Signal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.azure.core.implementation.tracing.Tracer.OPENTELEMETRY_SPAN_KEY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

public class TracerProviderTest {

    @Test
    public void startSpan() {
        // Act
        final Tracer tracer1 = mock(Tracer.class);
        List<Tracer> tracers = new ArrayList<>(Arrays.asList(tracer1));
        final TracerProvider tracerProvider = new TracerProvider(tracers);
        Context updatedContext = tracerProvider.startSpan(Context.NONE, ProcessKind.SEND);

        // Assert
        verify(tracer1, times(1)).start(eq("Azure.eventhubs.send"), any(), eq(ProcessKind.SEND));
    }

    @Test
    public void startSpanReturnsUpdatedContext() {
        // Act
        final Tracer tracer1 = mock(Tracer.class);
        List<Tracer> tracers = new ArrayList<>(Arrays.asList(tracer1));
        final TracerProvider tracerProvider = new TracerProvider(tracers);
        when(tracer1.start("Azure.eventhubs.send", Context.NONE, ProcessKind.SEND)).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(OPENTELEMETRY_SPAN_KEY, "value");
            }
        );
        Context updatedContext = tracerProvider.startSpan(Context.NONE, ProcessKind.SEND);

        // Assert
        Assert.assertEquals(Context.class, updatedContext.getClass());
        Assert.assertEquals(updatedContext.getData(OPENTELEMETRY_SPAN_KEY).get(), "value");
    }

    @Test
    public void endSpanSuccess() {
        // Act
        final Tracer tracer1 = mock(Tracer.class);
        List<Tracer> tracers = new ArrayList<>(Arrays.asList(tracer1));
        final TracerProvider tracerProvider = new TracerProvider(tracers);
        tracerProvider.endSpan(new Context(OPENTELEMETRY_SPAN_KEY, "value"), Signal.complete());

        // Assert
        verify(tracer1, times(1)).end(eq("success"), isNull(), any(Context.class));
    }

    @Test
    public void endSpanNoKey() {
        // Act
        final Tracer tracer1 = mock(Tracer.class);
        List<Tracer> tracers = new ArrayList<>(Arrays.asList(tracer1));
        final TracerProvider tracerProvider = new TracerProvider(tracers);
        tracerProvider.endSpan(Context.NONE, Signal.complete());

        // Assert
        verify(tracer1, never()).end("", null, Context.NONE);
    }

    @Test
    public void endSpanNoContext() {
        // Act
        final Tracer tracer1 = mock(Tracer.class);
        List<Tracer> tracers = new ArrayList<>(Arrays.asList(tracer1));
        final TracerProvider tracerProvider = new TracerProvider(tracers);
        tracerProvider.endSpan(null, Signal.complete());

        // Assert
        verify(tracer1, never()).end("", null, Context.NONE);
    }

    @Test
    public void endSpanError() {
        // Act
        final Tracer tracer1 = mock(Tracer.class);
        List<Tracer> tracers = new ArrayList<>(Arrays.asList(tracer1));
        final TracerProvider tracerProvider = new TracerProvider(tracers);
        Throwable testThrow = new Throwable("testError");
        Context sendContext = new Context(OPENTELEMETRY_SPAN_KEY, "value");
        tracerProvider.endSpan(sendContext, Signal.error(testThrow));

        // Assert
        verify(tracer1, times(1)).end("", testThrow, sendContext);
    }

    @Test
    public void endSpanAmqpException() {
        // Act
        final Tracer tracer1 = mock(Tracer.class);
        List<Tracer> tracers = new ArrayList<>(Arrays.asList(tracer1));
        final TracerProvider tracerProvider = new TracerProvider(tracers);
        final Exception exception = new AmqpException(true, ErrorCondition.NOT_FOUND, "", null);

        Context sendContext = new Context(OPENTELEMETRY_SPAN_KEY, "value");
        tracerProvider.endSpan(sendContext, Signal.error(exception));

        // Assert
        verify(tracer1, times(1)).end("amqp:not-found", exception, sendContext);
    }
}
