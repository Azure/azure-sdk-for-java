// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opencensus;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.ProcessKind;
import io.opencensus.implcore.trace.RecordEventsSpanImpl;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Link;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.samplers.Samplers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.azure.core.tracing.opencensus.OpenCensusTracer.COMPONENT;
import static com.azure.core.tracing.opencensus.OpenCensusTracer.MESSAGE_BUS_DESTINATION;
import static com.azure.core.tracing.opencensus.OpenCensusTracer.PEER_ENDPOINT;
import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    private Span parentSpan;
    private io.opencensus.common.Scope scope;

    @BeforeEach
    public void setUp() {
        openCensusTracer = new OpenCensusTracer();
        // Configure 100% sample rate, otherwise, few traces will be sampled.
        final TraceConfig traceConfig = Tracing.getTraceConfig();
        final TraceParams activeTraceParams = traceConfig.getActiveTraceParams();
        traceConfig.updateActiveTraceParams(activeTraceParams.toBuilder().setSampler(Samplers.alwaysSample()).build());
        // Get the global singleton Tracer object.
        tracer = Tracing.getTracer();
        // Start user parent span.
        scope = tracer.spanBuilder(PARENT_SPAN_KEY).startScopedSpan();
        parentSpan = tracer.getCurrentSpan();
        // Add parent span to tracingContext
        tracingContext = new Context(PARENT_SPAN_KEY, parentSpan);
    }

    @AfterEach
    public void tearDown() {
        // Clear out tracer and tracingContext objects
        tracer = null;
        tracingContext = null;
        Assertions.assertNull(tracer);
        Assertions.assertNull(tracingContext);
        scope.close();
    }

    @Test
    public void startSpanNullPointerException() {
        // Act
        assertThrows(NullPointerException.class, () -> openCensusTracer.start("", null));
    }

    @Test
    public void startSpanParentContextFlowTest() {
        // Arrange
        final SpanId parentSpanId = parentSpan.getContext().getSpanId();

        // Act
        final Context updatedContext = openCensusTracer.start(METHOD_NAME, tracingContext);

        // Assert
        assertSpanWithExplicitParent(updatedContext, parentSpanId);
        final RecordEventsSpanImpl recordEventsSpan =
            (RecordEventsSpanImpl) updatedContext.getData(PARENT_SPAN_KEY).get();
        Assertions.assertNull(recordEventsSpan.getKind());
    }

    @Test
    public void startSpanTestNoUserParent() {
        // Act
        final Context updatedContext = openCensusTracer.start(METHOD_NAME, Context.NONE);

        // Assert
        Assertions.assertNotNull(updatedContext.getData(PARENT_SPAN_KEY));

        //verify still get a valid span implementation
        Assertions.assertTrue(updatedContext.getData(PARENT_SPAN_KEY).get() instanceof RecordEventsSpanImpl);
        final RecordEventsSpanImpl recordEventsSpan =
            (RecordEventsSpanImpl) updatedContext.getData(PARENT_SPAN_KEY).get();

        Assertions.assertEquals(METHOD_NAME, recordEventsSpan.getName());
        Assertions.assertFalse(recordEventsSpan.toSpanData().getHasRemoteParent());
        Assertions.assertNotNull(recordEventsSpan.toSpanData().getParentSpanId());
    }

    @Test
    public void startSpanProcessKindSend() {
        // Arrange
        final SpanId parentSpanId = parentSpan.getContext().getSpanId();
        // Add additional metadata to spans for SEND
        final Context traceContext = tracingContext.addData(ENTITY_PATH_KEY, ENTITY_PATH_VALUE)
            .addData(HOST_NAME_KEY, HOSTNAME_VALUE);

        // Act
        final Context updatedContext = openCensusTracer.start(METHOD_NAME, traceContext, ProcessKind.SEND);

        // Assert
        // verify span created with explicit parent when for Process Kind SEND
        assertSpanWithExplicitParent(updatedContext, parentSpanId);
        final RecordEventsSpanImpl recordEventsSpan =
            (RecordEventsSpanImpl) updatedContext.getData(PARENT_SPAN_KEY).get();
        Assertions.assertEquals(Span.Kind.CLIENT, recordEventsSpan.getKind());

        // verify span attributes
        final Map<String, AttributeValue> attributeMap = recordEventsSpan.toSpanData().getAttributes()
            .getAttributeMap();
        Assertions.assertEquals(attributeMap.get(COMPONENT), AttributeValue.stringAttributeValue(COMPONENT_VALUE));
        Assertions.assertEquals(attributeMap.get(MESSAGE_BUS_DESTINATION),
            AttributeValue.stringAttributeValue(ENTITY_PATH_VALUE));
        Assertions.assertEquals(attributeMap.get(PEER_ENDPOINT), AttributeValue.stringAttributeValue(HOSTNAME_VALUE));
    }

    @Test
    public void startSpanProcessKindMessage() {
        // Arrange
        final SpanId parentSpanId = parentSpan.getContext().getSpanId();

        // Act
        final Context updatedContext = openCensusTracer.start(METHOD_NAME, tracingContext, ProcessKind.MESSAGE);

        // Assert
        // verify span created with explicit parent when no span context in the sending Context object
        assertSpanWithExplicitParent(updatedContext, parentSpanId);
        // verify no kind set on Span for message
        final RecordEventsSpanImpl recordEventsSpan =
            (RecordEventsSpanImpl) updatedContext.getData(PARENT_SPAN_KEY).get();
        Assertions.assertNull(recordEventsSpan.getKind());
        // verify diagnostic id and span context returned
        Assertions.assertNotNull(updatedContext.getData(SPAN_CONTEXT_KEY).get());
        Assertions.assertNotNull(updatedContext.getData(DIAGNOSTIC_ID_KEY).get());
    }

    @Test
    public void startSpanProcessKindProcess() {
        // Arrange
        final SpanId parentSpanId = parentSpan.getContext().getSpanId();

        // Act
        final Context updatedContext = openCensusTracer.start(METHOD_NAME, tracingContext, ProcessKind.PROCESS);

        // verify no parent span passed
        Assertions.assertFalse(tracingContext.getData(SPAN_CONTEXT_KEY).isPresent(), "When no parent span passed in context information");
        // verify span created with explicit parent
        assertSpanWithExplicitParent(updatedContext, parentSpanId);
        // verify scope returned
        Assertions.assertNotNull(updatedContext.getData("scope").get());
        final RecordEventsSpanImpl recordEventsSpan =
            (RecordEventsSpanImpl) updatedContext.getData(PARENT_SPAN_KEY).get();
        Assertions.assertEquals(Span.Kind.SERVER, recordEventsSpan.getKind());
    }

    @Test
    public void startProcessSpanWithRemoteParent() {
        // Arrange
        final Span testSpan = tracer.spanBuilder("child-span").startSpan();
        final SpanId testSpanId = testSpan.getContext().getSpanId();
        final Context traceContext = tracingContext.addData(SPAN_CONTEXT_KEY, testSpan.getContext());

        // Act
        final Context updatedContext = openCensusTracer.start(METHOD_NAME, traceContext, ProcessKind.PROCESS);

        // Assert
        Assertions.assertNotNull(updatedContext.getData("scope").get());
        // Assert new span created with remote parent context
        assertSpanWithRemoteParent(updatedContext, testSpanId);
    }

    @Test
    public void startSpanOverloadNullPointerException() {
        // Act
        assertThrows(NullPointerException.class, () -> openCensusTracer.start("", Context.NONE, null));
    }

    @Test
    public void addLinkTest() {
        // Arrange
        // Create a child-parent link between multiple spans
        final RecordEventsSpanImpl testSpan =
            (RecordEventsSpanImpl) tracer.spanBuilder("new-test-span").startSpan();
        final Context traceContext = tracingContext.addData(SPAN_CONTEXT_KEY, testSpan.getContext());
        final RecordEventsSpanImpl parentSpanImpl = (RecordEventsSpanImpl) parentSpan;
        final Link expectedLink = Link.fromSpanContext(testSpan.getContext(), Link.Type.PARENT_LINKED_SPAN);
        // Act
        openCensusTracer.addLink(traceContext);

        //Assert
        // verify parent span has the expected Link
        Link createdLink = parentSpanImpl.toSpanData().getLinks().getLinks().get(0);
        Assertions.assertEquals(expectedLink.getTraceId(), createdLink.getTraceId());
        Assertions.assertEquals(expectedLink.getSpanId(), createdLink.getSpanId());
    }

    @Test
    public void endSpanNoSuccessErrorMessageTest() {
        // Arrange
        final RecordEventsSpanImpl recordEventsSpan = (RecordEventsSpanImpl) tracer.getCurrentSpan();
        final String expectedStatus = "UNKNOWN";

        // Act
        openCensusTracer.end(null, null, tracingContext);

        // Assert
        Assertions.assertEquals(expectedStatus, recordEventsSpan.getStatus().getCanonicalCode().toString());
    }

    @Test
    public void endSpanErrorMessageTest() {
        // Arrange
        final RecordEventsSpanImpl recordEventsSpan = (RecordEventsSpanImpl) tracer.getCurrentSpan();
        final String throwableMessage = "custom error message";
        final String expectedStatus = "UNKNOWN";

        // Act
        openCensusTracer.end(null, new Throwable(throwableMessage), tracingContext);

        // Assert
        Assertions.assertEquals(expectedStatus, recordEventsSpan.getStatus().getCanonicalCode().toString());
        Assertions.assertEquals(throwableMessage, recordEventsSpan.getStatus().getDescription());
    }

    @Test
    public void endSpanTestThrowableResponseCode() {
        // Arrange
        final RecordEventsSpanImpl recordEventsSpan = (RecordEventsSpanImpl) tracer.getCurrentSpan();
        final String throwableMessage = "Resource not found";
        final String expectedStatus = "NOT_FOUND";

        // Act
        openCensusTracer.end(404, new Throwable(throwableMessage), tracingContext);

        // Assert
        Assertions.assertEquals(expectedStatus, recordEventsSpan.getStatus().getCanonicalCode().toString());
        Assertions.assertEquals(throwableMessage, recordEventsSpan.getStatus().getDescription());

    }

    private static void assertSpanWithExplicitParent(Context updatedContext, SpanId parentSpanId) {
        Assertions.assertNotNull(updatedContext.getData(PARENT_SPAN_KEY));

        // verify instance created of openCensus-impl (test impl), span implementation
        Assertions.assertTrue(updatedContext.getData(PARENT_SPAN_KEY).get() instanceof RecordEventsSpanImpl);

        final RecordEventsSpanImpl recordEventsSpan =
            (RecordEventsSpanImpl) updatedContext.getData(PARENT_SPAN_KEY).get();
        Assertions.assertEquals(METHOD_NAME, recordEventsSpan.getName());

        // verify span started with explicit parent
        Assertions.assertFalse(recordEventsSpan.toSpanData().getHasRemoteParent());
        Assertions.assertEquals(parentSpanId, recordEventsSpan.toSpanData().getParentSpanId());
    }

    private static void assertSpanWithRemoteParent(Context updatedContext, SpanId parentSpanId) {
        Assertions.assertNotNull(updatedContext.getData(PARENT_SPAN_KEY));

        // verify instance created of openCensus-impl (test impl), span implementation
        Assertions.assertTrue(updatedContext.getData(PARENT_SPAN_KEY).get() instanceof RecordEventsSpanImpl);

        // verify span created with provided name and kind server
        final RecordEventsSpanImpl recordEventsSpan =
            (RecordEventsSpanImpl) updatedContext.getData(PARENT_SPAN_KEY).get();
        Assertions.assertEquals(METHOD_NAME, recordEventsSpan.getName());
        Assertions.assertEquals(Span.Kind.SERVER, recordEventsSpan.getKind());

        // verify span started with remote parent
        Assertions.assertTrue(recordEventsSpan.toSpanData().getHasRemoteParent());
        Assertions.assertEquals(parentSpanId, recordEventsSpan.toSpanData().getParentSpanId());
    }
}
