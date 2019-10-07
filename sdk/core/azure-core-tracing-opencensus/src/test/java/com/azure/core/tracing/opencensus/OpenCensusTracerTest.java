// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opencensus;

import static com.azure.core.tracing.opencensus.OpenCensusTracer.DIAGNOSTIC_ID_KEY;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.core.util.Context;
import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import io.opencensus.trace.Span;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH;
import static com.azure.core.util.tracing.Tracer.HOST_NAME;
import static com.azure.core.util.tracing.Tracer.OPENCENSUS_SPAN_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT;

import java.util.Map;

/**
 * Tests OpenCensus tracing package using opencensus-impl
 */
public class OpenCensusTracerTest {
    private static final String METHOD_NAME = "Azure.eventhubs.send";
    private static final String HOSTNAME_VALUE = "testEventDataNameSpace.servicebus.windows.net";
    private static final String ENTITY_PATH_VALUE = "test";
    private static final String COMPONENT_VALUE = "eventhubs";
    private OpenCensusTracer openCensusTracer;
    private Tracer tracer;
    private Context tracingContext;
    Span parentSpan;

    @Before
    public void setUp() {
        System.out.println("Running: setUp");
        openCensusTracer = new OpenCensusTracer();
        // Configure 100% sample rate, otherwise, few traces will be sampled.
        TraceConfig traceConfig = Tracing.getTraceConfig();
        TraceParams activeTraceParams = traceConfig.getActiveTraceParams();
        traceConfig.updateActiveTraceParams(activeTraceParams.toBuilder().setSampler(Samplers.alwaysSample()).build());
        // Get the global singleton Tracer object.
        tracer = Tracing.getTracer();
        // Start user parent span.
        tracer.spanBuilder(OPENCENSUS_SPAN_KEY).startScopedSpan();
        parentSpan = tracer.getCurrentSpan();
        // Add parent span to tracingContext
        tracingContext =  new Context(OPENCENSUS_SPAN_KEY, parentSpan);
    }

    @After
    public void tearDown() {
        System.out.println("Running: tearDown");
        // Clear out tracer and tracingContext objects
        tracer = null;
        tracingContext = null;
        Assert.assertNull(tracer);
        Assert.assertNull(tracingContext);
    }

    @Test(expected = NullPointerException.class)
    public void startSpanNullPointerException() {
        // Act
        openCensusTracer.start("", null);
    }

    @Test
    public void startSpanParentContextFlowTest() {
        // Arrange
        SpanId parentSpanId = parentSpan.getContext().getSpanId();

        // Act
        final Context updatedContext = openCensusTracer.start(METHOD_NAME, tracingContext);

        // Assert
        assertSpanWithExplicitParent(updatedContext, parentSpanId);
        RecordEventsSpanImpl recordEventsSpan = (RecordEventsSpanImpl) updatedContext.getData(OPENCENSUS_SPAN_KEY).get();
        Assert.assertNull(recordEventsSpan.getKind());
    }

    @Test
    public void startSpanTestNoUserParent() {
        // Act
        final Context updatedContext = openCensusTracer.start(METHOD_NAME, Context.NONE);

        // Assert
        Assert.assertNotNull(updatedContext.getData(PARENT_SPAN_KEY));
        assertTrue(updatedContext.getData(PARENT_SPAN_KEY).get() instanceof Span);
    }

