// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.ProcessKind;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanData;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.Tracestate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static com.azure.core.tracing.opentelemetry.OpenTelemetryTracer.AZ_NAMESPACE_KEY;
import static com.azure.core.tracing.opentelemetry.OpenTelemetryTracer.MESSAGE_BUS_DESTINATION;
import static com.azure.core.tracing.opentelemetry.OpenTelemetryTracer.PEER_ENDPOINT;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static com.azure.core.util.tracing.Tracer.SCOPE_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_BUILDER_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static com.azure.core.util.tracing.Tracer.USER_SPAN_NAME_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests Azure-OpenTelemetry tracing package using openTelemetry-sdk
 */
public class OpenTelemetryTracerTest {
    private static final String METHOD_NAME = "EventHubs.send";
    private static final String HOSTNAME_VALUE = "testEventDataNameSpace.servicebus.windows.net";
    private static final String ENTITY_PATH_VALUE = "test";
    private static final String AZ_NAMESPACE_VALUE = "Microsoft.EventHub";

    private OpenTelemetryTracer openTelemetryTracer;
    private Tracer tracer;
    private Context tracingContext;
    private Span parentSpan;

    @BeforeEach
    public void setUp() {
        System.out.println("Running: setUp");
        openTelemetryTracer = new OpenTelemetryTracer();
        // Get the global singleton Tracer object.
        tracer = OpenTelemetry.getTracerFactory().get("TracerSdkTest");
        // Start user parent span.
        parentSpan = tracer.spanBuilder(PARENT_SPAN_KEY).startSpan();
        tracer.withSpan(parentSpan);
        // Add parent span to tracingContext
        tracingContext = new Context(PARENT_SPAN_KEY, parentSpan);
    }

    @AfterEach
    public void tearDown() {
        System.out.println("Running: tearDown");
        // Clear out tracer and tracingContext objects
        tracer = null;
        tracingContext = null;
        assertNull(tracer);
        assertNull(tracingContext);
    }

    @Test
    public void startSpanNullPointerException() {
        // Act
        assertThrows(NullPointerException.class, () -> openTelemetryTracer.start("", null));
    }

    @Test
    public void startSpanParentContextFlowTest() {
        // Arrange
        final SpanId parentSpanId = parentSpan.getContext().getSpanId();

        // Act
        // TODO (savaity): replace with the AZ_TRACING_NAMESPACE_KEY
        final Context updatedContext = openTelemetryTracer.start(METHOD_NAME,
            tracingContext.addData("az.tracing.namespace", AZ_NAMESPACE_VALUE));

        // Assert
        assertSpanWithExplicitParent(updatedContext, parentSpanId);
        final ReadableSpan recordEventsSpan =
            (ReadableSpan) updatedContext.getData(PARENT_SPAN_KEY).get();
        assertEquals(Span.Kind.INTERNAL, recordEventsSpan.toSpanData().getKind());
        final Map<String, AttributeValue> attributeMap = recordEventsSpan.toSpanData().getAttributes();
        assertEquals(attributeMap.get(AZ_NAMESPACE_KEY), AttributeValue.stringAttributeValue(AZ_NAMESPACE_VALUE));
    }

    @Test
    public void startSpanTestNoUserParent() {
        // Act
        final Context updatedContext = openTelemetryTracer.start(METHOD_NAME, Context.NONE);

        // Assert
        assertNotNull(updatedContext.getData(PARENT_SPAN_KEY));

        //verify still get a valid span implementation
        assertTrue(updatedContext.getData(PARENT_SPAN_KEY).get() instanceof ReadableSpan);
        final ReadableSpan recordEventsSpan =
            (ReadableSpan) updatedContext.getData(PARENT_SPAN_KEY).get();

        assertEquals(METHOD_NAME, recordEventsSpan.getName());
        assertFalse(recordEventsSpan.getSpanContext().isRemote());
        assertNotNull(recordEventsSpan.toSpanData().getParentSpanId());
    }

