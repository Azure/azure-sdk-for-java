// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opencensus;

import com.azure.core.implementation.tracing.ProcessKind;
import com.azure.core.tracing.opencensus.implementation.AmqpPropagationFormatUtil;
import com.azure.core.util.Context;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.OPENCENSUS_SPAN_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

// TODO: samvaity - Need to add integration test - https://github.com/Azure/azure-sdk-for-java/issues/5156
public class OpenCensusTracerTest {
    private static final String METHOD_NAME = "Azure.eventhubs.send";
    private OpenCensusTracer openCensusTracer;

    @Before
    public void setup() {
        openCensusTracer = new OpenCensusTracer();
    }

    @Test(expected = NullPointerException.class)
    public void startSpanNullPointerException() {
        // Act
        openCensusTracer.start("", null);
    }

    @Test
    public void startSpanReturnsNewSpanTest() {
        // Act
        final Context updatedContext = openCensusTracer.start(METHOD_NAME, Context.NONE);

        // Assert
        Assert.assertNotNull(updatedContext.getData(OPENCENSUS_SPAN_KEY));
        assertTrue(updatedContext.getData(OPENCENSUS_SPAN_KEY).get() instanceof Span);
    }

    // @Test
    // public void startSpanParentContextFlowTest() {
    //     // Arrange
    //     // tracer1.spanBuilder("parent-span").startScopedSpan();
    //     // Context traceContext = new Context(OPENCENSUS_SPAN_KEY, tracer1.getCurrentSpan());
    //
    //     when(tracer1.spanBuilderWithExplicitParent(any(), any())).thenReturn(null);
    //
    //     // Act
    //     final Context updatedContext = openCensusTracer1.start(METHOD_NAME, Context.NONE);
    //
    //     // Assert
    //     Assert.assertNotNull(updatedContext.getData(OPENCENSUS_SPAN_KEY));
    //     Span returnedSpan = (Span) updatedContext.getData(OPENCENSUS_SPAN_KEY).get();
    //     verify(tracer1, times(1)).spanBuilderWithExplicitParent(any(), any());
    //     Assert.assertNotEquals(returnedSpan, BlankSpan.INSTANCE);
    //     Assert.assertEquals(returnedSpan, tracer.getCurrentSpan());
    //
    //     Capture the argument of the doSomething function
    //     ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    //     ArgumentCaptor<Span> captor2 = ArgumentCaptor.forClass(Span.class);
    //     verify(tracer1).spanBuilderWithExplicitParent(captor.getValue(), captor2.getValue());
    //
    //     // Assert the argument
    //     assertEquals(captor.getValue(), METHOD_NAME);
    //     assertTrue(captor2.getValue() instanceof Span);
    // }

    @Test(expected = NullPointerException.class)
    public void startSpanOverloadNullPointerException() {
        // Act
        openCensusTracer.start("", Context.NONE, null);
    }

    @Test
    public void startSpanSendOperation() {
        // Act
        final Context updatedContext = openCensusTracer.start(METHOD_NAME, Context.NONE, ProcessKind.SEND);

        // Assert
        assertTrue(updatedContext.getData(OPENCENSUS_SPAN_KEY).get() instanceof Span);
    }

    @Test
    public void startSpanReceiveOperation() {
        // Act
        final Context returnedContext = openCensusTracer.start(METHOD_NAME, Context.NONE, ProcessKind.RECEIVE);

        // Assert
        assertTrue(returnedContext.getData(OPENCENSUS_SPAN_KEY).get() instanceof Span);
        assertNotNull(returnedContext.getData(DIAGNOSTIC_ID_KEY));
        assertNotNull(returnedContext.getData(SPAN_CONTEXT));
        String diagnosticId = (String) returnedContext.getData(DIAGNOSTIC_ID_KEY).get();
        SpanContext returnedSpanContext = (SpanContext) returnedContext.getData(SPAN_CONTEXT).get();
        // validate the span context and diagnostic Id are the same
        assertEquals(diagnosticId, AmqpPropagationFormatUtil.getDiagnosticId(returnedSpanContext));
    }
}