    @Test
    public void startSpanProcessKindSend() {
        // Arrange
        tracer.spanBuilder(OPENCENSUS_SPAN_KEY).startScopedSpan();
        SpanId parentSpanId = parentSpan.getContext().getSpanId();
        // Add additional metadata to spans for SEND
        Context traceContext = tracingContext.addData(ENTITY_PATH, ENTITY_PATH_VALUE).addData(HOST_NAME, HOSTNAME_VALUE);

        // Act
        final Context updatedContext = openCensusTracer.start(METHOD_NAME, traceContext, ProcessKind.SEND);

        // Assert
        // verify span created with explicit parent when for Process Kind SEND
        assertSpanWithExplicitParent(updatedContext, parentSpanId);
        RecordEventsSpanImpl recordEventsSpan = (RecordEventsSpanImpl) updatedContext.getData(OPENCENSUS_SPAN_KEY).get();
        Assert.assertEquals(Span.Kind.CLIENT, recordEventsSpan.getKind());

        // verify span attributes
        Map<String, AttributeValue> attributeMap = recordEventsSpan.toSpanData().getAttributes().getAttributeMap();
        Assert.assertEquals(attributeMap.get(COMPONENT), AttributeValue.stringAttributeValue(COMPONENT_VALUE));
        Assert.assertEquals(attributeMap.get(MESSAGE_BUS_DESTINATION), AttributeValue.stringAttributeValue(ENTITY_PATH_VALUE));
        Assert.assertEquals(attributeMap.get(PEER_ENDPOINT), AttributeValue.stringAttributeValue(HOSTNAME_VALUE));
    }

    @Test
    public void startSpanProcessKindReceive() {
        // Arrange
        tracer.spanBuilder(OPENCENSUS_SPAN_KEY).startScopedSpan();
        SpanId parentSpanId = parentSpan.getContext().getSpanId();

        // Act
        final Context updatedContext = openCensusTracer.start(METHOD_NAME, tracingContext, ProcessKind.RECEIVE);

        // Assert
        // verify span created with explicit parent when no span context in the sending Context object
        assertSpanWithExplicitParent(updatedContext, parentSpanId);
        // verify no kind set on Span for receive
        RecordEventsSpanImpl recordEventsSpan = (RecordEventsSpanImpl) updatedContext.getData(OPENCENSUS_SPAN_KEY).get();
        Assert.assertNull(recordEventsSpan.getKind());
        // verify diagnostic id and span context returned
        Assert.assertNotNull(updatedContext.getData(SPAN_CONTEXT).get());
        Assert.assertNotNull(updatedContext.getData(DIAGNOSTIC_ID_KEY).get());
    }

    @Test
    public void startSpanProcessKindProcess() {
        // Arrange
        tracer.spanBuilder(OPENCENSUS_SPAN_KEY).startScopedSpan();
        SpanId parentSpanId = parentSpan.getContext().getSpanId();

        // Act
        final Context updatedContext = openCensusTracer.start(METHOD_NAME, tracingContext, ProcessKind.PROCESS);

        // verify span created with explicit parent
        assertSpanWithExplicitParent(updatedContext, parentSpanId);
        // verify scope returned
        Assert.assertNotNull(updatedContext.getData("scope").get());
        RecordEventsSpanImpl recordEventsSpan = (RecordEventsSpanImpl) updatedContext.getData(OPENCENSUS_SPAN_KEY).get();
        Assert.assertEquals(Span.Kind.SERVER, recordEventsSpan.getKind());
    }

    @Test
    public void startProcessSpanWithRemoteParent() {
        // Arrange
        Span testSpan = tracer.spanBuilder("child-span").startSpan();
        SpanId testSpanId = testSpan.getContext().getSpanId();
        Context traceContext = tracingContext.addData(SPAN_CONTEXT, testSpan.getContext());

        // Act
        final Context updatedContext = openCensusTracer.start(METHOD_NAME, traceContext, ProcessKind.PROCESS);

        // Assert
        Assert.assertNotNull(updatedContext.getData("scope").get());
        // Assert new span created with remote parent context
        assertSpanWithRemoteParent(updatedContext, testSpanId);
    }

    @Test(expected = NullPointerException.class)
    public void startSpanOverloadNullPointerException() {
        // Act
        openCensusTracer.start("", Context.NONE, null);
    }

    //add tests for number of child spans created and how parent span relation

