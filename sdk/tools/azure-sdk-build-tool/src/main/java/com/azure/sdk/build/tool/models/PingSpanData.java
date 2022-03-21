package com.azure.sdk.build.tool.models;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Sends a ping to Application Insights when the build tool is run.
 */
public class PingSpanData implements SpanData {

    private final String traceId;
    private final String spanId;
    private final long startEpochNanos;
    private final long endEpochNanos;

    /**
     * Creates an instance of ping span to export to Application Insights.
     */
    public PingSpanData() {
        this.traceId = UUID.randomUUID().toString();
        this.spanId = UUID.randomUUID().toString();
        this.startEpochNanos = MILLISECONDS.toNanos(Instant.now().toEpochMilli());
        this.endEpochNanos = MILLISECONDS.toNanos(Instant.now().toEpochMilli());
    }

    @Override
    public SpanContext getSpanContext() {
        return SpanContext.create(traceId, spanId, TraceFlags.getDefault(), TraceState.getDefault());
    }

    @Override
    public String getTraceId() {
        return this.traceId;
    }

    @Override
    public String getSpanId() {
        return this.spanId;
    }

    @Override
    public SpanContext getParentSpanContext() {
        return SpanContext.create(traceId, spanId, TraceFlags.getDefault(), TraceState.getDefault());
    }

    @Override
    public String getParentSpanId() {
        return SpanData.super.getParentSpanId();
    }

    @Override
    public Resource getResource() {
        return null;
    }

    @Override
    public InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
        return InstrumentationLibraryInfo.create("AzureSDKMavenBuildTool", "1");
    }

    @Override
    public String getName() {
        return "azsdk-maven-build-tool";
    }

    @Override
    public SpanKind getKind() {
        return SpanKind.INTERNAL;
    }

    @Override
    public long getStartEpochNanos() {
        return this.startEpochNanos;
    }

    @Override
    public Attributes getAttributes() {
        return Attributes.builder().build();
    }

    @Override
    public List<EventData> getEvents() {
        return Collections.emptyList();
    }

    @Override
    public List<LinkData> getLinks() {
        return Collections.emptyList();
    }

    @Override
    public StatusData getStatus() {
        return StatusData.ok();
    }

    @Override
    public long getEndEpochNanos() {
        return this.endEpochNanos;
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