    @Test
    public void startSpanProcessKindSend() {
        // Arrange
        final SpanId parentSpanId = parentSpan.getContext().getSpanId();
        // Start user parent span.
        final Span.Builder spanBuilder = tracer.spanBuilder(METHOD_NAME);
        // Add additional metadata to spans for SEND
        final Context traceContext = tracingContext.addData(ENTITY_PATH_KEY, ENTITY_PATH_VALUE)
            .addData(HOST_NAME_KEY, HOSTNAME_VALUE).addData(SPAN_BUILDER_KEY, spanBuilder)
            .addData(AZ_TRACING_NAMESPACE_KEY, AZ_NAMESPACE_VALUE);

        // Act
        final Context updatedContext = openTelemetryTracer.start(METHOD_NAME, traceContext, ProcessKind.SEND);

        // Assert
        // verify span created with explicit parent when for Process Kind SEND
        assertSpanWithExplicitParent(updatedContext, parentSpanId);
        final ReadableSpan recordEventsSpan =
            (ReadableSpan) updatedContext.getData(PARENT_SPAN_KEY).get();
        assertEquals(Span.Kind.CLIENT, recordEventsSpan.toSpanData().getKind());

        // verify span attributes
        final Map<String, AttributeValue> attributeMap = recordEventsSpan.toSpanData().getAttributes();
        verifySpanAttributes(attributeMap);
    }

    @Test
    public void startSpanProcessKindMessage() {
        // Arrange
        final SpanId parentSpanId = parentSpan.getContext().getSpanId();

        // Act
        final Context updatedContext = openTelemetryTracer.start(METHOD_NAME, tracingContext, ProcessKind.MESSAGE);

        // Assert
        // verify span created with explicit parent when no span context in the sending Context object
        assertSpanWithExplicitParent(updatedContext, parentSpanId);
        // verify no kind set on Span for message
        final ReadableSpan recordEventsSpan =
            (ReadableSpan) updatedContext.getData(PARENT_SPAN_KEY).get();
        assertEquals(Span.Kind.PRODUCER, recordEventsSpan.toSpanData().getKind());
        // verify diagnostic id and span context returned
        assertNotNull(updatedContext.getData(SPAN_CONTEXT_KEY).get());
        assertNotNull(updatedContext.getData(DIAGNOSTIC_ID_KEY).get());
    }

    @Test
    public void startSpanProcessKindProcess() {
        // Arrange
        final SpanId parentSpanId = parentSpan.getContext().getSpanId();
        // Add additional metadata to spans for SEND
        final Context traceContext = tracingContext.addData(ENTITY_PATH_KEY, ENTITY_PATH_VALUE)
                                         .addData(HOST_NAME_KEY, HOSTNAME_VALUE)
                                         .addData(AZ_TRACING_NAMESPACE_KEY, AZ_NAMESPACE_VALUE);
        // Act
        final Context updatedContext = openTelemetryTracer.start(METHOD_NAME, traceContext, ProcessKind.PROCESS);

        // verify no parent span passed
        assertFalse(tracingContext.getData(SPAN_CONTEXT_KEY).isPresent(),
            "When no parent span passed in context information");
        // verify span created with explicit parent
        assertSpanWithExplicitParent(updatedContext, parentSpanId);
        // verify scope returned
        assertNotNull(updatedContext.getData(SCOPE_KEY).get());
        final ReadableSpan recordEventsSpan =
            (ReadableSpan) updatedContext.getData(PARENT_SPAN_KEY).get();
        assertEquals(Span.Kind.CONSUMER, recordEventsSpan.toSpanData().getKind());
        // verify span attributes
        final Map<String, AttributeValue> attributeMap = recordEventsSpan.toSpanData().getAttributes();
        verifySpanAttributes(attributeMap);
    }

    @Test
    public void getSpanBuilderTest() {
        // Act
        final Context updatedContext = openTelemetryTracer.getSharedSpanBuilder(METHOD_NAME, Context.NONE);

        assertTrue(updatedContext.getData(SPAN_BUILDER_KEY).isPresent());
    }

    @Test
    public void startProcessSpanWithRemoteParent() {
        // Arrange
        final Span testSpan = tracer.spanBuilder("child-span").startSpan();
        final SpanId testSpanId = testSpan.getContext().getSpanId();
        final SpanContext spanContext = SpanContext.createFromRemoteParent(
            testSpan.getContext().getTraceId(),
            testSpan.getContext().getSpanId(),
            testSpan.getContext().getTraceFlags(),
            testSpan.getContext().getTracestate());
        final Context traceContext = tracingContext.addData(SPAN_CONTEXT_KEY, spanContext);

        // Act
        final Context updatedContext = openTelemetryTracer.start(METHOD_NAME, traceContext, ProcessKind.PROCESS);

        // Assert
        assertNotNull(updatedContext.getData(SCOPE_KEY).get());
        // Assert new span created with remote parent context
        assertSpanWithRemoteParent(updatedContext, testSpanId);
    }

    @Test
    public void startSpanOverloadNullPointerException() {

        // Assert
        assertThrows(NullPointerException.class, () ->
            openTelemetryTracer.start("", Context.NONE, null));
    }

