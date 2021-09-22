// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.core.util.tracing.StartSpanOptions;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests Azure-OpenTelemetry tracing package using openTelemetry-sdk
 */
@SuppressWarnings("try")
public class OpenTelemetryTracerTest {

    private static final String METHOD_NAME = "EventHubs.send";
    private static final String HOSTNAME_VALUE = "testEventDataNameSpace.servicebus.windows.net";
    private static final String ENTITY_PATH_VALUE = "test";
    private static final String AZ_NAMESPACE_VALUE = "Microsoft.Eventhub";
    private static final Long MESSAGE_ENQUEUED_VALUE = Instant.ofEpochSecond(561639205).getEpochSecond();
    private static final int TRACEPARENT_DELIMITER_SIZE = 1;
    private static final int TRACE_ID_HEX_SIZE = TraceId.getLength();
    private static final int SPAN_ID_HEX_SIZE = SpanId.getLength();
    private static final int TRACE_ID_OFFSET = 2 + TRACEPARENT_DELIMITER_SIZE;
    private static final int SPAN_ID_OFFSET =
        TRACE_ID_OFFSET + TRACE_ID_HEX_SIZE + TRACEPARENT_DELIMITER_SIZE;
    private static final int TRACE_OPTION_OFFSET =
        SPAN_ID_OFFSET + SPAN_ID_HEX_SIZE + TRACEPARENT_DELIMITER_SIZE;
    private static OpenTelemetryTracer openTelemetryTracer;

    private static Tracer tracer = OpenTelemetrySdk.builder().build().getTracer("TracerSdkTest");
    private Context tracingContext;
    private Span parentSpan;

    private final HashMap<String, Object> expectedAttributeMap = new HashMap<String, Object>() {
        {
            put(OpenTelemetryTracer.MESSAGE_BUS_DESTINATION, ENTITY_PATH_VALUE);
            put(OpenTelemetryTracer.PEER_ENDPOINT, HOSTNAME_VALUE);
            put(AZ_NAMESPACE_KEY, AZ_NAMESPACE_VALUE);
        }
    };

    @BeforeEach
    public void setUp() {
        // Start user parent span.
        parentSpan = tracer.spanBuilder(PARENT_SPAN_KEY).setNoParent().startSpan();

        // Add parent span to tracingContext
        tracingContext = new Context(PARENT_SPAN_KEY, parentSpan);
        openTelemetryTracer = new OpenTelemetryTracer(tracer);
    }

