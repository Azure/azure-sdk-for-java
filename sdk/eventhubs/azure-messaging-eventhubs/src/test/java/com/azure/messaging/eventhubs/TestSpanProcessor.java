// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LoggingEventBuilder;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSpanProcessor implements SpanProcessor {
    private static final ClientLogger LOGGER = new ClientLogger(TestSpanProcessor.class);
    private final ConcurrentLinkedDeque<ReadableSpan> spans = new ConcurrentLinkedDeque<>();
    private final String entityName;
    private final String namespace;
    private final String testName;

    private final AtomicReference<Consumer<ReadableSpan>> notifier = new AtomicReference<>();

    public TestSpanProcessor(String namespace, String entityName, String testName) {
        this.namespace = namespace;
        this.entityName = entityName;
        this.testName = testName;
    }

    public List<ReadableSpan> getEndedSpans() {
        return new ArrayList<>(spans);
    }

    @Override
    public void onStart(Context context, ReadWriteSpan readWriteSpan) {
    }

    @Override
    public boolean isStartRequired() {
        return false;
    }

    @Override
    public void onEnd(ReadableSpan readableSpan) {
        SpanData span = readableSpan.toSpanData();
        Attributes attributes = span.getAttributes();

        InstrumentationScopeInfo instrumentationScopeInfo = span.getInstrumentationScopeInfo();
        LoggingEventBuilder log = LOGGER.atInfo()
                .addKeyValue("testName", testName)
                .addKeyValue("name", span.getName())
                .addKeyValue("traceId", span.getTraceId())
                .addKeyValue("spanId", span.getSpanId())
                .addKeyValue("parentSpanId", span.getParentSpanId())
                .addKeyValue("kind", span.getKind())
                .addKeyValue("tracerName", instrumentationScopeInfo.getName())
                .addKeyValue("tracerVersion", instrumentationScopeInfo.getVersion())
                .addKeyValue("attributes", attributes);

        for (int i = 0; i < span.getLinks().size(); i++) {
            LinkData link = span.getLinks().get(i);
            log.addKeyValue("linkTraceId" + i, link.getSpanContext().getTraceId())
                    .addKeyValue("linkSpanId" + i, link.getSpanContext().getSpanId())
                    .addKeyValue("linkAttributes" + i, link.getAttributes());
        }
        log.log("got span");

        spans.add(readableSpan);
        Consumer<ReadableSpan> filter = notifier.get();
        if (filter != null) {
            filter.accept(readableSpan);
        }

        // Various attribute keys can be found in:
        // sdk/core/azure-core-metrics-opentelemetry/src/main/java/com/azure/core/metrics/opentelemetry/OpenTelemetryAttributes.java
        // sdk/core/azure-core-tracing-opentelemetry/src/main/java/com/azure/core/tracing/opentelemetry/OpenTelemetryUtils.java
        assertEquals("Microsoft.EventHub", attributes.get(AttributeKey.stringKey("az.namespace")));
        assertEquals("eventhubs", attributes.get(AttributeKey.stringKey("messaging.system")));
        assertEquals(entityName, attributes.get(AttributeKey.stringKey("messaging.destination.name")));
        assertEquals(namespace, attributes.get(AttributeKey.stringKey("server.address")));
    }

    public void notifyIfCondition(CountDownLatch countDownLatch, Predicate<ReadableSpan> filter) {
        notifier.set((span) -> {
            if (filter.test(span)) {
                LOGGER.atInfo()
                        .addKeyValue("traceId", span.getSpanContext().getTraceId())
                        .addKeyValue("spanId", span.getSpanContext().getSpanId())
                        .log("condition met");
                countDownLatch.countDown();
            }
        });
    }

    @Override
    public boolean isEndRequired() {
        return true;
    }

    @Override
    public CompletableResultCode shutdown() {
        notifier.set(null);
        return CompletableResultCode.ofSuccess();
    }
}
