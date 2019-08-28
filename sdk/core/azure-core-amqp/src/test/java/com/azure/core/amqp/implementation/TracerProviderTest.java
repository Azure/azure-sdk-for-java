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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.azure.core.implementation.tracing.Tracer.OPENTELEMETRY_SPAN_KEY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

public class TracerProviderTest {

    @Test
    public void startSpan() {
        // Arrange
        final Tracer tracer1 = mock(Tracer.class);
        List<Tracer> tracers = Arrays.asList(tracer1);
        final TracerProvider tracerProvider = new TracerProvider(tracers);

        // Act
        Context updatedContext = tracerProvider.startSpan(Context.NONE, ProcessKind.SEND);

        // Assert
        verify(tracer1, times(1)).start(eq("Azure.eventhubs.send"), any(), eq(ProcessKind.SEND));
    }

    @Test
    public void isEnabled() {
        // Arrange
        List<Tracer> tracers = Collections.emptyList();
        final TracerProvider tracerProvider = new TracerProvider(tracers);

        // Act & Assert
        Assert.assertEquals(false, tracerProvider.isEnabled());
    }

    @Test
    public void startSpanReturnsUpdatedContext() {
        // Arrange
        final Tracer tracer1 = mock(Tracer.class);
        List<Tracer> tracers = Arrays.asList(tracer1);
        final TracerProvider tracerProvider = new TracerProvider(tracers);
        when(tracer1.start("Azure.eventhubs.send", Context.NONE, ProcessKind.SEND)).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(OPENTELEMETRY_SPAN_KEY, "value");
            }
        );

        // Act
        Context updatedContext = tracerProvider.startSpan(Context.NONE, ProcessKind.SEND);

        // Assert
        Assert.assertEquals(Context.class, updatedContext.getClass());
        Assert.assertEquals(updatedContext.getData(OPENTELEMETRY_SPAN_KEY).get(), "value");
    }

    @Test
    public void endSpanSuccess() {
        // Arrange
        final Tracer tracer1 = mock(Tracer.class);
        List<Tracer> tracers = Arrays.asList(tracer1);
        final TracerProvider tracerProvider = new TracerProvider(tracers);

        // Act
        tracerProvider.endSpan(new Context(OPENTELEMETRY_SPAN_KEY, "value"), Signal.complete());

        // Assert
        verify(tracer1, times(1)).end(eq("success"), isNull(), any(Context.class));
    }

    @Test
    public void endSpanNoKey() {
        // Arrange
        final Tracer tracer1 = mock(Tracer.class);
        List<Tracer> tracers = Arrays.asList(tracer1);
        final TracerProvider tracerProvider = new TracerProvider(tracers);

        // Act
        tracerProvider.endSpan(Context.NONE, Signal.complete());

        // Assert
        verify(tracer1, never()).end("", null, Context.NONE);
    }

    @Test
    public void endSpanError() {
        // Arrange
        final Tracer tracer1 = mock(Tracer.class);
        List<Tracer> tracers = Arrays.asList(tracer1);
        final TracerProvider tracerProvider = new TracerProvider(tracers);
        Throwable testThrow = new Throwable("testError");
        Context sendContext = new Context(OPENTELEMETRY_SPAN_KEY, "value");

        // Act
        tracerProvider.endSpan(sendContext, Signal.error(testThrow));

        // Assert
        verify(tracer1, times(1)).end("", testThrow, sendContext);
    }

    @Test
    public void endSpanAmqpException() {
        // Arrange
        final Tracer tracer1 = mock(Tracer.class);
        List<Tracer> tracers = Arrays.asList(tracer1);
        final TracerProvider tracerProvider = new TracerProvider(tracers);
        final Exception exception = new AmqpException(true, ErrorCondition.NOT_FOUND, "", null);
        Context sendContext = new Context(OPENTELEMETRY_SPAN_KEY, "value");

        // Act
        tracerProvider.endSpan(sendContext, Signal.error(exception));

        // Assert
        verify(tracer1, times(1)).end(ErrorCondition.NOT_FOUND.getErrorCondition(), exception, sendContext);
    }
}
