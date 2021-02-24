package com.azure.monitor.opentelemetry.exporter.perf;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class RemoteDependencySpanData implements SpanData {
    @Override
    public String getTraceId() {
        return "07598c799fb1d0fea06d3eb06c78a449";
    }

    @Override
    public String getSpanId() {
        return "9e426ec07f818c56";
    }

    @Override
    public boolean isSampled() {
        return false;
    }

    @Override
    public TraceState getTraceState() {
        return TraceState.getDefault();
    }

    @Override
    public SpanContext getParentSpanContext() {
        return SpanContext.create("07598c799fb1d0fea06d3eb06c78a449", "d1d983e9ef38c4de", (byte) 1, TraceState.getDefault());
    }

    @Override
    public Resource getResource() {
        return Resource.create(getAttributes());
    }

    @Override
    public InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
        return InstrumentationLibraryInfo.create("perf-test", "1.0.0-beta.1");
    }

    @Override
    public String getName() {
        return "AppConfig.setKey";
    }

    @Override
    public Span.Kind getKind() {
        return Span.Kind.INTERNAL;
    }

    @Override
    public long getStartEpochNanos() {
        return 1614067747724534300L;
    }

    @Override
    public Attributes getAttributes() {
        return Attributes.builder()
            .put("data", String.valueOf(new HashMap<String, String>() {{
                put("az.namespace", "Microsoft.AppConfiguration");
            }}))
            .put("capacity", 1000)
            .put("totalAddedValues", 1)
            .build();
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
        return StatusData.create(StatusCode.OK, "success");
    }

    @Override
    public long getEndEpochNanos() {
        return 1614067748854251600L;
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
        return 1;
    }
}