    @Test
    public void addLinkTest() {
        // Arrange
        Span.Builder span = tracer.spanBuilder("parent-span");
        Span toLinkSpan = tracer.spanBuilder("new test span").startSpan();

        Context spanContext = new Context(
            SPAN_CONTEXT_KEY, toLinkSpan.getContext());
        SpanData.Link expectedLink = SpanData.Link.create(toLinkSpan.getContext());

        // Act
        openTelemetryTracer.addLink(spanContext.addData(SPAN_BUILDER_KEY, span));
        ReadableSpan span1 = (ReadableSpan) span.startSpan();

        //Assert
        // verify parent span has the expected Link
        Link createdLink = span1.toSpanData().getLinks().get(0);
        Assertions.assertEquals(1, span1.toSpanData().getLinks().size());
        Assertions.assertEquals(expectedLink.getContext().getTraceId(), createdLink.getContext().getTraceId());
        Assertions.assertEquals(expectedLink.getContext().getSpanId(), createdLink.getContext().getSpanId());
    }

    @Test
    public void addLinkNoSpanContextTest() {
        // Arrange
        Span.Builder span = tracer.spanBuilder("parent-span");

        // Act
        openTelemetryTracer.addLink(new Context(SPAN_BUILDER_KEY, span));
        ReadableSpan span1 = (ReadableSpan) span.startSpan();

        //Assert
        // verify no links were added
        assertEquals(span1.toSpanData().getLinks().size(), 0);
    }

    @Test
    public void addLinkNoSpanToLinkTest() {
        // Arrange
        Span.Builder span = tracer.spanBuilder("parent-span");

        // Act
        openTelemetryTracer.addLink(Context.NONE);
        ReadableSpan span1 = (ReadableSpan) span.startSpan();

        //Assert
        // verify no links were added
        assertEquals(span1.toSpanData().getLinks().size(), 0);
    }

    @Test
    public void endSpanNoSuccessErrorMessageTest() {
        // Arrange
        final ReadableSpan recordEventsSpan = (ReadableSpan) tracer.getCurrentSpan();
        final String expectedStatus = "UNKNOWN";

        // Act
        openTelemetryTracer.end(null, null, tracingContext);

        // Assert
        assertEquals(expectedStatus, recordEventsSpan.toSpanData().getStatus().getCanonicalCode().name());
    }

    @Test
    public void endSpanErrorMessageTest() {
        // Arrange
        final ReadableSpan recordEventsSpan = (ReadableSpan) tracer.getCurrentSpan();
        final String throwableMessage = "custom error message";
        final String expectedStatus = "UNKNOWN";

        // Act
        openTelemetryTracer.end(null, new Throwable(throwableMessage), tracingContext);

        // Assert
        assertEquals(expectedStatus, recordEventsSpan.toSpanData().getStatus().getCanonicalCode().name());
        assertEquals(throwableMessage, recordEventsSpan.toSpanData().getStatus().getDescription());
    }

    @Test
    public void endSpanTestThrowableResponseCode() {
        // Arrange
        final ReadableSpan recordEventsSpan = (ReadableSpan) tracer.getCurrentSpan();
        final String throwableMessage = "Resource not found";
        final String expectedStatus = "NOT_FOUND";

        // Act
        openTelemetryTracer.end(404, new Throwable(throwableMessage), tracingContext);

        // Assert
        assertEquals(expectedStatus, recordEventsSpan.toSpanData().getStatus().getCanonicalCode().name());
        assertEquals(throwableMessage, recordEventsSpan.toSpanData().getStatus().getDescription());

    }

    @Test
    public void setAttributeTest() {
        // Arrange
        final String firstKey = "first-key";
        final String firstKeyValue = "first-value";
        Context spanContext = openTelemetryTracer.start(METHOD_NAME, tracingContext);
        final ReadableSpan recordEventsSpan = (ReadableSpan) spanContext.getData(PARENT_SPAN_KEY).get();

        // Act
        openTelemetryTracer.setAttribute(firstKey, firstKeyValue, spanContext);

        // Assert
        final Map<String, AttributeValue> attributeMap = recordEventsSpan.toSpanData().getAttributes();
        assertEquals(attributeMap.get(firstKey), AttributeValue.stringAttributeValue(firstKeyValue));
    }

    @Test
    public void setAttributeNoSpanTest() {
        // Arrange
        final String firstKey = "first-key";
        final String firstKeyValue = "first-value";
        Context spanContext = openTelemetryTracer.start(METHOD_NAME, tracingContext);
        final ReadableSpan recordEventsSpan = (ReadableSpan) spanContext.getData(PARENT_SPAN_KEY).get();

        // Act
        openTelemetryTracer.setAttribute(firstKey, firstKeyValue, Context.NONE);

        // Assert
        final Map<String, AttributeValue> attributeMap = recordEventsSpan.toSpanData().getAttributes();
        assertEquals(attributeMap.size(), 0);
    }

