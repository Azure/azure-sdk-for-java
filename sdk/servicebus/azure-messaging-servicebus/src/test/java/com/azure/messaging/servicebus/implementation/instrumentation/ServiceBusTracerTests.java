// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation.instrumentation;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServiceBusTracerTests {
    @Test
    public void testSpanEndNoError() {
        Tracer inner = mock(Tracer.class);
        when(inner.isEnabled()).thenReturn(true);

        ServiceBusTracer tracer = new ServiceBusTracer(inner, "fqdn", "entityPath");
        tracer.endSpan(null, Context.NONE, null);

        verify(inner, times(1)).end(isNull(), isNull(), same(Context.NONE));
    }

    @Test
    public void testSpanEndNoErrorAndScope() {
        Tracer inner = mock(Tracer.class);
        when(inner.isEnabled()).thenReturn(true);

        ServiceBusTracer tracer = new ServiceBusTracer(inner, "fqdn", "entityPath");
        AtomicBoolean closed = new AtomicBoolean();
        tracer.endSpan(null, Context.NONE, () -> closed.set(true));

        verify(inner, times(1)).end(isNull(), isNull(), same(Context.NONE));
        assertTrue(closed.get());
    }

    @ParameterizedTest
    @MethodSource("getAmqpException")
    public void testSpanEndException(Exception amqpException, String expectedStatus) {
        Tracer inner = mock(Tracer.class);
        when(inner.isEnabled()).thenReturn(true);

        ServiceBusTracer tracer = new ServiceBusTracer(inner, "fqdn", "entityPath");

        tracer.endSpan(amqpException, Context.NONE, null);

        verify(inner, times(1)).end(eq(expectedStatus), eq(amqpException), same(Context.NONE));
    }

    public static Stream<Arguments> getAmqpException() {
        return Stream.of(Arguments.of(new RuntimeException("foo"), null),
            Arguments.of(new AmqpException(false, "foo", null, null), null),
            Arguments.of(new AmqpException(false, AmqpErrorCondition.NOT_FOUND, "foo", null),
                AmqpErrorCondition.NOT_FOUND.getErrorCondition()),
            Arguments.of(new AmqpException(false, AmqpErrorCondition.TIMEOUT_ERROR, "", null),
                AmqpErrorCondition.TIMEOUT_ERROR.getErrorCondition()),
            Arguments.of(
                new AmqpException(false, AmqpErrorCondition.SERVER_BUSY_ERROR, null, new RuntimeException("foo"), null),
                AmqpErrorCondition.SERVER_BUSY_ERROR.getErrorCondition()));
    }
}
