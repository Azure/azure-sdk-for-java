// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.instrumentation;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.Exceptions;

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

public class EventHubsTracerTests {
    @Test
    public void testSpanEndNoError() {
        Tracer inner = mock(Tracer.class);
        when(inner.isEnabled()).thenReturn(true);

        EventHubsTracer tracer = new EventHubsTracer(inner, "fqdn", "entityPath", null);
        tracer.endSpan(null, null, Context.NONE, null);

        verify(inner, times(1)).end(isNull(), isNull(), same(Context.NONE));
    }

    @Test
    public void testSpanEndNoErrorAndScope() {
        Tracer inner = mock(Tracer.class);
        when(inner.isEnabled()).thenReturn(true);

        EventHubsTracer tracer = new EventHubsTracer(inner, "fqdn", "entityPath", null);
        AtomicBoolean closed = new AtomicBoolean();
        tracer.endSpan(null, null, Context.NONE, () -> closed.set(true));

        verify(inner, times(1)).end(isNull(), isNull(), same(Context.NONE));
        assertTrue(closed.get());
    }

    @ParameterizedTest
    @MethodSource("getAmqpException")
    public void testSpanEndException(Exception amqpException, Exception cause, String expectedStatus) {
        Tracer inner = mock(Tracer.class);
        when(inner.isEnabled()).thenReturn(true);

        EventHubsTracer tracer = new EventHubsTracer(inner, "fqdn", "entityPath", null);

        tracer.endSpan(null, amqpException, Context.NONE, null);

        verify(inner, times(1)).end(eq(expectedStatus), eq(cause), same(Context.NONE));
    }

    public static Stream<Arguments> getAmqpException() {
        RuntimeException runtimeException = new RuntimeException("foo");
        AmqpException amqpNoCauseNoCondition = new AmqpException(false, "foo", null, null);
        AmqpException amqpNoCauseCondition = new AmqpException(false, AmqpErrorCondition.NOT_FOUND, "foo", null);
        AmqpException amqpNoCauseConditionMessage = new AmqpException(false, AmqpErrorCondition.TIMEOUT_ERROR, "test", null);
        AmqpException amqpCauseCondition = new AmqpException(false, AmqpErrorCondition.SERVER_BUSY_ERROR, null, runtimeException, null);
        return Stream.of(
            Arguments.of(runtimeException, runtimeException, RuntimeException.class.getName()),
            Arguments.of(Exceptions.propagate(runtimeException), runtimeException, RuntimeException.class.getName()),
            Arguments.of(amqpNoCauseNoCondition, amqpNoCauseNoCondition, AmqpException.class.getName()),
            Arguments.of(amqpNoCauseCondition, amqpNoCauseCondition, amqpNoCauseCondition.getErrorCondition().getErrorCondition()),
            Arguments.of(amqpNoCauseConditionMessage, amqpNoCauseConditionMessage, amqpNoCauseConditionMessage.getErrorCondition().getErrorCondition()),
            Arguments.of(amqpCauseCondition, runtimeException, amqpCauseCondition.getErrorCondition().getErrorCondition()));
    }
}