    @Test
    public void setSpanNameTest() {
        // Arrange
        Context initialContext = Context.NONE;
        final String spanName = "child-span";

        // Act
        Context updatedContext = openTelemetryTracer.setSpanName(spanName, initialContext);

        // Assert
        assertEquals(updatedContext.getData(USER_SPAN_NAME_KEY).get(), spanName);
    }

    @Test
    public void extractContextValidDiagnosticId() {
        // Arrange
        String diagnosticId = "00-bc7293302f5dc6de8a2372491092df95-dfd6fee494751d3f-01";
        SpanContext validSpanContext = SpanContext.create(
            TraceId.fromLowerBase16(diagnosticId, 3),
            SpanId.fromLowerBase16(diagnosticId, 36),
            TraceFlags.fromLowerBase16(diagnosticId, 53),
            Tracestate.builder().build());

        // Act
        Context updatedContext = openTelemetryTracer.extractContext(diagnosticId, Context.NONE);

        // Assert
        Optional<Object> spanContextOptional = updatedContext.getData(SPAN_CONTEXT_KEY);
        assertNotNull(spanContextOptional);
        SpanContext spanContext = (SpanContext) spanContextOptional.get();
        assertEquals(spanContext, validSpanContext);
    }


    @Test
    public void extractContextInvalidDiagnosticId() {
        // Arrange
        String diagnosticId = "00000000000000000000000000000000";
        SpanContext invalidSpanContext = SpanContext.create(
            TraceId.getInvalid(),
            SpanId.getInvalid(),
            TraceFlags.getDefault(),
            Tracestate.getDefault()
        );

        // Act
        Context updatedContext = openTelemetryTracer.extractContext(diagnosticId, Context.NONE);

        // Assert
        Optional<Object> spanContextOptional = updatedContext.getData(SPAN_CONTEXT_KEY);
        assertNotNull(spanContextOptional);
        SpanContext spanContext = (SpanContext) spanContextOptional.get();
        assertEquals(spanContext, invalidSpanContext);
    }

    private static void assertSpanWithExplicitParent(Context updatedContext, SpanId parentSpanId) {
        assertNotNull(updatedContext.getData(PARENT_SPAN_KEY).get());

        // verify instance created of opentelemetry-sdk (test impl), span implementation
        assertTrue(updatedContext.getData(PARENT_SPAN_KEY).get() instanceof ReadableSpan);

        final ReadableSpan recordEventsSpan =
            (ReadableSpan) updatedContext.getData(PARENT_SPAN_KEY).get();

        assertEquals(METHOD_NAME, recordEventsSpan.getName());

        // verify span started with explicit parent
        assertFalse(recordEventsSpan.toSpanData().getHasRemoteParent());
        assertEquals(parentSpanId, recordEventsSpan.toSpanData().getParentSpanId());
    }

    private static void assertSpanWithRemoteParent(Context updatedContext, SpanId parentSpanId) {
        assertNotNull(updatedContext.getData(PARENT_SPAN_KEY).get());

        // verify instance created of openTelemetry-sdk (test impl), span implementation
        assertTrue(updatedContext.getData(PARENT_SPAN_KEY).get() instanceof ReadableSpan);

        // verify span created with provided name and kind server
        final ReadableSpan recordEventsSpan =
            (ReadableSpan) updatedContext.getData(PARENT_SPAN_KEY).get();
        assertEquals(METHOD_NAME, recordEventsSpan.getName());
        assertEquals(Span.Kind.CONSUMER, recordEventsSpan.toSpanData().getKind());

        // verify span started with remote parent
        assertTrue(recordEventsSpan.toSpanData().getHasRemoteParent());
        assertEquals(parentSpanId, recordEventsSpan.toSpanData().getParentSpanId());
    }

    private static void verifySpanAttributes(Map<String, AttributeValue> attributeMap) {
        assertEquals(attributeMap.get(AZ_NAMESPACE_KEY),
            AttributeValue.stringAttributeValue(AZ_NAMESPACE_VALUE));
        assertEquals(attributeMap.get(MESSAGE_BUS_DESTINATION),
            AttributeValue.stringAttributeValue(ENTITY_PATH_VALUE));
        assertEquals(attributeMap.get(PEER_ENDPOINT), AttributeValue.stringAttributeValue(HOSTNAME_VALUE));
    }
}
