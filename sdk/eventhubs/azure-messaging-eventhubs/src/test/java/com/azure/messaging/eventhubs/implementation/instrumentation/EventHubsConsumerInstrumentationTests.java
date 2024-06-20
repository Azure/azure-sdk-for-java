// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.instrumentation;

import com.azure.core.amqp.AmqpMessageConstant;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.core.test.utils.metrics.TestCounter;
import com.azure.core.test.utils.metrics.TestHistogram;
import com.azure.core.test.utils.metrics.TestMeasurement;
import com.azure.core.test.utils.metrics.TestMeter;
import com.azure.core.tracing.opentelemetry.OpenTelemetryTracingOptions;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.SampleCheckpointStore;
import com.azure.messaging.eventhubs.TestSpanProcessor;
import com.azure.messaging.eventhubs.TestUtils;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.ContextView;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.messaging.eventhubs.TestUtils.assertAllAttributes;
import static com.azure.messaging.eventhubs.TestUtils.assertSpanStatus;
import static com.azure.messaging.eventhubs.TestUtils.attributesToMap;
import static com.azure.messaging.eventhubs.TestUtils.getSpanName;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.PROCESS;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.RECEIVE;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.CHECKPOINT;
import static io.opentelemetry.api.trace.SpanKind.CONSUMER;
import static io.opentelemetry.api.trace.SpanKind.INTERNAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventHubsConsumerInstrumentationTests {
    private static final String FQDN = "fqdn";
    private static final String ENTITY_NAME = "entityName";
    private static final String CONSUMER_GROUP = "consumerGroup";
    private static final String TRACEPARENT1 = "00-1123456789abcdef0123456789abcdef-0123456789abcdef-01";
    private static final String TRACEID1 = TRACEPARENT1.substring(3, 35);
    private static final String SPANID1 = TRACEPARENT1.substring(36, 52);
    private static final String TRACEPARENT2 = "00-2123456789abcdef0123456789abcdef-0123456789abcdef-01";
    private static final String TRACEPARENT3 = "00-3123456789abcdef0123456789abcdef-0123456789abcdef-01";
    private Tracer tracer;
    private TestMeter meter;
    private TestSpanProcessor spanProcessor;

    private CheckpointStore checkpointStore;
    @BeforeEach
    public void setup(TestInfo testInfo) {
        spanProcessor = new TestSpanProcessor(FQDN, ENTITY_NAME, testInfo.getDisplayName());
        OpenTelemetry otel = OpenTelemetrySdk.builder()
                .setTracerProvider(
                        SdkTracerProvider.builder()
                                .addSpanProcessor(spanProcessor)
                                .build())
                .build();

        TracingOptions tracingOptions = new OpenTelemetryTracingOptions().setOpenTelemetry(otel);
        tracer = TracerProvider.getDefaultProvider()
                .createTracer("test", null, "Microsoft.EventHub", tracingOptions);
        meter = new TestMeter();
        checkpointStore = new SampleCheckpointStore();
    }

    @AfterEach
    public void teardown() {
        spanProcessor.shutdown();
        spanProcessor.close();
        meter.close();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @SuppressWarnings("try")
    public void startAsyncConsumeDisabledInstrumentation(boolean sync) {
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(null, null,
                FQDN, ENTITY_NAME, CONSUMER_GROUP, sync);

        try (InstrumentationScope scope =
                     instrumentation.startAsyncConsume(createMessage(Instant.now()), "0")) {
            assertNull(scope.getStartTime());
        }

        TestHistogram lag = meter.getHistograms().get("messaging.eventhubs.consumer.lag");
        assertNull(lag);
        assertEquals(0, spanProcessor.getEndedSpans().size());
    }

    @Test
    @SuppressWarnings("try")
    public void startAsyncConsumeSyncReportsLag() {
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(tracer, meter,
                FQDN, ENTITY_NAME, CONSUMER_GROUP, true);

        int measurements = 3;
        Integer[] lags = new Integer[]{ -10, 0, 10};
        Integer[] expectedLags = new Integer[]{ 10, 0, 0 };
        String[] partitionIds = new String[]{"1", "2", "3"};

        for (int i = 0; i < measurements; i++) {
            try (InstrumentationScope scope =
                         instrumentation.startAsyncConsume(createMessage(Instant.now().plusSeconds(lags[i])), partitionIds[i])) {
                // lag is reported about received message and don't need to report processing errors
                // so those will be ignored
                if (i == 0) {
                    scope.setCancelled();
                } else if (i == 1) {
                    scope.setError(new RuntimeException("error"));
                }
            }
        }

        TestHistogram lag = meter.getHistograms().get("messaging.eventhubs.consumer.lag");
        assertNotNull(lag);
        assertEquals(measurements, lag.getMeasurements().size());
        for (int i = 0; i < measurements; i++) {
            assertEquals(expectedLags[i], lag.getMeasurements().get(i).getValue(), 10);
            assertAllAttributes(FQDN, ENTITY_NAME, partitionIds[i], CONSUMER_GROUP, null, null,
                    lag.getMeasurements().get(i).getAttributes());
        }

        // sync consumer reports spans in different instrumentation point
        assertEquals(0, spanProcessor.getEndedSpans().size());

        assertEquals(0, meter.getHistograms().get("messaging.process.duration").getMeasurements().size());
    }

    @Test
    @SuppressWarnings("try")
    public void startAsyncConsumeAsyncReportsLagAndSpans() {
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(tracer, meter,
                FQDN, ENTITY_NAME, CONSUMER_GROUP, false);

        int measurements = 3;
        Integer[] lags = new Integer[]{ -10, 0, 10};
        String[] partitionIds = new String[]{"1", "2", "3"};
        String[] expectedErrors = new String[3];
        for (int i = 0; i < measurements; i++) {
            try (InstrumentationScope scope =
                         instrumentation.startAsyncConsume(createMessage(Instant.now().plusSeconds(lags[i])), partitionIds[i])) {
                // lag is reported about received message and don't need to report processing errors
                // so those will be ignored on lag, but will be reflected on processing spans
                if (i == 0) {
                    expectedErrors[i] = "cancelled";
                    scope.setCancelled();
                } else if (i == 1) {
                    expectedErrors[i] = RuntimeException.class.getName();
                    scope.setError(new RuntimeException("test"));
                }

                assertTrue(scope.getStartTime().toEpochMilli() <= Instant.now().toEpochMilli());
                assertTrue(Span.current().getSpanContext().isValid());
            }
        }

        assertEquals(measurements, spanProcessor.getEndedSpans().size());
        for (int i = 0; i < measurements; i++) {
            SpanData span = spanProcessor.getEndedSpans().get(i).toSpanData();
            assertEquals(getSpanName(PROCESS, ENTITY_NAME), span.getName());
            Map<String, Object> attributes = attributesToMap(span.getAttributes());
            assertAllAttributes(FQDN, ENTITY_NAME, partitionIds[i], CONSUMER_GROUP, expectedErrors[i],
                PROCESS, attributes);
            assertNotNull(attributes.get("messaging.eventhubs.message.enqueued_time"));
            assertSpanStatus(i == 0 ? "cancelled" : i == 1 ? "test" : null, span);
            assertProcessDuration(null, partitionIds[i], expectedErrors[i]);
        }
    }

    @Test
    @SuppressWarnings("try")
    public void asyncConsumerSpansHaveLinks() throws InterruptedException {
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(tracer, meter,
                FQDN, ENTITY_NAME, CONSUMER_GROUP, false);

        Instant enqueuedTime = Instant.now();
        Message message = createMessage(enqueuedTime, TRACEPARENT1);
        int durationMillis = 100;
        try (InstrumentationScope scope = instrumentation.startAsyncConsume(message, "0")) {
            Thread.sleep(100);
            assertEquals(TRACEID1, Span.current().getSpanContext().getTraceId());
        }

        // async consumer should report spans
        assertEquals(1, spanProcessor.getEndedSpans().size());
        SpanData span = spanProcessor.getEndedSpans().get(0).toSpanData();
        assertEquals(getSpanName(PROCESS, ENTITY_NAME), span.getName());
        assertEquals(CONSUMER, span.getKind());
        assertEquals(SPANID1, span.getParentSpanId());
        assertTrue(span.getEndEpochNanos() - span.getStartEpochNanos() >= durationMillis * 1_000_000);

        Map<String, Object> attributes = attributesToMap(span.getAttributes());
        assertAllAttributes(FQDN, ENTITY_NAME, "0", CONSUMER_GROUP, null, PROCESS, attributes);
        assertEquals(enqueuedTime.getEpochSecond(), attributes.get("messaging.eventhubs.message.enqueued_time"));

        assertTrue(span.getLinks().get(0).getSpanContext().isValid());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @SuppressWarnings("try")
    public void syncReceiveDisabledInstrumentation(boolean sync) {
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(null, null,
                FQDN, ENTITY_NAME, CONSUMER_GROUP, sync);

        Flux<PartitionEvent> events = Flux.just(createPartitionEvent(Instant.now(), null, "0"));
        instrumentation.syncReceive(events, "0");

        TestHistogram lag = meter.getHistograms().get("messaging.eventhubs.consumer.lag");
        assertNull(lag);
        assertEquals(0, spanProcessor.getEndedSpans().size());
    }

    public static Stream<Arguments> syncReceiveErrors() {
        return Stream.of(
                Arguments.of(false, null, null, null),
                Arguments.of(true, null, "cancelled", "cancelled"),
                Arguments.of(false, new RuntimeException("test"), RuntimeException.class.getName(), "test"),
                Arguments.of(false, Exceptions.propagate(new RuntimeException("test")), RuntimeException.class.getName(), "test")
        );
    }

    @ParameterizedTest
    @MethodSource("syncReceiveErrors")
    @SuppressWarnings("try")
    public void syncReceiveOneEvent(boolean cancel, Throwable error, String expectedErrorType, String spanDescription) {
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(tracer, meter,
                FQDN, ENTITY_NAME, CONSUMER_GROUP, false);

        String partitionId = "0";
        Flux<PartitionEvent> events = Flux.just(createPartitionEvent(Instant.now(), null, partitionId))
                .flatMap(e -> error == null ? Mono.just(e) : Mono.error(error));

        StepVerifier.Step<PartitionEvent> stepVerifier = StepVerifier.create(instrumentation.syncReceive(events, partitionId));

        if (cancel) {
            stepVerifier.thenCancel().verify();
        } else if (error != null) {
            stepVerifier.expectErrorMessage(error.getMessage()).verify();
        } else {
            stepVerifier
                    .expectNextCount(1)
                    .expectComplete()
                    .verify();
        }

        assertOperationDuration(RECEIVE, partitionId, expectedErrorType);
        assertConsumedCount(expectedErrorType == null ? 1 : 0, partitionId, null, RECEIVE);
        assertReceiveSpan(expectedErrorType == null ? 1 : 0, partitionId, expectedErrorType, spanDescription);
    }

    @ParameterizedTest
    @MethodSource("syncReceiveErrors")
    @SuppressWarnings("try")
    public void syncReceiveBatchEvent(boolean cancel, Throwable error, String expectedErrorType, String spanDescription) {
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(tracer, meter,
                FQDN, ENTITY_NAME, CONSUMER_GROUP, false);

        String partitionId = "1";

        int count = 3;
        Flux<PartitionEvent> events = Flux.just(
                createPartitionEvent(Instant.now(), TRACEPARENT1, partitionId),
                createPartitionEvent(Instant.now(), TRACEPARENT2, partitionId),
                createPartitionEvent(Instant.now(), TRACEPARENT3, partitionId));

        if (error != null) {
            events = events.concatWith(Mono.error(error));
        }

        StepVerifier.Step<PartitionEvent> stepVerifier = StepVerifier.create(instrumentation.syncReceive(events, partitionId))
                .expectNextCount(count);

        if (cancel) {
            stepVerifier
                    .thenCancel().verify();
        } else if (error != null) {
            stepVerifier
                    .expectErrorMessage(error.getMessage())
                    .verify();
        } else {
            stepVerifier
                    .expectComplete()
                    .verify();
        }

        assertOperationDuration(RECEIVE, partitionId, expectedErrorType);
        assertConsumedCount(count, partitionId, null, RECEIVE);
        SpanData span = assertReceiveSpan(count, partitionId, expectedErrorType, spanDescription);
        assertEquals(count, span.getLinks().size());
        for (int j = 0; j < count; j++) {
            LinkData link = span.getLinks().get(j);
            assertTrue(link.getSpanContext().isValid());
            assertNotNull(link.getAttributes().get(AttributeKey.longKey("messaging.eventhubs.message.enqueued_time")));
        }
    }

    public static Stream<Arguments> processErrors() {
        AmqpException amqpException = new AmqpException(false, AmqpErrorCondition.SERVER_BUSY_ERROR, null, new RuntimeException("test"), null);
        return Stream.of(
                Arguments.of(null, null, null),
                Arguments.of(new RuntimeException("test"), RuntimeException.class.getName(), "test"),
                Arguments.of(amqpException, amqpException.getErrorCondition().getErrorCondition(), "test"),
                Arguments.of(Exceptions.propagate(new RuntimeException("test")), RuntimeException.class.getName(), "test")
        );
    }

    @ParameterizedTest
    @MethodSource("processErrors")
    @SuppressWarnings("try")
    public void processOneEvent(Throwable error, String expectedErrorType, String spanDescription) throws InterruptedException {
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(tracer, meter,
                FQDN, ENTITY_NAME, CONSUMER_GROUP, false);

        String partitionId = "0";
        try (InstrumentationScope scope = instrumentation.startProcess(createEventContext(Instant.now(), TRACEPARENT1, partitionId))) {
            scope.setError(error);
            Thread.sleep(200);
        }

        assertProcessDuration(Duration.ofMillis(200), partitionId, expectedErrorType);
        assertConsumedCount(1, partitionId, expectedErrorType, PROCESS);
        SpanData span = assertProcessSpan(partitionId, expectedErrorType, spanDescription);
        assertNull(span.getAttributes().get(AttributeKey.longKey("messaging.batch.message_count")));
        assertNotNull(span.getAttributes().get(AttributeKey.longKey("messaging.eventhubs.message.enqueued_time")));

        assertEquals(1, span.getLinks().size());
        LinkData link = span.getLinks().get(0);
        assertTrue(link.getSpanContext().isValid());
    }

    @ParameterizedTest
    @MethodSource("processErrors")
    @SuppressWarnings("try")
    public void processBatch(Throwable error, String expectedErrorType, String spanDescription) throws InterruptedException {
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(tracer, meter,
                FQDN, ENTITY_NAME, CONSUMER_GROUP, false);

        String partitionId = "1";

        int count = 3;
        List<EventData> events = Arrays.asList(
                createEventData(Instant.now(), TRACEPARENT1),
                createEventData(Instant.now(), TRACEPARENT2),
                createEventData(Instant.now(), TRACEPARENT3));
        PartitionContext partitionContext = new PartitionContext(FQDN, ENTITY_NAME, CONSUMER_GROUP, partitionId);
        EventBatchContext batchContext = new EventBatchContext(partitionContext, events, checkpointStore, null);

        try (InstrumentationScope scope = instrumentation.startProcess(batchContext)) {
            scope.setError(error);
            Thread.sleep(200);
        }

        assertProcessDuration(Duration.ofMillis(200), partitionId, expectedErrorType);
        assertConsumedCount(3, partitionId, expectedErrorType, PROCESS);
        SpanData span = assertProcessSpan(partitionId, expectedErrorType, spanDescription);
        assertEquals(count, span.getAttributes().get(AttributeKey.longKey("messaging.batch.message_count")));
        assertNull(span.getAttributes().get(AttributeKey.longKey("messaging.eventhubs.message.enqueued_time")));

        assertEquals(count, span.getLinks().size());
        for (int j = 0; j < count; j++) {
            LinkData link = span.getLinks().get(j);
            assertTrue(link.getSpanContext().isValid());
            assertNotNull(link.getAttributes().get(AttributeKey.longKey("messaging.eventhubs.message.enqueued_time")));
        }
    }

    public static Stream<Arguments> checkpointErrors() {
        return Stream.of(
                Arguments.of(false, null, null, null),
                Arguments.of(true, null, "cancelled", "cancelled"),
                Arguments.of(false, new RuntimeException("test"), RuntimeException.class.getName(), "test"),
                Arguments.of(false, Exceptions.propagate(new RuntimeException("test")), RuntimeException.class.getName(), "test")
        );
    }

    @Test
    public void checkpointWithDisabledInstrumentation() {
        CheckpointStore inner = new SampleCheckpointStore();

        EventHubsConsumerInstrumentation disabled = new EventHubsConsumerInstrumentation(null, null,
                FQDN, ENTITY_NAME, CONSUMER_GROUP, false);

        CheckpointStore instrumented = InstrumentedCheckpointStore.create(inner, disabled);
        assertSame(instrumented, inner);
    }

    @ParameterizedTest
    @MethodSource("checkpointErrors")
    @SuppressWarnings("try")
    public void checkpoint(boolean cancel, Throwable error, String expectedErrorType, String spanDescription) {
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(tracer, meter,
                FQDN, ENTITY_NAME, CONSUMER_GROUP, false);

        String partitionId = "0";

        CheckpointStore store = InstrumentedCheckpointStore.create(
                new TestCheckpointStore(i -> error == null ? i : Mono.error(error)),
                instrumentation);

        StepVerifier.FirstStep<Void> stepVerifier =
                StepVerifier.create(store.updateCheckpoint(createCheckpoint(partitionId)));

        if (cancel) {
            stepVerifier.thenCancel().verify();
        } else if (error != null) {
            stepVerifier.expectErrorMessage(error.getMessage()).verify();
        } else {
            stepVerifier.expectComplete().verify();
        }

        assertOperationDuration(CHECKPOINT, partitionId, expectedErrorType);
        assertCheckpointSpan(partitionId, expectedErrorType, spanDescription);
    }

    @Test
    @SuppressWarnings("try")
    public void checkpointPassesContextToDownstream() {
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(tracer, meter,
                FQDN, ENTITY_NAME, CONSUMER_GROUP, false);

        String partitionId = "0";

        AtomicReference<ContextView> capturedContext = new AtomicReference<>();

        CheckpointStore store = InstrumentedCheckpointStore.create(
                new TestCheckpointStore(inner -> Mono.deferContextual(ctx -> {
                    capturedContext.set(ctx);
                    return inner;
                })),
                instrumentation);

        StepVerifier.create(store.updateCheckpoint(createCheckpoint(partitionId))
                        .contextWrite(ctx -> ctx.put("foo", "bar")))
            .expectComplete()
            .verify();

        // testing internal details - we should have trace-context key with otel span in the context
        // and also client-method-call-flag set by azure-core OtelTracer used to suppress nested spans
        assertTrue((Boolean) capturedContext.get().get("client-method-call-flag"));
        assertInstanceOf(io.opentelemetry.context.Context.class, capturedContext.get().get("trace-context"));
        assertEquals("bar", capturedContext.get().get("foo"));

        assertOperationDuration(CHECKPOINT, partitionId, null);
        assertCheckpointSpan(partitionId, null, null);
    }

    private static Checkpoint createCheckpoint(String partitionId) {
        return new Checkpoint()
                .setFullyQualifiedNamespace(FQDN)
                .setEventHubName(ENTITY_NAME)
                .setSequenceNumber(1L)
                .setPartitionId(partitionId)
                .setOffset(2L)
                .setConsumerGroup(CONSUMER_GROUP);
    }
    private static Message createMessage(Instant enqueuedTime) {
        Message message = Message.Factory.create();
        message.setMessageAnnotations(new MessageAnnotations(Collections.singletonMap(Symbol.getSymbol(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue()), enqueuedTime)));
        message.setApplicationProperties(new ApplicationProperties(Collections.emptyMap()));
        return message;
    }

    private static Message createMessage(Instant enqueuedTime, String traceparent) {
        Message message = Message.Factory.create();
        message.setMessageAnnotations(new MessageAnnotations(Collections.singletonMap(Symbol.getSymbol(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue()), enqueuedTime)));
        message.setApplicationProperties(new ApplicationProperties(Collections.singletonMap("traceparent", traceparent)));
        return message;
    }

    private static PartitionEvent createPartitionEvent(Instant enqueuedTime, String traceparent, String partitionId) {
        PartitionContext context = new PartitionContext(FQDN, ENTITY_NAME, CONSUMER_GROUP, partitionId);

        EventData data = createEventData(enqueuedTime, traceparent);
        return new PartitionEvent(context, data, null);
    }

    private EventContext createEventContext(Instant enqueuedTime, String traceparent, String partitionId) {
        PartitionContext context = new PartitionContext(FQDN, ENTITY_NAME, CONSUMER_GROUP, partitionId);

        EventData data = createEventData(enqueuedTime, traceparent);
        return new EventContext(context, data, checkpointStore, null);
    }

    private static EventData createEventData(Instant enqueuedTime, String traceparent) {
        AmqpAnnotatedMessage annotatedMessage = new AmqpAnnotatedMessage(
                AmqpMessageBody.fromData("foo".getBytes(StandardCharsets.UTF_8)));
        annotatedMessage.getApplicationProperties().put("traceparent", traceparent);
        annotatedMessage.getMessageAnnotations().put(AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue(), enqueuedTime);
        return TestUtils.createEventData(annotatedMessage, 25L, 14L, enqueuedTime);
    }

    private SpanData assertReceiveSpan(int expectedBatchSize, String partitionId, String expectedErrorType, String spanDescription) {
        assertEquals(1, spanProcessor.getEndedSpans().size());
        SpanData span = spanProcessor.getEndedSpans().get(0).toSpanData();
        assertEquals(getSpanName(RECEIVE, ENTITY_NAME), span.getName());
        assertEquals(CONSUMER, span.getKind());
        Map<String, Object> attributes = attributesToMap(span.getAttributes());
        assertAllAttributes(FQDN, ENTITY_NAME, partitionId, CONSUMER_GROUP, expectedErrorType, RECEIVE, attributes);
        assertSpanStatus(spanDescription, span);

        assertEquals((long) expectedBatchSize, attributes.get("messaging.batch.message_count"));
        return span;
    }

    private SpanData assertProcessSpan(String partitionId, String expectedErrorType, String spanDescription) {
        assertEquals(1, spanProcessor.getEndedSpans().size());
        SpanData span = spanProcessor.getEndedSpans().get(0).toSpanData();
        assertEquals(getSpanName(PROCESS, ENTITY_NAME), span.getName());
        assertEquals(CONSUMER, span.getKind());
        Map<String, Object> attributes = attributesToMap(span.getAttributes());
        assertAllAttributes(FQDN, ENTITY_NAME, partitionId, CONSUMER_GROUP, expectedErrorType, PROCESS, attributes);
        assertSpanStatus(spanDescription, span);
        return span;
    }

    private SpanData assertCheckpointSpan(String partitionId, String expectedErrorType, String spanDescription) {
        assertEquals(1, spanProcessor.getEndedSpans().size());
        SpanData span = spanProcessor.getEndedSpans().get(0).toSpanData();
        assertEquals(getSpanName(CHECKPOINT, ENTITY_NAME), span.getName());
        assertEquals(INTERNAL, span.getKind());
        Map<String, Object> attributes = attributesToMap(span.getAttributes());
        assertAllAttributes(FQDN, ENTITY_NAME, partitionId, CONSUMER_GROUP, expectedErrorType, CHECKPOINT, attributes);
        assertSpanStatus(spanDescription, span);
        return span;
    }

    private void assertOperationDuration(OperationName operationName, String partitionId, String expectedErrorType) {
        TestHistogram operationDuration = meter.getHistograms().get("messaging.client.operation.duration");
        assertNotNull(operationDuration);
        assertEquals(1, operationDuration.getMeasurements().size());
        assertAllAttributes(FQDN, ENTITY_NAME, partitionId, CONSUMER_GROUP, expectedErrorType,
                operationName, operationDuration.getMeasurements().get(0).getAttributes());
    }

    private void assertProcessDuration(Duration duration, String partitionId, String expectedErrorType) {
        TestHistogram processDuration = meter.getHistograms().get("messaging.process.duration");
        assertNotNull(processDuration);
        List<TestMeasurement<Double>> durationPerPartition = processDuration.getMeasurements().stream()
                        .filter(m -> partitionId.equals(m.getAttributes().get("messaging.destination.partition.id")))
                        .collect(Collectors.toList());
        assertEquals(1, durationPerPartition.size());
        if (duration != null) {
            double sec = getDoubleSeconds(duration);
            assertEquals(sec, durationPerPartition.get(0).getValue(), sec);
        }

        assertAllAttributes(FQDN, ENTITY_NAME, partitionId, CONSUMER_GROUP, expectedErrorType,
                PROCESS, durationPerPartition.get(0).getAttributes());
    }

    private void assertConsumedCount(int count, String partitionId, String errorType, OperationName operationName) {
        TestCounter processCounter = meter.getCounters().get("messaging.client.consumed.messages");
        assertNotNull(processCounter);
        assertEquals(count > 0 ? 1 : 0, processCounter.getMeasurements().size());
        if (count > 0) {
            assertEquals(Long.valueOf(count), processCounter.getMeasurements().get(0).getValue());
            assertAllAttributes(FQDN, ENTITY_NAME, partitionId, CONSUMER_GROUP, errorType, operationName,
                processCounter.getMeasurements().get(0).getAttributes());
        }
    }

    private double getDoubleSeconds(Duration duration) {
        return duration.toNanos() / 1_000_000_000.0;
    }

    private static class TestCheckpointStore implements CheckpointStore {
        private final CheckpointStore inner;
        private final Function<Mono<Void>, Mono<Void>> onUpdateCheckpoint;

        TestCheckpointStore(CheckpointStore inner, Function<Mono<Void>, Mono<Void>> onUpdateCheckpoint) {
            this.onUpdateCheckpoint = onUpdateCheckpoint;
            this.inner = inner;
        }

        TestCheckpointStore(Function<Mono<Void>, Mono<Void>> onCheckpoint) {
            this(new SampleCheckpointStore(), onCheckpoint);
        }

        @Override
        public Flux<PartitionOwnership> listOwnership(String fullyQualifiedNamespace, String eventHubName, String consumerGroup) {
            return inner.listOwnership(fullyQualifiedNamespace, eventHubName, consumerGroup);
        }

        @Override
        public Flux<PartitionOwnership> claimOwnership(List<PartitionOwnership> requestedPartitionOwnerships) {
            return inner.claimOwnership(requestedPartitionOwnerships);
        }

        @Override
        public Flux<Checkpoint> listCheckpoints(String fullyQualifiedNamespace, String eventHubName, String consumerGroup) {
            return inner.listCheckpoints(fullyQualifiedNamespace, eventHubName, consumerGroup);
        }

        @Override
        public Mono<Void> updateCheckpoint(Checkpoint checkpoint) {
            return onUpdateCheckpoint.apply(inner.updateCheckpoint(checkpoint));
        }
    }
}
