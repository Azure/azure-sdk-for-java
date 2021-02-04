// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.ProcessKind;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.azure.core.tracing.opentelemetry.OpenTelemetryTracer.AZ_NAMESPACE_KEY;
import static com.azure.core.tracing.opentelemetry.OpenTelemetryTracer.MESSAGE_ENQUEUED_TIME;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static com.azure.core.util.tracing.Tracer.SCOPE_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_BUILDER_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static com.azure.core.util.tracing.Tracer.USER_SPAN_NAME_KEY;
import static io.opentelemetry.api.trace.StatusCode.UNSET;
import static io.opentelemetry.semconv.trace.attributes.SemanticAttributes.MESSAGING_DESTINATION;
import static io.opentelemetry.semconv.trace.attributes.SemanticAttributes.PEER_SERVICE;
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
    private static final String AZ_NAMESPACE_VALUE = "Microsoft.Eventhub";
    private static final Long MESSAGE_ENQUEUED_VALUE = Instant.ofEpochSecond(561639205).getEpochSecond();

    private OpenTelemetryTracer openTelemetryTracer;
    private Tracer tracer;
    private Context tracingContext;
    private Span parentSpan;
    private Scope scope;
    private HashMap<String, Object> expectedAttributeMap = new HashMap<String, Object>() {
        {
            put(MESSAGING_DESTINATION.getKey(), ENTITY_PATH_VALUE);
            put(PEER_SERVICE.getKey(), HOSTNAME_VALUE);
            put(AZ_NAMESPACE_KEY, AZ_NAMESPACE_VALUE);
        }
    };

    @BeforeEach
    public void setUp() {
        openTelemetryTracer = new OpenTelemetryTracer();
        // Get the global singleton Tracer object.
        tracer = OpenTelemetrySdk.builder().build().getTracer("TracerSdkTest");
        // Start user parent span.
        parentSpan = tracer.spanBuilder(PARENT_SPAN_KEY).startSpan();
        scope = parentSpan.makeCurrent();
        // Add parent span to tracingContext
        tracingContext = new Context(PARENT_SPAN_KEY, parentSpan);
    }

    @AfterEach
    public void tearDown() {
        // Clear out tracer and tracingContext objects
        scope.close();
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
        final String parentSpanId = parentSpan.getSpanContext().getSpanIdAsHexString();

        // Act
        final Context updatedContext = openTelemetryTracer.start(METHOD_NAME,
            tracingContext.addData(AZ_TRACING_NAMESPACE_KEY, AZ_NAMESPACE_VALUE));

        // Assert
        assertSpanWithExplicitParent(updatedContext, parentSpanId);
        final ReadableSpan recordEventsSpan =
            (ReadableSpan) updatedContext.getData(PARENT_SPAN_KEY).get();
        assertEquals(Span.Kind.INTERNAL, recordEventsSpan.toSpanData().getKind());
        final Attributes attributeMap = recordEventsSpan.toSpanData().getAttributes();
        assertEquals(attributeMap.get(AttributeKey.stringKey(AZ_NAMESPACE_KEY)), AZ_NAMESPACE_VALUE);
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
        final String parentSpanId = parentSpan.getSpanContext().getSpanIdAsHexString();

        // Start user parent span.
        final SpanBuilder spanBuilder = tracer.spanBuilder(METHOD_NAME);
        // Add additional metadata to spans for SEND
        final Context traceContext = tracingContext
            .addData(ENTITY_PATH_KEY, ENTITY_PATH_VALUE)
            .addData(HOST_NAME_KEY, HOSTNAME_VALUE)
            .addData(SPAN_BUILDER_KEY, spanBuilder)
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
        final Attributes attributeMap = recordEventsSpan.toSpanData().getAttributes();

        verifySpanAttributes(attributeMap, expectedAttributeMap);
    }

    @Test
    public void startSpanProcessKindMessage() {
        // Arrange
        final String parentSpanId = parentSpan.getSpanContext().getSpanIdAsHexString();

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

        final Attributes attributeMap = recordEventsSpan.toSpanData().getAttributes();
        verifySpanAttributes(attributeMap, expectedAttributeMap);
    }

    @Test
    public void startSpanProcessKindProcess() {
        // Arrange
        final String parentSpanId = parentSpan.getSpanContext().getSpanIdAsHexString();
        // Add additional metadata to spans for PROCESS
        final Context traceContext = tracingContext
            .addData(ENTITY_PATH_KEY, ENTITY_PATH_VALUE)
            .addData(HOST_NAME_KEY, HOSTNAME_VALUE)
            .addData(AZ_TRACING_NAMESPACE_KEY, AZ_NAMESPACE_VALUE)
            .addData(MESSAGE_ENQUEUED_TIME, MESSAGE_ENQUEUED_VALUE); // only in PROCESS

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
        final Attributes attributeMap = recordEventsSpan.toSpanData().getAttributes();

        // additional only in process spans.
        expectedAttributeMap.put(MESSAGE_ENQUEUED_TIME, MESSAGE_ENQUEUED_VALUE);
        expectedAttributeMap.put(AZ_NAMESPACE_KEY, AZ_NAMESPACE_VALUE);

        verifySpanAttributes(attributeMap, expectedAttributeMap);
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
        final String testSpanId = testSpan.getSpanContext().getSpanIdAsHexString();
        final SpanContext spanContext = SpanContext.createFromRemoteParent(
            testSpan.getSpanContext().getTraceIdAsHexString(),
            testSpan.getSpanContext().getSpanIdAsHexString(),
            testSpan.getSpanContext().getTraceFlags(),
            testSpan.getSpanContext().getTraceState());
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
        SpanBuilder span = tracer.spanBuilder("parent-span");
        Span toLinkSpan = tracer.spanBuilder("new test span").startSpan();

        Context spanContext = new Context(
            SPAN_CONTEXT_KEY, toLinkSpan.getSpanContext());
        LinkData expectedLink = LinkData.create(toLinkSpan.getSpanContext());

        // Act
        openTelemetryTracer.addLink(spanContext.addData(SPAN_BUILDER_KEY, span));
        ReadableSpan span1 = (ReadableSpan) span.startSpan();

        //Assert
        // verify parent span has the expected Link
        LinkData createdLink = span1.toSpanData().getLinks().get(0);
        assertEquals(1, span1.toSpanData().getLinks().size());
        assertEquals(expectedLink.getSpanContext().getTraceIdAsHexString(),
            createdLink.getSpanContext().getTraceIdAsHexString());
        assertEquals(expectedLink.getSpanContext().getSpanIdAsHexString(),
            createdLink.getSpanContext().getSpanIdAsHexString());
    }

    @Test
    public void addLinkNoSpanContextTest() {
        // Arrange
        SpanBuilder span = tracer.spanBuilder("parent-span");

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
        SpanBuilder span = tracer.spanBuilder("parent-span");

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
        final ReadableSpan recordEventsSpan = (ReadableSpan) Span.current();

        // Act
        openTelemetryTracer.end(null, null, tracingContext);

        // Assert
        assertEquals(UNSET, recordEventsSpan.toSpanData().getStatus().getStatusCode());
    }

    @Test
    public void endSpanErrorMessageTest() {
        // Arrange
        final ReadableSpan recordEventsSpan = (ReadableSpan) Span.current();
        final String throwableMessage = "custom error message";

        // Act
        openTelemetryTracer.end(null, new Throwable(throwableMessage), tracingContext);

        // Assert
        assertEquals(StatusCode.ERROR, recordEventsSpan.toSpanData().getStatus().getStatusCode());
        List<EventData> events = recordEventsSpan.toSpanData().getEvents();
        assertEquals(1, events.size());
        EventData event = events.get(0);
        assertEquals("exception", event.getName());
        assertEquals("custom error message", event.getAttributes().get(SemanticAttributes.EXCEPTION_MESSAGE));
    }

    @Test
    public void endSpanTestThrowableResponseCode() {
        // Arrange
        final ReadableSpan recordEventsSpan = (ReadableSpan) Span.current();

        // Act
        openTelemetryTracer.end(404, new Throwable("this is an exception"), tracingContext);

        // Assert
        assertEquals(StatusCode.ERROR, recordEventsSpan.toSpanData().getStatus().getStatusCode());
        assertEquals("Not Found", recordEventsSpan.toSpanData().getStatus().getDescription());

        List<EventData> events = recordEventsSpan.toSpanData().getEvents();
        assertEquals(1, events.size());
        EventData event = events.get(0);
        assertEquals("exception", event.getName());
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
        final Attributes attributeMap = recordEventsSpan.toSpanData().getAttributes();
        assertEquals(attributeMap.get(AttributeKey.stringKey(firstKey)), firstKeyValue);
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
        final Attributes attributeMap = recordEventsSpan.toSpanData().getAttributes();
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
            TraceId.bytesToHex(TraceId.bytesFromHex(diagnosticId, 3)),
            SpanId.bytesToHex(SpanId.bytesFromHex(diagnosticId, 36)),
            TraceFlags.byteFromHex(diagnosticId, 53),
            TraceState.builder().build());

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
            TraceState.getDefault()
        );

        // Act
        Context updatedContext = openTelemetryTracer.extractContext(diagnosticId, Context.NONE);

        // Assert
        Optional<Object> spanContextOptional = updatedContext.getData(SPAN_CONTEXT_KEY);
        assertNotNull(spanContextOptional);
        SpanContext spanContext = (SpanContext) spanContextOptional.get();
        assertEquals(spanContext, invalidSpanContext);
    }

    private static void assertSpanWithExplicitParent(Context updatedContext, String parentSpanId) {
        assertNotNull(updatedContext.getData(PARENT_SPAN_KEY).get());

        // verify instance created of opentelemetry-sdk (test impl), span implementation
        // TODO: (no longer instance of ReadableSpan?
        assertTrue(updatedContext.getData(PARENT_SPAN_KEY).get() instanceof ReadableSpan);

        final ReadableSpan recordEventsSpan =
            (ReadableSpan) updatedContext.getData(PARENT_SPAN_KEY).get();

        assertEquals(METHOD_NAME, recordEventsSpan.getName());

        // verify span started with explicit parent
        assertFalse(recordEventsSpan.toSpanData().getParentSpanContext().isRemote());
        assertEquals(parentSpanId, recordEventsSpan.toSpanData().getParentSpanId());
    }

    private static void assertSpanWithRemoteParent(Context updatedContext, String parentSpanId) {
        assertNotNull(updatedContext.getData(PARENT_SPAN_KEY).get());

        // verify instance created of openTelemetry-sdk (test impl), span implementation
        assertTrue(updatedContext.getData(PARENT_SPAN_KEY).get() instanceof ReadableSpan);

        // verify span created with provided name and kind server
        final ReadableSpan recordEventsSpan =
            (ReadableSpan) updatedContext.getData(PARENT_SPAN_KEY).get();
        assertEquals(METHOD_NAME, recordEventsSpan.getName());
        assertEquals(Span.Kind.CONSUMER, recordEventsSpan.toSpanData().getKind());

        // verify span started with remote parent
        assertTrue(recordEventsSpan.toSpanData().getParentSpanContext().isRemote());
        assertEquals(parentSpanId, recordEventsSpan.toSpanData().getParentSpanId());
    }

    private static void verifySpanAttributes(Attributes actualAttributeMap,
        Map<String, Object> expectedMap) {
        actualAttributeMap.forEach((attributeKey, attributeValue) ->
            assertEquals(expectedMap.get(attributeKey.getKey()), attributeValue));
    }
}
