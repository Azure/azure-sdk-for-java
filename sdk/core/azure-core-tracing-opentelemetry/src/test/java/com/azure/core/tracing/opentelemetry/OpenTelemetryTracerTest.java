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
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.azure.core.tracing.opentelemetry.OpenTelemetryTracer.AZ_NAMESPACE_KEY;
import static com.azure.core.tracing.opentelemetry.OpenTelemetryTracer.MESSAGE_ENQUEUED_TIME;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;
import static com.azure.core.util.tracing.Tracer.SCOPE_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_BUILDER_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static com.azure.core.util.tracing.Tracer.USER_SPAN_NAME_KEY;
import static io.opentelemetry.api.trace.StatusCode.UNSET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
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
    private OpenTelemetryTracer openTelemetryTracer;
    private static final SpanContext TEST_CONTEXT = SpanContext.create("0123456789abcdef0123456789abcdef", "0123456789abcdef", TraceFlags.getSampled(), TraceState.getDefault());

    private TestExporter testExporter;
    private Tracer tracer;
    private SpanProcessor spanProcessor;
    private Context tracingContext;
    private Span parentSpan;

    private final HashMap<String, Object> expectedAttributeMap = new HashMap<String, Object>() {
        {
            put(OpenTelemetryTracer.MESSAGE_BUS_DESTINATION, ENTITY_PATH_VALUE);
            put(OpenTelemetryTracer.PEER_ENDPOINT, HOSTNAME_VALUE);
            put(AZ_NAMESPACE_KEY, AZ_NAMESPACE_VALUE);
        }
    };

    class TestSpanProcessor implements SpanProcessor {

        private final SpanExporter exporter;

        TestSpanProcessor(SpanExporter exporter) {
            this.exporter = exporter;
        }

        @Override
        public void onStart(io.opentelemetry.context.Context parentContext, ReadWriteSpan span) {

        }

        @Override
        public boolean isStartRequired() {
            return false;
        }

        @Override
        public void onEnd(ReadableSpan span) {
            exporter.export(Collections.singletonList(span.toSpanData()));
        }

        @Override
        public boolean isEndRequired() {
            return true;
        }
    }

    @BeforeEach
    public void setUp() {
        testExporter = new TestExporter();
        spanProcessor = new TestSpanProcessor(testExporter);
        tracer = OpenTelemetrySdk.builder()
            .setTracerProvider(SdkTracerProvider.builder()
                .addSpanProcessor(spanProcessor)
                .build())
            .build().getTracer("TracerSdkTest");

        // Start user parent span.
        parentSpan = tracer.spanBuilder(METHOD_NAME)
            .setSpanKind(SpanKind.SERVER)
            .setNoParent().startSpan();

        // Add parent span to tracingContext
        tracingContext = new Context(PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.root().with(parentSpan));
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
        final ReadableSpan recordEventsSpan = getSpan(updatedContext);
        assertEquals(SpanKind.INTERNAL, recordEventsSpan.toSpanData().getKind());
        final Attributes attributeMap = recordEventsSpan.toSpanData().getAttributes();
        assertEquals(attributeMap.get(AttributeKey.stringKey(AZ_NAMESPACE_KEY)), AZ_NAMESPACE_VALUE);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void parentSpanKeyBackwardCompatibility() {
        // Act
        Span parentSpan = tracer.spanBuilder(METHOD_NAME).startSpan();
        Context updatedContext = openTelemetryTracer.start(METHOD_NAME, Context.NONE.addData(PARENT_SPAN_KEY, parentSpan));

        // Assert
        assertSpanWithExplicitParent(updatedContext, parentSpan.getSpanContext().getSpanId());
    }

    @Test
    public void startSpanTestNoUserParent() {
        // Act
        final Context updatedContext = openTelemetryTracer.start(METHOD_NAME, Context.NONE);

        // Assert
        final ReadableSpan recordEventsSpan = getSpan(updatedContext);

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
        ReadableSpan recordEventsSpan = assertSpanWithExplicitParent(updatedContext, parentSpanId);
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
        final ReadableSpan recordEventsSpan = assertSpanWithExplicitParent(updatedContext, parentSpanId);
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
        final ReadableSpan recordEventsSpan = assertSpanWithExplicitParent(updatedContext, parentSpanId);
        // verify no kind set on Span for message
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
        final ReadableSpan recordEventsSpan = assertSpanWithExplicitParent(updatedContext, parentSpanId);
        // verify scope returned
        assertNotNull(updatedContext.getData(SCOPE_KEY).get());
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
        SpanBuilder span = tracer.spanBuilder(METHOD_NAME);
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
        SpanBuilder span = tracer.spanBuilder(METHOD_NAME);

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
        SpanBuilder span = tracer.spanBuilder(METHOD_NAME);

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
                assertSame(Span.current(), getSpan(started));
            } catch (Exception e) {
                fail();
            } finally {
                openTelemetryTracer.end("foo", null, started);
            }

            assertSame(parentSpan, Span.current());
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    public void startEndCurrentSpanBackwardCompatible() {
        try (Scope parentScope = parentSpan.makeCurrent()) {
            Span span = tracer.spanBuilder(METHOD_NAME).startSpan();
            final Context contextWithSpanUnderDeprecatedKey = Context.NONE.addData(PARENT_SPAN_KEY, span);

            try (AutoCloseable scope = openTelemetryTracer.makeSpanCurrent(contextWithSpanUnderDeprecatedKey)) {
                assertSame(Span.current(), span);
            } catch (Exception e) {
                fail();
            } finally {
                openTelemetryTracer.end("foo", null, contextWithSpanUnderDeprecatedKey);
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
        final ReadableSpan recordEventsSpan = getSpan(spanContext);

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
        final ReadableSpan recordEventsSpan = getSpan(spanContext);

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
        final ReadableSpan recordEventsSpan = getSpan(tracingContext);
        assertEquals(METHOD_NAME, recordEventsSpan.getName());
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
        final ReadableSpan recordEventsSpan = getSpan(tracingContext);
        assertEquals(METHOD_NAME, recordEventsSpan.getName());
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
        final ReadableSpan recordEventsSpan = getSpan(tracingContext);
        assertEquals(METHOD_NAME, recordEventsSpan.getName());
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
        final ReadableSpan recordEventsSpan = getSpan(tracingContext);
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
        final ReadableSpan recordEventsSpan = getSpan(tracingContext);
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
        final ReadableSpan recordEventsSpan = getSpan(tracingContext);
        assertEquals(METHOD_NAME, recordEventsSpan.getName());
        List<EventData> eventData = recordEventsSpan.toSpanData().getEvents();
        assertNotNull(eventData);
        assertEquals(1, eventData.size());
    }

    @Test
    public void startSpanWithOptionsNameEmptyParent() {
        final StartSpanOptions options = new StartSpanOptions(com.azure.core.util.tracing.SpanKind.INTERNAL);
        final Context started = openTelemetryTracer.start(METHOD_NAME, options, new Context(PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.root()));
        final ReadableSpan span = getSpan(started);
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
        final ReadableSpan span = getSpan(started);

        assertEquals("foo", span.getName());
    }

    @Test
    public void startSpanWithOptionsNameImplicitParent() {
        final StartSpanOptions options = new StartSpanOptions(com.azure.core.util.tracing.SpanKind.INTERNAL);

        try (Scope scope = parentSpan.makeCurrent()) {
            final Context started = openTelemetryTracer.start(METHOD_NAME, options, Context.NONE);
            final ReadableSpan span = getSpan(started);
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
        final Context started = openTelemetryTracer.start(METHOD_NAME, options, new Context(PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.root().with(explicitParentSpan)));

        final ReadableSpan span = getSpan(started);
        final SpanData spanData = span.toSpanData();

        assertEquals(explicitParentSpan.getSpanContext().getTraceId(), spanData.getTraceId());
        assertEquals(explicitParentSpan.getSpanContext().getSpanId(), spanData.getParentSpanId());
    }

    @Test
    public void startSpanWithInternalKind() {
        final StartSpanOptions options = new StartSpanOptions(com.azure.core.util.tracing.SpanKind.INTERNAL);
        final Context started = openTelemetryTracer.start(METHOD_NAME, options, Context.NONE);
        final ReadableSpan span = getSpan(started);

        assertEquals(SpanKind.INTERNAL, span.getKind());
    }

    @Test
    public void startSpanWithClientKind() {
        final StartSpanOptions options = new StartSpanOptions(com.azure.core.util.tracing.SpanKind.CLIENT);
        final Context started = openTelemetryTracer.start(METHOD_NAME, options, Context.NONE);
        final ReadableSpan span = getSpan(started);

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
        final ReadableSpan span = getSpan(started);

        verifySpanAttributes(expectedAttributes, span.toSpanData().getAttributes());
    }

    @Test
    public void suppressNestedClientSpan() {
        Context outer = openTelemetryTracer.start("outer", Context.NONE);
        Context innerSuppressed = openTelemetryTracer.start("innerSuppressed", outer);
        Context innerNotSuppressed = openTelemetryTracer.start("innerNotSuppressed", new StartSpanOptions(com.azure.core.util.tracing.SpanKind.PRODUCER), innerSuppressed);

        openTelemetryTracer.end("ok", null, innerNotSuppressed);
        assertEquals(1, testExporter.getSpans().size());
        openTelemetryTracer.end("ok", null, innerSuppressed);

        assertEquals(1, testExporter.getSpans().size());
        openTelemetryTracer.end("ok", null, outer);

        assertEquals(2, testExporter.getSpans().size());

        SpanData innerNotSuppressedSpan = testExporter.getSpans().get(0);
        SpanData outerSpan = testExporter.getSpans().get(1);
        assertEquals(innerNotSuppressedSpan.getSpanContext().getTraceId(), outerSpan.getSpanContext().getTraceId());
        assertEquals(innerNotSuppressedSpan.getParentSpanId(), outerSpan.getSpanContext().getSpanId());
    }

    @Test
    public void suppressNestedInterleavedClientSpan() {
        Context outer = openTelemetryTracer.getSharedSpanBuilder("outer", Context.NONE);
        openTelemetryTracer.addLink(outer.addData(SPAN_CONTEXT_KEY, TEST_CONTEXT));
        outer = openTelemetryTracer.start("outer", outer, ProcessKind.SEND);

        Context inner1Suppressed = openTelemetryTracer.start("innerSuppressed", outer);
        Context inner1NotSuppressed = openTelemetryTracer.start("innerNotSuppressed", new StartSpanOptions(com.azure.core.util.tracing.SpanKind.PRODUCER), inner1Suppressed);
        Context inner2Suppressed = openTelemetryTracer.start("innerSuppressed", inner1NotSuppressed);

        openTelemetryTracer.end("ok", null, inner2Suppressed);
        assertEquals(0, testExporter.getSpans().size());

        openTelemetryTracer.end("ok", null, inner1NotSuppressed);
        openTelemetryTracer.end("ok", null, inner1Suppressed);
        openTelemetryTracer.end("ok", null, outer);
        assertEquals(2, testExporter.getSpans().size());

        SpanData innerNotSuppressedSpan = testExporter.getSpans().get(0);
        SpanData outerSpan = testExporter.getSpans().get(1);
        assertEquals(innerNotSuppressedSpan.getSpanContext().getTraceId(), outerSpan.getSpanContext().getTraceId());
        assertEquals(innerNotSuppressedSpan.getParentSpanId(), outerSpan.getSpanContext().getSpanId());
    }

    @Test
    public void suppressNestedMultipleLayersSpan() {
        Context outer = openTelemetryTracer.start("outer", Context.NONE);
        Context inner1Suppressed = openTelemetryTracer.start("innerSuppressed", outer);
        Context inner2Suppressed = openTelemetryTracer.start("inner2Suppressed", inner1Suppressed);

        openTelemetryTracer.end("ok", null, inner2Suppressed);
        openTelemetryTracer.end("ok", null, inner1Suppressed);
        assertEquals(0, testExporter.getSpans().size());

        openTelemetryTracer.end("ok", null, outer);
        assertEquals(1, testExporter.getSpans().size());
    }

    @ParameterizedTest
    @MethodSource("spanKinds")
    public void suppressNestedClientSpan(com.azure.core.util.tracing.SpanKind outerKind, com.azure.core.util.tracing.SpanKind innerKind, boolean shouldSuppressInner) {
        Context outer = openTelemetryTracer.start("outer", new StartSpanOptions(outerKind), Context.NONE);
        Context inner = openTelemetryTracer.start("inner", new StartSpanOptions(innerKind), outer);
        Context neverSuppressed = openTelemetryTracer.start("innerNotSuppressed", new StartSpanOptions(com.azure.core.util.tracing.SpanKind.PRODUCER), inner);

        openTelemetryTracer.end("ok", null, neverSuppressed);
        assertEquals(1, testExporter.getSpans().size());

        openTelemetryTracer.end("ok", null, inner);
        assertEquals(shouldSuppressInner ? 1 : 2, testExporter.getSpans().size());

        openTelemetryTracer.end("ok", null, outer);
        assertEquals(shouldSuppressInner ? 2 : 3, testExporter.getSpans().size());

        SpanData neverSuppressedSpan = testExporter.getSpans().get(0);

        if (shouldSuppressInner) {
            SpanData outerSpan = testExporter.getSpans().get(1);
            assertEquals(neverSuppressedSpan.getParentSpanId(), outerSpan.getSpanContext().getSpanId());
        } else {
            SpanData innerSpan = testExporter.getSpans().get(1);
            SpanData outerSpan = testExporter.getSpans().get(2);
            assertEquals(neverSuppressedSpan.getParentSpanId(), innerSpan.getSpanContext().getSpanId());
            assertEquals(innerSpan.getParentSpanId(), outerSpan.getSpanContext().getSpanId());
        }
    }

    private static Stream<Arguments> spanKinds() {
        return Stream.of(
            Arguments.of(com.azure.core.util.tracing.SpanKind.CLIENT, com.azure.core.util.tracing.SpanKind.CLIENT, true),
            Arguments.of(com.azure.core.util.tracing.SpanKind.CLIENT, com.azure.core.util.tracing.SpanKind.INTERNAL, true),
            Arguments.of(com.azure.core.util.tracing.SpanKind.CLIENT, com.azure.core.util.tracing.SpanKind.PRODUCER, false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.CLIENT, com.azure.core.util.tracing.SpanKind.CONSUMER, false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.CLIENT, com.azure.core.util.tracing.SpanKind.SERVER, false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.INTERNAL, com.azure.core.util.tracing.SpanKind.CLIENT, true),
            Arguments.of(com.azure.core.util.tracing.SpanKind.INTERNAL, com.azure.core.util.tracing.SpanKind.INTERNAL, true),
            Arguments.of(com.azure.core.util.tracing.SpanKind.INTERNAL, com.azure.core.util.tracing.SpanKind.PRODUCER, false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.INTERNAL, com.azure.core.util.tracing.SpanKind.CONSUMER, false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.INTERNAL, com.azure.core.util.tracing.SpanKind.SERVER, false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.PRODUCER, com.azure.core.util.tracing.SpanKind.CLIENT, false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.PRODUCER, com.azure.core.util.tracing.SpanKind.INTERNAL, false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.PRODUCER, com.azure.core.util.tracing.SpanKind.PRODUCER, false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.PRODUCER, com.azure.core.util.tracing.SpanKind.CONSUMER, false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.PRODUCER, com.azure.core.util.tracing.SpanKind.SERVER, false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.CONSUMER, com.azure.core.util.tracing.SpanKind.CLIENT, false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.CONSUMER, com.azure.core.util.tracing.SpanKind.INTERNAL, false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.CONSUMER, com.azure.core.util.tracing.SpanKind.PRODUCER, false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.CONSUMER, com.azure.core.util.tracing.SpanKind.CONSUMER, false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.CONSUMER, com.azure.core.util.tracing.SpanKind.SERVER, false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.SERVER, com.azure.core.util.tracing.SpanKind.CLIENT, false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.SERVER, com.azure.core.util.tracing.SpanKind.INTERNAL, false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.SERVER, com.azure.core.util.tracing.SpanKind.PRODUCER, false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.SERVER, com.azure.core.util.tracing.SpanKind.CONSUMER, false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.SERVER, com.azure.core.util.tracing.SpanKind.SERVER, false));

    }

    @Test
    public void suppressNestedClientSpanAttributes() {
        Context outer = openTelemetryTracer.start("outer", Context.NONE);
        openTelemetryTracer.setAttribute("outer1", "foo", outer);

        Context innerSuppressed = openTelemetryTracer.start("innerSuppressed", outer);
        openTelemetryTracer.setAttribute("outer2", "bar", outer);
        openTelemetryTracer.setAttribute("innerSuppressed", "foo", innerSuppressed);

        openTelemetryTracer.end("ok", null, innerSuppressed);
        openTelemetryTracer.end("ok", null, outer);

        SpanData outerSpan = testExporter.getSpans().get(0);
        assertEquals("outer", outerSpan.getName());

        Map<String, Object> outerAttributesExpected = new HashMap<String, Object>() {{
                put("outer1", "foo");
                put("outer2", "bar");
            }};

        verifySpanAttributes(outerAttributesExpected, outerSpan.getAttributes());
    }

    @Test
    public void suppressNestedClientSpanEvents() {
        Context outer = openTelemetryTracer.start("outer", Context.NONE);
        openTelemetryTracer.addEvent("outer1", null, null, outer);

        Context innerSuppressed = openTelemetryTracer.start("innerSuppressed", outer);
        openTelemetryTracer.addEvent("outer2", null, null, outer);
        openTelemetryTracer.addEvent("innerSuppressed", null, null, innerSuppressed);

        openTelemetryTracer.end("ok", null, innerSuppressed);
        openTelemetryTracer.end("ok", null, outer);

        SpanData outerSpan = testExporter.getSpans().get(0);
        assertEquals(2, outerSpan.getEvents().size());
        assertEquals("outer1", outerSpan.getEvents().get(0).getName());
        assertEquals("outer2", outerSpan.getEvents().get(1).getName());
    }

    @Test
    public void suppressNestedClientSpanLinks() {
        Context outer = openTelemetryTracer.start("outer", Context.NONE);
        Context innerSuppressed = openTelemetryTracer.getSharedSpanBuilder("innerSuppressed", outer);
        openTelemetryTracer.addLink(innerSuppressed.addData(SPAN_CONTEXT_KEY, TEST_CONTEXT));
        innerSuppressed = openTelemetryTracer.start("innerSuppressed", innerSuppressed, ProcessKind.SEND);

        openTelemetryTracer.end("ok", null, innerSuppressed);

        assertTrue(testExporter.getSpans().isEmpty());
    }

    @Test
    public void suppressNestedClientSpanMakeCurrent() throws Exception {
        Context outer = openTelemetryTracer.start("outer", Context.NONE);
        AutoCloseable outerScope = openTelemetryTracer.makeSpanCurrent(outer);
        Span outerSpan = Span.current();
        Context inner = openTelemetryTracer.start("inner", outer);

        AutoCloseable innerScope = openTelemetryTracer.makeSpanCurrent(outer);
        assertSame(outerSpan, Span.current());
        innerScope.close();
        assertSame(outerSpan, Span.current());
        openTelemetryTracer.end("ok", null, inner);
        assertTrue(testExporter.getSpans().isEmpty());
    }

    @Test
    public void startSendSpanWithoutBuilder() {
        Context outer = openTelemetryTracer.start("outer", new StartSpanOptions(com.azure.core.util.tracing.SpanKind.SERVER), Context.NONE);
        Context sendNoBuilder = openTelemetryTracer.start("sendNoBuilder", outer, ProcessKind.SEND);

        assertNotSame(sendNoBuilder, outer);
        openTelemetryTracer.end("ok", null, sendNoBuilder);
        openTelemetryTracer.end("ok", null, outer);

        assertEquals(2, testExporter.getSpans().size());

        SpanData sendNoBuilderSpan = testExporter.getSpans().get(0);
        SpanData outerSpan = testExporter.getSpans().get(1);
        assertEquals(sendNoBuilderSpan.getSpanContext().getTraceId(), outerSpan.getSpanContext().getTraceId());
        assertEquals(sendNoBuilderSpan.getParentSpanId(), outerSpan.getSpanContext().getSpanId());
    }

    private static ReadableSpan getSpan(Context context) {
        Optional<Object> otelCtx =  context.getData(PARENT_TRACE_CONTEXT_KEY);
        assertTrue(otelCtx.isPresent());
        assertTrue(io.opentelemetry.context.Context.class.isAssignableFrom(otelCtx.get().getClass()));
        Span span = Span.fromContext((io.opentelemetry.context.Context) otelCtx.get());
        assertTrue(span.getSpanContext().isValid());
        assertTrue(ReadableSpan.class.isAssignableFrom(span.getClass()));

        return (ReadableSpan) span;
    }

    private static ReadableSpan assertSpanWithExplicitParent(Context updatedContext, String parentSpanId) {
        final ReadableSpan recordEventsSpan = getSpan(updatedContext);

        assertEquals(METHOD_NAME, recordEventsSpan.getName());

        // verify span started with explicit parent
        assertFalse(recordEventsSpan.toSpanData().getParentSpanContext().isRemote());
        assertEquals(parentSpanId, recordEventsSpan.toSpanData().getParentSpanId());
        return recordEventsSpan;
    }

    private static ReadableSpan assertSpanWithRemoteParent(Context updatedContext, String parentSpanId) {
        final ReadableSpan recordEventsSpan = getSpan(updatedContext);

        assertEquals(METHOD_NAME, recordEventsSpan.getName());
        assertEquals(SpanKind.CONSUMER, recordEventsSpan.toSpanData().getKind());

        // verify span started with remote parent
        assertTrue(recordEventsSpan.toSpanData().getParentSpanContext().isRemote());
        assertEquals(parentSpanId, recordEventsSpan.toSpanData().getParentSpanId());
        return recordEventsSpan;
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