    @AfterEach
    public void tearDown() {
        // Clear out tracer and tracingContext objects
        tracingContext = null;
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
        final String parentSpanId = parentSpan.getSpanContext().getSpanId();

        // Act
        final Context updatedContext = openTelemetryTracer.start(METHOD_NAME,
            tracingContext.addData(AZ_TRACING_NAMESPACE_KEY, AZ_NAMESPACE_VALUE));

        // Assert
        assertSpanWithExplicitParent(updatedContext, parentSpanId);
        final ReadableSpan recordEventsSpan =
            (ReadableSpan) updatedContext.getData(PARENT_SPAN_KEY).get();
        assertEquals(SpanKind.INTERNAL, recordEventsSpan.toSpanData().getKind());
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
    public void startSharedBuilderAndSpanProcessKindSend() {
        // Arrange
        final String parentSpanId = parentSpan.getSpanContext().getSpanId();

        // Add additional metadata to spans for SEND
        final Context traceContext = tracingContext
            .addData(ENTITY_PATH_KEY, ENTITY_PATH_VALUE)
            .addData(HOST_NAME_KEY, HOSTNAME_VALUE)
            .addData(AZ_TRACING_NAMESPACE_KEY, AZ_NAMESPACE_VALUE);

        // Start user parent span.
        final Context withBuilder = openTelemetryTracer.getSharedSpanBuilder(METHOD_NAME, traceContext);

        // Act
        final Context updatedContext = openTelemetryTracer.start(METHOD_NAME, withBuilder, ProcessKind.SEND);

        // Assert
        // verify span created with explicit parent when for Process Kind SEND
        assertSpanWithExplicitParent(updatedContext, parentSpanId);
        final ReadableSpan recordEventsSpan =
            (ReadableSpan) updatedContext.getData(PARENT_SPAN_KEY).get();
        assertEquals(SpanKind.CLIENT, recordEventsSpan.toSpanData().getKind());

        // verify span attributes
        final Attributes attributeMap = recordEventsSpan.toSpanData().getAttributes();

        verifySpanAttributes(expectedAttributeMap, attributeMap);
    }

    @Test
    public void startSpanProcessKindSend() {
        // Arrange
        final String parentSpanId = parentSpan.getSpanContext().getSpanId();

        // Start user parent span.
        final SpanBuilder spanBuilder = tracer.spanBuilder(METHOD_NAME)
            .setParent(io.opentelemetry.context.Context.root().with(parentSpan))
            .setSpanKind(SpanKind.CLIENT);
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
        assertEquals(SpanKind.CLIENT, recordEventsSpan.toSpanData().getKind());

        // verify span attributes
        final Attributes attributeMap = recordEventsSpan.toSpanData().getAttributes();

        verifySpanAttributes(expectedAttributeMap, attributeMap);
    }

    @Test
    public void startSpanProcessKindMessage() {
        // Arrange
        final String parentSpanId = parentSpan.getSpanContext().getSpanId();
        final Context contextWithAttributes = tracingContext
            .addData(ENTITY_PATH_KEY, ENTITY_PATH_VALUE)
            .addData(HOST_NAME_KEY, HOSTNAME_VALUE)
            .addData(AZ_TRACING_NAMESPACE_KEY, AZ_NAMESPACE_VALUE);

        // Act
        final Context updatedContext = openTelemetryTracer.start(METHOD_NAME, contextWithAttributes, ProcessKind.MESSAGE);

        // Assert
        // verify span created with explicit parent when no span context in the sending Context object
        assertSpanWithExplicitParent(updatedContext, parentSpanId);
        // verify no kind set on Span for message
        final ReadableSpan recordEventsSpan =
            (ReadableSpan) updatedContext.getData(PARENT_SPAN_KEY).get();
        assertEquals(SpanKind.PRODUCER, recordEventsSpan.toSpanData().getKind());
        // verify diagnostic id and span context returned
        assertNotNull(updatedContext.getData(SPAN_CONTEXT_KEY).get());
        assertNotNull(updatedContext.getData(DIAGNOSTIC_ID_KEY).get());

        final Attributes attributeMap = recordEventsSpan.toSpanData().getAttributes();
        verifySpanAttributes(expectedAttributeMap, attributeMap);
    }

    @Test
    public void startSpanProcessKindProcess() {
        // Arrange
        final String parentSpanId = parentSpan.getSpanContext().getSpanId();
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
        assertEquals(SpanKind.CONSUMER, recordEventsSpan.toSpanData().getKind());

        // verify span attributes
        final Attributes attributeMap = recordEventsSpan.toSpanData().getAttributes();

        // additional only in process spans.
        expectedAttributeMap.put(MESSAGE_ENQUEUED_TIME, MESSAGE_ENQUEUED_VALUE);
        expectedAttributeMap.put(AZ_NAMESPACE_KEY, AZ_NAMESPACE_VALUE);

        verifySpanAttributes(expectedAttributeMap, attributeMap);
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
        final String testSpanId = testSpan.getSpanContext().getSpanId();
        final SpanContext spanContext = SpanContext.createFromRemoteParent(
            testSpan.getSpanContext().getTraceId(),
            testSpan.getSpanContext().getSpanId(),
            testSpan.getSpanContext().getTraceFlags(),
            testSpan.getSpanContext().getTraceState());
        final Context traceContext = tracingContext.addData(SPAN_CONTEXT_KEY, spanContext);

        // Act
        final Context updatedContext = openTelemetryTracer.start(METHOD_NAME, traceContext, ProcessKind.PROCESS);

        // Assert
        assertNotNull(updatedContext.getData(SCOPE_KEY).get());
        // Assert new span created with remote parent context
        assertSpanWithRemoteParent(updatedContext, testSpanId);
        assertTrue(updatedContext.getData(SCOPE_KEY).isPresent());
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
        SpanBuilder span = tracer.spanBuilder(PARENT_SPAN_KEY);
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
        assertEquals(expectedLink.getSpanContext().getTraceId(),
            createdLink.getSpanContext().getTraceId());
        assertEquals(expectedLink.getSpanContext().getSpanId(),
            createdLink.getSpanContext().getSpanId());
    }

    @Test
    public void addLinkNoSpanContextTest() {
        // Arrange
        SpanBuilder span = tracer.spanBuilder(PARENT_SPAN_KEY);

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
        SpanBuilder span = tracer.spanBuilder(PARENT_SPAN_KEY);

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
        final ReadableSpan recordEventsSpan = (ReadableSpan) parentSpan;

        // Act
        openTelemetryTracer.end(null, null, tracingContext);

        // Assert
        assertEquals(UNSET, recordEventsSpan.toSpanData().getStatus().getStatusCode());
    }

    @Test
    public void endSpanErrorMessageTest() {
        // Arrange
        final ReadableSpan recordEventsSpan = (ReadableSpan) parentSpan;
        final String throwableMessage = "custom error message";

        // Act
        openTelemetryTracer.end(null, new Throwable(throwableMessage), tracingContext);

        // Assert
        assertEquals(StatusCode.ERROR, recordEventsSpan.toSpanData().getStatus().getStatusCode());
        List<EventData> events = recordEventsSpan.toSpanData().getEvents();
        assertEquals(1, events.size());
        EventData event = events.get(0);
        assertEquals("exception", event.getName());
        assertEquals("custom error message",
            event.getAttributes().get(AttributeKey.stringKey("exception.message")));
    }

    @Test
    public void endSpanTestThrowableResponseCode() {
        // Arrange
        final ReadableSpan recordEventsSpan = (ReadableSpan) parentSpan;

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

    class TestScope implements Scope {
        private boolean closed = false;
        @Override
        public void close() {
            closed = true;
        }

        public boolean isClosed() {
            return this.closed;
        }
    }

    @Test
    public void endSpanClosesScope() {
        final TestScope testScope = new TestScope();
        openTelemetryTracer.end("foo", null, tracingContext.addData(SCOPE_KEY, testScope));
        assertTrue(testScope.isClosed());
    }

    @Test
    public void startEndCurrentSpan() {
        try (Scope parentScope = parentSpan.makeCurrent()) {
            final Context started = openTelemetryTracer.start(METHOD_NAME, tracingContext);

            try (AutoCloseable scope = openTelemetryTracer.makeSpanCurrent(started)) {
                assertSame(Span.current(), started.getData(PARENT_SPAN_KEY).get());
            } catch (Exception e) {
                fail();
            } finally {
                openTelemetryTracer.end("foo", null, started);
            }

            assertSame(parentSpan, Span.current());
        }
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
        String traceparent = "00-bc7293302f5dc6de8a2372491092df95-dfd6fee494751d3f-01";
        String traceId =
            traceparent.substring(TRACE_ID_OFFSET, TRACE_ID_OFFSET + TraceId.getLength());
        String spanId = traceparent.substring(SPAN_ID_OFFSET, SPAN_ID_OFFSET + SpanId.getLength());

        TraceFlags traceFlags = TraceFlags.fromHex(traceparent, TRACE_OPTION_OFFSET);

        SpanContext validSpanContext = SpanContext.createFromRemoteParent(
            traceId,
            spanId,
            traceFlags,
            TraceState.builder().build());

        // Act
        Context updatedContext = openTelemetryTracer.extractContext(traceparent, Context.NONE);

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
        SpanContext invalidSpanContext = SpanContext.getInvalid();

        // Act
        Context updatedContext = openTelemetryTracer.extractContext(diagnosticId, Context.NONE);

        // Assert
        Optional<Object> spanContextOptional = updatedContext.getData(SPAN_CONTEXT_KEY);
        assertNotNull(spanContextOptional);
        SpanContext spanContext = (SpanContext) spanContextOptional.get();
        assertEquals(spanContext, invalidSpanContext);
    }

    @Test
    public void addEventWithNonNullEventName() {
        // Arrange
        final String eventName = "event-0";

        // Act
        openTelemetryTracer.addEvent(eventName, null, null, tracingContext);

        // Assert
        final ReadableSpan recordEventsSpan = (ReadableSpan) tracingContext.getData(PARENT_SPAN_KEY).get();
        assertEquals(PARENT_SPAN_KEY, recordEventsSpan.getName());
        List<EventData> eventData = recordEventsSpan.toSpanData().getEvents();
        assertNotNull(eventData);
        assertEquals(1, eventData.size());
        assertEquals(eventName, eventData.get(0).getName());
    }

    @Test
    public void addEventWithAttributes() {
        // Arrange
        final String eventName = "event-0";
        Map<String, Object> input = new HashMap<String, Object>() {{
                put("attr1", "value1");
                put("attr2", true);
                put("attr3", 1L);
                put("attr4", 1.0);
                put("attr5", new double[] {1.0, 2.0, 3.0});
                put("attr6", null);
            }};

        // Act
        openTelemetryTracer.addEvent(eventName, input, null, tracingContext);

        // Assert
        final ReadableSpan recordEventsSpan = (ReadableSpan) tracingContext.getData(PARENT_SPAN_KEY).get();
        assertEquals(PARENT_SPAN_KEY, recordEventsSpan.getName());
        List<EventData> eventData = recordEventsSpan.toSpanData().getEvents();
        assertNotNull(eventData);
        assertEquals(1, eventData.size());
        assertEquals(eventName, eventData.get(0).getName());
        Attributes attributes = eventData.get(0).getAttributes();
        assertEquals(input.size() - 1, attributes.size());
        Attributes expectedEventAttrs = Attributes.builder()
            .put("attr1", "value1")
            .put("attr2", true)
            .put("attr3", 1L)
            .put("attr4", 1.0)
            .put("attr5", new double[] {1.0, 2.0, 3.0})
            .build();

        expectedEventAttrs.forEach((attributeKey, attrValue) -> assertEquals(attrValue, attributes.get(attributeKey)));
    }

    @Test
    public void addEventWithTimeSpecification() {
        // Arrange
        final String eventName = "event-0";
        OffsetDateTime eventTime = OffsetDateTime.parse("2021-01-01T18:35:24.00Z");

        // Act
        openTelemetryTracer.addEvent(eventName, null, eventTime, tracingContext);

        // Assert
        final ReadableSpan recordEventsSpan = (ReadableSpan) tracingContext.getData(PARENT_SPAN_KEY).get();
        assertEquals(PARENT_SPAN_KEY, recordEventsSpan.getName());
        List<EventData> eventData = recordEventsSpan.toSpanData().getEvents();
        assertNotNull(eventData);
        assertEquals(1, eventData.size());
        assertEquals(eventName, eventData.get(0).getName());
        assertEquals(eventTime,
            OffsetDateTime.ofInstant(Instant.ofEpochMilli(eventData.get(0).getEpochNanos() / 1000000), ZoneOffset.UTC));
    }

    @Test
    public void addEventAfterSpanEnd() {
        // Arrange
        final String eventName = "event-0";

        // Act
        parentSpan.end();
        openTelemetryTracer.addEvent(eventName, null, null, tracingContext);

        // Assert
        final ReadableSpan recordEventsSpan = (ReadableSpan) tracingContext.getData(PARENT_SPAN_KEY).get();
        List<EventData> eventData = recordEventsSpan.toSpanData().getEvents();
        assertNotNull(eventData);
        // no event associated once span has ended and the user tries to add an event.
        assertEquals(0, eventData.size());
    }

    @Test
    public void addEventWithChildSpan() {
        // Arrange
        tracingContext = openTelemetryTracer.start("child-span-1", tracingContext);
        final String eventName = "event-0";
        OffsetDateTime eventTime = OffsetDateTime.parse("2021-01-01T18:35:24.00Z");

        // Act
        openTelemetryTracer.addEvent(eventName, null, eventTime, tracingContext);

        // Assert
        final ReadableSpan recordEventsSpan = (ReadableSpan) tracingContext.getData(PARENT_SPAN_KEY).get();
        assertEquals("child-span-1", recordEventsSpan.getName());
        List<EventData> eventData = recordEventsSpan.toSpanData().getEvents();
        assertNotNull(eventData);
        assertEquals(1, eventData.size());
        assertEquals(eventName, eventData.get(0).getName());
        assertEquals(eventTime,
            OffsetDateTime.ofInstant(Instant.ofEpochMilli(eventData.get(0).getEpochNanos() / 1000000), ZoneOffset.UTC));
    }

    @Test
    public void addEventWithoutEventSpanContext() {
        // Arrange
        final String eventName = "event-0";
        OffsetDateTime eventTime = OffsetDateTime.parse("2021-01-01T18:35:24.00Z");

        // Act
        try (Scope scope = parentSpan.makeCurrent()) {
            openTelemetryTracer.addEvent(eventName, null, eventTime);
        }
        // Assert
        final ReadableSpan recordEventsSpan = (ReadableSpan) tracingContext.getData(PARENT_SPAN_KEY).get();
        assertEquals(PARENT_SPAN_KEY, recordEventsSpan.getName());
        List<EventData> eventData = recordEventsSpan.toSpanData().getEvents();
        assertNotNull(eventData);
        assertEquals(1, eventData.size());
    }

    @Test
    public void startSpanWithOptionsNameEmptyParent() {
        final StartSpanOptions options = new StartSpanOptions(com.azure.core.util.tracing.SpanKind.INTERNAL);
        final Context started = openTelemetryTracer.start(METHOD_NAME, options, Context.NONE.addData(PARENT_SPAN_KEY, Span.getInvalid()));
        final ReadableSpan span = (ReadableSpan) started.getData(PARENT_SPAN_KEY).get();
        final SpanData spanData = span.toSpanData();

        assertEquals(METHOD_NAME, span.getName());
        assertEquals(SpanKind.INTERNAL, span.getKind());

        assertEquals("0000000000000000", spanData.getParentSpanId());
        assertTrue(spanData.getAttributes().isEmpty());
        assertFalse(started.getData(SCOPE_KEY).isPresent());
    }

    @Test
    public void startSpanWithOptionsNameUserNameWins() {
        final StartSpanOptions options = new StartSpanOptions(com.azure.core.util.tracing.SpanKind.INTERNAL);

        final Context started = openTelemetryTracer.start(METHOD_NAME, options, tracingContext.addData(USER_SPAN_NAME_KEY, "foo"));
        final ReadableSpan span = (ReadableSpan) started.getData(PARENT_SPAN_KEY).get();

        assertEquals("foo", span.getName());
    }

    @Test
    public void startSpanWithOptionsNameImplicitParent() {
        final StartSpanOptions options = new StartSpanOptions(com.azure.core.util.tracing.SpanKind.INTERNAL);

        try (Scope scope = parentSpan.makeCurrent()) {
            final Context started = openTelemetryTracer.start(METHOD_NAME, options, Context.NONE);
            final ReadableSpan span = (ReadableSpan) started.getData(PARENT_SPAN_KEY).get();
            final SpanData spanData = span.toSpanData();

            assertEquals(METHOD_NAME, span.getName());
            assertEquals(SpanKind.INTERNAL, span.getKind());

            assertEquals(parentSpan.getSpanContext().getTraceId(), spanData.getTraceId());
            assertEquals(parentSpan.getSpanContext().getSpanId(), spanData.getParentSpanId());

            assertTrue(spanData.getAttributes().isEmpty());
            assertFalse(started.getData(SCOPE_KEY).isPresent());
        }
    }

    @Test
    public void startSpanWithOptionsNameExplicitParent() {
        final StartSpanOptions options = new StartSpanOptions(com.azure.core.util.tracing.SpanKind.INTERNAL);

        final Span explicitParentSpan = tracer.spanBuilder("foo").setNoParent().startSpan();
        final Context started = openTelemetryTracer.start(METHOD_NAME, options, new Context(PARENT_SPAN_KEY, explicitParentSpan));

        final ReadableSpan span = (ReadableSpan) started.getData(PARENT_SPAN_KEY).get();
        final SpanData spanData = span.toSpanData();

        assertEquals(explicitParentSpan.getSpanContext().getTraceId(), spanData.getTraceId());
        assertEquals(explicitParentSpan.getSpanContext().getSpanId(), spanData.getParentSpanId());
    }

    @Test
    public void startSpanWithInternalKind() {
        final StartSpanOptions options = new StartSpanOptions(com.azure.core.util.tracing.SpanKind.INTERNAL);
        final Context started = openTelemetryTracer.start(METHOD_NAME, options, Context.NONE);
        final ReadableSpan span = (ReadableSpan) started.getData(PARENT_SPAN_KEY).get();

        assertEquals(SpanKind.INTERNAL, span.getKind());
    }

    @Test
    public void startSpanWithClientKind() {
        final StartSpanOptions options = new StartSpanOptions(com.azure.core.util.tracing.SpanKind.CLIENT);
        final Context started = openTelemetryTracer.start(METHOD_NAME, options, Context.NONE);
        final ReadableSpan span = (ReadableSpan) started.getData(PARENT_SPAN_KEY).get();

        assertEquals(SpanKind.CLIENT, span.getKind());
    }

    @Test
    public void startSpanWithAttributes() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("S", "foo");
        attributes.put("I", 1);
        attributes.put("L", 10L);
        attributes.put("D", 0.1d);
        attributes.put("B", true);
        attributes.put("S[]", new String[]{"foo"});
        attributes.put("L[]", new long[]{10L});
        attributes.put("D[]", new double[]{0.1d});
        attributes.put("B[]", new boolean[]{true});
        attributes.put("I[]", new int[]{1});


        final Attributes expectedAttributes = Attributes.builder()
            .put("S", "foo")
            .put("L", 10L)
            .put("D", 0.1d)
            .put("B", true)
            .put("S[]",  new String[]{"foo"})
            .put("L[]", new long[] {10L})
            .put("D[]", new double[] {0.1d})
            .put("B[]", new boolean[] {true})
            .build();

        final StartSpanOptions options = new StartSpanOptions(com.azure.core.util.tracing.SpanKind.INTERNAL);
        attributes.forEach(options::setAttribute);

        final Context started = openTelemetryTracer.start(METHOD_NAME, options, tracingContext);
        final ReadableSpan span = (ReadableSpan) started.getData(PARENT_SPAN_KEY).get();

        verifySpanAttributes(expectedAttributes, span.toSpanData().getAttributes());
    }

    private static void assertSpanWithExplicitParent(Context updatedContext, String parentSpanId) {
        assertNotNull(updatedContext.getData(PARENT_SPAN_KEY).get());

        // verify instance created of opentelemetry-sdk (test impl), span implementation
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
        assertEquals(SpanKind.CONSUMER, recordEventsSpan.toSpanData().getKind());

        // verify span started with remote parent
        assertTrue(recordEventsSpan.toSpanData().getParentSpanContext().isRemote());
        assertEquals(parentSpanId, recordEventsSpan.toSpanData().getParentSpanId());
    }

    private static void verifySpanAttributes(Map<String, Object> expectedMap, Attributes actualAttributeMap) {
        assertEquals(expectedMap.size(), actualAttributeMap.size());

        actualAttributeMap.forEach((attributeKey, attributeValue) -> {
            assertEquals(expectedMap.get(attributeKey.getKey()), attributeValue);
        });
    }

    private static void verifySpanAttributes(Attributes expected, Attributes actual) {
        assertEquals(expected.size(), actual.size());

        assertTrue(expected.asMap().entrySet().stream().allMatch(e -> e.getValue().equals(actual.get(e.getKey()))));
    }

}
