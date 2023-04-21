// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LoggingEventBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;

import java.util.List;

class LoggingSpanProcessor implements SpanProcessor {
    private final ClientLogger logger;
    LoggingSpanProcessor(ClientLogger logger) {
        this.logger = logger;
    }

    @Override
    public void onStart(Context context, ReadWriteSpan readWriteSpan) {
        logger.atInfo()
            .addKeyValue("name", readWriteSpan.getName())
            .addKeyValue("kind", readWriteSpan.getKind())
            .addKeyValue("traceId", readWriteSpan.getSpanContext().getTraceId())
            .addKeyValue("spanId", readWriteSpan.getSpanContext().getSpanId())
            .log("span started");
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan readableSpan) {
        SpanData data = readableSpan.toSpanData();
        LoggingEventBuilder log = data.getStatus().getStatusCode() == StatusCode.ERROR ? logger.atError() : logger.atInfo();
        log.addKeyValue("name", data.getName())
            .addKeyValue("kind", data.getKind())
            .addKeyValue("traceId", data.getSpanContext().getTraceId())
            .addKeyValue("spanId", data.getSpanContext().getSpanId())
            .addKeyValue("status", data.getStatus().getStatusCode())
            .addKeyValue("statusDescription", data.getStatus().getDescription())
            .addKeyValue("durationMs", readableSpan.getLatencyNanos() / 1000_000d)
            .addKeyValue("startEpoch", data.getStartEpochNanos())
            .addKeyValue("parentSpanId", data.getParentSpanContext().getSpanId());

        data.getAttributes().forEach((key, value) -> {
            if (!key.getKey().equals("az.namespace") && !key.getKey().equals("messaging.system")) {
                log.addKeyValue(key.getKey(), value);
            }
        });

        List<LinkData> links = data.getLinks();
        for (int l = 0; l < data.getLinks().size(); l ++) {
            log.addKeyValue("link[" + l + "].traceId", links.get(l).getSpanContext().getTraceId());
            log.addKeyValue("link[" + l + "].spanId", links.get(l).getSpanContext().getSpanId());
            log.addKeyValue("link[" + l + "].attributeCount", links.get(l).getTotalAttributeCount());
        }

        log.log("span ended");
    }

    @Override
    public boolean isEndRequired() {
        return true;
    }
}
