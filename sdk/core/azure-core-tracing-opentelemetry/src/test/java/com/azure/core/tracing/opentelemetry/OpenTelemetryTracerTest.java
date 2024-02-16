// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.TracingLink;
import io.opentelemetry.api.OpenTelemetry;
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
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.Exceptions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static com.azure.core.tracing.opentelemetry.OpenTelemetryTracer.MESSAGE_ENQUEUED_TIME;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_BUILDER_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static io.opentelemetry.api.trace.StatusCode.ERROR;
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
    private static final int SPAN_ID_OFFSET = TRACE_ID_OFFSET + TRACE_ID_HEX_SIZE + TRACEPARENT_DELIMITER_SIZE;
    private static final int TRACE_OPTION_OFFSET = SPAN_ID_OFFSET + SPAN_ID_HEX_SIZE + TRACEPARENT_DELIMITER_SIZE;
    private OpenTelemetryTracer openTelemetryTracer;
    private Tracer tracer;
    private InMemorySpanExporter testExporter;
    private SpanProcessor spanProcessor;
    private Context tracingContext;
    private Span parentSpan;
    private SdkTracerProvider tracerProvider;
    private OpenTelemetry openTelemetry;

    private final HashMap<String, Object> expectedAttributeMap = new HashMap<String, Object>() {
        {
            put("messaging.destination.name", ENTITY_PATH_VALUE);
            put("server.address", HOSTNAME_VALUE);
            put("az.namespace", AZ_NAMESPACE_VALUE);
        }
    };

    @BeforeEach
    public void setUp() {
        testExporter = InMemorySpanExporter.create();
        spanProcessor = SimpleSpanProcessor.create(testExporter);
        tracerProvider = SdkTracerProvider.builder().addSpanProcessor(spanProcessor).build();

        openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();

        tracer = tracerProvider.get("test");
        // Start user parent span.
        parentSpan = tracer.spanBuilder(METHOD_NAME).setSpanKind(SpanKind.SERVER).setNoParent().startSpan();

        // Add parent span to tracingContext
        tracingContext
            = new Context(PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.root().with(parentSpan));
        openTelemetryTracer = new OpenTelemetryTracer("test", null, AZ_NAMESPACE_VALUE,
            new OpenTelemetryTracingOptions().setOpenTelemetry(openTelemetry));
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
        final Context span = openTelemetryTracer.start(METHOD_NAME, tracingContext);

        // Assert
        final SpanData spanData = assertSpanWithExplicitParent(span, parentSpanId).toSpanData();
        assertEquals(SpanKind.INTERNAL, spanData.getKind());
        final Attributes attributeMap = spanData.getAttributes();
        assertEquals(attributeMap.get(AttributeKey.stringKey("az.namespace")), AZ_NAMESPACE_VALUE);
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
    @SuppressWarnings("deprecation")
    public void startSharedBuilderAndSpanProcessKindSend() {
        // Arrange
        final String parentSpanId = parentSpan.getSpanContext().getSpanId();

        // Add additional metadata to spans for SEND
        final Context traceContext = tracingContext.addData(ENTITY_PATH_KEY, ENTITY_PATH_VALUE)
            .addData(HOST_NAME_KEY, HOSTNAME_VALUE)
            .addData("az.namespace", "ignored");

        // Start user parent span.
        final Context withBuilder = openTelemetryTracer.getSharedSpanBuilder(METHOD_NAME, traceContext);

        // Act
        final Context updatedContext
            = openTelemetryTracer.start(METHOD_NAME, withBuilder, com.azure.core.util.tracing.ProcessKind.SEND);

        // Assert
        // verify span created with explicit parent when for Process Kind SEND
        ReadableSpan recordEventsSpan = assertSpanWithExplicitParent(updatedContext, parentSpanId);
        assertEquals(SpanKind.CLIENT, recordEventsSpan.toSpanData().getKind());

        // verify span attributes
        final Attributes attributeMap = recordEventsSpan.toSpanData().getAttributes();

        verifySpanAttributes(expectedAttributeMap, attributeMap);
    }

    @Test
    public void startWithAttributes() {
        // Arrange
        final String parentSpanId = parentSpan.getSpanContext().getSpanId();

        StartSpanOptions options = new StartSpanOptions(com.azure.core.util.tracing.SpanKind.PRODUCER);
        options.setAttribute(ENTITY_PATH_KEY, ENTITY_PATH_VALUE).setAttribute(HOST_NAME_KEY, HOSTNAME_VALUE);

        // Act
        final Context span = openTelemetryTracer.start(METHOD_NAME, options, tracingContext);

        // Assert
        // verify span created with explicit parent when for Process Kind SEND
        ReadableSpan recordEventsSpan = assertSpanWithExplicitParent(span, parentSpanId);
        assertEquals(SpanKind.PRODUCER, recordEventsSpan.toSpanData().getKind());

        // verify span attributes
        final Attributes attributeMap = recordEventsSpan.toSpanData().getAttributes();

        verifySpanAttributes(expectedAttributeMap, attributeMap);
    }

    @Test
    public void fallbackToAzNamespaceFromContext() {
        // Arrange
        OpenTelemetryTracer noAzTracer = new OpenTelemetryTracer("test", null, null,
            new OpenTelemetryTracingOptions().setOpenTelemetry(openTelemetry));

        // Act
        final Context span = noAzTracer.start(METHOD_NAME, new Context("az.namespace", "foo"));

        // Assert
        SpanData spanData = getSpan(span).toSpanData();
        assertEquals(1, spanData.getAttributes().size());
        assertEquals("foo", spanData.getAttributes().get(AttributeKey.stringKey("az.namespace")));
    }

    @Test
    public void startWithLinks() {
        // Arrange
        SpanContext linkCtx1 = SpanContext.create(IdGenerator.random().generateTraceId(),
            IdGenerator.random().generateSpanId(), TraceFlags.getDefault(), TraceState.getDefault());
        SpanContext linkCtx2 = SpanContext.create(IdGenerator.random().generateTraceId(),
            IdGenerator.random().generateSpanId(), TraceFlags.getDefault(), TraceState.getDefault());

        StartSpanOptions options = new StartSpanOptions(com.azure.core.util.tracing.SpanKind.CLIENT)
            .setAttribute(ENTITY_PATH_KEY, ENTITY_PATH_VALUE)
            .setAttribute(HOST_NAME_KEY, HOSTNAME_VALUE)
            .addLink(new TracingLink(new Context(SPAN_CONTEXT_KEY, linkCtx1), Collections.singletonMap("foo", "bar")))
            .addLink(new TracingLink(new Context(SPAN_CONTEXT_KEY, linkCtx2)));

        // Act
        final Context span = openTelemetryTracer.start(METHOD_NAME, options, Context.NONE);

        // Assert
        SpanData spanData = getSpan(span).toSpanData();
        assertEquals(SpanKind.CLIENT, spanData.getKind());

        // verify span attributes
        final Attributes attributeMap = spanData.getAttributes();
        verifySpanAttributes(expectedAttributeMap, attributeMap);

        assertEquals(2, spanData.getLinks().size());
        assertSame(linkCtx1, spanData.getLinks().get(0).getSpanContext());
        assertSame(linkCtx2, spanData.getLinks().get(1).getSpanContext());
        assertEquals(1, spanData.getLinks().get(0).getAttributes().size());
        assertEquals("bar", spanData.getLinks().get(0).getAttributes().get(AttributeKey.stringKey("foo")));
        assertEquals(0, spanData.getLinks().get(1).getAttributes().size());
    }

    @Test
    public void startWithRemoteParent() {
        // Arrange
        SpanContext remoteParent = SpanContext.create(IdGenerator.random().generateTraceId(),
            IdGenerator.random().generateSpanId(), TraceFlags.getSampled(), TraceState.getDefault());

        StartSpanOptions options = new StartSpanOptions(com.azure.core.util.tracing.SpanKind.CONSUMER)
            .setAttribute(ENTITY_PATH_KEY, ENTITY_PATH_VALUE)
            .setAttribute(HOST_NAME_KEY, HOSTNAME_VALUE)
            .setRemoteParent(new Context(SPAN_CONTEXT_KEY, remoteParent));

        // Act
        final Context span = openTelemetryTracer.start(METHOD_NAME, options, Context.NONE);

        // Assert
        SpanData spanData = assertSpanWithExplicitParent(span, remoteParent.getSpanId()).toSpanData();
        assertEquals(SpanKind.CONSUMER, spanData.getKind());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void startSpanProcessKindSend() {
        // Arrange
        final String parentSpanId = parentSpan.getSpanContext().getSpanId();

        // Start user parent span.
        final SpanBuilder spanBuilder = tracer.spanBuilder(METHOD_NAME)
            .setParent(io.opentelemetry.context.Context.root().with(parentSpan))
            .setSpanKind(SpanKind.CLIENT);
        // Add additional metadata to spans for SEND
        final Context traceContext = tracingContext.addData(ENTITY_PATH_KEY, ENTITY_PATH_VALUE)
            .addData(HOST_NAME_KEY, HOSTNAME_VALUE)
            .addData(SPAN_BUILDER_KEY, spanBuilder)
            .addData(AZ_TRACING_NAMESPACE_KEY, AZ_NAMESPACE_VALUE);

        // Act
        final Context updatedContext
            = openTelemetryTracer.start(METHOD_NAME, traceContext, com.azure.core.util.tracing.ProcessKind.SEND);

        // Assert
        // verify span created with explicit parent when for Process Kind SEND
        final ReadableSpan recordEventsSpan = assertSpanWithExplicitParent(updatedContext, parentSpanId);
        assertEquals(SpanKind.CLIENT, recordEventsSpan.toSpanData().getKind());

        // verify span attributes
        final Attributes attributeMap = recordEventsSpan.toSpanData().getAttributes();

        verifySpanAttributes(expectedAttributeMap, attributeMap);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void startSpanProcessKindMessage() {
        // Arrange
        final String parentSpanId = parentSpan.getSpanContext().getSpanId();
        final Context contextWithAttributes = tracingContext.addData(ENTITY_PATH_KEY, ENTITY_PATH_VALUE)
            .addData(HOST_NAME_KEY, HOSTNAME_VALUE)
            .addData(AZ_TRACING_NAMESPACE_KEY, AZ_NAMESPACE_VALUE);

        // Act
        final Context updatedContext = openTelemetryTracer.start(METHOD_NAME, contextWithAttributes,
            com.azure.core.util.tracing.ProcessKind.MESSAGE);

        // Assert
        // verify span created with explicit parent when no span context in the sending Context object
        final ReadableSpan recordEventsSpan = assertSpanWithExplicitParent(updatedContext, parentSpanId);
        // verify no kind set on Span for message
        assertEquals(SpanKind.PRODUCER, recordEventsSpan.toSpanData().getKind());
        // verify diagnostic id and span context returned
        // assertNotNull(updatedContext.getData(SPAN_CONTEXT_KEY).get());
        assertNotNull(updatedContext.getData(DIAGNOSTIC_ID_KEY).get());

        final Attributes attributeMap = recordEventsSpan.toSpanData().getAttributes();
        verifySpanAttributes(expectedAttributeMap, attributeMap);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void startSpanProcessKindProcess() {
        // Arrange
        final String parentSpanId = parentSpan.getSpanContext().getSpanId();
        // Add additional metadata to spans for PROCESS
        final Context traceContext = tracingContext.addData(ENTITY_PATH_KEY, ENTITY_PATH_VALUE)
            .addData(HOST_NAME_KEY, HOSTNAME_VALUE)
            .addData(AZ_TRACING_NAMESPACE_KEY, AZ_NAMESPACE_VALUE)
            .addData(MESSAGE_ENQUEUED_TIME, MESSAGE_ENQUEUED_VALUE); // only in PROCESS

        // Act
        final Context updatedContext
            = openTelemetryTracer.start(METHOD_NAME, traceContext, com.azure.core.util.tracing.ProcessKind.PROCESS);

        // verify no parent span passed
        assertFalse(tracingContext.getData(SPAN_CONTEXT_KEY).isPresent(),
            "When no parent span passed in context information");
        // verify span created with explicit parent
        final ReadableSpan recordEventsSpan = assertSpanWithExplicitParent(updatedContext, parentSpanId);
        // verify scope returned
        // assertNotNull(updatedContext.getData(SCOPE_KEY).get());
        assertEquals(SpanKind.CONSUMER, recordEventsSpan.toSpanData().getKind());

        // verify span attributes
        final Attributes attributeMap = recordEventsSpan.toSpanData().getAttributes();

        // additional only in process spans.
        expectedAttributeMap.put(MESSAGE_ENQUEUED_TIME, MESSAGE_ENQUEUED_VALUE);
        expectedAttributeMap.put(AZ_TRACING_NAMESPACE_KEY, AZ_NAMESPACE_VALUE);

        verifySpanAttributes(expectedAttributeMap, attributeMap);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void getSpanBuilderTest() {
        // Act
        final Context updatedContext = openTelemetryTracer.getSharedSpanBuilder(METHOD_NAME, Context.NONE);

        assertTrue(updatedContext.getData(SPAN_BUILDER_KEY).isPresent());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void startProcessSpanWithRemoteParent() {
        // Arrange
        final Span testSpan = tracer.spanBuilder("child-span").startSpan();
        final String testSpanId = testSpan.getSpanContext().getSpanId();
        final SpanContext spanContext = SpanContext.createFromRemoteParent(testSpan.getSpanContext().getTraceId(),
            testSpan.getSpanContext().getSpanId(), testSpan.getSpanContext().getTraceFlags(),
            testSpan.getSpanContext().getTraceState());
        final Context traceContext = tracingContext.addData(SPAN_CONTEXT_KEY, spanContext);

        // Act
        final Context updatedContext
            = openTelemetryTracer.start(METHOD_NAME, traceContext, com.azure.core.util.tracing.ProcessKind.PROCESS);

        // Assert new span created with remote parent context
        assertSpanWithRemoteParent(updatedContext, testSpanId);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void startProcessSpanWithLinks() {
        // Arrange
        final Context spanBuilder = openTelemetryTracer.getSharedSpanBuilder("span", Context.NONE);

        Span link1 = tracer.spanBuilder("link1").startSpan();
        Span link2 = tracer.spanBuilder("link2").startSpan();

        openTelemetryTracer.addLink(spanBuilder.addData(SPAN_CONTEXT_KEY, link1.getSpanContext()));
        openTelemetryTracer.addLink(spanBuilder.addData(SPAN_CONTEXT_KEY, link2.getSpanContext())
            .addData(MESSAGE_ENQUEUED_TIME, MESSAGE_ENQUEUED_VALUE));

        // Act
        final Context spanCtx
            = openTelemetryTracer.start(METHOD_NAME, spanBuilder, com.azure.core.util.tracing.ProcessKind.PROCESS);
        openTelemetryTracer.end(null, null, spanCtx);

        // Assert
        ReadableSpan span = getSpan(spanCtx);
        List<LinkData> links = span.toSpanData().getLinks();
        assertEquals(2, links.size());
        assertEquals(link1.getSpanContext().getTraceId(), links.get(0).getSpanContext().getTraceId());
        assertEquals(link1.getSpanContext().getSpanId(), links.get(0).getSpanContext().getSpanId());
        assertEquals(0, links.get(0).getAttributes().size());

        assertEquals(link2.getSpanContext().getTraceId(), links.get(1).getSpanContext().getTraceId());
        assertEquals(link2.getSpanContext().getSpanId(), links.get(1).getSpanContext().getSpanId());
        Attributes linkAttributes = links.get(1).getAttributes();
        assertEquals(1, linkAttributes.size());
        assertEquals(MESSAGE_ENQUEUED_VALUE, linkAttributes.get(AttributeKey.longKey(MESSAGE_ENQUEUED_TIME)));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void startConsumeSpanWitStartTimeInContext() {
        // Arrange
        final Context spanBuilder = openTelemetryTracer.getSharedSpanBuilder("span",
            new Context("span-start-time", Instant.now().minusSeconds(1000)));

        Span link = tracer.spanBuilder("link1").startSpan();

        openTelemetryTracer.addLink(spanBuilder.addData(SPAN_CONTEXT_KEY, link.getSpanContext()));

        // Act
        final Context spanCtx
            = openTelemetryTracer.start(METHOD_NAME, spanBuilder, com.azure.core.util.tracing.ProcessKind.PROCESS);
        openTelemetryTracer.end(null, null, spanCtx);

        // Assert
        ReadableSpan span = getSpan(spanCtx);
        assertEquals(1, span.toSpanData().getLinks().size());
        assertEquals(span.getLatencyNanos() / 1000_000_000d, 1000d, 10);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void startSpanOverloadNullPointerException() {

        // Assert
        assertThrows(NullPointerException.class, () -> openTelemetryTracer.start("", Context.NONE, null));
    }

    @Test
    public void startSpanInvalid() {
        assertThrows(NullPointerException.class, () -> openTelemetryTracer.start(null, Context.NONE));
        assertThrows(NullPointerException.class, () -> openTelemetryTracer.start("span", null));
        assertThrows(NullPointerException.class, () -> openTelemetryTracer.start("span", null, Context.NONE));
        assertThrows(NullPointerException.class, () -> openTelemetryTracer.start(null,
            new StartSpanOptions(com.azure.core.util.tracing.SpanKind.CONSUMER), Context.NONE));
        assertThrows(NullPointerException.class, () -> openTelemetryTracer.start("span",
            new StartSpanOptions(com.azure.core.util.tracing.SpanKind.CONSUMER), null));

    }

    @Test
    @SuppressWarnings("deprecation")
    public void addLinkTest() {
        // Arrange
        StartSpanOptions spanBuilder = new StartSpanOptions(com.azure.core.util.tracing.SpanKind.INTERNAL);
        Span toLinkSpan = tracer.spanBuilder("new test span").startSpan();

        Context linkContext = new Context(SPAN_CONTEXT_KEY, toLinkSpan.getSpanContext());
        LinkData expectedLink = LinkData.create(toLinkSpan.getSpanContext());

        // Act
        openTelemetryTracer.addLink(linkContext.addData(SPAN_BUILDER_KEY, spanBuilder));

        Context span = openTelemetryTracer.start(METHOD_NAME, spanBuilder, Context.NONE);
        ReadableSpan span1 = getSpan(span);

        // Assert
        // verify parent span has the expected Link
        LinkData createdLink = span1.toSpanData().getLinks().get(0);
        assertEquals(1, span1.toSpanData().getLinks().size());
        assertEquals(expectedLink.getSpanContext().getTraceId(), createdLink.getSpanContext().getTraceId());
        assertEquals(expectedLink.getSpanContext().getSpanId(), createdLink.getSpanContext().getSpanId());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void addLinkNoSpanContextTest() {
        // Arrange
        SpanBuilder span = tracer.spanBuilder(METHOD_NAME);

        // Act
        openTelemetryTracer.addLink(new Context(SPAN_BUILDER_KEY, span));
        ReadableSpan span1 = (ReadableSpan) span.startSpan();

        // Assert
        // verify no links were added
        assertEquals(span1.toSpanData().getLinks().size(), 0);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void addLinkNoSpanToLinkTest() {
        // Arrange
        SpanBuilder span = tracer.spanBuilder(METHOD_NAME);

        // Act
        openTelemetryTracer.addLink(Context.NONE);
        ReadableSpan span1 = (ReadableSpan) span.startSpan();

        // Assert
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
        final ReadableSpan span = (ReadableSpan) parentSpan;
        final String throwableMessage = "custom error message";

        // Act
        openTelemetryTracer.end(null, new Throwable(throwableMessage), tracingContext);

        // Assert
        assertEquals(StatusCode.ERROR, span.toSpanData().getStatus().getStatusCode());
        assertEquals(throwableMessage, span.toSpanData().getStatus().getDescription());
        assertEquals(Throwable.class.getName(), span.getAttribute(AttributeKey.stringKey("error.type")));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void endSpanTestThrowableResponseCode() {
        // Arrange
        final ReadableSpan span = (ReadableSpan) parentSpan;

        // Act
        openTelemetryTracer.end(404, new Throwable("this is an exception"), tracingContext);

        // Assert
        assertEquals(StatusCode.ERROR, span.toSpanData().getStatus().getStatusCode());
        assertEquals("this is an exception", span.toSpanData().getStatus().getDescription());
        assertEquals(Throwable.class.getName(), span.getAttribute(AttributeKey.stringKey("error.type")));
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
        final ReadableSpan readableSpan = getSpan(spanContext);

        // Act
        openTelemetryTracer.setAttribute(firstKey, firstKeyValue, Context.NONE);

        // Assert
        final Attributes attributeMap = readableSpan.toSpanData().getAttributes();
        assertNull(attributeMap.get(AttributeKey.stringKey(firstKey)));
        assertEquals(attributeMap.size(), 1);
        assertEquals(AZ_NAMESPACE_VALUE, attributeMap.get(AttributeKey.stringKey("az.namespace")));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void extractContextValidDiagnosticId() {
        // Arrange
        String traceparent = "00-bc7293302f5dc6de8a2372491092df95-dfd6fee494751d3f-01";
        String traceId = traceparent.substring(TRACE_ID_OFFSET, TRACE_ID_OFFSET + TraceId.getLength());
        String spanId = traceparent.substring(SPAN_ID_OFFSET, SPAN_ID_OFFSET + SpanId.getLength());

        TraceFlags traceFlags = TraceFlags.fromHex(traceparent, TRACE_OPTION_OFFSET);

        SpanContext validSpanContext
            = SpanContext.createFromRemoteParent(traceId, spanId, traceFlags, TraceState.builder().build());

        // Act
        Context updatedContext = openTelemetryTracer.extractContext(traceparent, Context.NONE);

        // Assert
        Optional<Object> spanContextOptional = updatedContext.getData(SPAN_CONTEXT_KEY);
        assertNotNull(spanContextOptional);
        SpanContext spanContext = (SpanContext) spanContextOptional.get();
        assertEquals(spanContext, validSpanContext);
    }

    @Test
    @SuppressWarnings("deprecation")
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
        Map<String, Object> input = new HashMap<String, Object>() {
            {
                put("attr1", "value1");
                put("attr2", true);
                put("attr3", 1L);
                put("attr4", 2);
                put("attr5", (short) 3);
                put("attr6", (byte) 4);
                put("attr7", 1.0);
                put("attr8", 2F);
                put("attr9", new double[] { 1.0, 2.0, 3.0 });
                put("attr10", new long[] { 1L, 2L, 3L });
                put("attr11", new boolean[] { true });
                put("attr12", null);
            }
        };

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
        assertEquals(8, attributes.size());
        Attributes expectedEventAttrs = Attributes.builder()
            .put(AttributeKey.stringKey("attr1"), "value1")
            .put(AttributeKey.booleanKey("attr2"), true)
            .put(AttributeKey.longKey("attr3"), 1)
            .put(AttributeKey.longKey("attr4"), 2)
            .put(AttributeKey.longKey("attr5"), 3)
            .put(AttributeKey.longKey("attr6"), 4)
            .put(AttributeKey.doubleKey("attr7"), 1.0)
            .put(AttributeKey.doubleKey("attr8"), 2.0)
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
    public void startSpanWithOptionsNameEmptyParent() {
        final StartSpanOptions options = new StartSpanOptions(com.azure.core.util.tracing.SpanKind.INTERNAL);
        final Context started = openTelemetryTracer.start(METHOD_NAME, options,
            new Context(PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.root()));
        final ReadableSpan span = getSpan(started);
        final SpanData spanData = span.toSpanData();

        assertEquals(METHOD_NAME, span.getName());
        assertEquals(SpanKind.INTERNAL, span.getKind());

        assertEquals("0000000000000000", spanData.getParentSpanId());
        assertEquals(1, spanData.getAttributes().size());
        assertEquals(AZ_NAMESPACE_VALUE, spanData.getAttributes().get(AttributeKey.stringKey("az.namespace")));
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
        }
    }

    @Test
    public void startSpanWithOptionsNameExplicitParent() {
        final StartSpanOptions options = new StartSpanOptions(com.azure.core.util.tracing.SpanKind.INTERNAL);

        final Span explicitParentSpan = tracer.spanBuilder("foo").setNoParent().startSpan();
        final Context started = openTelemetryTracer.start(METHOD_NAME, options,
            new Context(PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.root().with(explicitParentSpan)));

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
        attributes.put("S[]", new String[] { "foo" });
        attributes.put("L[]", new long[] { 10L });
        attributes.put("D[]", new double[] { 0.1d });
        attributes.put("B[]", new boolean[] { true });
        attributes.put("I[]", new int[] { 1 });

        final Attributes expectedAttributes = Attributes.builder()
            .put("S", "foo")
            .put("L", 10L)
            .put("I", 1)
            .put("D", 0.1d)
            .put("B", true)
            .put("az.namespace", AZ_NAMESPACE_VALUE)
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
        Context innerNotSuppressed = openTelemetryTracer.start("innerNotSuppressed",
            new StartSpanOptions(com.azure.core.util.tracing.SpanKind.PRODUCER), innerSuppressed);

        openTelemetryTracer.end("ok", null, innerNotSuppressed);
        assertEquals(1, testExporter.getFinishedSpanItems().size());
        openTelemetryTracer.end("ok", null, innerSuppressed);

        assertEquals(1, testExporter.getFinishedSpanItems().size());
        openTelemetryTracer.end("ok", null, outer);

        assertEquals(2, testExporter.getFinishedSpanItems().size());

        SpanData innerNotSuppressedSpan = testExporter.getFinishedSpanItems().get(0);
        SpanData outerSpan = testExporter.getFinishedSpanItems().get(1);
        assertEquals(innerNotSuppressedSpan.getSpanContext().getTraceId(), outerSpan.getSpanContext().getTraceId());
        assertEquals(innerNotSuppressedSpan.getParentSpanId(), outerSpan.getSpanContext().getSpanId());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void suppressNestedInterleavedClientSpan() {
        Context outer = openTelemetryTracer.start("outer", Context.NONE, com.azure.core.util.tracing.ProcessKind.SEND);

        Context inner1NotSuppressed = openTelemetryTracer.start("innerSuppressed", outer);
        Context inner2Suppressed = openTelemetryTracer.start("innerSuppressed", inner1NotSuppressed);
        Context inner2NotSuppressed = openTelemetryTracer.start("innerNotSuppressed",
            new StartSpanOptions(com.azure.core.util.tracing.SpanKind.PRODUCER), inner2Suppressed);

        openTelemetryTracer.end("ok", null, inner2NotSuppressed);
        openTelemetryTracer.end("ok", null, inner2Suppressed);
        openTelemetryTracer.end("ok", null, inner1NotSuppressed);
        openTelemetryTracer.end("ok", null, outer);
        assertEquals(3, testExporter.getFinishedSpanItems().size());

        SpanData inner2 = testExporter.getFinishedSpanItems().get(0);
        SpanData inner1 = testExporter.getFinishedSpanItems().get(1);
        SpanData outerSpan = testExporter.getFinishedSpanItems().get(2);

        assertEquals(inner2.getSpanContext().getTraceId(), inner1.getSpanContext().getTraceId());
        assertEquals(inner2.getParentSpanId(), inner1.getSpanContext().getSpanId());
        assertEquals(inner1.getSpanContext().getTraceId(), outerSpan.getSpanContext().getTraceId());
        assertEquals(inner1.getParentSpanId(), outerSpan.getSpanContext().getSpanId());
    }

    @Test
    public void suppressNestedInterleavedClientSpanWithOptions() {
        Context outer = openTelemetryTracer.start("outer", Context.NONE);

        Context inner1Suppressed = openTelemetryTracer.start("innerSuppressed", outer);
        Context inner1NotSuppressed = openTelemetryTracer.start("innerNotSuppressed",
            new StartSpanOptions(com.azure.core.util.tracing.SpanKind.CLIENT), inner1Suppressed);
        Context inner2Suppressed = openTelemetryTracer.start("innerSuppressed", inner1NotSuppressed);

        openTelemetryTracer.end("ok", null, inner2Suppressed);
        assertEquals(0, testExporter.getFinishedSpanItems().size());

        openTelemetryTracer.end("ok", null, inner1NotSuppressed);
        openTelemetryTracer.end("ok", null, inner1Suppressed);
        openTelemetryTracer.end("ok", null, outer);
        assertEquals(2, testExporter.getFinishedSpanItems().size());

        SpanData innerNotSuppressedSpan = testExporter.getFinishedSpanItems().get(0);
        SpanData outerSpan = testExporter.getFinishedSpanItems().get(1);
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
        assertEquals(0, testExporter.getFinishedSpanItems().size());

        openTelemetryTracer.end("ok", null, outer);
        assertEquals(1, testExporter.getFinishedSpanItems().size());
    }

    @ParameterizedTest
    @MethodSource("spanKinds")
    public void suppressNestedClientSpan(com.azure.core.util.tracing.SpanKind outerKind,
        com.azure.core.util.tracing.SpanKind innerKind, boolean shouldSuppressInner) {
        Context outer = openTelemetryTracer.start("outer", new StartSpanOptions(outerKind), Context.NONE);
        Context inner = openTelemetryTracer.start("inner", new StartSpanOptions(innerKind), outer);
        Context neverSuppressed = openTelemetryTracer.start("innerNotSuppressed",
            new StartSpanOptions(com.azure.core.util.tracing.SpanKind.PRODUCER), inner);

        openTelemetryTracer.end("ok", null, neverSuppressed);
        assertEquals(1, testExporter.getFinishedSpanItems().size());

        openTelemetryTracer.end("ok", null, inner);
        assertEquals(shouldSuppressInner ? 1 : 2, testExporter.getFinishedSpanItems().size());

        openTelemetryTracer.end("ok", null, outer);
        assertEquals(shouldSuppressInner ? 2 : 3, testExporter.getFinishedSpanItems().size());

        SpanData neverSuppressedSpan = testExporter.getFinishedSpanItems().get(0);

        if (shouldSuppressInner) {
            SpanData outerSpan = testExporter.getFinishedSpanItems().get(1);
            assertEquals(neverSuppressedSpan.getParentSpanId(), outerSpan.getSpanContext().getSpanId());
        } else {
            SpanData innerSpan = testExporter.getFinishedSpanItems().get(1);
            SpanData outerSpan = testExporter.getFinishedSpanItems().get(2);
            assertEquals(neverSuppressedSpan.getParentSpanId(), innerSpan.getSpanContext().getSpanId());
            assertEquals(innerSpan.getParentSpanId(), outerSpan.getSpanContext().getSpanId());
        }
    }

    public static Stream<Arguments> spanKinds() {
        return Stream.of(
            Arguments.of(com.azure.core.util.tracing.SpanKind.INTERNAL, com.azure.core.util.tracing.SpanKind.INTERNAL,
                true),
            Arguments.of(com.azure.core.util.tracing.SpanKind.CLIENT, com.azure.core.util.tracing.SpanKind.CLIENT,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.CLIENT, com.azure.core.util.tracing.SpanKind.INTERNAL,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.CLIENT, com.azure.core.util.tracing.SpanKind.PRODUCER,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.CLIENT, com.azure.core.util.tracing.SpanKind.CONSUMER,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.CLIENT, com.azure.core.util.tracing.SpanKind.SERVER,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.INTERNAL, com.azure.core.util.tracing.SpanKind.CLIENT,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.INTERNAL, com.azure.core.util.tracing.SpanKind.PRODUCER,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.INTERNAL, com.azure.core.util.tracing.SpanKind.CONSUMER,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.INTERNAL, com.azure.core.util.tracing.SpanKind.SERVER,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.PRODUCER, com.azure.core.util.tracing.SpanKind.CLIENT,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.PRODUCER, com.azure.core.util.tracing.SpanKind.INTERNAL,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.PRODUCER, com.azure.core.util.tracing.SpanKind.PRODUCER,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.PRODUCER, com.azure.core.util.tracing.SpanKind.CONSUMER,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.PRODUCER, com.azure.core.util.tracing.SpanKind.SERVER,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.CONSUMER, com.azure.core.util.tracing.SpanKind.CLIENT,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.CONSUMER, com.azure.core.util.tracing.SpanKind.INTERNAL,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.CONSUMER, com.azure.core.util.tracing.SpanKind.PRODUCER,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.CONSUMER, com.azure.core.util.tracing.SpanKind.CONSUMER,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.CONSUMER, com.azure.core.util.tracing.SpanKind.SERVER,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.SERVER, com.azure.core.util.tracing.SpanKind.CLIENT,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.SERVER, com.azure.core.util.tracing.SpanKind.INTERNAL,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.SERVER, com.azure.core.util.tracing.SpanKind.PRODUCER,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.SERVER, com.azure.core.util.tracing.SpanKind.CONSUMER,
                false),
            Arguments.of(com.azure.core.util.tracing.SpanKind.SERVER, com.azure.core.util.tracing.SpanKind.SERVER,
                false));
    }

    @Test
    public void suppressNestedClientSpanAttributes() {
        Context outer = openTelemetryTracer.start("outer", Context.NONE);
        openTelemetryTracer.setAttribute("outer1", "foo", outer);

        Context innerSuppressed = openTelemetryTracer.start("innerSuppressed", outer);
        openTelemetryTracer.setAttribute("outer2", "bar", outer);
        openTelemetryTracer.setAttribute("innerSuppressed", "foo", innerSuppressed);

        openTelemetryTracer.end("success", null, innerSuppressed);
        openTelemetryTracer.end("success", null, outer);

        SpanData outerSpan = testExporter.getFinishedSpanItems().get(0);
        assertEquals("outer", outerSpan.getName());

        Map<String, Object> outerAttributesExpected = new HashMap<String, Object>() {
            {
                put("outer1", "foo");
                put("outer2", "bar");
                put("az.namespace", AZ_NAMESPACE_VALUE);
            }
        };

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

        SpanData outerSpan = testExporter.getFinishedSpanItems().get(0);
        assertEquals(2, outerSpan.getEvents().size());
        assertEquals("outer1", outerSpan.getEvents().get(0).getName());
        assertEquals("outer2", outerSpan.getEvents().get(1).getName());
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
        assertTrue(testExporter.getFinishedSpanItems().isEmpty());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void startSendSpanWithoutBuilder() {
        Context outer = openTelemetryTracer.start("outer",
            new StartSpanOptions(com.azure.core.util.tracing.SpanKind.SERVER), Context.NONE);
        Context sendNoBuilder
            = openTelemetryTracer.start("sendNoBuilder", outer, com.azure.core.util.tracing.ProcessKind.SEND);

        assertNotSame(sendNoBuilder, outer);
        openTelemetryTracer.end("ok", null, sendNoBuilder);
        openTelemetryTracer.end("ok", null, outer);

        assertEquals(2, testExporter.getFinishedSpanItems().size());

        SpanData sendNoBuilderSpan = testExporter.getFinishedSpanItems().get(0);
        SpanData outerSpan = testExporter.getFinishedSpanItems().get(1);
        assertEquals(sendNoBuilderSpan.getSpanContext().getTraceId(), outerSpan.getSpanContext().getTraceId());
        assertEquals(sendNoBuilderSpan.getParentSpanId(), outerSpan.getSpanContext().getSpanId());
    }

    @Test
    public void invalidParent() {
        Context explicitInvalidParent = new Context(PARENT_TRACE_CONTEXT_KEY, Span.wrap(SpanContext.getInvalid()));
        Span outer = tracer.spanBuilder("outer").startSpan();
        try (Scope scope = outer.makeCurrent()) {
            Context inner = openTelemetryTracer.start("inner", explicitInvalidParent);
            openTelemetryTracer.end("ok", null, inner);
            outer.end();
        }

        assertEquals(2, testExporter.getFinishedSpanItems().size());

        SpanData innerSpan = testExporter.getFinishedSpanItems().get(0);
        SpanData outerSpan = testExporter.getFinishedSpanItems().get(1);
        assertEquals(innerSpan.getSpanContext().getTraceId(), outerSpan.getSpanContext().getTraceId());
        assertEquals(innerSpan.getParentSpanId(), outerSpan.getSpanContext().getSpanId());
    }

    @Test
    public void invalidRemoteParent() {
        Context explicitInvalidParent = new Context(SPAN_CONTEXT_KEY, SpanContext.getInvalid());
        Span outer = tracer.spanBuilder("outer").startSpan();
        try (Scope scope = outer.makeCurrent()) {
            StartSpanOptions options = new StartSpanOptions(com.azure.core.util.tracing.SpanKind.CONSUMER)
                .setRemoteParent(explicitInvalidParent);
            Context inner = openTelemetryTracer.start("inner", options, Context.NONE);
            openTelemetryTracer.end("ok", null, inner);
            outer.end();
        }

        assertEquals(2, testExporter.getFinishedSpanItems().size());

        SpanData innerSpan = testExporter.getFinishedSpanItems().get(0);
        assertFalse(innerSpan.getParentSpanContext().isValid());
    }

    @Test
    public void setStatusSuccess() {
        final Context span = openTelemetryTracer.start(METHOD_NAME, tracingContext);
        openTelemetryTracer.end(null, null, span);

        SpanData spanData = getSpan(span).toSpanData();
        assertEquals(UNSET, spanData.getStatus().getStatusCode());
    }

    @Test
    public void setStatusErrorMessage() {
        final Context span = openTelemetryTracer.start(METHOD_NAME, tracingContext);
        openTelemetryTracer.end("foo", null, span);

        SpanData spanData = getSpan(span).toSpanData();
        assertEquals(ERROR, spanData.getStatus().getStatusCode());
        assertEquals("foo", spanData.getAttributes().get(AttributeKey.stringKey("error.type")));
        assertEquals("foo", spanData.getStatus().getDescription());
    }

    @Test
    public void setStatusErrorMessageNoDescription() {
        final Context span = openTelemetryTracer.start(METHOD_NAME, tracingContext);
        openTelemetryTracer.end("", null, span);

        SpanData spanData = getSpan(span).toSpanData();
        assertEquals(ERROR, spanData.getStatus().getStatusCode());
        assertEquals("", spanData.getAttributes().get(AttributeKey.stringKey("error.type")));
        assertEquals("", spanData.getStatus().getDescription());
    }

    @ParameterizedTest
    @MethodSource("exceptions")
    public void setStatusThrowable(Throwable error, Throwable originalError) {
        final Context span = openTelemetryTracer.start(METHOD_NAME, tracingContext);
        openTelemetryTracer.end(null, error, span);

        SpanData spanData = getSpan(span).toSpanData();
        assertEquals(ERROR, spanData.getStatus().getStatusCode());
        assertEquals(originalError.getMessage(), spanData.getStatus().getDescription());
        assertEquals(0, spanData.getEvents().size());
        assertEquals(originalError.getClass().getName(),
            spanData.getAttributes().get(AttributeKey.stringKey("error.type")));
    }

    @Test
    public void setErrorThrowableAndStatusMessage() {
        final Context span = openTelemetryTracer.start(METHOD_NAME, tracingContext);
        Throwable error = new IOException("bar");
        openTelemetryTracer.end("foo", error, span);

        SpanData spanData = getSpan(span).toSpanData();
        assertEquals(ERROR, spanData.getStatus().getStatusCode());
        assertEquals("bar", spanData.getStatus().getDescription());
        assertEquals("foo", spanData.getAttributes().get(AttributeKey.stringKey("error.type")));
    }

    @Test
    public void getInvalidSpanContext() {
        // Act
        Context context = openTelemetryTracer.extractContext(name -> "");

        // Assert
        assertNotNull(context);
        assertTrue(context.getData(SPAN_CONTEXT_KEY).isPresent());
        assertFalse(((SpanContext) context.getData(SPAN_CONTEXT_KEY).get()).isValid(),
            "Invalid diagnostic Id, returns invalid SpanContext ");
    }

    @Test
    public void getValidSpanContext() {
        // Act
        Context context = openTelemetryTracer.extractContext(
            name -> "traceparent".equals(name) ? "00-0af7651916cd43dd8448eb211c80319c-b9c7c989f97918e1-01" : null);

        // Assert
        assertNotNull(context);
        assertTrue(((SpanContext) context.getData(SPAN_CONTEXT_KEY).get()).isValid(),
            "Valid diagnostic Id, returns valid SpanContext ");
    }

    @Test
    public void getValidDiagnosticId() {
        // Act
        Context context = openTelemetryTracer.extractContext(
            name -> "Diagnostic-Id".equals(name) ? "00-0af7651916cd43dd8448eb211c80319c-b9c7c989f97918e1-01" : null);

        // Assert
        assertNotNull(context);
        assertTrue(((SpanContext) context.getData(SPAN_CONTEXT_KEY).get()).isValid(),
            "Valid diagnostic Id, returns valid SpanContext ");
    }

    private static ReadableSpan getSpan(Context context) {
        Optional<Object> otelCtx = context.getData(PARENT_TRACE_CONTEXT_KEY);
        assertTrue(otelCtx.isPresent());
        assertTrue(io.opentelemetry.context.Context.class.isAssignableFrom(otelCtx.get().getClass()));
        Span span = Span.fromContext((io.opentelemetry.context.Context) otelCtx.get());
        assertTrue(span.getSpanContext().isValid());
        assertTrue(ReadableSpan.class.isAssignableFrom(span.getClass()));

        return (ReadableSpan) span;
    }

    private static ReadableSpan assertSpanWithExplicitParent(Context context, String parentSpanId) {
        final ReadableSpan span = getSpan(context);

        assertEquals(METHOD_NAME, span.getName());

        // verify span started with explicit parent
        assertFalse(span.toSpanData().getParentSpanContext().isRemote());
        assertEquals(parentSpanId, span.toSpanData().getParentSpanId());
        return span;
    }

    private static ReadableSpan assertSpanWithRemoteParent(Context context, String parentSpanId) {
        final ReadableSpan span = getSpan(context);

        assertEquals(METHOD_NAME, span.getName());
        assertEquals(SpanKind.CONSUMER, span.toSpanData().getKind());

        // verify span started with remote parent
        assertTrue(span.toSpanData().getParentSpanContext().isRemote());
        assertEquals(parentSpanId, span.toSpanData().getParentSpanId());
        return span;
    }

    private static void verifySpanAttributes(Map<String, Object> expectedMap, Attributes actualAttributeMap) {
        assertEquals(expectedMap.size(), actualAttributeMap.size());

        actualAttributeMap.forEach(
            (attributeKey, attributeValue) -> assertEquals(expectedMap.get(attributeKey.getKey()), attributeValue));
    }

    private static void verifySpanAttributes(Attributes expected, Attributes actual) {
        assertEquals(expected.size(), actual.size());

        assertTrue(expected.asMap().entrySet().stream().allMatch(e -> e.getValue().equals(actual.get(e.getKey()))));
    }

    public static Stream<Arguments> exceptions() {
        IOException rootCause = new IOException("foo");
        return Stream.of(Arguments.of(rootCause, rootCause), Arguments.of(Exceptions.propagate(rootCause), rootCause),
            Arguments.of(new UncheckedIOException(rootCause), rootCause),
            Arguments.of(Exceptions.propagate(new UncheckedIOException(rootCause)), rootCause),
            Arguments.of(new ExecutionException(rootCause), rootCause),
            Arguments.of(new InvocationTargetException(rootCause), rootCause),
            Arguments.of(new InvocationTargetException(rootCause), rootCause),
            Arguments.of(new UndeclaredThrowableException(rootCause), rootCause),
            Arguments.of(
                new UndeclaredThrowableException(new InvocationTargetException(Exceptions.propagate(rootCause))),
                rootCause));
    }
}
