// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.test.utils.metrics.TestCounter;
import com.azure.core.test.utils.metrics.TestHistogram;
import com.azure.core.test.utils.metrics.TestMeter;
import com.azure.core.tracing.opentelemetry.OpenTelemetryTracingOptions;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.messaging.eventhubs.TestUtils.assertAllAttributes;
import static com.azure.messaging.eventhubs.TestUtils.assertSpanStatus;
import static com.azure.messaging.eventhubs.TestUtils.attributesToMap;
import static com.azure.messaging.eventhubs.TestUtils.getSpanName;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.EVENT;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.SEND;
import static io.opentelemetry.api.trace.SpanKind.CLIENT;
import static io.opentelemetry.api.trace.SpanKind.PRODUCER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class EventHubsProducerInstrumentationTests {
    private static final String FQDN = "fqdn";
    private static final String ENTITY_NAME = "entityName";
    private Tracer tracer;
    private TestMeter meter;
    private TestSpanProcessor spanProcessor;

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
    }

    @AfterEach
    public void teardown() {
        spanProcessor.shutdown();
        spanProcessor.close();
        meter.close();
    }

    @Test
    @SuppressWarnings("try")
    public void sendBatchDisabledInstrumentation() {
        EventHubsProducerInstrumentation instrumentation = new EventHubsProducerInstrumentation(null, null,
                FQDN, ENTITY_NAME);

        EventDataBatch batch = new EventDataBatch(1024, null, null,
                () -> null, instrumentation);
        batch.tryAdd(new EventData("test"));
        Mono<Void> inner = Mono.empty();
        assertSame(inner, instrumentation.sendBatch(inner, batch));
    }

    public static Stream<Arguments> sendBatchErrors() {
        return Stream.of(
                Arguments.of(false, null, null, null),
                Arguments.of(true, null, "cancelled", "cancelled"),
                Arguments.of(false, new RuntimeException("test"), RuntimeException.class.getName(), "test"),
                Arguments.of(false, Exceptions.propagate(new RuntimeException("test")), RuntimeException.class.getName(), "test")
        );
    }

    @ParameterizedTest
    @MethodSource("sendBatchErrors")
    @SuppressWarnings("try")
    public void sendBatchOneEvent(boolean cancel, Throwable error, String expectedErrorType, String spanDescription) {
        EventHubsProducerInstrumentation instrumentation = new EventHubsProducerInstrumentation(tracer, meter,
                FQDN, ENTITY_NAME);
        EventDataBatch batch = new EventDataBatch(1024, null, null,
                () -> null, instrumentation);
        batch.tryAdd(new EventData("test"));
        Mono<Void> inner = Mono.defer(() -> error == null ? Mono.empty() : Mono.error(error));

        StepVerifier.FirstStep<Void> stepVerifier =
                StepVerifier.create(instrumentation.sendBatch(inner, batch));

        if (cancel) {
            stepVerifier.thenCancel().verify();
        } else if (error != null) {
            stepVerifier.expectErrorMessage(error.getMessage()).verify();
        } else {
            stepVerifier.expectComplete().verify();
        }

        assertSendDuration(null, expectedErrorType);
        assertBatchCount(1, null, expectedErrorType);
        assertSpans(1, null, expectedErrorType, spanDescription);
    }

    @ParameterizedTest
    @MethodSource("sendBatchErrors")
    @SuppressWarnings("try")
    public void sendBatchMultipleEvents(boolean cancel, Throwable error, String expectedErrorType, String spanDescription) {
        EventHubsProducerInstrumentation instrumentation = new EventHubsProducerInstrumentation(tracer, meter,
                FQDN, ENTITY_NAME);

        String partitionId = "1";
        EventDataBatch batch = new EventDataBatch(1024, partitionId, null,
                () -> null, instrumentation);

        int count = 3;
        for (int i = 0; i < count; i++) {
            batch.tryAdd(new EventData("test" + i));
        }

        Mono<Void> inner = Mono.defer(() -> error == null ? Mono.empty() : Mono.error(error));

        StepVerifier.FirstStep<Void> stepVerifier =
                StepVerifier.create(instrumentation.sendBatch(inner, batch));

        if (cancel) {
            stepVerifier.thenCancel().verify();
        } else if (error != null) {
            stepVerifier.expectErrorMessage(error.getMessage()).verify();
        } else {
            stepVerifier.expectComplete().verify();
        }

        assertSendDuration(partitionId, expectedErrorType);
        assertBatchCount(count, partitionId, expectedErrorType);
        assertSpans(count, partitionId, expectedErrorType, spanDescription);
    }

    private void assertSendDuration(String partitionId, String expectedErrorType) {
        TestHistogram publishDuration = meter.getHistograms().get("messaging.client.operation.duration");
        assertNotNull(publishDuration);
        assertEquals(1, publishDuration.getMeasurements().size());
        assertAllAttributes(FQDN, ENTITY_NAME, partitionId, null, expectedErrorType,
                SEND, publishDuration.getMeasurements().get(0).getAttributes());
    }

    private void assertSpans(int batchSize, String partitionId, String expectedErrorType, String spanDescription) {
        assertEquals(batchSize + 1, spanProcessor.getEndedSpans().size());
        List<SpanData> eventSpans = spanProcessor.getEndedSpans().stream()
                .filter(s -> s.getKind().equals(PRODUCER))
                .map(s -> s.toSpanData())
                .collect(Collectors.toList());
        assertEquals(batchSize, eventSpans.size());

        List<SpanData> publishSpans = spanProcessor.getEndedSpans().stream()
                .filter(s -> s.getKind().equals(CLIENT))
                .map(s -> s.toSpanData())
                .collect(Collectors.toList());
        assertEquals(1, publishSpans.size());

        assertEquals(getSpanName(SEND, ENTITY_NAME), publishSpans.get(0).getName());
        Map<String, Object> publishAttributes = attributesToMap(publishSpans.get(0).getAttributes());
        assertAllAttributes(FQDN, ENTITY_NAME, partitionId, null, expectedErrorType,
            SEND, publishAttributes);
        assertEquals(Long.valueOf(batchSize), publishAttributes.get("messaging.batch.message_count"));
        assertSpanStatus(spanDescription, publishSpans.get(0));

        List<LinkData> links = publishSpans.get(0).getLinks();
        for (int i = 0; i < batchSize; i++) {
            assertEquals(getSpanName(EVENT, ENTITY_NAME), eventSpans.get(i).getName());
            Map<String, Object> eventAttributes = attributesToMap(eventSpans.get(i).getAttributes());
            assertAllAttributes(FQDN, ENTITY_NAME, null, null, null,
                EVENT, eventAttributes);
            assertSpanStatus(null, eventSpans.get(i));

            SpanContext createContext = eventSpans.get(i).getSpanContext();
            links.stream()
                    .filter(l -> spanContextEquals(l.getSpanContext(), createContext))
                    .findFirst().orElseThrow(() -> new AssertionError("Link not found"));
        }
    }

    private static boolean spanContextEquals(SpanContext c1, SpanContext c2) {
        // we don't take remote into account, so can't use equals implementation on SpanContext
        return c1.getTraceId().equals(c2.getTraceId())
                && c1.getSpanId().equals(c2.getSpanId())
                && c1.getTraceFlags().equals(c2.getTraceFlags())
                && c1.getTraceState().equals(c2.getTraceState());
    }

    private void assertBatchCount(int count, String partitionId, String expectedErrorType) {
        TestCounter batchCounter = meter.getCounters().get("messaging.client.published.messages");
        assertNotNull(batchCounter);
        assertEquals(1, batchCounter.getMeasurements().size());
        assertEquals(Long.valueOf(count), batchCounter.getMeasurements().get(0).getValue());
        assertAllAttributes(FQDN, ENTITY_NAME, partitionId, null, expectedErrorType,
                SEND, batchCounter.getMeasurements().get(0).getAttributes());
    }
}
