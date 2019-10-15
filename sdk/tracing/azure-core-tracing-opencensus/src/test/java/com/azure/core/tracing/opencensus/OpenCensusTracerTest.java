// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opencensus;

import static com.azure.core.tracing.opencensus.OpenCensusTracer.DIAGNOSTIC_ID_KEY;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.core.tracing.opencensus.implementation.AmqpPropagationFormatUtil;
import com.azure.core.util.Context;
import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import io.opencensus.trace.Span;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        Assert.assertNotNull(updatedContext.getData(PARENT_SPAN_KEY));
        assertTrue(updatedContext.getData(PARENT_SPAN_KEY).get() instanceof Span);
    }

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
        assertTrue(updatedContext.getData(PARENT_SPAN_KEY).get() instanceof Span);
    }

    @Test
    public void startSpanReceiveOperation() {
        // Act
        final Context returnedContext = openCensusTracer.start(METHOD_NAME, Context.NONE, ProcessKind.RECEIVE);

        // Assert
        assertTrue(returnedContext.getData(PARENT_SPAN_KEY).get() instanceof Span);
        assertNotNull(returnedContext.getData(DIAGNOSTIC_ID_KEY));
        assertNotNull(returnedContext.getData(SPAN_CONTEXT_KEY));
        String diagnosticId = (String) returnedContext.getData(DIAGNOSTIC_ID_KEY).get();
        Span returnedSpan = (Span) returnedContext.getData(PARENT_SPAN_KEY).get();
        // validate the span context and diagnostic Id are the same
        assertEquals(diagnosticId, AmqpPropagationFormatUtil.getDiagnosticId(returnedSpan.getContext()));
    }
}
