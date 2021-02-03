// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.opentelemetry.exporter.azuremonitor;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * A sample span data implementation.
 */
public class SampleSpanData implements SpanData {

    @Override
    public String getTraceId() {
        return TraceId.fromLongs(10L, 2L);
    }

    @Override
    public String getSpanId() {
        return SpanId.fromLong(1);
    }

    @Override
    public boolean isSampled() {
        return false;
    }

    @Override
    public TraceState getTraceState() {
        return TraceState.builder().build();
    }

    @Override
    public SpanContext getParentSpanContext() {
        return null;
    }

    @Override
    public String getParentSpanId() {
        return SpanId.fromLong(0);
    }

    @Override
    public Resource getResource() {
        return null;
    }

    @Override
    public InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
        return InstrumentationLibraryInfo.create("TestLib", "1");
    }

    @Override
    public String getName() {
        return "/service/resource";
    }

    @Override
    public Span.Kind getKind() {
        return Span.Kind.INTERNAL;
    }

    @Override
    public long getStartEpochNanos() {
        return MILLISECONDS.toNanos(Instant.now().toEpochMilli());
    }

    @Override
    public Attributes getAttributes() {
        return Attributes.builder()
            .put(SemanticAttributes.HTTP_STATUS_CODE.getKey(), 200L)
            .put(SemanticAttributes.HTTP_URL.getKey(), "http://localhost")
            .put(SemanticAttributes.HTTP_METHOD.getKey(), "GET")
            .put("ai.sampling.percentage", 100.0)
            .build();
    }

    @Override
    public List<EventData> getEvents() {
        return new ArrayList<>();
    }

    @Override
    public List<LinkData> getLinks() {
        return new ArrayList<>();
    }

    @Override
    public StatusData getStatus() {
        return StatusData.ok();
    }

    @Override
    public long getEndEpochNanos() {
        return MILLISECONDS.toNanos(Instant.now().toEpochMilli());
    }

    @Override
    public boolean hasEnded() {
        return false;
    }

    @Override
    public int getTotalRecordedEvents() {
        return 0;
    }

    @Override
    public int getTotalRecordedLinks() {
        return 0;
    }

    @Override
    public int getTotalAttributeCount() {
        return 0;
    }
}