    @Test
    public void addLinkTest() {
        // Arrange
        // Create a child-parent link between multiple spans
        RecordEventsSpanImpl existingSpan = (RecordEventsSpanImpl) tracer.spanBuilder("existing-span").startSpan();
        Context traceContext = tracingContext.addData(SPAN_CONTEXT, existingSpan.getContext());
        RecordEventsSpanImpl parentSpanImpl = (RecordEventsSpanImpl) parentSpan;

        // Act
        openCensusTracer.addLink(traceContext);

        //Assert
        // TODO: existing -> parentSpanImpl comes after after add Link, confirm this behavior?
        Assert.assertEquals(parentSpanImpl.getPrev(), existingSpan);
        Assert.assertEquals(parentSpanImpl.toSpanData().getChildSpanCount().intValue(), 1);
        Assert.assertEquals(parentSpanImpl.toSpanData().getLinks().getLinks().size(), 1);
    }

    @Test
    public void endSpanNoSuccessErrorMessageTest() {
        // Arrange
        RecordEventsSpanImpl recordEventsSpan = (RecordEventsSpanImpl) tracer.getCurrentSpan();
        final String expectedStatus = "UNKNOWN";

        // Act
        openCensusTracer.end(null, null, tracingContext);

        // Assert
        assertTrue(updatedContext.getData(PARENT_SPAN_KEY).get() instanceof Span);
    }

    @Test
    public void endSpanErrorMessageTest() {
        // Arrange
        RecordEventsSpanImpl recordEventsSpan = (RecordEventsSpanImpl) tracer.getCurrentSpan();
        final String throwableMessage = "custom error message";
        final String expectedStatus = "UNKNOWN";

        // Act
        openCensusTracer.end(null, new Throwable(throwableMessage), tracingContext);

        // Assert
        assertTrue(returnedContext.getData(PARENT_SPAN_KEY).get() instanceof Span);
        assertNotNull(returnedContext.getData(DIAGNOSTIC_ID_KEY));
        assertNotNull(returnedContext.getData(SPAN_CONTEXT_KEY));
        String diagnosticId = (String) returnedContext.getData(DIAGNOSTIC_ID_KEY).get();
        Span returnedSpan = (Span) returnedContext.getData(PARENT_SPAN_KEY).get();
        // validate the span context and diagnostic Id are the same
        assertEquals(diagnosticId, AmqpPropagationFormatUtil.getDiagnosticId(returnedSpan.getContext()));
    }

    private void assertSpanWithExplicitParent(Context updatedContext, SpanId parentSpanId) {
        Assert.assertNotNull(updatedContext.getData(OPENCENSUS_SPAN_KEY));

        // verify instance created of openCensus-impl (test impl), span implementation
        Assert.assertTrue(updatedContext.getData(OPENCENSUS_SPAN_KEY).get() instanceof RecordEventsSpanImpl);

        RecordEventsSpanImpl recordEventsSpan = (RecordEventsSpanImpl) updatedContext.getData(OPENCENSUS_SPAN_KEY).get();
        Assert.assertEquals(METHOD_NAME, recordEventsSpan.getName());

        // verify span started with explicit parent
        Assert.assertFalse(recordEventsSpan.toSpanData().getHasRemoteParent());
        Assert.assertEquals(parentSpanId, recordEventsSpan.toSpanData().getParentSpanId());
    }

    private void assertSpanWithRemoteParent(Context updatedContext, SpanId parentSpanId) {
        Assert.assertNotNull(updatedContext.getData(OPENCENSUS_SPAN_KEY));

        // verify instance created of openCensus-impl (test impl), span implementation
        Assert.assertTrue(updatedContext.getData(OPENCENSUS_SPAN_KEY).get() instanceof RecordEventsSpanImpl);

        // verify span created with provided name and kind server
        RecordEventsSpanImpl recordEventsSpan = (RecordEventsSpanImpl) updatedContext.getData(OPENCENSUS_SPAN_KEY).get();
        Assert.assertEquals(METHOD_NAME, recordEventsSpan.getName());
        Assert.assertEquals(Span.Kind.SERVER, recordEventsSpan.getKind());

        // verify span started with remote parent
        Assert.assertTrue(recordEventsSpan.toSpanData().getHasRemoteParent());
        Assert.assertEquals(parentSpanId, recordEventsSpan.toSpanData().getParentSpanId());
    }
}
