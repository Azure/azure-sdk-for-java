// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.ProcessKind;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Tracer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static com.azure.core.tracing.opentelemetry.OpenTelemetryTracer.COMPONENT;
import static com.azure.core.tracing.opentelemetry.OpenTelemetryTracer.MESSAGE_BUS_DESTINATION;
import static com.azure.core.tracing.opentelemetry.OpenTelemetryTracer.PEER_ENDPOINT;
import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;

/**
 * Tests Azure-OpenTelemetry tracing package using opentelemetry-sdk
 */
public class OpenTelemetryTracerTest {
    private static final String METHOD_NAME = "Azure.eventhubs.send";
    private static final String HOSTNAME_VALUE = "testEventDataNameSpace.servicebus.windows.net";
    private static final String ENTITY_PATH_VALUE = "test";
    private static final String COMPONENT_VALUE = "eventhubs";
    private OpenTelemetryTracer openTelemetryTracer;
    private Tracer tracer;
    private Context tracingContext;
    private Span parentSpan;

    @Before
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
        openTelemetryTracer.start("", null);
    }

    @Test
    public void startSpanParentContextFlowTest() {
        // Arrange
        final SpanId parentSpanId = parentSpan.getContext().getSpanId();

        // Act
        final Context updatedContext = openTelemetryTracer.start(METHOD_NAME, tracingContext);

        // Assert
        assertSpanWithExplicitParent(updatedContext, parentSpanId);
        final ReadableSpan recordEventsSpan =
            (ReadableSpan) updatedContext.getData(PARENT_SPAN_KEY).get();
        Assert.assertEquals(Span.Kind.INTERNAL, recordEventsSpan.toSpanData().getKind());
        Assert.assertEquals(true, true);
    }

    @Test
    public void startSpanTestNoUserParent() {
        // Act
        final Context updatedContext = openTelemetryTracer.start(METHOD_NAME, Context.NONE);

        // Assert
        Assert.assertNotNull(updatedContext.getData(PARENT_SPAN_KEY));

        //verify still get a valid span implementation
        Assert.assertTrue(updatedContext.getData(PARENT_SPAN_KEY).get() instanceof ReadableSpan);
        final ReadableSpan recordEventsSpan =
            (ReadableSpan) updatedContext.getData(PARENT_SPAN_KEY).get();

        Assert.assertEquals(METHOD_NAME, recordEventsSpan.getName());
        Assert.assertFalse(recordEventsSpan.getSpanContext().isRemote());
        Assert.assertNotNull(recordEventsSpan.toSpanData().getParentSpanId());
    }

    @Test
    public void startSpanProcessKindSend() {
        // Arrange
        final SpanId parentSpanId = parentSpan.getContext().getSpanId();
        // Add additional metadata to spans for SEND
        final Context traceContext = tracingContext.addData(ENTITY_PATH_KEY, ENTITY_PATH_VALUE)
            .addData(HOST_NAME_KEY, HOSTNAME_VALUE);

        // Act
        final Context updatedContext = openTelemetryTracer.start(METHOD_NAME, traceContext, ProcessKind.SEND);

        // Assert
        // verify span created with explicit parent when for Process Kind SEND
        assertSpanWithExplicitParent(updatedContext, parentSpanId);
        final ReadableSpan recordEventsSpan =
            (ReadableSpan) updatedContext.getData(PARENT_SPAN_KEY).get();
        Assert.assertEquals(Span.Kind.PRODUCER, recordEventsSpan.toSpanData().getKind());

        // verify span attributes
        final Map<String, AttributeValue> attributeMap = recordEventsSpan.toSpanData().getAttributes();
        Assert.assertEquals(attributeMap.get(COMPONENT), AttributeValue.stringAttributeValue(COMPONENT_VALUE));
        Assert.assertEquals(attributeMap.get(MESSAGE_BUS_DESTINATION),
            AttributeValue.stringAttributeValue(ENTITY_PATH_VALUE));
        Assert.assertEquals(attributeMap.get(PEER_ENDPOINT), AttributeValue.stringAttributeValue(HOSTNAME_VALUE));
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
        Assert.assertEquals(Span.Kind.INTERNAL, recordEventsSpan.toSpanData().getKind());
        // verify diagnostic id and span context returned
        Assert.assertNotNull(updatedContext.getData(SPAN_CONTEXT_KEY).get());
        Assert.assertNotNull(updatedContext.getData(DIAGNOSTIC_ID_KEY).get());
    }

    @Test
    public void startSpanProcessKindProcess() {
        // Arrange
        final SpanId parentSpanId = parentSpan.getContext().getSpanId();

        // Act
        final Context updatedContext = openTelemetryTracer.start(METHOD_NAME, tracingContext, ProcessKind.PROCESS);

        // verify no parent span passed
        Assert.assertFalse("When no parent span passed in context information",
            tracingContext.getData(SPAN_CONTEXT_KEY).isPresent());
        // verify span created with explicit parent
        assertSpanWithExplicitParent(updatedContext, parentSpanId);
        // verify scope returned
        Assert.assertNotNull(updatedContext.getData("scope").get());
        final ReadableSpan recordEventsSpan =
            (ReadableSpan) updatedContext.getData(PARENT_SPAN_KEY).get();
        Assert.assertEquals(Span.Kind.SERVER, recordEventsSpan.toSpanData().getKind());
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
        Assert.assertNotNull(updatedContext.getData("scope").get());
        // Assert new span created with remote parent context
        assertSpanWithRemoteParent(updatedContext, testSpanId);
    }

    @Test(expected = NullPointerException.class)
    public void startSpanOverloadNullPointerException() {
        // Act
        openTelemetryTracer.start("", Context.NONE, null);
    }

    // TODO: Fix links for openTelemetry
    // @Test
    // public void addLinkTest() {
    //     // Arrange
    //     // Create a child-parent link between multiple spans
    //     final ReadableSpan testSpan =
    //         (ReadableSpan) tracer.spanBuilder("new-test-span").startSpan();
    //     final Context traceContext = tracingContext.addData(SPAN_CONTEXT_KEY, testSpan.getSpanContext());
    //     final ReadableSpan parentSpanImpl = (ReadableSpan) parentSpan;
    //     SpanData.Link expectedLink = SpanData.Link.create(testSpan.getSpanContext());
    //     // Act
    //     openTelemetryTracer.addLink(traceContext);
    //
    //     //Assert
    //     // verify parent span has the expected Link
    //     Assert.assertEquals(1, parentSpanImpl.toSpanData().getLinks().size());
    //     Link createdLink = parentSpanImpl.toSpanData().getLinks().get(0);
    //     Assert.assertEquals(expectedLink.getContext().getTraceId(), createdLink.getContext().getTraceId());
    //     Assert.assertEquals(expectedLink.getContext().getTraceId(), createdLink.getContext().getTraceId());
    // }

    @Test
    public void endSpanNoSuccessErrorMessageTest() {
        // Arrange
        final ReadableSpan recordEventsSpan = (ReadableSpan) tracer.getCurrentSpan();
        final String expectedStatus = "UNKNOWN";

        // Act
        openTelemetryTracer.end(null, null, tracingContext);

        // Assert
        Assert.assertEquals(expectedStatus, recordEventsSpan.toSpanData().getStatus().getCanonicalCode().toString());
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
        Assert.assertEquals(expectedStatus, recordEventsSpan.toSpanData().getStatus().getCanonicalCode().toString());
        Assert.assertEquals(throwableMessage, recordEventsSpan.toSpanData().getStatus().getDescription());
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
        Assert.assertEquals(expectedStatus, recordEventsSpan.toSpanData().getStatus().getCanonicalCode().toString());
        Assert.assertEquals(throwableMessage, recordEventsSpan.toSpanData().getStatus().getDescription());

    }

    private static void assertSpanWithExplicitParent(Context updatedContext, SpanId parentSpanId) {
        Assert.assertNotNull(updatedContext.getData(PARENT_SPAN_KEY));

        // verify instance created of openTelemetry-impl (test impl), span implementation
        Assert.assertTrue(updatedContext.getData(PARENT_SPAN_KEY).get() instanceof ReadableSpan);

        final ReadableSpan recordEventsSpan =
            (ReadableSpan) updatedContext.getData(PARENT_SPAN_KEY).get();
        Assert.assertEquals(METHOD_NAME, recordEventsSpan.getName());

        // verify span started with explicit parent
        Assert.assertFalse(recordEventsSpan.toSpanData().getHasRemoteParent());
        Assert.assertEquals(parentSpanId, recordEventsSpan.toSpanData().getParentSpanId());
    }

    private static void assertSpanWithRemoteParent(Context updatedContext, SpanId parentSpanId) {
        Assert.assertNotNull(updatedContext.getData(PARENT_SPAN_KEY));

        // verify instance created of opentelemetry-sdk (test impl), span implementation
        Assert.assertTrue(updatedContext.getData(PARENT_SPAN_KEY).get() instanceof ReadableSpan);

        // verify span created with provided name and kind server
        final ReadableSpan recordEventsSpan =
            (ReadableSpan) updatedContext.getData(PARENT_SPAN_KEY).get();
        Assert.assertEquals(METHOD_NAME, recordEventsSpan.getName());
        Assert.assertEquals(Span.Kind.SERVER, recordEventsSpan.toSpanData().getKind());

        // verify span started with remote parent
        Assert.assertTrue(recordEventsSpan.toSpanData().getHasRemoteParent());
        Assert.assertEquals(parentSpanId, recordEventsSpan.toSpanData().getParentSpanId());
    }
}
